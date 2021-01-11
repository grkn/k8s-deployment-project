package com.k8s.challenge.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.k8s.challenge.constant.ImagePullPolicy;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateDeploymentDto {

    @NotBlank
    private String apiVersion;
    @NotBlank
    private String kind;
    @NotBlank
    private String metaDataName;
    @JsonProperty("replicas")
    @NotNull
    @Min(value = 1)
    private Integer specReplicas;
    @NotBlank
    private String appName;

    @NotBlank
    @JsonProperty("image")
    private String imageName;
    private ImagePullPolicy imagePullPolicy;

    @NotEmpty
    @Valid
    private List<@Min(value = 1) @Max(value = 65535) Integer> containerPorts;
    private Boolean pretty;
    @NotBlank
    private String namespace;
    private String dryRun;

}
