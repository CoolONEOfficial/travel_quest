package ru.coolone.travelquest.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import ru.coolone.travelquest.R;

import static android.Manifest.permission.READ_CONTACTS;
import static android.R.layout.simple_dropdown_item_1line;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    static final String TAG = LoginActivity.class.getSimpleName();

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    // UI references.
    private AutoCompleteTextView mLoginView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Login form.
        mLoginView = findViewById(R.id.login_text_login_or_mail);
        populateAutoComplete();

        // Password
        mPasswordView = findViewById(R.id.login_text_password);
        mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });

        // Login button
        Button loginButton = findViewById(R.id.login_button_login);
        loginButton.setOnClickListener(view -> attemptLogin());

        // Signin text view
        TextView signinTextView = findViewById(R.id.login_text_view_signin);
        signinTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // To signin activity
                Intent intent = new Intent(LoginActivity.this, SigninActivity.class);
                startActivity(intent);
            }
        });

        // Login form
        mLoginFormView = findViewById(R.id.login_form);

        // Progress
        mProgressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mLoginView, R.string.login_permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, v -> requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS));
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    RequestQueue queue;

    private void attemptLogin() {
        // Reset errors
        mLoginView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String mLoginOrMail = mLoginView.getText().toString();
        String mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Create url
        Uri.Builder reqUrlBuilder = new Uri.Builder()
                .scheme("http")
                .encodedAuthority(getResources().getString(R.string.httpServerAuthority))
                .appendQueryParameter("target", "login");

        // - Setting url -

        // Check for a valid password, if the user entered one.
        if (mPassword.isEmpty()) {
            // Password not entered
            mPasswordView.setError(getString(R.string.login_error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (isPasswordValid(mPassword)) {
            // Add pass parameter
            reqUrlBuilder.appendQueryParameter("pass", mPassword);
        } else {
            // Invalid password
            mPasswordView.setError(getString(R.string.login_error_password_incorrect));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid mail or login address.
        if (mLoginOrMail.isEmpty()) {
            mLoginView.setError(getString(R.string.login_error_field_required));
            focusView = mLoginView;
            cancel = true;
        } else if (isMailValid(mLoginOrMail)) {
            // Add mail parameter
            reqUrlBuilder.appendQueryParameter("mail", mLoginOrMail);
        } else if (isLoginValid(mLoginOrMail)) {
            // Add login parameter
            reqUrlBuilder.appendQueryParameter("login", mLoginOrMail);
        } else {
            // Invalid login or mail
            mLoginView.setError(getString(R.string.login_error_login));
            focusView = mLoginView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            // Create request queue
            queue = Volley.newRequestQueue(this);

            // Build request url
            String reqUrlStr = reqUrlBuilder.build().toString();

            // Create request
            JsonObjectRequest jReq = new JsonObjectRequest(Request.Method.GET,
                    reqUrlStr,
                    null,
                    response -> {
                        showProgress(false);

                        boolean result = false;

                        String mSessionKey = "";
                        try {
                            // Get session key
                            mSessionKey = response.getString("sessionKey");
                            result = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (result) {
                            Toast.makeText(LoginActivity.this,
                                    "Session key is " + mSessionKey,
                                    Toast.LENGTH_LONG).show();

                            // Set default settings
                            MainActivity.setDefaultSettings(LoginActivity.this.getApplicationContext());

                            // To quests activity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("sessionKey", mSessionKey);
                            startActivity(intent);
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    getResources().getString(R.string.login_error_login),
                                    Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> {
                        // Hide progress
                        showProgress(false);

                        // Show error
                        Toast.makeText(LoginActivity.this,
                                "Request to server error:\n" + error.toString(),
                                Toast.LENGTH_LONG).show();
                    }
            );

            // Access the RequestQueue through your singleton class.
            queue.add(jReq);
        }
    }

    /**
     * @param mail Mail, that will be checked
     * @return Mail valid bool
     */
    private boolean isMailValid(String mail) {
        return mail.contains("@") &&
                mail.length() >= getResources().getInteger(R.integer.mail_len_min) &&
                mail.length() <= getResources().getInteger(R.integer.mail_len_max);
    }

    /**
     * @param password Pass, that will be checked
     * @return Pass valid bool
     */
    private boolean isPasswordValid(String password) {
        return password.length() >= getResources().getInteger(R.integer.password_len_min) &&
                password.length() <= getResources().getInteger(R.integer.password_len_max);
    }

    /**
     * @param login Login, that will be checked
     * @return Login valid bool
     */
    private boolean isLoginValid(String login) {
        return login.length() >= getResources().getInteger(R.integer.login_len_min) &&
                login.length() <= getResources().getInteger(R.integer.login_len_max);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        // Show login form
        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        // Show progress view
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only mail addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        // Get mail
        List<String> mails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            mails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        // Add mail autocomplete
        addMailsToAutoComplete(mails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addMailsToAutoComplete(List<String> emailAddressCollection) {
        // Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        simple_dropdown_item_1line, emailAddressCollection);

        mLoginView.setAdapter(adapter);
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }
}

