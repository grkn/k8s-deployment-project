package com.k8s.challenge.service;

import com.google.common.collect.ImmutableMap;
import com.k8s.challenge.objectfactory.V1DeploymentFactory;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

public class KubernetesClientServiceTest {

    private static final String ANY_USER_NAME = "anyUserName";
    private static final String ANY_NAMESPACE = "anyNamespace";
    private static final String NAME = "deployment";
    private static final String DRY_RUN = "anyDryRun";
    private static final Boolean IS_PRETTY = true;
    private static final String INVALID_NAMESPACE = "invalidNamespace";
    private static final String ANY_LABEL = "anyLabel";
    private KubernetesClientService kubernetesClientService;

    @Mock
    private AppsV1Api deploymentApi;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        kubernetesClientService = new KubernetesClientService(deploymentApi);
    }

    @Nested
    class ListDeployments {

        @Test
        void givenUserNameAndNamespace_whenListDeployments_thenReturnDeployments() throws ApiException {
            // Given
            Mockito.when(deploymentApi.listNamespacedDeployment(ANY_NAMESPACE, null, null, null,
                    null, "userName=" + ANY_USER_NAME, null, null, null, Boolean.FALSE))
                    .thenReturn(V1DeploymentFactory.createV1DeploymentList());
            // When
            V1DeploymentList v1DeploymentList = kubernetesClientService.listDeployments(ANY_NAMESPACE, ANY_USER_NAME);

            // Then
            Assertions.assertThat(v1DeploymentList).isNotNull();
            Assertions.assertThat(v1DeploymentList.getItems()).isNotEmpty();
            Assertions.assertThat(v1DeploymentList.getItems().get(0).getMetadata().getName()).isEqualTo(NAME);
        }

        @Test
        void givenUserNameAndNullNamespace_whenListDeployments_thenReturnDeployments() throws ApiException {
            // Given
            Mockito.when(deploymentApi.listDeploymentForAllNamespaces(null, null,
                    null, "userName=" + ANY_USER_NAME, Integer.MAX_VALUE, null, null, null, Boolean.FALSE))
                    .thenReturn(V1DeploymentFactory.createV1DeploymentList());
            // When
            V1DeploymentList v1DeploymentList = kubernetesClientService.listDeployments(null, ANY_USER_NAME);

            // Then
            Assertions.assertThat(v1DeploymentList).isNotNull();
            Assertions.assertThat(v1DeploymentList.getItems()).isNotEmpty();
            Assertions.assertThat(v1DeploymentList.getItems().get(0).getMetadata().getName()).isEqualTo(NAME);
        }

        @Test
        void givenUserNameAndInvalidNamespace_whenListDeployments_thenThrowsApiException() throws ApiException {
            // Given
            Mockito.when(deploymentApi.listNamespacedDeployment(INVALID_NAMESPACE, null, null, null,
                    null, "userName=" + ANY_USER_NAME, null, null, null, Boolean.FALSE))
                    .thenThrow(ApiException.class);
            // When
            ThrowableAssert.ThrowingCallable throwingCallable = () -> kubernetesClientService
                    .listDeployments(INVALID_NAMESPACE, ANY_USER_NAME);

            // Then
            Assertions.assertThatThrownBy(throwingCallable).isInstanceOf(ApiException.class);
        }
    }

    @Nested
    class CreateDeployment {

        @Test
        void givenValidParameters_whenCreateDeployment_thenReturnsDeployment() throws ApiException {
            // Given
            V1Deployment v1Deployment = V1DeploymentFactory.createV1Deployment();
            Mockito.when(deploymentApi.createNamespacedDeployment(ANY_NAMESPACE, v1Deployment, IS_PRETTY.toString(),
                    DRY_RUN, null)).thenReturn(v1Deployment);
            // When
            v1Deployment = kubernetesClientService
                    .createDeployment(ANY_NAMESPACE, v1Deployment, IS_PRETTY, DRY_RUN, ANY_USER_NAME);

            // Then
            Assertions.assertThat(v1Deployment.getMetadata().getName()).isEqualTo(NAME);
        }

        @Test
        void givenInValidParameters_whenCreateDeployment_thenThrowsApiException() throws ApiException {
            // Given
            V1Deployment v1Deployment = V1DeploymentFactory.createV1Deployment();
            Map<String, String> labels = new HashMap<>();
            labels.put(ANY_LABEL, ANY_LABEL);
            v1Deployment.getMetadata().setLabels(labels);
            Mockito.when(deploymentApi.createNamespacedDeployment(INVALID_NAMESPACE, v1Deployment, IS_PRETTY.toString(),
                    DRY_RUN, null)).thenThrow(ApiException.class);
            // When
            ThrowableAssert.ThrowingCallable throwingCallable = () -> kubernetesClientService
                    .createDeployment(INVALID_NAMESPACE, v1Deployment, IS_PRETTY, DRY_RUN, ANY_USER_NAME);

            // Then
            Assertions.assertThatThrownBy(throwingCallable).isInstanceOf(ApiException.class);
        }

    }

    @Nested
    class DeleteDeployment {

        @Test
        void givenNamespaceAndDeploymentNameParameters_whenDeleteDeployment_thenDeletesDeployment() throws ApiException {
            // When
            kubernetesClientService.deleteDeployment(NAME, ANY_NAMESPACE);
            // Then
            Mockito.verify(deploymentApi).deleteNamespacedDeployment(Mockito.eq(NAME), Mockito.eq(ANY_NAMESPACE),
                    Mockito.any(), Mockito.any(), Mockito.eq(0), Mockito.eq(true),
                    Mockito.any(), Mockito.any(V1DeleteOptions.class));

        }

        @Test
        void givenInvalidNamespaceAndDeploymentNameParameters_whenDeleteDeployment_thenDeletesDeployment() throws ApiException {
            //Given
            Mockito.when(deploymentApi.deleteNamespacedDeployment(Mockito.eq(NAME), Mockito.eq(INVALID_NAMESPACE),
                    Mockito.any(), Mockito.any(), Mockito.eq(0), Mockito.eq(true),
                    Mockito.any(), Mockito.any(V1DeleteOptions.class))).thenThrow(ApiException.class);
            // When
            ThrowableAssert.ThrowingCallable throwingCallable = () -> kubernetesClientService
                    .deleteDeployment(NAME, INVALID_NAMESPACE);
            // Then
            Assertions.assertThatThrownBy(throwingCallable).isInstanceOf(ApiException.class);

        }
    }
}
