package com.k8s.challenge.converter;

import com.k8s.challenge.entity.DeploymentEntity;
import com.k8s.challenge.resource.DeploymentResource;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DeploymentEntityToDeploymentResourceConverter implements Converter<DeploymentEntity, DeploymentResource> {
    @Override
    public DeploymentResource convert(DeploymentEntity deploymentEntity) {
        DeploymentResource deploymentResource = new DeploymentResource();
        deploymentResource.setReplicas(deploymentEntity.getReplicas());
        deploymentResource.setImage(deploymentEntity.getImage());
        deploymentResource.setName(deploymentEntity.getDeploymentName());
        deploymentResource.setNamespace(deploymentEntity.getNamespace());
        deploymentResource.setKind(deploymentEntity.getKind());
        deploymentResource.setApiVersion(deploymentEntity.getApiVersion());
        deploymentResource.setCreationTimestamp(deploymentEntity.getCreationTimestamp());
        return deploymentResource;
    }
}
