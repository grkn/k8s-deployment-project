package com.k8s.challenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k8s.challenge.Application;
import com.k8s.challenge.constant.ExceptionResponse;
import com.k8s.challenge.constant.ImagePullPolicy;
import com.k8s.challenge.dto.CreateDeploymentDto;
import com.k8s.challenge.entity.DeploymentEntity;
import com.k8s.challenge.entity.UserEntity;
import com.k8s.challenge.exception.handler.ExceptionHandlerAdvice;
import com.k8s.challenge.repository.DeploymentRepository;
import com.k8s.challenge.repository.UserRepository;
import com.k8s.challenge.service.KubernetesClientService;
import com.k8s.challenge.service.UserService;
import io.kubernetes.client.openapi.models.V1Deployment;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.UUID;

@ActiveProfiles("integration-test")
@ContextConfiguration(classes = Application.class)
@TestPropertySource(value = "classpath:application_test.properties")
@ExtendWith(SpringExtension.class)
public class DeploymentControllerIT {

    private static final String DEFAULT_NAMESPACE = "default";
    private static final String ANY_USER_NAME = "anyUserName";
    private static final String ANY_PASSWORD = "anyPassword";
    private static final String ANY_NAME = "anyName";
    private static final String ANY_IMAGE = "anyImage";
    private static final String ANY_DEPLOYMENT_NAME = "anyDeploymentName";
    private static final String OTHER_NAMESPACE = "otherNamespace";
    private static final String OTHER_DEPLOYMENT_NAME = "otherDeploymentName";
    private static final String OTHER_USER_NAME = "otherUserName";
    private static final String NGINX = "nginx";
    private static final String API_VERSION = "apps/v1";
    private static final String DEPLOYMENT = "Deployment";
    private static final String APP_NAME = "app-name";
    private MockMvc mockMvc;

    @Autowired
    private DeploymentController deploymentController;

    @Autowired
    private ExceptionHandlerAdvice exceptionHandlerAdvice;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeploymentRepository deploymentRepository;

    @Autowired
    private KubernetesClientService kubernetesClientService;

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(deploymentController)
                .setControllerAdvice(exceptionHandlerAdvice)
                .build();

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(ANY_USER_NAME, ANY_PASSWORD));
    }

    @AfterEach
    public void destroy() {
        userRepository.deleteAll();
        deploymentRepository.deleteAll();

    }

    @Nested
    class ListDeployments {

        private UserEntity userEntity;

        @BeforeEach
        void init() {
            userEntity = UserEntity.builder()
                    .userName(ANY_USER_NAME)
                    .password(ANY_PASSWORD)
                    .name(ANY_NAME)
                    .build();

            userEntity = userService.createUserEntity(userEntity);

            DeploymentEntity deploymentEntity = deploymentRepository.save(DeploymentEntity.builder()
                    .deploymentName(ANY_DEPLOYMENT_NAME)
                    .namespace(DEFAULT_NAMESPACE)
                    .replicas(2)
                    .image(ANY_IMAGE)
                    .user(userEntity)
                    .build());

            userEntity.setDeployments(Collections.singleton(deploymentEntity));
            userRepository.save(userEntity);
        }

        @Test
        void givenUserNameAndNullNamespace_whenListDeployment_thenListOfDeploymentsAreReturnedFromDatabase() throws Exception {
            // Given
            // When
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/user/{userName}/deployment", ANY_USER_NAME)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    // Then
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].name", IsEqual.equalTo(ANY_DEPLOYMENT_NAME)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].namespace", IsEqual.equalTo(DEFAULT_NAMESPACE)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].image", IsEqual.equalTo(ANY_IMAGE)));

        }

        @Test
        void givenUserNameAndOtherNamespace_whenListDeployment_thenListOfDeploymentsOnOtherNamespaceAreReturnedFromDatabase() throws Exception {
            // Given

            DeploymentEntity deploymentEntity = deploymentRepository.save(DeploymentEntity.builder()
                    .deploymentName(OTHER_DEPLOYMENT_NAME)
                    .namespace(OTHER_NAMESPACE)
                    .replicas(2)
                    .image(ANY_IMAGE)
                    .user(userEntity)
                    .build());

            deploymentRepository.save(deploymentEntity);

            // When
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/user/{userName}/deployment?namespace=" + OTHER_NAMESPACE,
                    ANY_USER_NAME)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    // Then
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].name", IsEqual.equalTo(OTHER_DEPLOYMENT_NAME)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].namespace", IsEqual.equalTo(OTHER_NAMESPACE)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].image", IsEqual.equalTo(ANY_IMAGE)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[1]").doesNotExist());

        }

        @Test
        void givenDifferentUserNameThanJWTTokenUserNameAndNullNamespace_whenListDeployments_thenThrowsAccessDeniedException() throws Exception {
            // Given
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(OTHER_USER_NAME, ANY_PASSWORD));
            // When
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/user/{userName}/deployment", ANY_USER_NAME)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    // Then
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("k8s-1000"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.reasonMessage")
                            .value("The request is unauthorized."));
        }

        @Test
        void givenUserNameAndNullNamespace_whenListDeployments_thenThrowsNotFoundException() throws Exception {
            // Given
            userRepository.deleteAll();
            // When
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/user/{userName}/deployment", ANY_USER_NAME)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    // Then
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ExceptionResponse.NOT_FOUND.getCode()));
        }

        @Test
        void givenUserNameAndNullNamespace_whenListDeployments_thenReturnsAllDeploymentsFromKubernetesApi() throws Exception {
            // Given
            String deploymentName = UUID.randomUUID().toString();
            CreateDeploymentDto createDeploymentDto = CreateDeploymentDto.builder()
                    .namespace(DEFAULT_NAMESPACE)
                    .metaDataName(deploymentName)
                    .appName(APP_NAME)
                    .imageName(NGINX)
                    .containerPorts(Collections.singletonList(80))
                    .apiVersion(API_VERSION)
                    .kind(DEPLOYMENT)
                    .imagePullPolicy(ImagePullPolicy.ALWAYS)
                    .specReplicas(1)
                    .build();

            V1Deployment v1Deployment = conversionService.convert(createDeploymentDto, V1Deployment.class);
            kubernetesClientService.createDeployment(createDeploymentDto.getNamespace(), v1Deployment, null,
                    null, ANY_USER_NAME);

            deploymentRepository.deleteAll();

            // When
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/user/{userName}/deployment", ANY_USER_NAME)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    // Then
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].name", IsEqual.equalTo(deploymentName)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].namespace", IsEqual.equalTo(DEFAULT_NAMESPACE)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].image", IsEqual.equalTo(NGINX)));

            kubernetesClientService.deleteDeployment(deploymentName, DEFAULT_NAMESPACE);
        }

        @Test
        void givenUserNameAndNullNamespace_whenListDeployments_thenReturnsEmptyDeploymentsFromKubernetesApi() throws Exception {
            // Given
            deploymentRepository.deleteAll();

            // When
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/user/{userName}/deployment", ANY_USER_NAME)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    // Then
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0]").doesNotExist());
        }

    }

    @Nested
    class CreateDeployment {

        private UserEntity userEntity;

        @BeforeEach
        void init() {
            userEntity = UserEntity.builder()
                    .userName(ANY_USER_NAME)
                    .password(ANY_PASSWORD)
                    .name(ANY_NAME)
                    .build();

            userEntity = userService.createUserEntity(userEntity);
        }

        @Test
        public void givenUserNameAndCreateDeploymentDto_whenCreateDeployment_thenCreatedDeploymentReturnedSuccessfully() throws Exception {
            // Given
            String deploymentName = UUID.randomUUID().toString();
            CreateDeploymentDto createDeploymentDto = CreateDeploymentDto.builder()
                    .namespace(DEFAULT_NAMESPACE)
                    .metaDataName(deploymentName)
                    .appName(APP_NAME)
                    .imageName(NGINX)
                    .containerPorts(Collections.singletonList(80))
                    .apiVersion(API_VERSION)
                    .kind(DEPLOYMENT)
                    .imagePullPolicy(ImagePullPolicy.ALWAYS)
                    .specReplicas(1)
                    .build();

            // When
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/{userName}/deployment", ANY_USER_NAME)
                    .content(objectMapper.writeValueAsString(createDeploymentDto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    // Then
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.name", IsEqual.equalTo(deploymentName)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.namespace", IsEqual.equalTo(DEFAULT_NAMESPACE)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.image", IsEqual.equalTo(NGINX)));

            kubernetesClientService.deleteDeployment(deploymentName, DEFAULT_NAMESPACE);
        }

        @Test
        public void givenUserNameAndEmptyDeploymentName_whenCreateDeployment_thenBadRequestExceptionIsThrown() throws Exception {
            // Given
            CreateDeploymentDto createDeploymentDto = CreateDeploymentDto.builder()
                    .namespace(DEFAULT_NAMESPACE)
                    //.metaDataName(deploymentName)
                    .appName(APP_NAME)
                    .imageName(NGINX)
                    .containerPorts(Collections.singletonList(80))
                    .apiVersion(API_VERSION)
                    .kind(DEPLOYMENT)
                    .imagePullPolicy(ImagePullPolicy.ALWAYS)
                    .specReplicas(1)
                    .build();

            // When
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/{userName}/deployment", ANY_USER_NAME)
                    .content(objectMapper.writeValueAsString(createDeploymentDto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    // Then
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.reasonMessage").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ExceptionResponse.BAD_REQUEST.getCode()))
                    .andReturn();
            // missing assertion
            Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains("metaDataName can not be null or empty");
        }

        @Test
        public void givenUserNameAndEmptyAppName_whenCreateDeployment_thenBadRequestExceptionIsThrown() throws Exception {
            // Given
            CreateDeploymentDto createDeploymentDto = CreateDeploymentDto.builder()
                    .namespace(DEFAULT_NAMESPACE)
                    .metaDataName(UUID.randomUUID().toString())
                    .imageName(NGINX)
                    .containerPorts(Collections.singletonList(80))
                    .apiVersion(API_VERSION)
                    .kind(DEPLOYMENT)
                    .imagePullPolicy(ImagePullPolicy.ALWAYS)
                    .specReplicas(1)
                    .build();

            // When
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/{userName}/deployment", ANY_USER_NAME)
                    .content(objectMapper.writeValueAsString(createDeploymentDto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    // Then
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.reasonMessage").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ExceptionResponse.BAD_REQUEST.getCode()))
                    .andReturn();
            // missing assertion
            Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains("appName can not be null or empty");
        }

        @Test
        public void givenUserNameAndEmptyImageName_whenCreateDeployment_thenBadRequestExceptionIsThrown() throws Exception {
            // Given
            CreateDeploymentDto createDeploymentDto = CreateDeploymentDto.builder()
                    .namespace(DEFAULT_NAMESPACE)
                    .metaDataName(UUID.randomUUID().toString())
                    .appName(APP_NAME)
                    .containerPorts(Collections.singletonList(80))
                    .apiVersion(API_VERSION)
                    .kind(DEPLOYMENT)
                    .imagePullPolicy(ImagePullPolicy.ALWAYS)
                    .specReplicas(1)
                    .build();

            // When
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/{userName}/deployment", ANY_USER_NAME)
                    .content(objectMapper.writeValueAsString(createDeploymentDto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    // Then
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.reasonMessage").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ExceptionResponse.BAD_REQUEST.getCode()))
                    .andReturn();
            // missing assertion
            Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains("imageName can not be null or empty");
        }
    }
}
