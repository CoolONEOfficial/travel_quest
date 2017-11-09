package ru.coolone.travelquest.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import ru.coolone.travelquest.R;

import static android.Manifest.permission.READ_CONTACTS;
import static android.R.layout.simple_dropdown_item_1line;

abstract public class AbstractAuthActivity
        extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    // Firebase auth
    FirebaseAuth auth;

    // --- Ui references ---

    // Auth button
    protected Button authButton;

    // Auth form
    protected View authFormView;

    // Progress
    protected View progressView;

    // Mail
    protected AutoCompleteTextView mailView;

    // Password
    protected EditText passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- Set action auth ---

        // After input password
        passwordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptAuth();
                return true;
            }
            return false;
        });

        // After click button
        authButton.setOnClickListener(view -> attemptAuth());

        // Populate autocomplete
        populateAutoComplete();
    }

    /**
     * @param mail Mail, that will be checked
     * @return Mail valid bool
     */
    protected InputError isMailValid(String mail) {
        if(mail.length() < getResources().getInteger(R.integer.mail_len_min))
            return InputError.INPUT_ERROR_SMALL;

        if(mail.length() > getResources().getInteger(R.integer.mail_len_max))
            return InputError.INPUT_ERROR_LONG;

        if(!mail.contains("@"))
            return InputError.INPUT_ERROR_INCORRECT;

        return InputError.INPUT_GOOD;
    }
    final protected InputError isMailValid() {
        return isMailValid(mailView.getText().toString());
    }

    /**
     * @param password Pass, that will be checked
     * @return Pass valid bool
     */
    protected InputError isPasswordValid(String password) {
        if(password.length() < getResources().getInteger(R.integer.password_len_min))
            return InputError.INPUT_ERROR_SMALL;

        if(password.length() > getResources().getInteger(R.integer.password_len_max))
            return InputError.INPUT_ERROR_LONG;

        return InputError.INPUT_GOOD;
    }
    final protected InputError isPasswordValid() {
        return isPasswordValid(passwordView.getText().toString());
    }

    /**
     * Shows / hides the progress UI and hides / shows the login form.
     */
    protected void setProgressVisibility(final boolean visibility) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        // Show login form
        authFormView.setVisibility(
                visibility
                        ? View.GONE
                        : View.VISIBLE
        );
        authFormView.animate().setDuration(shortAnimTime).alpha(
                visibility
                        ? 0
                        : 1
        ).setListener(new AnimatorListenerAdapter() {
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
        progressView.setVisibility(
                visibility
                        ? View.VISIBLE
                        : View.GONE
        );
        progressView.animate().setDuration(shortAnimTime).alpha(
                visibility
                        ? 1
                        : 0)
                .setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressView.setVisibility(
                        visibility
                                ? View.VISIBLE
                                : View.GONE
                );
            }
        });
    }

    /**
     * Hides or shows software keyboard
     */
    protected void setSoftKeyboardVisibility(boolean visibility) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
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

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
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

    protected void attemptAuth() {
        // Hide keyboard
        setSoftKeyboardVisibility(false);

        // Reset errors
        mailView.setError(null);
        passwordView.setError(null);
    }

    private void inputError(TextView textView,
                            InputError err,
                            int strErrIdIncorrect,
                            int strErrIdLong,
                            int strErrIdSmall) {
        // Get error string id
        int errStrResId = strErrIdIncorrect;
        switch (err) {
            case INPUT_ERROR_INCORRECT:
                errStrResId = strErrIdIncorrect;
                break;
            case INPUT_ERROR_LONG:
                errStrResId = strErrIdLong;
                break;
            case INPUT_ERROR_SMALL:
                errStrResId = strErrIdSmall;
                break;
            case INPUT_ERROR_EMPTY:
                errStrResId = R.string.error_field_required;
                break;
        }

        // Show error
        textView.setError(getResources().getString(errStrResId));
    }

    final protected void passwordError(InputError err) {
        inputError(
                passwordView,
                err,
                R.string.error_password_incorrect,
                R.string.error_password_long,
                R.string.error_password_small
        );
    }

    final protected void mailError(InputError err) {
        inputError(
                mailView,
                err,
                R.string.error_mail_incorrect,
                R.string.error_mail_long,
                R.string.error_mail_small
        );
    }

    enum InputError {
        INPUT_GOOD,
        INPUT_ERROR_LONG,
        INPUT_ERROR_SMALL,
        INPUT_ERROR_INCORRECT,
        INPUT_ERROR_EMPTY
    }

    protected boolean checkInput() {
        boolean inputGood = true;

        // Check mail
        InputError mailError = isMailValid();
        if(mailError != InputError.INPUT_GOOD) {
            inputGood = false;
            mailError(mailError);
        }

        // Check password
        InputError passwordError = isPasswordValid();
        if(passwordError != InputError.INPUT_GOOD) {
            inputGood = false;
            passwordError(passwordError);
        }

        return inputGood;
    }
}
