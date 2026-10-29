package com.fitnessapp.config.springSecurityConfig.jwt.jwtService;

import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.appAuth.model.entity.MobileAccountStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String uid) throws UsernameNotFoundException {
        return userRepo.findById(uid)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getId(),
                        "", // password is not used
                        user.getAccountStatus() == MobileAccountStatus.ACTIVE,
                        true,
                        true,
                        true,
                        Collections.emptyList()
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
