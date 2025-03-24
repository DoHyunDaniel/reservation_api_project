package com.reservation.service;

import static com.reservation.type.ErrorCode.CANNOT_CREATE_ADMIN;
import static com.reservation.type.ErrorCode.INVALID_ROLE;
import static com.reservation.type.ErrorCode.NOT_PARTNER;
import static com.reservation.type.ErrorCode.PASSWORD_UNMATCHED;
import static com.reservation.type.ErrorCode.USER_NOT_FOUND;
import static com.reservation.type.UserType.ADMIN;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reservation.domain.Store;
import com.reservation.domain.User;
import com.reservation.dto.StoreDto;
import com.reservation.dto.store.DeleteStore;
import com.reservation.dto.store.RegisterStore;
import com.reservation.dto.store.UpdateStore;
import com.reservation.exception.UserException;
import com.reservation.repository.StoreRepository;
import com.reservation.repository.UserRepository;
import com.reservation.type.ErrorCode;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    /**
     * 상점을 등록하는 메소드
     * - 점주(OWNER)이고 파트너 등록이 되어 있어야 등록 가능
     *
     * @param userId 상점을 등록하려는 사용자 ID
     * @param storeDto 등록할 상점 정보
     * @return 등록된 상점의 응답 객체
     * @throws UserException 점주가 아니거나, 파트너가 아닌 경우
     */
    @Transactional
    public RegisterStore.Response registerStore(Long userId, StoreDto storeDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.OWNER_NOT_FOUND));

        if (!"OWNER".equals(user.getUserType().name())) {
           throw new UserException(INVALID_ROLE); // 권한 없음
        }

        if (!user.isPartner()) {
            throw new UserException(NOT_PARTNER); // 파트너 아님
        }

        Store store = Store.builder()
                .storeName(storeDto.getStoreName())
                .lat(storeDto.getLat())
                .lng(storeDto.getLng())
                .detail(storeDto.getDetail())
                .owner(user)
                .build();

        Store savedStore = storeRepository.save(store);

        return RegisterStore.Response.fromEntity(savedStore);
    }

    /**
     * 상점을 삭제하는 메소드
     * - 상점 소유자 본인만 삭제할 수 있으며 비밀번호 확인이 필요함
     *
     * @param userId 요청한 사용자 ID
     * @param request 삭제 요청(상점 ID + 비밀번호)
     * @return 삭제된 상점 정보 응답
     * @throws UserException 상점이 없거나, 권한이 없거나, 비밀번호가 틀린 경우
     */
    @Transactional
    public DeleteStore.Response deleteStore(Long userId, DeleteStore.Request request) {
        Store store = storeRepository.findById(request.getId())
                .orElseThrow(() -> new UserException(ErrorCode.STORE_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.OWNER_NOT_FOUND));

        if (!store.getOwner().getId().equals(userId)) {
            throw new UserException(ErrorCode.INVALID_ROLE);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserException(PASSWORD_UNMATCHED);
        }

        storeRepository.delete(store);

        return DeleteStore.Response.from(StoreDto.fromEntity(store));
    }

    /**
     * 사용자의 위치를 기준으로 상점 목록을 정렬 조회하는 메소드
     * - 평점(rating), 거리(distance), 상호명(store_name) 기준으로 정렬 가능
     * - Haversine 공식을 이용해 거리 계산
     *
     * @param sortBy 정렬 기준 (rating, distance, etc.)
     * @param userLat 사용자 위도
     * @param userLng 사용자 경도
     * @return 정렬된 상점 목록
     */
    public List<StoreDto> getStores(String sortBy, Double userLat, Double userLng) {
        String sql = """
            SELECT 
                s.id AS store_id,
                s.store_name,
                s.lat,
                s.lng,
                s.detail,
                COALESCE(AVG(r.rating), 0) AS avg_rating,
                (6371 * ACOS(
                    COS(RADIANS(?)) * COS(RADIANS(s.lat)) 
                    * COS(RADIANS(s.lng) - RADIANS(?)) 
                    + SIN(RADIANS(?)) * SIN(RADIANS(s.lat))
                )) AS distance
            FROM Stores s
            LEFT JOIN Reviews r ON s.id = r.store_id
            GROUP BY s.id, s.store_name, s.lat, s.lng
            ORDER BY """ + getSortQuery(sortBy) + ", s.store_name ASC";

        return jdbcTemplate.query(sql, new Object[]{userLat, userLng, userLat}, storeRowMapper());
    }

    /**
     * 정렬 조건에 따라 ORDER BY 절을 생성하는 메소드
     * 
     * @param sortBy 정렬 기준 문자열
     * @return SQL ORDER BY 구문
     */
    private String getSortQuery(String sortBy) {
        return switch (sortBy) {
            case "rating" -> "avg_rating DESC";
            case "distance" -> "distance ASC";
            default -> "s.store_name ASC"; // 기본: 상호명
        };
    }

    /**
     * SQL 결과를 StoreDto로 매핑하는 RowMapper
     * 
     * @return StoreDto RowMapper 객체
     */
    private RowMapper<StoreDto> storeRowMapper() {
        return (rs, rowNum) -> StoreDto.builder()
                .id(rs.getLong("store_id"))
                .storeName(rs.getString("store_name"))
                .lat(rs.getDouble("lat"))
                .lng(rs.getDouble("lng"))
                .avgRating(rs.getDouble("avg_rating"))
                .distance(rs.getDouble("distance"))
                .build();
    }

    /**
     * 상점 정보를 수정하는 메소드
     * - 본인이 소유한 상점만 수정 가능하며 비밀번호 확인이 필요
     * - 관리자(ADMIN)로 변경은 불가
     *
     * @param userId 요청한 사용자 ID
     * @param request 수정 요청 객체
     * @return 수정된 상점 엔티티
     * @throws UserException 권한 없음, 비밀번호 불일치, 관리자 설정 시 예외 발생
     */
    @Transactional
    public Store updateStore(Long userId, @Valid UpdateStore.Request request) {
        Store store = storeRepository.findById(request.getId())
                .orElseThrow(() -> new UserException(ErrorCode.STORE_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.OWNER_NOT_FOUND));

        if (!store.getOwner().getId().equals(userId)) {
            throw new UserException(ErrorCode.INVALID_ROLE);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserException(PASSWORD_UNMATCHED);
        }

        if (request.getUserType() == ADMIN) {
            throw new UserException(CANNOT_CREATE_ADMIN);
        }

        store.setStoreName(request.getStoreName());
        store.setLat(request.getLat());
        store.setLng(request.getLng());
        store.setDetail(request.getDetail());

        return store;
    }
}
