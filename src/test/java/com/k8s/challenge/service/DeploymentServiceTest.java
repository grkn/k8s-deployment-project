package com.k8s.challenge.service;

import com.k8s.challenge.entity.DeploymentEntity;
import com.k8s.challenge.entity.UserEntity;
import com.k8s.challenge.exception.NotFoundException;
import com.k8s.challenge.objectfactory.V1DeploymentFactory;
import com.k8s.challenge.repository.DeploymentRepository;
import com.k8s.challenge.resource.DeploymentResource;
import io.kubernetes.client.openapi.ApiException;
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
import org.springframework.core.convert.ConversionService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class DeploymentServiceTest {

    private static final String DEFAULT_NAMESPACE = "default";
    private static final String ANY_USER_NAME = "anyUserName";
    private static final String ANY_NAMESPACE = "anyNamespace";
    private static final String ANY_NAME = "anyName";
    private static final String ANY_PASSWORD = "anyPassword";
    private static final String ANY_DEPLOYMENT_NAME = "anyDeploymentName";
    private static final String ANY_IMAGE = "anyImage";
    private static final String INVALID_USER_NAME = "invalidUserName";
    private static final Boolean IS_PRETTY = true;
    private static final String ANY_DRY_RUN = "anyDryRun";

    private DeploymentService deploymentService;

    @Mock
    private UserService userService;

    @Mock
    private KubernetesClientService kubernetesClientService;

    @Mock
    private DeploymentRepository deploymentRepository;

    @Mock
    private ConversionService conversionService;

    private UserEntity userEntity;
    private DeploymentEntity deploymentEntity;
    private DeploymentResource deploymentResource;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        deploymentService = new DeploymentService(userService, kubernetesClientService, deploymentRepository, conversionService);

        userEntity = UserEntity.builder()
                .name(ANY_NAME)
                .password(ANY_PASSWORD)
                .userName(ANY_USER_NAME)
                .build();

        deploymentEntity = DeploymentEntity.builder()
                .deploymentName(ANY_DEPLOYMENT_NAME)
                .namespace(DEFAULT_NAMESPACE)
                .replicas(2)
                .image(ANY_IMAGE)
                .user(userEntity)
                .build();

        deploymentResource = DeploymentResource.builder()
                .name(ANY_NAME)
                .namespace(ANY_NAMESPACE)
                .image(ANY_IMAGE)
                .replicas(2)
                .creationTimestamp(LocalDateTime.now())
                .build();
    }

    @Nested
    class ListDeployments {

        @Test
        void givenUserNameAndNamespace_whenListDeployments_thenReturnsListOfDeploymentResourcesFromDatabase() throws ApiException {
            // Given
            Mockito.when(userService.findUserByName(ANY_USER_NAME)).thenReturn(userEntity);
            Mockito.when(deploymentRepository.findAllByNamespaceAndUserUserNameOrderByCreationTimestampDesc(ANY_NAMESPACE, ANY_USER_NAME))
                    .thenReturn(Collections.singletonList(deploymentEntity));
            Mockito.when(conversionService.convert(deploymentEntity, DeploymentResource.class)).thenReturn(deploymentResource);

            // When
            List<DeploymentResource> deploymentResources = deploymentService.listDeployments(ANY_USER_NAME, ANY_NAMESPACE);

            // Then
            Assertions.assertThat(deploymentResources).isNotEmpty();
            Assertions.assertThat(deploymentResources.get(0).getName()).isEqualTo(ANY_NAME);
            Assertions.assertThat(deploymentResources.get(0).getImage()).isEqualTo(ANY_IMAGE);
            Assertions.assertThat(deploymentResources.get(0).getNamespace()).isEqualTo(ANY_NAMESPACE);

        }

        @Test
        void givenUserNameAndNullNamespace_whenListDeployments_thenReturnsListOfDeploymentResourcesFromDatabase() throws ApiException {
            // Given
            Mockito.when(userService.findUserByName(ANY_USER_NAME)).thenReturn(userEntity);
            Mockito.when(deploymentRepository.findAllByUserUserNameOrderByCreationTimestampDesc(ANY_USER_NAME))
                    .thenReturn(Collections.singletonList(deploymentEntity));
            Mockito.when(conversionService.convert(deploymentEntity, DeploymentResource.class)).thenReturn(deploymentResource);

            // When
            List<DeploymentResource> deploymentResources = deploymentService.listDeployments(ANY_USER_NAME, null);

            // Then
            Assertions.assertThat(deploymentResources).isNotEmpty();
            Assertions.assertThat(deploymentResources.get(0).getName()).isEqualTo(ANY_NAME);
            Assertions.assertThat(deploymentResources.get(0).getImage()).isEqualTo(ANY_IMAGE);
            Assertions.assertThat(deploymentResources.get(0).getNamespace()).isEqualTo(ANY_NAMESPACE);

        }

        @Test
        void givenInvalidUserNameAndNullNamespace_whenListDeployments_thenThrowNotFoundException() {
            // Given
            Mockito.when(userService.findUserByName(INVALID_USER_NAME)).thenThrow(NotFoundException.class);

            // When
            ThrowableAssert.ThrowingCallable throwingCallable = () -> deploymentService.listDeployments(INVALID_USER_NAME, null);

            // Then
            Assertions.assertThatThrownBy(throwingCallable).isInstanceOf(NotFoundException.class);
        }

        @Test
        void givenUserNameAndNullNamespace_whenListDeployments_thenReturnsListOfDeploymentResourcesFromKubernetesApi() throws ApiException {
            // Given
            V1DeploymentList v1DeploymentList = V1DeploymentFactory.createV1DeploymentList();
            Mockito.when(userService.findUserByName(ANY_USER_NAME)).thenReturn(userEntity);
            Mockito.when(deploymentRepository.findAllByUserUserNameOrderByCreationTimestampDesc(ANY_USER_NAME))
                    .thenReturn(Collections.emptyList());
            Mockito.when(kubernetesClientService.listDeployments(null, ANY_USER_NAME))
                    .thenReturn(v1DeploymentList);
            Mockito.when(conversionService.convert(v1DeploymentList.getItems().get(0), DeploymentResource.class))
                    .thenReturn(deploymentResource);
            Mockito.when(conversionService.convert(deploymentResource, DeploymentEntity.class)).thenReturn(deploymentEntity);
            // When
            List<DeploymentResource> deploymentResources = deploymentService.listDeployments(ANY_USER_NAME, null);

            // Then
            Assertions.assertThat(deploymentResources).isNotEmpty();
            Assertions.assertThat(deploymentResources.get(0).getName()).isEqualTo(ANY_NAME);
            Assertions.assertThat(deploymentResources.get(0).getImage()).isEqualTo(ANY_IMAGE);
            Assertions.assertThat(deploymentResources.get(0).getNamespace()).isEqualTo(ANY_NAMESPACE);

        }

        @Test
        void givenUserNameAndNullNamespace_whenListDeployments_thenReturnsEmptyDeploymentResourcesFromKubernetesApi() throws ApiException {
            // Given
            Mockito.when(userService.findUserByName(ANY_USER_NAME)).thenReturn(userEntity);
            Mockito.when(deploymentRepository.findAllByUserUserNameOrderByCreationTimestampDesc(ANY_USER_NAME))
                    .thenReturn(Collections.emptyList());
            Mockito.when(kubernetesClientService.listDeployments(null, ANY_USER_NAME))
                    .thenReturn(new V1DeploymentList());
            // When
            List<DeploymentResource> deploymentResources = deploymentService.listDeployments(ANY_USER_NAME, null);

            // Then
            Assertions.assertThat(deploymentResources).isEmpty();
        }
    }

    @Nested
    class CreateDeployment {

        @Test
        void givenAllValidParameters_whenCreateDeployment_thenReturnedDeploymentResource() throws ApiException {
            // Given
            Mockito.when(userService.findUserByName(ANY_USER_NAME)).thenReturn(userEntity);
            V1Deployment v1Deployment = V1DeploymentFactory.createV1Deployment();
            Mockito.when(kubernetesClientService.createDeployment(ANY_NAMESPACE, v1Deployment, IS_PRETTY, ANY_DRY_RUN,
                    ANY_USER_NAME)).thenReturn(v1Deployment);
            Mockito.when(conversionService.convert(v1Deployment, DeploymentResource.class)).thenReturn(deploymentResource);
            Mockito.when(conversionService.convert(deploymentResource, DeploymentEntity.class)).thenReturn(deploymentEntity);
            // When
            DeploymentResource deploymentResource = deploymentService
                    .createDeployment(ANY_USER_NAME, ANY_NAMESPACE, v1Deployment, IS_PRETTY, ANY_DRY_RUN);

            // Then
            Assertions.assertThat(deploymentResource).isNotNull();
            Assertions.assertThat(deploymentResource.getNamespace()).isEqualTo(ANY_NAMESPACE);
            Assertions.assertThat(deploymentResource.getImage()).isEqualTo(ANY_IMAGE);
            Assertions.assertThat(deploymentResource.getName()).isEqualTo(ANY_NAME);
        }

        @Test
        void givenMissingUserNameParameter_whenCreateDeployment_thenThrowsNotFoundException() {
            // Given
            Mockito.when(userService.findUserByName(ANY_USER_NAME)).thenThrow(NotFoundException.class);
            V1Deployment v1Deployment = V1DeploymentFactory.createV1Deployment();
            // When
            ThrowableAssert.ThrowingCallable throwingCallable = () -> deploymentService
                    .createDeployment(ANY_USER_NAME, ANY_NAMESPACE, v1Deployment, IS_PRETTY, ANY_DRY_RUN);

            // Then
            Assertions.assertThatThrownBy(throwingCallable).isInstanceOf(NotFoundException.class);
        }

        @Test
        void givenAllValidParameters_whenCreateDeployment_thenThrowsApiException() throws ApiException {
            // Given
            Mockito.when(userService.findUserByName(ANY_USER_NAME)).thenReturn(userEntity);
            V1Deployment v1Deployment = V1DeploymentFactory.createV1Deployment();
            Mockito.when(kubernetesClientService.createDeployment(ANY_NAMESPACE, v1Deployment, IS_PRETTY, ANY_DRY_RUN,
                    ANY_USER_NAME)).thenThrow(ApiException.class);
            // When
            ThrowableAssert.ThrowingCallable throwingCallable = () -> deploymentService
                    .createDeployment(ANY_USER_NAME, ANY_NAMESPACE, v1Deployment, IS_PRETTY, ANY_DRY_RUN);

            // Then
            Assertions.assertThatThrownBy(throwingCallable).isInstanceOf(ApiException.class);

        }
    }
}
