package com.fitnessapp.userDetailsAndAuthService.models.mapper;

import com.fitnessapp.userDetailsAndAuthService.models.dtos.UserDetailsDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos.UserLoginOrRegisterRequestDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.responseDtos.UserGetDetailsResponseDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.responseDtos.UserLoginResponseDto;
import com.fitnessapp.userDetailsAndAuthService.models.entitites.UserDetailsEntity;
import com.fitnessapp.userDetailsAndAuthService.models.entitites.UserEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-05T14:53:07+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserEntity dtoToUserEntity(UserLoginOrRegisterRequestDto requestDto) {
        if ( requestDto == null ) {
            return null;
        }

        UserEntity userEntity = new UserEntity();

        return userEntity;
    }

    @Override
    public UserDetailsEntity userDataDtoTouserDetailsEntity(UserDetailsDto userDetailsDto) {
        if ( userDetailsDto == null ) {
            return null;
        }

        UserDetailsEntity userDetailsEntity = new UserDetailsEntity();

        return userDetailsEntity;
    }

    @Override
    public UserDetailsDto userDetailsEntityToUserDataDto(UserDetailsEntity userDetailsEntity) {
        if ( userDetailsEntity == null ) {
            return null;
        }

        UserDetailsDto userDetailsDto = new UserDetailsDto();

        return userDetailsDto;
    }

    @Override
    public UserLoginResponseDto userEntityToLoginResponse(UserEntity userEntity) {
        if ( userEntity == null ) {
            return null;
        }

        UserLoginResponseDto userLoginResponseDto = new UserLoginResponseDto();

        return userLoginResponseDto;
    }

    @Override
    public UserGetDetailsResponseDto userEntityToUserDetailsResponseDto(UserEntity userEntity) {
        if ( userEntity == null ) {
            return null;
        }

        UserGetDetailsResponseDto userGetDetailsResponseDto = new UserGetDetailsResponseDto();

        return userGetDetailsResponseDto;
    }
}
