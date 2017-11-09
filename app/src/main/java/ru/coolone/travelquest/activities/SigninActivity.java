package ru.coolone.travelquest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;

import ru.coolone.travelquest.R;

public class SigninActivity extends AbstractAuthActivity {

    final static String TAG = SigninActivity.class.getSimpleName();

    // --- Ui references ---

    // Login edit text
    private EditText loginView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // Login
        loginView = findViewById(R.id.signin_text_login);

        // Mail
        mailView = findViewById(R.id.signin_text_mail);

        // Password
        passwordView = findViewById(R.id.signin_text_password);

        // Login button
        authButton = findViewById(R.id.signin_button_signin);

        // Login text view
        TextView textViewLogin = findViewById(R.id.signin_text_view_login);
        textViewLogin.setOnClickListener(view -> {
            // To login activity
            Intent intent = new Intent(SigninActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Login form
        authFormView = findViewById(R.id.signin_form);

        // Progress
        progressView = findViewById(R.id.signin_progress);
    }

    @Override
    protected void attemptAuth() {
        if (!checkInput()) {
            auth.createUserWithEmailAndPassword(
                    mailView.getText().toString(),
                    passwordView.getText().toString()
            ).addOnCompleteListener(
                    this,
                    task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = auth.getCurrentUser();
//                        updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getParent(), getResources().getString(R.string.error_auth),
                                    Toast.LENGTH_SHORT).show();
//                        updateUI(null);
                        }
                    }
            );
        }
    }
}
