package com.k8s.challenge.entity;

import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class UserEntity extends BaseEntity<String> {

    private String name;
    private String userName;
    private String password;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<DeploymentEntity> deployments;

    @ManyToMany(mappedBy = "users", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<AuthorizationEntity> authorizations;

}
