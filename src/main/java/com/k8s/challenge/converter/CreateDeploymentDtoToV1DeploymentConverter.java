package com.k8s.challenge.converter;

import com.google.common.collect.ImmutableMap;
import com.k8s.challenge.dto.CreateDeploymentDto;
import io.kubernetes.client.openapi.models.*;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CreateDeploymentDtoToV1DeploymentConverter implements Converter<CreateDeploymentDto, V1Deployment> {

    private static final String APP = "app";

    @Override
    public V1Deployment convert(CreateDeploymentDto createDeploymentDto) {
        V1Deployment v1Deployment = new V1Deployment();
        v1Deployment.setApiVersion(createDeploymentDto.getApiVersion());
        v1Deployment.setKind(createDeploymentDto.getKind());

        // Metadata name set operation
        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        v1ObjectMeta.setName(createDeploymentDto.getMetaDataName());
        v1Deployment.setMetadata(v1ObjectMeta);

        // Spec set operations
        V1DeploymentSpec v1DeploymentSpec = new V1DeploymentSpec();
        v1DeploymentSpec.setReplicas(createDeploymentDto.getSpecReplicas());
        V1LabelSelector v1LabelSelector = new V1LabelSelector();
        v1LabelSelector.setMatchLabels(ImmutableMap.of(APP, createDeploymentDto.getAppName()));
        v1DeploymentSpec.setSelector(v1LabelSelector);

        V1PodTemplateSpec v1PodTemplateSpec = new V1PodTemplateSpec();
        v1ObjectMeta = new V1ObjectMeta();
        v1ObjectMeta.setLabels(ImmutableMap.of(APP, createDeploymentDto.getAppName()));
        v1PodTemplateSpec.setMetadata(v1ObjectMeta);
        V1PodSpec v1PodSpec = new V1PodSpec();
        V1Container v1Container = new V1Container();
        v1Container.setImage(createDeploymentDto.getImageName());
        v1Container.setImagePullPolicy(createDeploymentDto.getImagePullPolicy().getValue());
        v1Container.setName(createDeploymentDto.getAppName());

        List<V1ContainerPort> containerPorts = createDeploymentDto.getContainerPorts().stream().map(port -> {
            V1ContainerPort v1ContainerPort = new V1ContainerPort();
            v1ContainerPort.setContainerPort(port);
            return v1ContainerPort;
        }).collect(Collectors.toList());
        v1Container.setPorts(containerPorts);
        v1PodSpec.setContainers(Collections.singletonList(v1Container));
        v1PodTemplateSpec.setSpec(v1PodSpec);
        v1DeploymentSpec.setTemplate(v1PodTemplateSpec);
        v1Deployment.setSpec(v1DeploymentSpec);
        return v1Deployment;
    }
}
