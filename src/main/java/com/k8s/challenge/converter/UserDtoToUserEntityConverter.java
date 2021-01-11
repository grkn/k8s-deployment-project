package com.k8s.challenge.converter;

import com.k8s.challenge.dto.UserDto;
import com.k8s.challenge.entity.UserEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserDtoToUserEntityConverter implements Converter<UserDto, UserEntity> {
    @Override
    public UserEntity convert(UserDto userDto) {
        UserEntity userEntity = new UserEntity();
        userEntity.setName(userDto.getName());
        userEntity.setPassword(userDto.getPassword());
        userEntity.setUserName(userDto.getUserName());
        return userEntity;
    }
}
