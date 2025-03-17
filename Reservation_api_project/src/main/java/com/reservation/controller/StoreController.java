package com.reservation.controller;

import static com.reservation.type.ErrorCode.INVALID_ROLE;
import static com.reservation.type.ErrorCode.NOT_PARTNER;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.config.JwtTokenProvider;
import com.reservation.domain.User;
import com.reservation.dto.StoreDto;
import com.reservation.exception.UserException;
import com.reservation.repository.UserRepository;
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

    @PostMapping("/registerStore")
    public ResponseEntity<StoreDto> registerStore(
            @RequestBody StoreDto storeDto,
            HttpServletRequest request){
        
        // JWT에서 사용자 ID 및 역할 가져오기
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String role = jwtTokenProvider.getUserRoleFromToken(token);

        // 매장 등록 수행
        StoreDto registeredStore = storeService.registerStore(userId, storeDto);
        return ResponseEntity.ok(registeredStore);
    }
    
    @GetMapping("/list")
    public ResponseEntity<List<StoreDto>> getStoreList(
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLng) {
        
        List<StoreDto> stores = storeService.getStores(sortBy, userLat, userLng);
        return ResponseEntity.ok(stores);
    }
}
