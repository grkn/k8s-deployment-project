package com.k8s.challenge.resource;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DeploymentResource {
    private String apiVersion;
    private String kind;
    @JsonFormat(pattern = "dd/MM/yyyy hh:mm:ss")
    private LocalDateTime creationTimestamp;
    private String name;
    private String namespace;
    private String image;
    private Integer replicas;
}
