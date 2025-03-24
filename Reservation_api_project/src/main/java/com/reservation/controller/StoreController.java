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

    @PostMapping("/register")
    public ResponseEntity<RegisterStore.Response> registerStore(
            @RequestBody StoreDto storeDto,
            HttpServletRequest request){
        
        // JWT에서 사용자 ID 및 역할 가져오기
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String role = jwtTokenProvider.getUserRoleFromToken(token);

        // 매장 등록 수행
        RegisterStore.Response registeredStore = storeService.registerStore(userId, storeDto);
        return ResponseEntity.ok(registeredStore);
    }
    
	@DeleteMapping("/delete")
	public ResponseEntity<Response> deleteStore(@Valid @RequestBody DeleteStore.Request request, HttpServletRequest httpRequest) {
		Long userId = (Long) httpRequest.getAttribute("userId");
		DeleteStore.Response response = storeService.deleteStore(userId, request);
        
        return ResponseEntity.ok(response);
	}
	
	@PutMapping("/update")
	public ResponseEntity<UpdateStore.Response> updateStore(
	        @Valid @RequestBody UpdateStore.Request request,
	        HttpServletRequest httpRequest) {

	    Long userId = (Long) httpRequest.getAttribute("userId");
	    Store updatedStore = storeService.updateStore(userId, request);
	    return ResponseEntity.ok(UpdateStore.Response.fromEntity(updatedStore));
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
