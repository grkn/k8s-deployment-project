package com.k8s.challenge.repository;

import com.k8s.challenge.entity.DeploymentEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DeploymentRepository extends CrudRepository<DeploymentEntity, String> {
    List<DeploymentEntity> findAllByNamespaceAndUserUserNameOrderByCreationTimestampDesc(String namespace ,String userName);
    List<DeploymentEntity> findAllByUserUserNameOrderByCreationTimestampDesc(String userName);
}
