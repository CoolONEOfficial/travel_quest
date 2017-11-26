package ru.coolone.travelquest.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

import ru.coolone.travelquest.R;

public class SigninActivity extends AbstractAuthActivity {

    final static String TAG = SigninActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Context
        context = getApplicationContext();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // Views
        mailView = findViewById(R.id.signin_text_mail);
        passwordView = findViewById(R.id.signin_text_password);
        authButton = findViewById(R.id.signin_button_signin);
        authFormView = findViewById(R.id.signin_form);
        progressView = findViewById(R.id.signin_progress_layout);
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

    @Override
    protected void onAuthSuccess(FirebaseUser user) {
        // Send verification letter
        user.sendEmailVerification();

        // To mail verification
        Intent intent = new Intent(this, ConfirmMailActivity.class);
        startActivity(intent);
        finish();
    }
}
