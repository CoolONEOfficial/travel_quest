package ru.coolone.travelquest.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import org.androidannotations.annotations.EFragment;

import ru.coolone.travelquest.R;

@EFragment
public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }
}
