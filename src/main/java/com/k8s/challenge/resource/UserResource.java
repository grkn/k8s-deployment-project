package com.k8s.challenge.resource;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResource {
    private String userName;
    private String name;
}
