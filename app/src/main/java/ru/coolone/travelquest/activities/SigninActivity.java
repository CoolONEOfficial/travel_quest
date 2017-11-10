package ru.coolone.travelquest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;

import ru.coolone.travelquest.R;

public class SigninActivity extends AbstractAuthActivity {

    final static String TAG = SigninActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Context
        context = getApplicationContext();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // Mail
        mailView = findViewById(R.id.signin_text_mail);

        // Password
        passwordView = findViewById(R.id.signin_text_password);

        // Login button
        authButton = findViewById(R.id.signin_button_signin);

        // Auth form
        authFormView = findViewById(R.id.signin_form);

        // Progress
        progressView = findViewById(R.id.signin_progress);

        // Login text view
        TextView textViewLogin = findViewById(R.id.signin_text_view_login);
        textViewLogin.setOnClickListener(view -> {
            // To login activity
            Intent intent = new Intent(SigninActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Initialize parent views
        initViews();
    }

    @Override
    void onAuth() {
        // Signin
        auth.createUserWithEmailAndPassword(
                mailView.getText().toString(),
                passwordView.getText().toString()
        ).addOnCompleteListener(this, this::onAuthComplete);
    }
}
