package com.k8s.challenge.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.collect.ImmutableSet;
import com.k8s.challenge.constant.ChallengeConstant;
import com.k8s.challenge.entity.AuthorizationEntity;
import com.k8s.challenge.entity.UserEntity;
import com.k8s.challenge.exception.BadRequestException;
import com.k8s.challenge.exception.NotFoundException;
import com.k8s.challenge.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@AllArgsConstructor
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserEntity findUserByName(String userName) {
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new NotFoundException(String.format("User can not be found by given username : %s",
                        userName)));
    }

    @Transactional
    public UserEntity createUserEntity(UserEntity userEntity) {
        LOGGER.trace("Create user request is received for user: {} ", userEntity.getUserName());
        if (userRepository.findByUserName(userEntity.getUserName()).isPresent()) {
            throw new BadRequestException("User already exists");
        }

        AuthorizationEntity authorizationEntity = new AuthorizationEntity();
        authorizationEntity.setAuth(ChallengeConstant.ROLE_USER);
        authorizationEntity.setUsers(ImmutableSet.of(userEntity));

        userEntity.setAuthorizations(ImmutableSet.of(authorizationEntity));
        String password = passwordEncoder.encode(userEntity.getPassword());
        userEntity.setPassword(password);
        LOGGER.trace("Create user request is finished for user: {} ", userEntity.getUserName());
        return userRepository.save(userEntity);
    }

    public String createToken(UserEntity userEntity) {
        LOGGER.trace("Create access token request is received for user: {} ", userEntity.getUserName());

        UserEntity persistedUser = findUserByName(userEntity.getUserName());

        if (passwordEncoder.matches(userEntity.getPassword(), persistedUser.getPassword())) {
            LOGGER.trace("Create access token request is finished for user: {} ", userEntity.getUserName());
            return JWT.create()
                    .withSubject(persistedUser.getUserName())
                    .withExpiresAt(new Date(System.currentTimeMillis() + ChallengeConstant.EXPIRE_TIME))
                    .sign(Algorithm.HMAC512(ChallengeConstant.DUMMY_SIGN.getBytes()));
        }

        throw new NotFoundException("Username and password are not valid");
    }

}
