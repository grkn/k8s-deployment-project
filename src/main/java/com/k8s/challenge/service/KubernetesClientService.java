package com.k8s.challenge.service;

import com.google.common.collect.ImmutableMap;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.util.generic.options.DeleteOptions;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@AllArgsConstructor
public class KubernetesClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KubernetesClientService.class);
    private final AppsV1Api deploymentApi;

    /**
     * @param namespace namespace parameter is optional
     * @return
     * @throws ApiException
     */
    public V1DeploymentList listDeployments(String namespace, String userName) throws ApiException {
        LOGGER.trace("List deployments request is received for namespace: {}", namespace);
        if (StringUtils.isEmpty(namespace)) {
            V1DeploymentList v1DeploymentList = deploymentApi.listDeploymentForAllNamespaces(null, null,
                    null, "userName=" + userName, Integer.MAX_VALUE, null, null, null, Boolean.FALSE);
            LOGGER.trace("List deployments request is finished for namespace: {}. Result size: {}",
                    namespace, v1DeploymentList.getItems() != null ? v1DeploymentList.getItems().size() : 0);
            return v1DeploymentList;
        }

        V1DeploymentList v1DeploymentList = deploymentApi.listNamespacedDeployment(namespace, null, null, null,
                null, "userName=" + userName, null, null, null, Boolean.FALSE);
        LOGGER.trace("List deployments request is finished for namespace: {}. Result size: {}",
                namespace, v1DeploymentList.getItems() != null ? v1DeploymentList.getItems().size() : 0);
        return v1DeploymentList;
    }

    public V1Deployment createDeployment(String namespace, V1Deployment deployment, Boolean pretty, String dryRun, String userName)
            throws ApiException {
        LOGGER.trace("Create deployment request is received for namespace: {}", namespace);
        if (CollectionUtils.isEmpty(deployment.getMetadata().getLabels())) {
            deployment.getMetadata().setLabels(ImmutableMap.of("userName", userName));
        } else {
            deployment.getMetadata().getLabels().put("userName", userName);
        }
        V1Deployment v1Deployment = deploymentApi.createNamespacedDeployment(namespace, deployment,
                pretty != null ? pretty.toString() : null, dryRun, null);
        LOGGER.trace("Create deployment request is finished for namespace: {}", namespace);
        return v1Deployment;
    }

    public void deleteDeployment(String name, String namespace) throws ApiException {
        deploymentApi.deleteNamespacedDeployment(name, namespace, null, null, 0, true,
                null, new DeleteOptions());
    }
}
