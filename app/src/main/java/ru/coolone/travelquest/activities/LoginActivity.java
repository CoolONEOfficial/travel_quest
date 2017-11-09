package ru.coolone.travelquest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;

import ru.coolone.travelquest.R;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity
        extends AbstractAuthActivity {

    static final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_login);

        // Mail
        mailView = findViewById(R.id.login_text_login_or_mail);

        // Password
        passwordView = findViewById(R.id.login_text_password);

        // Login button
        authButton = findViewById(R.id.login_button_login);

        // Signin text view
        TextView signinTextView = findViewById(R.id.login_text_view_signin);
        signinTextView.setOnClickListener(view -> {
            // To signin activity
            Intent intent = new Intent(LoginActivity.this, SigninActivity.class);
            startActivity(intent);
        });

        // Login form
        authFormView = findViewById(R.id.login_form);

        // Progress
        progressView = findViewById(R.id.login_progress);

        super.onCreate(savedInstanceState);
    }

    /**
     * Attempts to login the account specified by the auth form.
     * If there are form errors (invalid mail, pass, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    @Override
    protected void attemptAuth() {
        if(checkInput()) {
            // Login
            auth.signInWithEmailAndPassword(
                    mailView.getText().toString(),
                    passwordView.getText().toString()
            ).addOnCompleteListener(
                    this,
                    task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = auth.getCurrentUser();
                            //                        updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(getParent(), getResources().getString(R.string.error_auth),
                                    Toast.LENGTH_SHORT).show();
                            //                        updateUI(null);
                        }
                    });
        }

//        // Store values at the time of the login attempt.
//        String mLoginOrMail = mailView.getText().toString();
//        String mPassword = passwordView.getText().toString();
//
//        boolean cancel = false;
//        View focusView = null;
//
//        // Create url
//        Uri.Builder reqUrlBuilder = new Uri.Builder()
//                .scheme("http")
//                .encodedAuthority(getResources().getString(R.string.httpServerAuthority))
//                .appendQueryParameter("target", "login");
//
//        // - Setting url -
//
//        // Check for a valid password, if the user entered one.
//        if (mPassword.isEmpty()) {
//            // Password not entered
//            passwordView.setError(getString(R.string.login_error_field_required));
//            focusView = passwordView;
//            cancel = true;
//        } else if (isPasswordValid(mPassword)) {
//            // Add pass parameter
//            reqUrlBuilder.appendQueryParameter("pass", mPassword);
//        } else {
//            // Invalid password
//            passwordView.setError(getString(R.string.login_error_password_incorrect));
//            focusView = passwordView;
//            cancel = true;
//        }
//
//        // Check for a valid mail or login address.
//        if (mLoginOrMail.isEmpty()) {
//            mailView.setError(getString(R.string.login_error_field_required));
//            focusView = mailView;
//            cancel = true;
//        } else if (isMailValid(mLoginOrMail)) {
//            // Add mail parameter
//            reqUrlBuilder.appendQueryParameter("mail", mLoginOrMail);
//        } else if (isLoginValid(mLoginOrMail)) {
//            // Add login parameter
//            reqUrlBuilder.appendQueryParameter("login", mLoginOrMail);
//        } else {
//            // Invalid login or mail
//            mailView.setError(getString(R.string.login_error_login));
//            focusView = mailView;
//            cancel = true;
//        }
//
//        if (cancel) {
//            // There was an error; don't attempt login and focus the first
//            // form field with an error.
//            focusView.requestFocus();
//        } else {
//            // Show a progress spinner, and kick off a background task to
//            // perform the user login attempt.
//            setProgressVisibility(true);
//
//            // Create request queue
//            RequestQueue queue = Volley.newRequestQueue(this);
//
//            // Build request url
//            String reqUrlStr = reqUrlBuilder.build().toString();
//
//            // Create request
//            JsonObjectRequest jReq = new JsonObjectRequest(Request.Method.GET,
//                    reqUrlStr,
//                    null,
//                    response -> {
//                        setProgressVisibility(false);
//
//                        boolean result = false;
//
//                        String mSessionKey = "";
//                        try {
//                            // Get session key
//                            mSessionKey = response.getString("sessionKey");
//                            result = true;
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//                        if (result) {
//                            Toast.makeText(LoginActivity.this,
//                                    "Session key is " + mSessionKey,
//                                    Toast.LENGTH_LONG).show();
//
//                            // Set default settings
//                            MainActivity.setDefaultSettings(LoginActivity.this.getApplicationContext());
//
//                            // To quests activity
//                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                            intent.putExtra("sessionKey", mSessionKey);
//                            startActivity(intent);
//                        } else {
//                            Toast.makeText(LoginActivity.this,
//                                    getResources().getString(R.string.login_error_login),
//                                    Toast.LENGTH_LONG).show();
//                        }
//                    },
//                    error -> {
//                        // Hide progress
//                        setProgressVisibility(false);
//
//                        // Show error
//                        Toast.makeText(LoginActivity.this,
//                                "Request to server error:\n" + error.toString(),
//                                Toast.LENGTH_LONG).show();
//                    }
//            );
//
//            // Access the RequestQueue through your singleton class.
//            queue.add(jReq);
//        }
    }

}

