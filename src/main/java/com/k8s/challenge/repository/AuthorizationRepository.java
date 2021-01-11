package com.k8s.challenge.repository;

import com.k8s.challenge.entity.AuthorizationEntity;
import org.springframework.data.repository.CrudRepository;

public interface AuthorizationRepository extends CrudRepository<AuthorizationEntity, String> {
}
