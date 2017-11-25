package ru.coolone.travelquest.ui.fragments.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.coolone.travelquest.R;

public class AboutFragment extends Fragment {

    public AboutFragment() {
        // Required empty public constructor
    }

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_about, container, false);

        v.findViewById(R.id.about_logo).setOnClickListener(
                view -> {
                    Intent browserIntent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://www.coolone.ru")
                    );
                    startActivity(browserIntent);
                }
        );

        return v;
    }
}
