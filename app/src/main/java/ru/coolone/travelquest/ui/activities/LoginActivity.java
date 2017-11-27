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

        // Views
        mailView = findViewById(R.id.login_text_mail);
        passwordView = findViewById(R.id.login_text_password);
        authButton = findViewById(R.id.login_button_login);
        authFormView = findViewById(R.id.login_form);
        progressLayout = findViewById(R.id.login_progress_layout);
        progressBar = findViewById(R.id.login_progress_bar);
        progressTitle = findViewById(R.id.login_progress_title);
        TextView signinTextView = findViewById(R.id.login_text_view_signin);

        // Signin text view
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

