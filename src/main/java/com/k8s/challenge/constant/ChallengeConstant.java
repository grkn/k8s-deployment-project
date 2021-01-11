package com.k8s.challenge.constant;

public final class ChallengeConstant {


    public static final String ROLE_USER = "ROLE_USER";
    public static final String DUMMY_SIGN = "anySign";
    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final long EXPIRE_TIME = 1000 * 60 * 60 * 2L;
    public static final String AUTHORIZE_ENDPOINT = "/api/v1/authorize";
    public static final String TOKEN_ENDPOINT = "/api/v1/token";

    private ChallengeConstant() {
    }
}
