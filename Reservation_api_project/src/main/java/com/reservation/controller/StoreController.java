package com.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.config.JwtTokenProvider;
import com.reservation.dto.StoreDto;
import com.reservation.exception.UserException;
import com.reservation.service.StoreService;
import com.reservation.type.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/stores")
public class StoreController {
    private final StoreService storeService;
    private final JwtTokenProvider jwtTokenProvider;

    public StoreController(StoreService storeService, JwtTokenProvider jwtTokenProvider) {
        this.storeService = storeService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<StoreDto> registerStore(
            @RequestBody StoreDto storeDto,
            HttpServletRequest request) {
        
        // JWT에서 사용자 ID 및 역할 가져오기
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String role = jwtTokenProvider.getUserRoleFromToken(token);

        // 점주(OWNER)인지 확인
        if (!"OWNER".equals(role)) {
           throw new UserException(ErrorCode.INVALID_ROLE); // 권한 없음
        }

        // 매장 등록 수행
        StoreDto registeredStore = storeService.registerStore(userId, storeDto);
        return ResponseEntity.ok(registeredStore);
    }
}
