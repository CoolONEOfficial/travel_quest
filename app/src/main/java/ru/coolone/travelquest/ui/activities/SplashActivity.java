package ru.coolone.travelquest.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Class<?> intentClass;

        // Switch last login method
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            if (!user.isEmailVerified())
                // To confirm mail screen
                intentClass = ConfirmMailActivity.class;
            else {
                intentClass = MainActivity.class;
            }
        } else
            intentClass = LoginActivity.class;

        // Set intent
        Intent intent = new Intent();
        intent.setClass(this, intentClass);

        // Go to target activity
        startActivity(intent);
        finish();
    }
}
