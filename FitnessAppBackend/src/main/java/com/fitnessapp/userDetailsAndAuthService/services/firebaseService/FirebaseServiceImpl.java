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

//    @Override
//    public FirebaseToken verifyFirebaseIdToken(String firebaseIdToken) {
//        //It returns the decoded token...from this we can get user details in firebase like uid, email, etc.
//        FirebaseToken decodedToken = null;
//        try {
//            decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseIdToken);
//        } catch (FirebaseAuthException e) {
//            throw new RuntimeException(e);
//        }
//        return decodedToken;
//    }
}
