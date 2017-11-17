package ru.coolone.travelquest.ui.fragments.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import ru.coolone.travelquest.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }
}
