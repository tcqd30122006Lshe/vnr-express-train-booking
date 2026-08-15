package com.trainbooking.mapper;

import com.trainbooking.dto.request.UserCreationRequest;
import com.trainbooking.dto.response.UserResponse;
import com.trainbooking.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);
}