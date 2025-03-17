package com.reservation.service;

import static com.reservation.type.ErrorCode.INVALID_ROLE;
import static com.reservation.type.ErrorCode.NOT_PARTNER;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reservation.domain.Store;
import com.reservation.domain.User;
import com.reservation.dto.StoreDto;
import com.reservation.exception.UserException;
import com.reservation.repository.StoreRepository;
import com.reservation.repository.UserRepository;
import com.reservation.type.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public StoreDto registerStore(Long userId, StoreDto storeDto) {
        // 점주 정보 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.OWNER_NOT_FOUND));

        // 점주(OWNER)인지 확인
        if (!"OWNER".equals(user.getUserType().name())) {
           throw new UserException(INVALID_ROLE); // 권한 없음
        }
        
        // 파트너 여부 확인
        if(!user.isPartner()) {
        	throw new UserException(NOT_PARTNER); // 파트너 아님
        }
        
        
        // Store 엔터티 생성
        Store store = Store.builder()
                .storeName(storeDto.getStoreName())
                .lat(storeDto.getLat())
                .lng(storeDto.getLng())
                .detail(storeDto.getDetail())
                .owner(user)  // 점주 설정
                .build();

        
        // DB 저장
        Store savedStore = storeRepository.save(store);

        return StoreDto.fromEntity(savedStore);
    }
    
    // 상점 목록을 동적으로 정렬해주는 메소드
    // 거리는 Haversine 공식을 통해 구하기
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
    
    private String getSortQuery(String sortBy) {
        return switch (sortBy) {
            case "rating" -> "avg_rating DESC";
            case "distance" -> "distance ASC";
            default -> "s.store_name ASC"; // 기본값: 가나다순
        };
    }
    
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
}
