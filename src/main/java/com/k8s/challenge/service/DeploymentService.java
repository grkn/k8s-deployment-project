package com.k8s.challenge.service;

import com.k8s.challenge.entity.DeploymentEntity;
import com.k8s.challenge.entity.UserEntity;
import com.k8s.challenge.repository.DeploymentRepository;
import com.k8s.challenge.resource.DeploymentResource;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Deployment;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DeploymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentService.class);
    private final UserService userService;
    private final KubernetesClientService kubernetesClientService;
    private final DeploymentRepository deploymentRepository;
    private final ConversionService conversionService;

    @Transactional
    public DeploymentResource createDeployment(String userName, String namespace, V1Deployment deployment, Boolean pretty, String dryRun) throws ApiException {
        LOGGER.trace("Create deployment request received from user : {}", userName);
        UserEntity userEntity = userService.findUserByName(userName);
        V1Deployment createdDeployment = kubernetesClientService.createDeployment(namespace, deployment, pretty, dryRun, userName);
        DeploymentResource deploymentResource = conversionService.convert(createdDeployment, DeploymentResource.class);
        DeploymentEntity deploymentEntity = conversionService.convert(deploymentResource, DeploymentEntity.class);
        deploymentEntity.setUser(userEntity);

        if (CollectionUtils.isEmpty(userEntity.getDeployments())) {
            userEntity.setDeployments(new HashSet<>());
        }
        userEntity.getDeployments().add(deploymentEntity);
        LOGGER.trace("Create deployment request finished for user : {}. Result is : {}", userName, deploymentResource.toString());
        return deploymentResource;
    }

    @Transactional
    public List<DeploymentResource> listDeployments(String userName, String namespace) throws ApiException {
        LOGGER.trace("List deployment request received from user : {} with namespace: {}", userName, namespace);
        UserEntity userEntity = userService.findUserByName(userName);
        List<DeploymentEntity> persistedDeployments;
        if (StringUtils.isEmpty(namespace)) {
            persistedDeployments = deploymentRepository
                    .findAllByUserUserNameOrderByCreationTimestampDesc(userEntity.getUserName());
        } else {
            persistedDeployments = deploymentRepository
                    .findAllByNamespaceAndUserUserNameOrderByCreationTimestampDesc(namespace, userName);
        }

        if (CollectionUtils.isEmpty(persistedDeployments)) {
            List<V1Deployment> v1Deployments = kubernetesClientService.listDeployments(namespace, userName).getItems();
            if (CollectionUtils.isEmpty(v1Deployments)) {
                LOGGER.trace("List deployment request finished for user : {} with namespace: {}. Result is empty", userName, namespace);
                return Collections.emptyList();
            } else {

                List<DeploymentResource> deploymentResources = v1Deployments.stream()
                        .map(v1Deployment -> conversionService.convert(v1Deployment, DeploymentResource.class))
                        .collect(Collectors.toList());

                migrateFromKubernetesToDb(userEntity, deploymentResources);
                LOGGER.trace("List deployment request finished for user : {} with namespace: {}. Result is {}",
                        userName, namespace, deploymentResources.toString());
                return deploymentResources;
            }
        }

        List<DeploymentResource> deploymentResources = persistedDeployments
                .stream()
                .map(deploymentEntity -> conversionService.convert(deploymentEntity, DeploymentResource.class))
                .collect(Collectors.toList());
        LOGGER.trace("List deployment request finished for user : {} with namespace: {}. Result is {}",
                userName, namespace, deploymentResources.toString());
        return deploymentResources;
    }

    private void migrateFromKubernetesToDb(UserEntity userEntity, List<DeploymentResource> deploymentResources) {
        Set<DeploymentEntity> deploymentEntities = deploymentResources.stream()
                .map(deploymentResource -> conversionService.convert(deploymentResource, DeploymentEntity.class))
                .peek(deploymentEntity -> deploymentEntity.setUser(userEntity))
                .collect(Collectors.toSet());
        userEntity.setDeployments(deploymentEntities);
    }
}
