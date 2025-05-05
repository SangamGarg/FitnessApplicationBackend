package com.fitnessapp.userDetailsAndAuthService.services.firebaseService;

import com.fitnessapp.userDetailsAndAuthService.models.dtos.ErrorDto;
import com.fitnessapp.userDetailsAndAuthService.userDetailsAndAuthServiceUtilities.AppConstantsUserAndAuthService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class FirebaseServiceImpl implements FirebaseService {

    @Override
    public ResponseEntity<?> verifyFirebaseIdToken(String firebaseIdToken) {
        try {
            //It returns the decoded token...from this we can get user details in firebase like uid, email, etc.
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseIdToken);
            return ResponseEntity.ok(decodedToken);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ErrorDto.builder()
                            .status(AppConstantsUserAndAuthService.ERROR_API_STATUS)
                            .errorMessage("Invalid Firebase token")
                            .statusCode(401)
                            .build()
            );
        }
    }
}
