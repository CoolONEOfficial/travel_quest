package ru.coolone.travelquest.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import ru.coolone.travelquest.R;

public class SigninActivity extends AppCompatActivity {

    final static String TAG = SigninActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

//        PhoneAuthProvider.getInstance().verifyPhoneNumber(
//                phoneNumber,        // Phone number to verify
//                60,                 // Timeout duration
//                TimeUnit.SECONDS,   // Unit of timeout
//                this,               // Activity (for callback binding)
//                mCallbacks);        // OnVerificationStateChangedCallbacks

        // Login text view
        TextView textViewLogin = findViewById(R.id.signin_text_view_login);
        textViewLogin.setOnClickListener(view -> {
            // To login activity
            Intent intent = new Intent(SigninActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
