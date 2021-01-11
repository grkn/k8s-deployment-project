package com.k8s.challenge.objectfactory;

import com.google.common.collect.ImmutableMap;
import com.k8s.challenge.constant.ImagePullPolicy;
import io.kubernetes.client.openapi.models.*;

import java.util.Collections;
import java.util.List;

public final class V1DeploymentFactory {

    private static final String API_VERSION = "apps/v1";
    private static final String KIND = "Deployment";
    private static final String NAME = "deployment";
    private static final String APP = "app";
    private static final String IMAGE = "nginx";
    private static final Integer PORT = 80;

    private V1DeploymentFactory() {
    }

    public static V1Deployment createV1Deployment() {
        V1Deployment v1Deployment = new V1Deployment();
        v1Deployment.setApiVersion(API_VERSION);
        v1Deployment.setKind(KIND);

        // Metadata name set operation
        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        v1ObjectMeta.setName(NAME);
        v1Deployment.setMetadata(v1ObjectMeta);

        // Spec set operations
        V1DeploymentSpec v1DeploymentSpec = new V1DeploymentSpec();
        v1DeploymentSpec.setReplicas(2);
        V1LabelSelector v1LabelSelector = new V1LabelSelector();
        v1LabelSelector.setMatchLabels(ImmutableMap.of(APP, APP));
        v1DeploymentSpec.setSelector(v1LabelSelector);

        V1PodTemplateSpec v1PodTemplateSpec = new V1PodTemplateSpec();
        v1ObjectMeta = new V1ObjectMeta();
        v1ObjectMeta.setLabels(ImmutableMap.of(APP, APP));
        v1PodTemplateSpec.setMetadata(v1ObjectMeta);
        V1PodSpec v1PodSpec = new V1PodSpec();
        V1Container v1Container = new V1Container();
        v1Container.setImage(IMAGE);
        v1Container.setImagePullPolicy(ImagePullPolicy.ALWAYS.getValue());
        v1Container.setName(APP);

        V1ContainerPort v1ContainerPort = new V1ContainerPort();
        v1ContainerPort.containerPort(PORT);
        List<V1ContainerPort> containerPorts = Collections.singletonList(v1ContainerPort);
        v1Container.setPorts(containerPorts);
        v1PodSpec.setContainers(Collections.singletonList(v1Container));
        v1PodTemplateSpec.setSpec(v1PodSpec);
        v1DeploymentSpec.setTemplate(v1PodTemplateSpec);
        v1Deployment.setSpec(v1DeploymentSpec);

        return v1Deployment;
    }


    public static V1DeploymentList createV1DeploymentList() {
        V1DeploymentList v1DeploymentList = new V1DeploymentList();
        v1DeploymentList.setItems(Collections.singletonList(createV1Deployment()));
        return v1DeploymentList;
    }
}
