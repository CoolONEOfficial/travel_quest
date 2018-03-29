package ru.coolone.travelquest.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ru.coolone.travelquest.R;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
    }

    private void toActivity(Class<? extends Activity> activity) {
        // Set intent
        Intent intent = new Intent();
        intent.setClass(this, activity);

        // Go to target activity
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Switch last login method
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            if (!user.isEmailVerified())
                // To confirm mail screen
                toActivity(ConfirmMailActivity.class);
            else {
                toActivity(MainActivity.class);
            }
        } else {
            AlertDialog.Builder ad = new AlertDialog.Builder(this);
            ad.setTitle(getString(R.string.alert_splash_title));
            ad.setMessage(getString(R.string.alert_splash_text));
            ad.setPositiveButton(getString(R.string.alert_splash_button_auth),
                    (dialog, which) -> {
                        toActivity(LoginActivity.class);
                    }
            );
            ad.setNegativeButton(getString(R.string.alert_splash_button_anonymous),
                    (dialog, which) -> FirebaseAuth.getInstance().signInAnonymously() // try sign in anonymously
                            .addOnCompleteListener(this, task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "signInAnonymously:success");
                                    toActivity(MainActivity.class);
                                } else {
                                    Log.w(TAG, "signInAnonymously:failure", task.getException());
                                    Toast.makeText(this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    toActivity(SplashActivity.class);
                                }
                            })
            );
            ad.show();
        }
    }
}
