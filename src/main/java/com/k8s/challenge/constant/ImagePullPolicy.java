package com.k8s.challenge.constant;

public enum ImagePullPolicy {
    IF_NOT_PRESENT("IfNotPresent"), ALWAYS("Always"), NEVER("Never");

    private String value;

    ImagePullPolicy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
