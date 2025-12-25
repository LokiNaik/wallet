package com.wallet.controller;

import com.wallet.dto.*;
import com.wallet.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest createUserRequest){
        UserResponse user = userService.createUser(createUserRequest);
        if (user.getId() == null || user.getEmail() == null){
            return new ResponseEntity<>(ApiResponse.error("User not created"), HttpStatus.FORBIDDEN);
        }
        return  new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/info")
    public ResponseEntity<?> getUserInfo(@PathVariable Long id) {

       return userService.getUserInfo(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
