package com.k8s.challenge.resource;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResource {

    private String accessToken;
}
