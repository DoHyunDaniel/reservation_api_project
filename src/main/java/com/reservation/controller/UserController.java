package com.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.domain.User;
import com.reservation.dto.UserDto;
import com.reservation.dto.store.UpdateStore;
import com.reservation.dto.user.CreateUser;
import com.reservation.dto.user.DeleteUser;
import com.reservation.dto.user.UpdateUser;
import com.reservation.dto.user.UpdateUserPartnership;
import com.reservation.dto.user.DeleteUser.Response;
import com.reservation.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원 가입 API
     * - 일반 사용자 또는 점주(OWNER)만 가입 가능 (관리자 계정 생성은 차단됨)
     * - 아이디, 이메일, 전화번호 중복 시 예외 발생
     *
     * @param request 회원가입 요청 DTO
     * @return 생성된 사용자 정보 (userId, nickname 등 포함)
     */
    @PostMapping("/signup")
    public ResponseEntity<CreateUser.Response> createUser(@Valid @RequestBody CreateUser.Request request) {
        User user = userService.createUser(request);
        return ResponseEntity.ok(CreateUser.Response.from(UserDto.fromEntity(user)));
    }

    /**
     * 회원 탈퇴 API
     * - 비밀번호 검증 후, 사용자 DB에서 삭제 (Hard Delete)
     *
     * @param request 회원 삭제 요청 DTO (비밀번호 포함)
     * @param httpRequest 인증 정보 포함 (userId)
     * @return 삭제된 사용자 정보 (간단 요약)
     */
    @DeleteMapping("/delete")
    public ResponseEntity<DeleteUser.Response> deleteUser(
            @Valid @RequestBody DeleteUser.Request request,
            HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");
        DeleteUser.Response response = userService.deleteUser(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 회원 정보 수정 API
     * - 이메일, 닉네임, 전화번호, 유저타입 등 수정 가능
     * - 비밀번호도 변경 가능 (암호화 적용)
     * - 관리자 전환은 제한됨
     *
     * @param request 수정 요청 DTO (현재 비밀번호 포함)
     * @param httpRequest 인증 정보 포함
     * @return 수정된 사용자 정보
     */
    @PutMapping("/update")
    public ResponseEntity<UpdateUser.Response> updateUser(
            @Valid @RequestBody UpdateUser.Request request,
            HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");
        User updatedUser = userService.updateUser(userId, request);
        return ResponseEntity.ok(UpdateUser.Response.from(updatedUser));
    }

    /**
     * 점주의 '파트너 등록 요청' 처리 API
     * - OWNER 유형의 사용자만 가능
     * - 비밀번호 확인 후 파트너 여부를 true로 변경
     *
     * @param request 요청 DTO (비밀번호 포함)
     * @param httpRequest 인증 정보 포함 (userId)
     * @return 변경된 사용자 정보 (파트너 여부 포함)
     */
    @PutMapping("/updatePartnership")
    public ResponseEntity<UpdateUserPartnership.Response> updateIsPartner(
            @Valid @RequestBody UpdateUserPartnership.Request request,
            HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");
        User updatedUser = userService.updateIsPartner(userId, request);
        return ResponseEntity.ok(UpdateUserPartnership.Response.from(UserDto.fromEntity(updatedUser)));
    }
}
