package ru.coolone.travelquest.ui.activities;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.github.paolorotolo.appintro.AppIntro;

import org.androidannotations.annotations.EActivity;

import lombok.val;
import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.fragments.IntroFragment_;
import ru.coolone.travelquest.ui.fragments.AboutFragment_;

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

        // Frags
        addSlide(AboutFragment_.builder().build());

        addSlide(
                IntroFragment_.builder()
                        .image(R.drawable.intro_places)
                        .title(getString(R.string.intro_places_title))
                        .text(getString(R.string.intro_places_text))
                        .build()
        );

        addSlide(
                IntroFragment_.builder()
                        .image(R.drawable.intro_add_details)
                        .title(getString(R.string.intro_add_details_title))
                        .text(getString(R.string.intro_add_details_text))
                        .build()
        );

        addSlide(
                IntroFragment_.builder()
                        .image(R.drawable.intro_search)
                        .title(getString(R.string.intro_search_title))
                        .text(getString(R.string.intro_search_text))
                        .build()
        );

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
