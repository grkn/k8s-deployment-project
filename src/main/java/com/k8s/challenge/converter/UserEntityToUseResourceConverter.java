package com.k8s.challenge.converter;

import com.k8s.challenge.entity.UserEntity;
import com.k8s.challenge.resource.UserResource;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserEntityToUseResourceConverter implements Converter<UserEntity, UserResource> {

    @Override
    public UserResource convert(UserEntity userEntity) {
        UserResource userResource = new UserResource();
        userResource.setName(userEntity.getName());
        userResource.setUserName(userEntity.getUserName());
        return userResource;
    }
}
