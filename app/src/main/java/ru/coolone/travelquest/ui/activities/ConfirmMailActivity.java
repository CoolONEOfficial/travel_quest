package ru.coolone.travelquest.ui.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.ref.WeakReference;

import ru.coolone.travelquest.R;

public class ConfirmMailActivity extends AppCompatActivity {

    final FirebaseUser user = FirebaseAuth
            .getInstance()
            .getCurrentUser();
    RelativeLayout progressLayout;
    LinearLayout confirmLayout;
    Button checkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_mail);

        if (user == null) {
            // To login
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        }

        progressLayout = findViewById(R.id.confirm_mail_progress_layout);
        confirmLayout = findViewById(R.id.confirm_mail_layout);
        checkButton = findViewById(R.id.confirm_mail_button);

        checkButton.setOnClickListener(
                view -> {
                    CheckConfirmTask task = new CheckConfirmTask(this);
                    task.execute();
                }
        );

        // Fix action bar color
        getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(
                        ContextCompat.getColor(
                                this,
                                R.color.colorPrimary
                        )
                )
        );
    }

    private void onSuccess() {
        // To main activity
        Intent intent = new Intent(this, MainActivity_.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void onFailure() {
        Toast.makeText(
                this,
                getResources().getString(R.string.confirm_mail_error),
                Toast.LENGTH_LONG
        ).show();
    }

    private void showProgress() {
        runOnUiThread(
                () -> {
                    progressLayout.setVisibility(View.VISIBLE);
                    confirmLayout.setVisibility(View.GONE);
                }
        );
    }

    private void hideProgress() {
        runOnUiThread(
                () -> {
                    progressLayout.setVisibility(View.GONE);
                    confirmLayout.setVisibility(View.VISIBLE);
                }
        );
    }

    private static class CheckConfirmTask extends AsyncTask<Void, Void, Boolean> {

        WeakReference<ConfirmMailActivity> parent;

        CheckConfirmTask(ConfirmMailActivity parent) {
            this.parent = new WeakReference<>(parent);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            parent.get().showProgress();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Log.d(parent.getClass().getSimpleName(), "User in async task: " + parent.get().user);
            if (parent.get().user != null) {
                Task<Void> reloadTask = parent.get().user.reload();
                while (!reloadTask.isComplete()) {
                }
                return parent.get().user.isEmailVerified();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            parent.get().hideProgress();

            if (aBoolean) {
                parent.get().onSuccess();
            } else {
                parent.get().onFailure();
            }
        }
    }
}
