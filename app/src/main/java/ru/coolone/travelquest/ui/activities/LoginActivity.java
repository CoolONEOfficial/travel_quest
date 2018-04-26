package ru.coolone.travelquest.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import org.androidannotations.annotations.EActivity;

import ru.coolone.travelquest.R;

/**
 * A login screen that offers login via email/password.
 */
@SuppressLint("Registered")
@EActivity
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
        oauthFormView = findViewById(R.id.oauth_form);
        oauthGoogleView = findViewById(R.id.login_oauth_google);
        progressLayout = findViewById(R.id.login_progress_layout);
        progressBar = findViewById(R.id.login_progress_bar);
        progressTitle = findViewById(R.id.login_progress_title);
        TextView signinTextView = findViewById(R.id.login_text_view_signin);

        signinTextView.setOnClickListener(view -> {
            // To signin activity
            Intent intent = new Intent(LoginActivity.this, SigninActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
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

