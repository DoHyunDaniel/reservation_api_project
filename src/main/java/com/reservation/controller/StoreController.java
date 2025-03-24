package com.reservation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.config.JwtTokenProvider;
import com.reservation.domain.Store;
import com.reservation.domain.User;
import com.reservation.dto.store.DeleteStore;
import com.reservation.dto.store.RegisterStore;
import com.reservation.dto.store.UpdateStore;
import com.reservation.dto.store.DeleteStore.Response;
import com.reservation.dto.StoreDto;
import com.reservation.dto.UserDto;
import com.reservation.service.StoreService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/stores")
public class StoreController {

    private final StoreService storeService;
    private final JwtTokenProvider jwtTokenProvider;

    public StoreController(StoreService storeService, JwtTokenProvider jwtTokenProvider) {
        this.storeService = storeService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 점주가 자신의 매장을 등록하는 API
     * - 파트너 인증을 받은 OWNER만 등록 가능
     *
     * @param storeDto 매장 정보 DTO
     * @param request HTTP 요청 (Authorization 헤더 포함)
     * @return 등록된 매장 정보
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterStore.Response> registerStore(
            @RequestBody StoreDto storeDto,
            HttpServletRequest request) {

        // 토큰에서 사용자 정보 추출
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String role = jwtTokenProvider.getUserRoleFromToken(token);

        RegisterStore.Response registeredStore = storeService.registerStore(userId, storeDto);
        return ResponseEntity.ok(registeredStore);
    }

    /**
     * 점주가 본인의 매장을 삭제하는 API
     * - 비밀번호 확인 후 매장 삭제
     *
     * @param request 삭제 요청 DTO (storeId + 비밀번호 포함)
     * @param httpRequest 인증 정보 포함 (userId)
     * @return 삭제된 매장 정보
     */
    @DeleteMapping("/delete")
    public ResponseEntity<DeleteStore.Response> deleteStore(
            @Valid @RequestBody DeleteStore.Request request,
            HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");
        DeleteStore.Response response = storeService.deleteStore(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 점주가 매장 정보를 수정하는 API
     * - storeId, 위치, 이름, 상세 설명 등 수정 가능
     * - 관리자 계정은 수정 불가
     *
     * @param request 매장 수정 요청
     * @param httpRequest 인증 정보 포함 (userId)
     * @return 수정된 매장 정보
     */
    @PutMapping("/update")
    public ResponseEntity<UpdateStore.Response> updateStore(
            @Valid @RequestBody UpdateStore.Request request,
            HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");
        Store updatedStore = storeService.updateStore(userId, request);
        return ResponseEntity.ok(UpdateStore.Response.fromEntity(updatedStore));
    }

    /**
     * 매장 리스트 조회 API
     * - 사용자 또는 비회원이 접근 가능
     * - 정렬 기준 (평점순, 거리순, 이름순) 선택 가능
     *
     * @param sortBy 정렬 기준: rating, distance, name (기본값: name)
     * @param userLat 사용자 현재 위도 (거리 정렬 시 필요)
     * @param userLng 사용자 현재 경도
     * @return 정렬된 매장 리스트
     */
    @GetMapping("/list")
    public ResponseEntity<List<StoreDto>> getStoreList(
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLng) {

        List<StoreDto> stores = storeService.getStores(sortBy, userLat, userLng);
        return ResponseEntity.ok(stores);
    }
}
