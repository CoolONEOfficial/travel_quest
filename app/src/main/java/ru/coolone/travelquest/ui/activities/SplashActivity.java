package ru.coolone.travelquest.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Go to...
        Intent intent;
        if (user == null) {
            // ...authentication
            intent = new Intent(this, LoginActivity.class);
        } else if (!user.isEmailVerified()) {
            // ...mail confirm
            intent = new Intent(this, ConfirmMailActivity.class);
        } else {
            // ...main
            intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
