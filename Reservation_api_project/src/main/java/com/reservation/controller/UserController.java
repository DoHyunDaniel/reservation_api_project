package com.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.domain.User;
import com.reservation.dto.CreateUser;
import com.reservation.dto.DeleteUser;
import com.reservation.dto.DeleteUser.Response;
import com.reservation.dto.UserDto;
import com.reservation.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<CreateUser.Response> createUser(@Valid @RequestBody CreateUser.Request request) {
        User user = userService.createUser(request);
        return ResponseEntity.ok(CreateUser.Response.from(UserDto.fromEntity(user)));
    }
    
	@DeleteMapping("/delete")
	public ResponseEntity<Response> deleteUser(@RequestBody @Valid DeleteUser.Request request) {
        DeleteUser.Response response = userService.deleteUser(request);
        
        return ResponseEntity.ok(response);
	}
}
