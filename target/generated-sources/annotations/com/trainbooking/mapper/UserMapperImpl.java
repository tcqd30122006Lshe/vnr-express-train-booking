package com.trainbooking.mapper;

import com.trainbooking.dto.request.UserCreationRequest;
import com.trainbooking.dto.response.UserResponse;
import com.trainbooking.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-15T16:18:24+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toUser(UserCreationRequest request) {
        if ( request == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.address( request.getAddress() );
        user.companyName( request.getCompanyName() );
        user.dob( request.getDob() );
        user.email( request.getEmail() );
        user.fullName( request.getFullName() );
        user.gender( request.getGender() );
        user.idCard( request.getIdCard() );
        user.passengerType( request.getPassengerType() );
        user.password( request.getPassword() );
        user.phone( request.getPhone() );
        user.taxCode( request.getTaxCode() );
        user.username( request.getUsername() );

        return user.build();
    }

    @Override
    public UserResponse toUserResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.address( user.getAddress() );
        userResponse.companyName( user.getCompanyName() );
        userResponse.dob( user.getDob() );
        userResponse.email( user.getEmail() );
        userResponse.fullName( user.getFullName() );
        userResponse.gender( user.getGender() );
        userResponse.idCard( user.getIdCard() );
        userResponse.passengerType( user.getPassengerType() );
        userResponse.phone( user.getPhone() );
        userResponse.taxCode( user.getTaxCode() );
        userResponse.username( user.getUsername() );

        return userResponse.build();
    }
}
