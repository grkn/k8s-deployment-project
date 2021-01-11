package com.k8s.challenge.repository;

import com.k8s.challenge.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<UserEntity, String> {

    Optional<UserEntity> findByUserName(String userName);
}
