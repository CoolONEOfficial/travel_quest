package ru.coolone.travelquest.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import ru.coolone.travelquest.R;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity
        extends AbstractAuthActivity {

    static final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Context
        context = getApplicationContext();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Mail
        mailView = findViewById(R.id.login_text_mail);

        // Password
        passwordView = findViewById(R.id.login_text_password);

        // Login button
        authButton = findViewById(R.id.login_button_login);

        // Login form
        authFormView = findViewById(R.id.login_form);

        // Progress
        progressView = findViewById(R.id.login_progress);

        // Signin text view
        TextView signinTextView = findViewById(R.id.login_text_view_signin);
        signinTextView.setOnClickListener(view -> {
            // To signin activity
            Intent intent = new Intent(LoginActivity.this, SigninActivity.class);
            startActivity(intent);
        });

        // Initialize parent views
        initViews();
    }

    @Override
    protected void onAuth() {
        // Login
        auth.signInWithEmailAndPassword(
                mailView.getText().toString(),
                passwordView.getText().toString()
        ).addOnCompleteListener(this, this::onAuthComplete);
    }

}

