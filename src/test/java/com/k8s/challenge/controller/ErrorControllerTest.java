package com.k8s.challenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

import static com.k8s.challenge.constant.ChallengeConstant.ROLE_USER;

public class ErrorControllerTest {

    private static final String ANY_USERNAME = "anyUserName";
    private static final String ANY_PASSWORD = "anyPassword";
    private ErrorController errorController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private PrintWriter printWriter;

    @BeforeEach
    void init() throws IOException {
        MockitoAnnotations.openMocks(this);
        errorController = new ErrorController(objectMapper);

        Mockito.when(httpServletResponse.getWriter()).thenReturn(printWriter);
    }

    @Test
    void givenSecurityUserWithoutUserRole_whenApplicationErrorIsCalled_thenUnauthorizedExceptionIsWrittenToStream() throws IOException {
        // Given
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(ANY_USERNAME, ANY_PASSWORD));

        // When
        errorController.applicationError(httpServletResponse);
        // Then
        Mockito.verify(httpServletResponse).setStatus(HttpStatus.UNAUTHORIZED.value());
        Mockito.verify(printWriter).write(Mockito.anyString());
    }

    @Test
    void givenSecurityUserWithUserRole_whenApplicationErrorIsCalled_thenServerErrorExceptionIsWrittenToStream() throws IOException {
        // Given
        GrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(ROLE_USER);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(ANY_USERNAME, ANY_PASSWORD,
                        Collections.singleton(simpleGrantedAuthority)));
        // When
        errorController.applicationError(httpServletResponse);
        // Then
        Mockito.verify(httpServletResponse).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        Mockito.verify(printWriter).write(Mockito.anyString());
    }
}
