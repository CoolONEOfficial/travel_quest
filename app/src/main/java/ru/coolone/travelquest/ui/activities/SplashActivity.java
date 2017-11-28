package ru.coolone.travelquest.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent();

        // Set intent target activity
        Class<?> intentClass;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (user.getProviderId().equals("firebase") // check firebase auth
                    && !user.isEmailVerified())
                intentClass = ConfirmMailActivity.class;
            else {
                intentClass = MainActivity.class;
                intent.putExtra("auth_type", AbstractAuthActivity.AuthTypes.FIREBASE.ordinal());
            }
        } else if (GoogleSignIn.getLastSignedInAccount(this) != null) { // check google oauth
            intentClass = MainActivity.class;
            intent.putExtra("auth_type", AbstractAuthActivity.AuthTypes.OAUTH_GOOGLE.ordinal());
        } else
            intentClass = LoginActivity.class;

        // Go to target activity
        intent.setClass(this, intentClass);
        startActivity(intent);
        finish();
    }
}
