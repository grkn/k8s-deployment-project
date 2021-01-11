package com.k8s.challenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k8s.challenge.constant.ChallengeConstant;
import com.k8s.challenge.constant.ExceptionResponse;
import com.k8s.challenge.resource.ErrorResource;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/error")
@AllArgsConstructor
public class ErrorController {

    private final ObjectMapper objectMapper;

    @GetMapping
    public void applicationError(HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResource errorResource;
        if (SecurityContextHolder.getContext().getAuthentication() == null ||
                !SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                        .contains(new SimpleGrantedAuthority(ChallengeConstant.ROLE_USER))) {
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            errorResource = new ErrorResource(ExceptionResponse.UNAUTHORIZED.getCode(),
                    ExceptionResponse.UNAUTHORIZED.getMessage());
        } else {
            httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResource = new ErrorResource(ExceptionResponse.SERVER_ERROR.getCode(),
                    ExceptionResponse.SERVER_ERROR.getMessage());
        }

        httpServletResponse.getWriter().write(objectMapper.writeValueAsString(errorResource));
        httpServletResponse.getWriter().flush();
    }
}
