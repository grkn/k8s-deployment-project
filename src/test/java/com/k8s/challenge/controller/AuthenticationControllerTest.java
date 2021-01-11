package com.k8s.challenge.controller;

import com.k8s.challenge.dto.UserDto;
import com.k8s.challenge.entity.UserEntity;
import com.k8s.challenge.resource.TokenResource;
import com.k8s.challenge.resource.UserResource;
import com.k8s.challenge.service.UserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.ResponseEntity;

public class AuthenticationControllerTest {

    private static final String ANY_NAME = "anyName";
    private static final String ANY_PASSWORD = "anyPassword";
    private static final String ANY_USER_NAME = "anyUserName";
    private static final String ANY_TOKEN = "anyToken";

    private AuthenticationController authenticationController;

    @Mock
    private ConversionService conversionService;

    @Mock
    private UserService userService;

    private UserDto userDto;
    private UserEntity userEntity;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        authenticationController = new AuthenticationController(conversionService, userService);

         userDto = UserDto.builder()
                .name(ANY_NAME)
                .userName(ANY_USER_NAME)
                .password(ANY_PASSWORD)
                .build();

        userEntity = UserEntity.builder()
                .userName(ANY_NAME)
                .password(ANY_PASSWORD)
                .build();
    }

    @Nested
    class LoginEndpoint {
        @Test
        void givenValidUserDto_whenAuthorizeUser_thenUserSignedUpSuccessfully() {
            // Given
            UserResource userResource = UserResource.builder()
                    .name(ANY_NAME)
                    .userName(ANY_USER_NAME)
                    .build();

            Mockito.when(conversionService.convert(userDto, UserEntity.class)).thenReturn(userEntity);
            Mockito.when(userService.createUserEntity(userEntity)).thenReturn(userEntity);
            Mockito.when(conversionService.convert(userEntity, UserResource.class)).thenReturn(userResource);
            // When
            ResponseEntity<UserResource> userResourceResponseEntity = authenticationController.authorize(userDto);
            // Then
            Assertions.assertThat(userResourceResponseEntity.getBody()).isNotNull();
            Assertions.assertThat(userResourceResponseEntity.getBody().getName()).isEqualTo(ANY_NAME);
            Assertions.assertThat(userResourceResponseEntity.getBody().getUserName()).isEqualTo(ANY_USER_NAME);
        }
    }

    @Nested
    class TokenEndpoint {
        @Test
        void givenValidUserDto_whenGetAccessToken_thenUserGetsTokenSuccessfully() {
            // Given

            Mockito.when(conversionService.convert(userDto, UserEntity.class)).thenReturn(userEntity);
            Mockito.when(userService.createToken(userEntity)).thenReturn(ANY_TOKEN);

            // When
            ResponseEntity<TokenResource> tokenResourceResponseEntity = authenticationController.getAccessToken(userDto);

            // Then
            Assertions.assertThat(tokenResourceResponseEntity.getBody()).isNotNull();
            Assertions.assertThat(tokenResourceResponseEntity.getBody().getAccessToken()).isEqualTo(ANY_TOKEN);
        }
    }
}
