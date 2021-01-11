package com.k8s.challenge.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.credentials.ClientCertificateAuthentication;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Configuration
public class ChallengeConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChallengeConfig.class);

    @Bean
    public ConversionService conversionService(List<Converter> converters) {
        GenericConversionService conversionService = new GenericConversionService();
        converters.stream().forEach(conversionService::addConverter);
        return conversionService;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    @Configuration
    @Profile("!integration-test")
    @ConfigurationProperties(prefix = "k8s")
    public static class K8sClientConfig {

        private String path;
        private String clientCrt;
        private String clientKey;
        private String caCrt;

        @Bean
        public ApiClient apiClient() throws IOException {
            ClientCertificateAuthentication clientCertificateAuthentication;
            LOGGER.info("BasePath is {}, Client Cert Path is {}, Client Key Path is {}, Ca Cert Path is {}",
                    path, clientCrt, clientKey, caCrt);
            if (StringUtils.isEmpty(clientCrt) || StringUtils.isEmpty(clientKey)) {
                clientCertificateAuthentication = new ClientCertificateAuthentication(readFileAsByte("client.crt"),
                        readFileAsByte("client.key"));
            } else {
                clientCertificateAuthentication = new ClientCertificateAuthentication(readFileAsByte(new File(clientCrt)),
                        readFileAsByte(new File(clientKey)));
            }

            ClientBuilder clientBuilder = ClientBuilder
                    .standard()
                    .setBasePath(path)
                    .setAuthentication(clientCertificateAuthentication)
                    .setVerifyingSsl(true);
            if (!StringUtils.isEmpty(caCrt)) {
                clientBuilder.setCertificateAuthority(readFileAsByte(new File(caCrt)));
            }
            ApiClient apiClient = clientBuilder.build();
            apiClient.setHttpClient(
                    apiClient.getHttpClient().newBuilder().readTimeout(Duration.ZERO).build());
            io.kubernetes.client.openapi.Configuration.setDefaultApiClient(apiClient);
            return apiClient;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public void setClientCrt(String clientCrt) {
            this.clientCrt = clientCrt;
        }

        public void setClientKey(String clientKey) {
            this.clientKey = clientKey;
        }

        public void setCaCrt(String caCrt) {
            this.caCrt = caCrt;
        }
    }

    @Configuration
    @Profile("integration-test")
    @ConfigurationProperties(prefix = "k8s")
    public static class K8sClientTestConfig {

        private String path;
        private String clientCrt;
        private String clientKey;

        @Bean
        public ApiClient apiClient() throws IOException {
            setCertificatesAndPath();
            ClientCertificateAuthentication clientCertificateAuthentication;
            LOGGER.info("BasePath is {}, Client Cert Path is {}, Client Key Path is {}", path, clientCrt, clientKey);
            if (StringUtils.isEmpty(clientCrt) || StringUtils.isEmpty(clientKey)) {
                clientCertificateAuthentication = new  ClientCertificateAuthentication(readFileAsByte("client_test.crt"),
                        readFileAsByte("client_test.key"));
            } else {
                clientCertificateAuthentication = new ClientCertificateAuthentication(readFileAsByte(new File(clientCrt)),
                        readFileAsByte(new File(clientKey)));
            }

            ApiClient apiClient = ClientBuilder
                    .standard()
                    .setBasePath(path)
                    .setAuthentication(clientCertificateAuthentication)
                    .setVerifyingSsl(true)
                    .build();
            apiClient.setHttpClient(
                    apiClient.getHttpClient().newBuilder().readTimeout(Duration.ZERO).build());
            io.kubernetes.client.openapi.Configuration.setDefaultApiClient(apiClient);
            return apiClient;
        }

        private void setCertificatesAndPath() {
            if (System.getProperty("K8S_PATH") != null) {
                path = System.getProperty("K8S_PATH");
            }
            if (System.getProperty("K8S_CLIENTCRT") != null) {
                clientCrt = System.getProperty("K8S_CLIENTCRT");
            }
            if (System.getProperty("K8S_CLIENTKEY") != null) {
                clientKey = System.getProperty("K8S_CLIENTKEY");
            }
        }

        public void setPath(String path) {
            this.path = path;
        }

        public void setClientCrt(String clientCrt) {
            this.clientCrt = clientCrt;
        }

        public void setClientKey(String clientKey) {
            this.clientKey = clientKey;
        }
    }

    @Bean(name = "deploymentApi")
    public AppsV1Api appsV1Api(ApiClient apiClient) {
        AppsV1Api appsV1Api = new AppsV1Api();
        appsV1Api.setApiClient(apiClient);
        return appsV1Api;
    }

    private static byte[] readFileAsByte(String fileName) throws IOException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        byte[] arr = new byte[1024 * 5];
        int length = inputStream.read(arr);
        return Arrays.copyOfRange(arr, 0, length);
    }

    private static byte[] readFileAsByte(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        byte[] arr = new byte[1024 * 5];
        int length = inputStream.read(arr);
        return Arrays.copyOfRange(arr, 0, length);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
