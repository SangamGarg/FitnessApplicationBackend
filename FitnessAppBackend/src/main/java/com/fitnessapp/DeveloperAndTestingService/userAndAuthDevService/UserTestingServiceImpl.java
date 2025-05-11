package com.fitnessapp.DeveloperAndTestingService.userAndAuthDevService;

import com.fitnessapp.userDetailsAndAuthService.services.jwtService.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserTestingServiceImpl implements UserTestingService {

    private final UserTestingRepository userTestingRepository;

    public UserTestingServiceImpl(UserTestingRepository userTestingRepository) {
        this.userTestingRepository = userTestingRepository;
    }

    @Transactional
    @Override
    public ResponseEntity<?> saveUserForDevAndTest(String name) {
        try {
            String randomStr = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
            String key = name.concat(randomStr);
            String hashedKey = new BCryptPasswordEncoder(10).encode(key);
            UserTestingEntity user = userTestingRepository.findByNameIgnoreCase(name);
            if (user != null) {
                return ResponseEntity.badRequest().body("User with name : " + name + " already exist");
            }
            userTestingRepository.save(UserTestingEntity.builder().name(name).key(hashedKey).build());

            return ResponseEntity.ok("User For Dev And Testing Registered with name : " + name + "\nKey : " + key);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> deleteUserForDevAndTest(String name) {
        try {

            UserTestingEntity user = userTestingRepository.findByNameIgnoreCase(name);
            if (user == null) {
                return ResponseEntity.badRequest().body("User Not Found With Given Name");
            }
            userTestingRepository.delete(user);
            return ResponseEntity.ok("User For Dev And Testing Deleted with name : " + name);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @Override
    public List<UserTestingEntity> getUsersForDevAndTest() {
        try {
            return userTestingRepository.findAll();
        } catch (Exception e) {
            return null;
        }
    }
}
