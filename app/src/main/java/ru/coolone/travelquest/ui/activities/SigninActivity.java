package ru.coolone.travelquest.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;

import org.androidannotations.annotations.EActivity;

import ru.coolone.travelquest.R;

@EActivity
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
        oauthGoogleView = findViewById(R.id.signin_oauth_google);
        progressLayout = findViewById(R.id.signin_progress_layout);
        progressBar = findViewById(R.id.signin_progress_bar);
        progressTitle = findViewById(R.id.signin_progress_title);
        TextView textViewLogin = findViewById(R.id.signin_text_view_login);

        textViewLogin.setOnClickListener(view -> {
            // To login activity
            Intent intent = new Intent(SigninActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // Initialize parent views
        initViews();
    }

    @Override
    void onAuth() {
        // Signin
        progressTitle.setText(getResources().getString(R.string.signin_progress));
        auth.createUserWithEmailAndPassword(
                mailView.getText().toString(),
                passwordView.getText().toString()
        ).addOnCompleteListener(this, this::onAuthComplete);
    }

    @Override
    protected void onAuthSuccess(FirebaseUser user) {
        // Send verification letter

        progressTitle.setText(getResources().getString(R.string.signin_progress_confirmation));
        progressLayout.setVisibility(View.VISIBLE);

        Log.d(TAG, "Sending verification letter to ");
        user.sendEmailVerification().addOnSuccessListener(
                aVoid -> {
                    // To mail verification
                    Intent intent = new Intent(this, ConfirmMailActivity.class);
                    startActivity(intent);
                    finish();
                }
        ).addOnFailureListener(
                e -> {
                    // Show error
                    Toast.makeText(
                            this,
                            getResources().getString(R.string.signin_confirmation_error),
                            Toast.LENGTH_SHORT
                    ).show();

                    // Delete account
                    user.delete();
                }
        );
    }
}
