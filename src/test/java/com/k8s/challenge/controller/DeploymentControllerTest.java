package com.k8s.challenge.controller;

import com.k8s.challenge.dto.CreateDeploymentDto;
import com.k8s.challenge.resource.DeploymentResource;
import com.k8s.challenge.service.DeploymentService;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentBuilder;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

public class DeploymentControllerTest {

    private static final Integer ANY_PORT = 80;
    private static final String ANY_NAMESPACE = "anyNamespace";
    private static final String ANY_USER_NAME = "anyUserName";
    private static final String OTHER_USER_NAME = "otherUserName";
    private static final String ANY_NAME = "anyName";
    private static final String ANY_PASSWORD = "anyPassword";
    private static final String ANY_IMAGE = "anyImage";

    private DeploymentController deploymentController;

    @Mock
    private DeploymentService deploymentService;

    @Mock
    private ConversionService conversionService;


    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        deploymentController = new DeploymentController(deploymentService, conversionService);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(ANY_USER_NAME, ANY_PASSWORD));
    }

    @Nested
    class ListDeployments {

        @Test
        void givenAnyNamespaceAndUserName_whenListDeployments_thenReturnAllDeploymentList() throws ApiException {
            // Given
            DeploymentResource deploymentResource = DeploymentResource.builder()
                    .name(ANY_NAME)
                    .namespace(ANY_NAMESPACE)
                    .build();

            Mockito.when(deploymentService.listDeployments(ANY_USER_NAME, ANY_NAMESPACE))
                    .thenReturn(Collections.singletonList(deploymentResource));
            // When
            ResponseEntity<List<DeploymentResource>> deploymentResourcesEntity = deploymentController
                    .listDeployments(ANY_NAMESPACE, ANY_USER_NAME);

            // Then
            Assertions.assertThat(deploymentResourcesEntity.getBody()).isNotNull();
            List<DeploymentResource> deploymentResources = deploymentResourcesEntity.getBody();
            for (DeploymentResource deploymentResource1 : deploymentResources) {
                Assertions.assertThat(deploymentResource1.getNamespace()).isEqualTo(ANY_NAMESPACE);
            }
        }

        @Test
        void givenAnyNamespaceAndUserName_whenListDeployments_thenReturnEmptyDeploymentList() throws ApiException {
            // Given

            Mockito.when(deploymentService.listDeployments(ANY_USER_NAME, ANY_NAMESPACE)).thenReturn(Collections.emptyList());
            // When
            ResponseEntity<List<DeploymentResource>> deploymentResourcesEntity = deploymentController
                    .listDeployments(ANY_NAMESPACE, ANY_USER_NAME);
            // Then
            Assertions.assertThat(deploymentResourcesEntity.getBody()).isEmpty();
        }

        @Test
        void givenAnyNamespaceAndUserName_whenListDeployments_thenThrowsApiException() throws ApiException {
            // Given
            Mockito.when(deploymentService.listDeployments(ANY_USER_NAME, ANY_NAMESPACE)).thenThrow(new ApiException());
            // When
            ThrowableAssert.ThrowingCallable throwingCallable = () -> deploymentController
                    .listDeployments(ANY_NAMESPACE, ANY_USER_NAME);
            // Then
            Assertions.assertThatThrownBy(throwingCallable).isInstanceOf(ApiException.class);
        }

        @Test
        void givenAnyNamespaceAndUserName_whenListDeployments_thenThrowsAccessDeniedException() {
            // Given
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(OTHER_USER_NAME, ANY_PASSWORD));
            // When
            ThrowableAssert.ThrowingCallable throwingCallable = () -> deploymentController
                    .listDeployments(ANY_NAMESPACE, ANY_USER_NAME);
            // Then
            Assertions.assertThatThrownBy(throwingCallable).isInstanceOf(AccessDeniedException.class);
        }
    }


    @Nested
    class CreateDeployment {

        @Test
        void givenUserNameAndDeploymentDto_whenCreateDeployment_thenCreatesDeploymentInKubernetesEnvironment() throws ApiException {
            // Given
            CreateDeploymentDto createDeploymentDto = CreateDeploymentDto.builder()
                    .appName(ANY_NAME)
                    .containerPorts(Collections.singletonList(ANY_PORT))
                    .metaDataName(ANY_NAME)
                    .imageName(ANY_IMAGE)
                    .namespace(ANY_NAMESPACE)
                    .build();

            V1Deployment v1Deployment = new V1Deployment();

            DeploymentResource deploymentResource = DeploymentResource.builder()
                    .name(ANY_NAME)
                    .namespace(ANY_NAMESPACE)
                    .image(ANY_IMAGE)
                    .build();

            Mockito.when(conversionService.convert(createDeploymentDto, V1Deployment.class)).thenReturn(v1Deployment);
            Mockito.when(deploymentService.createDeployment(ANY_USER_NAME, createDeploymentDto.getNamespace(), v1Deployment,
                    createDeploymentDto.getPretty(), createDeploymentDto.getDryRun())).thenReturn(deploymentResource);
            // When
            ResponseEntity<DeploymentResource> deploymentResourceResponseEntity =
                    deploymentController.createDeployment(createDeploymentDto, ANY_USER_NAME);

            // Then
            Assertions.assertThat(deploymentResourceResponseEntity.getBody()).isNotNull();
            Assertions.assertThat(deploymentResourceResponseEntity.getBody().getNamespace()).isEqualTo(ANY_NAMESPACE);
            Assertions.assertThat(deploymentResourceResponseEntity.getBody().getName()).isEqualTo(ANY_NAME);
            Assertions.assertThat(deploymentResourceResponseEntity.getBody().getImage()).isEqualTo(ANY_IMAGE);
        }

        @Test
        void givenUserNameAndDeploymentDto_whenCreateDeployment_thenThrowsApiException() throws ApiException {
            // Given
            CreateDeploymentDto createDeploymentDto = CreateDeploymentDto.builder()
                    .appName(ANY_NAME)
                    .containerPorts(Collections.singletonList(ANY_PORT))
                    .metaDataName(ANY_NAME)
                    .imageName(ANY_IMAGE)
                    .namespace(ANY_NAMESPACE)
                    .build();

            V1Deployment v1Deployment = new V1Deployment();

            Mockito.when(conversionService.convert(createDeploymentDto, V1Deployment.class)).thenReturn(v1Deployment);
            Mockito.when(deploymentService.createDeployment(ANY_USER_NAME, createDeploymentDto.getNamespace(), v1Deployment,
                    createDeploymentDto.getPretty(), createDeploymentDto.getDryRun())).thenThrow(ApiException.class);
            // When
            ThrowableAssert.ThrowingCallable throwingCallable = () ->
                    deploymentController.createDeployment(createDeploymentDto, ANY_USER_NAME);

            // Then
            Assertions.assertThatThrownBy(throwingCallable).isInstanceOf(ApiException.class);
        }

        @Test
        void givenOtherUserNameAndDeploymentDto_whenCreateDeployment_thenThrowsApiException() {
            // Given
            CreateDeploymentDto createDeploymentDto = CreateDeploymentDto.builder()
                    .appName(ANY_NAME)
                    .containerPorts(Collections.singletonList(ANY_PORT))
                    .metaDataName(ANY_NAME)
                    .imageName(ANY_IMAGE)
                    .namespace(ANY_NAMESPACE)
                    .build();

            // When
            ThrowableAssert.ThrowingCallable throwingCallable = () ->
                    deploymentController.createDeployment(createDeploymentDto, OTHER_USER_NAME);

            // Then
            Assertions.assertThatThrownBy(throwingCallable).isInstanceOf(AccessDeniedException.class);
        }
    }
}
