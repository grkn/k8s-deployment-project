package com.k8s.challenge.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class DeploymentEntity extends BaseEntity<String> {

    private String image;
    private String deploymentName;
    private String appName;
    private String namespace;
    private Integer replicas;
    private String apiVersion;
    private String kind;
    private LocalDateTime creationTimestamp;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

}
