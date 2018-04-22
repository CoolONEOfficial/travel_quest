package ru.coolone.travelquest.ui.activities;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

import org.androidannotations.annotations.EActivity;

import lombok.val;
import ru.coolone.travelquest.R;

/**
 * @author coolone
 * @since 20.04.18
 */
@SuppressLint("Registered")
@EActivity
public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest.
        addSlide(AppIntroFragment.newInstance(
                getString(R.string.alert_greetings_title),
                getString(R.string.alert_greetings_text),
                R.mipmap.ic_launcher_round,
                ContextCompat.getColor(this, R.color.primary)
        ));

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.alert_places_title),
                getString(R.string.alert_places_text),
                R.drawable.ic_place,
                ContextCompat.getColor(this, R.color.primary)
        ));

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.alert_add_details_title),
                getString(R.string.alert_add_details_text),
                R.drawable.ic_edit_place,
                ContextCompat.getColor(this, R.color.primary)
        ));

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.alert_search_title),
                getString(R.string.alert_search_text),
                R.drawable.ic_search,
                ContextCompat.getColor(this, R.color.primary)
        ));

        // Override bar/separator color.
        setBarColor(ContextCompat.getColor(this, R.color.primary_dark));
        setSeparatorColor(ContextCompat.getColor(this, R.color.primary_dark));

        // Hide Skip/Done button.
        showSkipButton(true);
        setProgressButtonEnabled(true);

        // Fix action bar color
        getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(
                        ContextCompat.getColor(
                                this,
                                R.color.colorPrimaryDark
                        )
                )
        );

        val listener = (View.OnClickListener) v -> MainActivity.getAuthDialog(
                this,
                task -> {
                    if (task.isSuccessful()) {
                        MainActivity_.intent(this).start();
                        finish();
                    }
                }
        ).show();

        skipButton.setOnClickListener(listener);
        doneButton.setOnClickListener(listener);
    }
}
