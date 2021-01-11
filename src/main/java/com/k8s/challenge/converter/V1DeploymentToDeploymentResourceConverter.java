package com.k8s.challenge.converter;

import com.k8s.challenge.resource.DeploymentResource;
import io.kubernetes.client.openapi.models.V1Deployment;
import org.joda.time.DateTime;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;

@Component
public class V1DeploymentToDeploymentResourceConverter implements Converter<V1Deployment, DeploymentResource> {

    @Override
    public DeploymentResource convert(V1Deployment v1Deployment) {
        DeploymentResource deploymentResource = new DeploymentResource();
        deploymentResource.setApiVersion(v1Deployment.getApiVersion());
        deploymentResource.setKind(v1Deployment.getKind());
        if (v1Deployment.getSpec() != null && v1Deployment.getSpec().getTemplate() != null
                && v1Deployment.getSpec().getTemplate().getSpec() != null
                && !CollectionUtils.isEmpty(v1Deployment.getSpec().getTemplate().getSpec().getContainers())) {
            deploymentResource.setImage(v1Deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImage());
        }

        if (v1Deployment.getSpec() != null) {
            deploymentResource.setReplicas(v1Deployment.getSpec().getReplicas());
        }

        if (v1Deployment.getMetadata() != null) {
            deploymentResource.setName(v1Deployment.getMetadata().getName());
            deploymentResource.setNamespace(v1Deployment.getMetadata().getNamespace());

            DateTime dateTime = v1Deployment.getMetadata().getCreationTimestamp();
            if (dateTime != null) {
                LocalDateTime creationTimestamp = LocalDateTime.of(dateTime.getYear(), dateTime.getMonthOfYear(),
                        dateTime.getDayOfMonth(), dateTime.getHourOfDay(), dateTime.getMinuteOfHour(), dateTime.getSecondOfMinute());
                deploymentResource.setCreationTimestamp(creationTimestamp);
            }
        }
        return deploymentResource;
    }
}
