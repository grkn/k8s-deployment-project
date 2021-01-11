package com.k8s.challenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k8s.challenge.Application;
import com.k8s.challenge.constant.ExceptionResponse;
import com.k8s.challenge.dto.UserDto;
import com.k8s.challenge.entity.UserEntity;
import com.k8s.challenge.exception.handler.ExceptionHandlerAdvice;
import com.k8s.challenge.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ActiveProfiles("integration-test")
@ContextConfiguration(classes = Application.class)
@TestPropertySource(value = "classpath:application_test.properties")
@ExtendWith(SpringExtension.class)
public class AuthenticationControllerIT {

    private static final String ANY_USER_NAME = "anyUserName";
    private static final String ANY_PASSWORD = "anyPassword";
    private static final String ANY_NAME = "anyName";
    private static final String WRONG_PASSWORD = "wrongPassword";

    private MockMvc mockMvc;

    @Autowired
    private AuthenticationController authenticationController;

    @Autowired
    private ExceptionHandlerAdvice exceptionHandlerAdvice;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authenticationController)
                .setControllerAdvice(exceptionHandlerAdvice)
                .build();
    }

    @AfterEach
    void destroy() {
        userRepository.deleteAll();
    }

    @Nested
    class AuthenticateEndpoint {

        @Test
        void givenValidUserDto_whenAuthenticateUser_thenAuthenticateUserSuccessfully() throws Exception {
            // Given
            UserDto userDto = UserDto.builder()
                    .userName(ANY_USER_NAME)
                    .password(ANY_PASSWORD)
                    .name(ANY_NAME)
                    .build();
            // When
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/authorize")
                    .content(objectMapper.writeValueAsString(userDto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    // Then
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.name", IsEqual.equalTo(ANY_NAME)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.userName", IsEqual.equalTo(ANY_USER_NAME)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.password").doesNotExist());

        }

        @Test
        void givenUserNameExistsInDatabase_whenAuthenticateUser_thenThrowsBadRequestException() throws Exception {
            // Given
            UserEntity userEntity = UserEntity.builder()
                    .userName(ANY_USER_NAME)
                    .password(bCryptPasswordEncoder.encode(ANY_PASSWORD))
                    .name(ANY_NAME)
                    .build();

            userRepository.save(userEntity);

            UserDto userDto = UserDto.builder()
                    .userName(ANY_USER_NAME)
                    .password(ANY_PASSWORD)
                    .name(ANY_NAME)
                    .build();
            // When
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/authorize")
                    .content(objectMapper.writeValueAsString(userDto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    // Then
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.code", IsEqual.equalTo(ExceptionResponse.BAD_REQUEST.getCode())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.reasonMessage").exists());

        }

        @Test
        void givenUserDtoWithMissingUserName_whenAuthenticateUser_thenAuthenticateUserSuccessfully() throws Exception {
            // Given
            UserDto userDto = UserDto.builder()
                    .password(ANY_PASSWORD)
                    .name(ANY_NAME)
                    .build();
            // When
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/authorize")
                    .content(objectMapper.writeValueAsString(userDto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    // Then
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.code",
                            IsEqual.equalTo(ExceptionResponse.BAD_REQUEST.getCode())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.reasonMessage").exists())
                    .andReturn();
            Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains("userName");
        }

        @Test
        void givenUserDtoWithMissingPassword_whenAuthenticateUser_thenAuthenticateUserSuccessfully() throws Exception {
            // Given
            // Given
            UserDto userDto = UserDto.builder()
                    .userName(ANY_USER_NAME)
                    .name(ANY_NAME)
                    .build();
            // When
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/authorize")
                    .content(objectMapper.writeValueAsString(userDto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    // Then
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.code",
                            IsEqual.equalTo(ExceptionResponse.BAD_REQUEST.getCode())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.reasonMessage").exists())
                    .andReturn();
            Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains("password");
        }
    }

    @Nested
    class TokenEndpoint {

        @Test
        void givenValidUserDto_whenGetTokenForUser_thenAccessTokenIsReceived() throws Exception {
            // Given
            userRepository.save(UserEntity.builder()
                    .name(ANY_NAME)
                    .userName(ANY_USER_NAME)
                    .password(bCryptPasswordEncoder.encode(ANY_PASSWORD))
                    .build());

            UserDto userDto = UserDto.builder()
                    .userName(ANY_USER_NAME)
                    .password(ANY_PASSWORD)
                    .build();
            // When
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/token")
                    .content(objectMapper.writeValueAsString(userDto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    // Then
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").exists());
        }

        @Test
        void givenWrongPassword_whenGetTokenForUser_thenThrowsNotFoundException() throws Exception {
            // Given
            userRepository.save(UserEntity.builder()
                    .name(ANY_NAME)
                    .userName(ANY_USER_NAME)
                    .password(bCryptPasswordEncoder.encode(ANY_PASSWORD))
                    .build());

            UserDto userDto = UserDto.builder()
                    .userName(ANY_USER_NAME)
                    .password(WRONG_PASSWORD)
                    .build();
            // When
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/token")
                    .content(objectMapper.writeValueAsString(userDto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    // Then
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ExceptionResponse.NOT_FOUND.getCode()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.reasonMessage")
                            .value("Username and password are not valid"));
        }

        @Test
        void givenInvalidUserDtoWithMissingUserName_whenGetTokenForUser_thenAccessTokenIsReceived() throws Exception {
            // Given
            UserDto userDto = UserDto.builder()
                    .password(ANY_PASSWORD)
                    .build();
            // When
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/token")
                    .content(objectMapper.writeValueAsString(userDto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    // Then
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ExceptionResponse.BAD_REQUEST.getCode()))
                    .andReturn();

            Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains("userName");
        }


        @Test
        void givenInvalidUserDtoWithMissingPassword_whengetTokenForUser_thenAccessTokenIsReceived() throws Exception {
            // Given
            UserDto userDto = UserDto.builder()
                    .userName(ANY_USER_NAME)
                    .build();
            // When
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/token")
                    .content(objectMapper.writeValueAsString(userDto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    // Then
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ExceptionResponse.BAD_REQUEST.getCode()))
                    .andReturn();

            Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains("password");
        }
    }
}
