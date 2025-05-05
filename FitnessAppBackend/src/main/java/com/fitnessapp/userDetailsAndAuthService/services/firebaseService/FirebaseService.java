package com.fitnessapp.userDetailsAndAuthService.services.firebaseService;

import com.google.firebase.auth.FirebaseToken;

public interface FirebaseService {
    FirebaseToken verifyFirebaseIdToken(String firebaseIdToken);
}
