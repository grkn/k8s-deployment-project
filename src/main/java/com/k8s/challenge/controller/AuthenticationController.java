package com.k8s.challenge.controller;

import com.k8s.challenge.dto.UserDto;
import com.k8s.challenge.entity.UserEntity;
import com.k8s.challenge.resource.TokenResource;
import com.k8s.challenge.resource.UserResource;
import com.k8s.challenge.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/api/v1")
@AllArgsConstructor
public class AuthenticationController {

    private final ConversionService conversionService;
    private final UserService userService;

    @PostMapping("/authorize")
    public ResponseEntity<UserResource> authorize(@RequestBody @Valid UserDto userDto) {
        return ResponseEntity.ok(conversionService.convert(userService.createUserEntity(conversionService.convert(userDto,
                UserEntity.class)), UserResource.class));
    }

    @PostMapping("/token")
    public ResponseEntity<TokenResource> getAccessToken(@RequestBody @Valid UserDto userDto) {
        return ResponseEntity.ok(new TokenResource(userService.
                createToken(conversionService.convert(userDto, UserEntity.class))));
    }
}
