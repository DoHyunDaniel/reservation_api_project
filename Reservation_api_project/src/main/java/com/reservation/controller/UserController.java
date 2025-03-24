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

    @PostMapping("/signup")
    public ResponseEntity<CreateUser.Response> createUser(@Valid @RequestBody CreateUser.Request request) {
        User user = userService.createUser(request);
        return ResponseEntity.ok(CreateUser.Response.from(UserDto.fromEntity(user)));
    }
    
	@DeleteMapping("/delete")
	public ResponseEntity<Response> deleteUser(@Valid @RequestBody DeleteUser.Request request, HttpServletRequest httpRequest) {
		Long userId = (Long) httpRequest.getAttribute("userId");
		DeleteUser.Response response = userService.deleteUser(userId, request);
        
        return ResponseEntity.ok(response);
	}
	
	@PutMapping("/update")
	public ResponseEntity<UpdateUser.Response> updateUser(@Valid @RequestBody UpdateUser.Request request, HttpServletRequest httpRequest) {
		Long userId = (Long) httpRequest.getAttribute("userId");
		User updatedUser = userService.updateUser(userId, request);
        return ResponseEntity.ok(UpdateUser.Response.from(updatedUser));
    }
	
	@PutMapping("/updatePartnership")
	public ResponseEntity<UpdateUserPartnership.Response> updateIsPartner(@Valid @RequestBody UpdateUserPartnership.Request request,HttpServletRequest httpRequest) {
		Long userId = (Long) httpRequest.getAttribute("userId");
		User updatedUser = userService.updateIsPartner(userId, request);
        return ResponseEntity.ok(UpdateUserPartnership.Response.from(UserDto.fromEntity(updatedUser)));
    }
}
