package ru.coolone.travelquest.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;
import java.util.List;

import ru.coolone.travelquest.R;

import static android.Manifest.permission.READ_CONTACTS;
import static android.R.layout.simple_dropdown_item_1line;

abstract public class AbstractAuthActivity
        extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String TAG = LoginActivity.class.getSimpleName();

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    // Firebase onAuth
    protected FirebaseAuth auth;
    // Auth button
    protected Button authButton;

    // --- Ui references ---
    // Auth form
    protected View authFormView;
    // Progress
    protected RelativeLayout progressLayout;
    protected ProgressBar progressBar;
    protected TextView progressTitle;
    // Mail
    protected AutoCompleteTextView mailView;
    // Password
    protected EditText passwordView;
    // OAuth
    protected ImageButton oauthGoogleView;
    protected GoogleSignInOptions oauthGoogleOptions;
    protected GoogleSignInClient oauthGoogleClient;
    // Context
    Context context;
    private boolean initViewsCalled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Auth
        auth = FirebaseAuth.getInstance();
        oauthGoogleOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

        oauthGoogleClient = GoogleSignIn.getClient(this, oauthGoogleOptions);

        // Populate autocomplete
        populateAutoComplete();
    }

    protected void initViews() {
        // Fix action bar color
        getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(
                        ContextCompat.getColor(
                                this,
                                R.color.colorPrimary
                        )
                )
        );

        // --- Set listeners ---

        // After input password
        passwordView.setOnEditorActionListener(
                (textView, id, keyEvent) -> {
                    if (checkPassword() == InputError.NONE) {
                        startAuth();
                        return true;
                    }
                    return false;
                }
        );

        // After input mail
        mailView.setOnEditorActionListener(
                (textView, id, keyEvent) -> checkMail() == InputError.NONE
        );

        // After click button
        authButton.setOnClickListener(
                view -> startAuth()
        );

        oauthGoogleView.setOnClickListener(
                view -> onOAuthGoogle()
        );

        // Activate called flag
        initViewsCalled = true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!initViewsCalled)
            Log.e(TAG, "Views not initialized! Use initViews() function.");

        if (context == null)
            Log.e(TAG, "Context is null!");
    }

    /**
     * @param mail Mail, that will be checked
     * @return Mail valid bool
     */
    protected InputError checkMailStr(String mail) {
        InputError error = InputError.NONE;
        mail = mail.trim();

        // Empty
        if (mail.isEmpty())
            error = InputError.EMPTY;
            // Small
        else if (mail.length() < context.getResources().getInteger(R.integer.mail_len_min))
            error = InputError.SMALL;
            // Long
        else if (mail.length() > context.getResources().getInteger(R.integer.mail_len_max))
            error = InputError.LONG;
            // Mail
        else if (!mail.contains("@"))
            error = InputError.INCORRECT;

        return error;
    }

    final protected InputError checkMail() {
        // Check error
        InputError error = checkMailStr(mailView.getText().toString());

        // Handle error
        if (error != InputError.NONE)
            inputError(mailView, error);

        return error;
    }

    /**
     * @param password Pass, that will be checked
     * @return Pass valid bool
     */
    protected InputError checkPasswordStr(String password) {
        InputError error = InputError.NONE;
        password = password.trim();

        // Empty
        if (password.isEmpty())
            error = InputError.EMPTY;
            // Small
        else if (password.length() < context.getResources().getInteger(R.integer.password_len_min))
            error = InputError.SMALL;
            // Long
        else if (password.length() > context.getResources().getInteger(R.integer.password_len_max))
            error = InputError.LONG;

        return error;
    }

    final protected InputError checkPassword() {
        // Check error
        InputError error = checkPasswordStr(passwordView.getText().toString());

        // Handle error
        if (error != InputError.NONE)
            inputError(passwordView, error);

        return error;
    }

    /**
     * Shows / hides the progress UI and hides / shows the login form.
     */
    private void setProgressVisibility(final boolean visibility) {
        runOnUiThread(() -> {
            final int shortAnimTime = context
                    .getResources()
                    .getInteger(android.R.integer.config_shortAnimTime);

            // Show login form
            authFormView.setVisibility(
                    visibility
                            ? View.GONE
                            : View.VISIBLE
            );
            authFormView
                    .animate()
                    .setDuration(shortAnimTime)
                    .alpha(
                            visibility
                                    ? 0
                                    : 1
                    ).setListener(
                    new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            authFormView.setVisibility(
                                    visibility
                                            ? View.GONE
                                            : View.VISIBLE
                            );
                        }
                    });

            // Show progress view
            progressLayout.setVisibility(
                    visibility
                            ? View.VISIBLE
                            : View.GONE
            );
            progressLayout
                    .animate()
                    .setDuration(shortAnimTime)
                    .alpha(
                            visibility
                                    ? 1
                                    : 0
                    ).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressLayout.setVisibility(
                            visibility
                                    ? View.VISIBLE
                                    : View.GONE
                    );
                }
            });
        });
    }

    protected void showProgress() {
        setProgressVisibility(true);
    }

    protected void hideProgress() {
        setProgressVisibility(false);
    }

    /**
     * Hides or shows software keyboard
     */
    protected void setSoftKeyboardVisibility(boolean visibility) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        IBinder windowToken = getCurrentFocus().getWindowToken();
        assert windowToken != null;
        imm.hideSoftInputFromWindow(windowToken, 0);
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
                new ArrayAdapter<>(this,
                        simple_dropdown_item_1line, emailAddressCollection);

        mailView.setAdapter(adapter);
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
            Snackbar.make(mailView, R.string.error_permission_rationale, Snackbar.LENGTH_INDEFINITE)
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

    final protected void startAuth() {
        // Hide keyboard
        setSoftKeyboardVisibility(false);

        // Reset errors
        mailView.setError(null);
        passwordView.setError(null);

        // Loader
        showProgress();

        // Check input
        if (checkInput())
            onAuth();
    }

    abstract void onAuth();

    private void inputError(TextView textView,
                            InputError err) {
        // Get error string id
        int errStrResId = R.string.error_field_incorrect;
        switch (err) {
            case INCORRECT:
                errStrResId = R.string.error_field_incorrect;
                break;
            case LONG:
                errStrResId = R.string.error_field_long;
                break;
            case SMALL:
                errStrResId = R.string.error_field_small;
                break;
            case EMPTY:
                errStrResId = R.string.error_field_required;
                break;
        }

        // Show error
        textView.setError(context.getResources().getString(errStrResId));
    }

    protected boolean checkInput() {

        // Check input
        return (
                checkMail() == InputError.NONE &&
                        checkPassword() == InputError.NONE
        );
    }

    protected void onAuthComplete(Task<AuthResult> authTask) {
        if (authTask.isSuccessful()) {
            Log.d(TAG, "SignInWithEmail success!");
            authTask.addOnCompleteListener(
                    task -> {
                        if (task.isSuccessful()) {
                            onAuthSuccess(task.getResult().getUser());
                        }
                    }
            );
        } else {
            hideProgress();
            Log.w(TAG, "SignInWithEmail error!", authTask.getException());
            onAuthError(authTask.getException());
        }
    }

    protected void onAuthSuccess(FirebaseUser user) {
        Log.d(TAG, "Auth success!");

        // Go...
        Intent intent;
        if (user.isEmailVerified()) {
            // ...to main activity
            intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            // ...to confirm mail activity
            intent = new Intent(this, ConfirmMailActivity.class);
        }
        startActivity(intent);
        finish();
    }

    protected void onAuthError(Exception e) {
        Log.w(TAG, "Auth error!", e);

        // Show error
        Toast.makeText(context,
                context.getResources().getString(R.string.error_auth)
                        + '\n' + e.getLocalizedMessage(),
                Toast.LENGTH_SHORT).show();
    }

    protected void onOAuthGoogle() {
        Log.d(TAG, "Starting google oauth...");
        Intent signInIntent = oauthGoogleClient.getSignInIntent();
        startActivityForResult(signInIntent, ActivityResult.OAUTH_GOOGLE.ordinal());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ActivityResult.OAUTH_GOOGLE.ordinal()) {
            // Get google oauth result
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                firebaseAuthWithGoogle(task.getResult(ApiException.class));
            } catch (ApiException e) {
                // Show error
                Toast.makeText(context,
                        context.getResources().getString(R.string.error_auth)
                                + '\n' + e.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        onOAuthGoogleSuccess();
                    } else {
                        // Show error
                        Toast.makeText(context,
                                context.getResources().getString(R.string.error_auth)
                                        + '\n' + task.getException().getLocalizedMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    protected void onOAuthGoogleSuccess() {
        Log.d(TAG, "Google OAuth success");

        // To main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    enum InputError {
        NONE,
        LONG,
        SMALL,
        INCORRECT,
        EMPTY
    }

    protected enum ActivityResult {
        OAUTH_GOOGLE
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
