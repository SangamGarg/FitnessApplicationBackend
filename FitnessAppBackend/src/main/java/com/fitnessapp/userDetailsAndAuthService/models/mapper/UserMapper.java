package com.fitnessapp.userDetailsAndAuthService.models.mapper;

import com.fitnessapp.userDetailsAndAuthService.models.dtos.UserDetailsDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.responseDtos.UserGetDetailsResponseDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos.UserLoginOrRegisterRequestDto;
import com.fitnessapp.userDetailsAndAuthService.models.dtos.responseDtos.UserLoginResponseDto;
import com.fitnessapp.userDetailsAndAuthService.models.entitites.UserDetailsEntity;
import com.fitnessapp.userDetailsAndAuthService.models.entitites.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    //    @Mapping(target = "id", ignore = true) // you’ll set this after Firebase verification
//    @Mapping(target = "email", ignore = true) // set manually later
//    @Mapping(target = "detailsEntityList", ignore = true)
    // if not used now
    UserEntity dtoToUserEntity(UserLoginOrRegisterRequestDto requestDto);


    UserDetailsEntity userDataDtoTouserDetailsEntity(UserDetailsDto userDetailsDto);


    // Maps UserDetailsEntity to UserDetailsDto
    UserDetailsDto userDetailsEntityToUserDataDto(UserDetailsEntity userDetailsEntity);

    // Maps full UserEntity to LoginResponseDto, except jwtToken
//    @Mapping(source = "userDetailsEntity", target = "userDetailsDto")
//    @Mapping(target = "jwtToken", ignore = true) // To be set manually
//    @Mapping(target = "status", ignore = true)   // To be set manually
//    @Mapping(target = "statusCode", ignore = true)
    UserLoginResponseDto userEntityToLoginResponse(UserEntity userEntity);
//
//    @Mapping(source = "userDetailsEntity", target = "userDetailsDto")
//    @Mapping(target = "status", ignore = true)   // To be set manually
//    @Mapping(target = "statusCode", ignore = true)
    UserGetDetailsResponseDto userEntityToUserDetailsResponseDto(UserEntity userEntity);
}
