package com.fitnessapp.userDetailsAndAuthService.services.firebaseService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;

@Service
public class FirebaseServiceImpl implements FirebaseService {

    @Override
    public FirebaseToken verifyFirebaseIdToken(String firebaseIdToken) {
        try {
            //It returns the decoded token...from this we can get user details in firebase like uid, email, etc.
            return FirebaseAuth.getInstance().verifyIdToken(firebaseIdToken);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e.getAuthErrorCode() + " Firebase Id Token Verification Failed- Reason :  " + e.getLocalizedMessage());
        }
    }
}
