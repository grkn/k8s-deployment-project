package com.k8s.challenge.converter;

import com.k8s.challenge.entity.DeploymentEntity;
import com.k8s.challenge.resource.DeploymentResource;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DeploymentResourceToDeploymentEntityConverter implements Converter<DeploymentResource, DeploymentEntity> {

    @Override
    public DeploymentEntity convert(DeploymentResource deploymentResource) {
        DeploymentEntity deploymentEntity = new DeploymentEntity();
        deploymentEntity.setReplicas(deploymentResource.getReplicas());
        deploymentEntity.setImage(deploymentResource.getImage());
        deploymentEntity.setDeploymentName(deploymentResource.getName());
        deploymentEntity.setNamespace(deploymentResource.getNamespace());
        deploymentEntity.setKind(deploymentResource.getKind());
        deploymentEntity.setApiVersion(deploymentResource.getApiVersion());
        deploymentEntity.setAppName(deploymentResource.getName());
        deploymentEntity.setCreationTimestamp(deploymentResource.getCreationTimestamp());
        return deploymentEntity;
    }
}
