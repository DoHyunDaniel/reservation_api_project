package com.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reservation.domain.Store;
import com.reservation.domain.User;
import com.reservation.dto.StoreDto;
import com.reservation.exception.UserException;
import com.reservation.repository.StoreRepository;
import com.reservation.repository.UserRepository;
import com.reservation.type.ErrorCode;

@Service
public class StoreService {
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public StoreService(StoreRepository storeRepository, UserRepository userRepository) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public StoreDto registerStore(Long ownerId, StoreDto storeDto) {
        // 점주 정보 확인
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserException(ErrorCode.OWNER_NOT_FOUND));

        // Store 엔터티 생성
        Store store = Store.builder()
                .storeName(storeDto.getStoreName())
                .lat(storeDto.getLat())
                .lng(storeDto.getLng())
                .detail(storeDto.getDetail())
                .owner(owner)  // 점주 설정
                .build();

        // DB 저장
        Store savedStore = storeRepository.save(store);

        return StoreDto.fromEntity(savedStore);
    }
}
