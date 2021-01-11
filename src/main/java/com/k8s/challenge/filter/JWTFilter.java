package com.k8s.challenge.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k8s.challenge.constant.ChallengeConstant;
import com.k8s.challenge.constant.ExceptionResponse;
import com.k8s.challenge.entity.UserEntity;
import com.k8s.challenge.exception.NotFoundException;
import com.k8s.challenge.resource.ErrorResource;
import com.k8s.challenge.service.UserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTFilter.class);
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        String token = httpServletRequest.getHeader(ChallengeConstant.AUTH_HEADER);
        if (token != null) {
            LOGGER.info("The request is authenticated. Performing Token validity");
            String userName;
            try {
                userName = JWT.require(Algorithm.HMAC512(ChallengeConstant.DUMMY_SIGN.getBytes()))
                        .build()
                        .verify(token.replace(ChallengeConstant.TOKEN_PREFIX, ""))
                        .getSubject();
            } catch (JWTVerificationException ex) {
                LOGGER.warn(String.format("Token is not valid. Token: %s", token), ex);
                generateErrorResponse(httpServletResponse, ExceptionResponse.UNAUTHORIZED);
                return;
            }
            LOGGER.info("Token is valid for username: {}", userName);
            try {
                UserEntity userEntity = userService.findUserByName(userName);
                List<GrantedAuthority> authList = userEntity.getAuthorizations()
                        .stream()
                        .map(authorizationEntity -> new SimpleGrantedAuthority(authorizationEntity.getAuth()))
                        .collect(Collectors.toList());
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(userEntity.getUserName(), userEntity.getPassword(), authList));
                LOGGER.debug("User has been found by given username: {} with authorities: {}", userName, authList.toString());
            } catch (NotFoundException ex) {
                LOGGER.warn("User couldn't be found with given username: {}", userName);
                generateErrorResponse(httpServletResponse, ExceptionResponse.NOT_FOUND);
                return;
            }
        }
        LOGGER.info("The request is NOT authenticated. It will continue to request chain.");
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private void generateErrorResponse(HttpServletResponse httpServletResponse, ExceptionResponse exceptionResponse) throws IOException {
        LOGGER.trace("Generating http error response");
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        httpServletResponse.setStatus(exceptionResponse.getStatus().value());
        ErrorResource errorResource = new ErrorResource(exceptionResponse.getCode(),
                exceptionResponse.getMessage());
        httpServletResponse.getWriter().write(objectMapper.writeValueAsString(errorResource));
        LOGGER.trace("Error response is {}", errorResource.toString());
        httpServletResponse.getWriter().flush();
    }
}
