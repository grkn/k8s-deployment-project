package com.k8s.challenge.service;

import com.k8s.challenge.entity.AuthorizationEntity;
import com.k8s.challenge.entity.UserEntity;
import com.k8s.challenge.exception.BadRequestException;
import com.k8s.challenge.exception.NotFoundException;
import com.k8s.challenge.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static com.k8s.challenge.constant.ChallengeConstant.ROLE_USER;

public class UserServiceTest {

    private static final String ANY_USER_NAME = "anyUserName";
    private static final String ANY_NAME = "anyName";
    private static final String ANY_PASSWORD = "anyPassword";
    private static final String ANY_ENCODED_PASSWORD = "anyEncodedPassword";
    private static final String INVALID_USER_NAME = "anyInvalidUserName";

    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    private UserEntity userEntity;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, passwordEncoder);

        userEntity = UserEntity.builder()
                .name(ANY_NAME)
                .password(ANY_PASSWORD)
                .userName(ANY_USER_NAME)
                .build();
    }

    @Nested
    class CreateUser {

        @Test
        void givenUserEntity_whenCreateUser_thenReturnCreatedUserWithHashedPassword() {
            // Given
            Mockito.when(passwordEncoder.encode(ANY_PASSWORD)).thenReturn(ANY_ENCODED_PASSWORD);
            Mockito.when(userRepository.save(userEntity)).thenReturn(UserEntity.builder()
                    .name(ANY_NAME)
                    .password(ANY_ENCODED_PASSWORD)
                    .userName(ANY_USER_NAME)
                    .authorizations(Collections.singleton(AuthorizationEntity.builder()
                            .auth(ROLE_USER).build()))
                    .build());
            // When
            UserEntity result = userService.createUserEntity(userEntity);
            // Then
            Assertions.assertThat(result.getPassword()).isEqualTo(ANY_ENCODED_PASSWORD);
            Assertions.assertThat(result.getName()).isEqualTo(ANY_NAME);
            Assertions.assertThat(result.getUserName()).isEqualTo(ANY_USER_NAME);
            Assertions.assertThat(result.getAuthorizations()).isNotEmpty();
        }

        @Test
        void givenUserNameThatExists_whenCreateUser_thenThrowsBadRequestException() {
            // Given
            Mockito.when(userRepository.findByUserName(ANY_USER_NAME)).thenReturn(Optional.of(userEntity));
            // When
            ThrowableAssert.ThrowingCallable throwingCallable = () -> userService.createUserEntity(userEntity);
            // Then
            Assertions.assertThatThrownBy(throwingCallable).isInstanceOf(BadRequestException.class);
        }

    }

    @Nested
    class CreateToken {

        @Test
        void givenValidUserEntity_whenCreateToken_thenReturnAccessToken() {
            // Given
            Mockito.when(userRepository.findByUserName(userEntity.getUserName()))
                    .thenReturn(Optional.of(userEntity));
            Mockito.when(passwordEncoder.matches(userEntity.getPassword(), userEntity.getPassword()))
                    .thenReturn(true);
            // When
            String accessToken = userService.createToken(userEntity);
            // Then
            Assertions.assertThat(accessToken).isNotBlank();
        }

        @Test
        void givenInValidPasswordInUserEntity_whenCreateToken_thenThrowNotFoundException() {
            // Given
            Mockito.when(userRepository.findByUserName(userEntity.getUserName()))
                    .thenReturn(Optional.of(userEntity));
            Mockito.when(passwordEncoder.matches(userEntity.getPassword(), userEntity.getPassword()))
                    .thenReturn(false);
            // When
            ThrowableAssert.ThrowingCallable throwingCallable = () -> userService.createToken(userEntity);

            // Then
            Assertions.assertThatThrownBy(throwingCallable).isInstanceOf(NotFoundException.class);
        }

    }

    @Nested
    class FindUser {

        @Test
        void givenUserName_whenFindUserEntity_thenUserEntityReturned() {
            // Given
            Mockito.when(userRepository.findByUserName(ANY_USER_NAME))
                    .thenReturn(Optional.of(userEntity));
            // When
            UserEntity result = userService.findUserByName(ANY_USER_NAME);
            // Then
            Assertions.assertThat(result).isNotNull();
        }

        @Test
        void givenInvalidUserName_whenFindUserEntity_thenThrowsNotFoundException() {
            // Given
            Mockito.when(userRepository.findByUserName(ANY_USER_NAME))
                    .thenReturn(Optional.of(userEntity));
            // When
            ThrowableAssert.ThrowingCallable throwingCallable = () -> userService.findUserByName(INVALID_USER_NAME);
            // Then
            Assertions.assertThatThrownBy(throwingCallable).isInstanceOf(NotFoundException.class);
        }
    }
}
