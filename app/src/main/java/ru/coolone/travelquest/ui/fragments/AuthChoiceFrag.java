package ru.coolone.travelquest.ui.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;

import lombok.val;
import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.activities.LoginActivity_;
import ru.coolone.travelquest.ui.activities.MainActivity_;

@EFragment
public class AuthChoiceFrag extends Fragment {

    private static final String TAG = AuthChoiceFrag.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_auth_choice, container, false);
    }

    @Click(R.id.frag_auth_choice_normal)
    void normalAuth() {
        LoginActivity_.intent(getContext())
                .start();
    }

    @Click(R.id.frag_auth_choice_anonymous)
    void anonymousAuth() {
        val progress = new ProgressDialog(getContext());
        progress.setTitle(getString(R.string.login_progress));
        progress.setCancelable(false);
        progress.show();

        FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(
                        getActivity(),
                        task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "signInAnonymously:success");

                                MainActivity_.intent(getContext())
                                        .start();
                                getActivity().finish();
                            } else {
                                Log.w(TAG, "signInAnonymously:failure", task.getException());
                                Toast.makeText(getContext(), "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            progress.dismiss();
                        }
                );
    }
}
