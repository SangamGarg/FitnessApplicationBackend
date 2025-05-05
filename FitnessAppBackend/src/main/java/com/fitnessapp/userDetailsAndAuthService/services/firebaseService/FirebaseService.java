package com.fitnessapp.userDetailsAndAuthService.services.firebaseService;

import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.ResponseEntity;

public interface FirebaseService {
    ResponseEntity<?> verifyFirebaseIdToken(String firebaseIdToken);
}
