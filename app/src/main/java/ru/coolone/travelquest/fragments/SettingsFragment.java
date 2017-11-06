package ru.coolone.travelquest.fragments;

import android.os.Bundle;
import android.support.v4.preference.PreferenceFragment;

import ru.coolone.travelquest.R;

public class SettingsFragment extends PreferenceFragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add settings
        addPreferencesFromResource(R.xml.settings);
    }
}
