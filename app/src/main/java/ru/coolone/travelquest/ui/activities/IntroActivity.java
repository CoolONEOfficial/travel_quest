package ru.coolone.travelquest.ui.activities;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro;

import org.androidannotations.annotations.EActivity;

import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.fragments.AboutFrag_;
import ru.coolone.travelquest.ui.fragments.IntroFrag_;
import ru.coolone.travelquest.ui.fragments.intro.AuthChoiceFrag_;
import ru.coolone.travelquest.ui.fragments.intro.CityPickerFrag_;

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
        addSlide(AboutFrag_.builder().build());

        addSlide(
                IntroFrag_.builder()
                        .image(R.drawable.intro_places)
                        .title(getString(R.string.intro_places_title))
                        .text(getString(R.string.intro_places_text))
                        .build()
        );

        addSlide(
                IntroFrag_.builder()
                        .image(R.drawable.intro_add_details)
                        .title(getString(R.string.intro_add_details_title))
                        .text(getString(R.string.intro_add_details_text))
                        .build()
        );

        addSlide(
                IntroFrag_.builder()
                        .image(R.drawable.intro_search)
                        .title(getString(R.string.intro_search_title))
                        .text(getString(R.string.intro_search_text))
                        .build()
        );

        addSlide(CityPickerFrag_.builder().build());

        addSlide(AuthChoiceFrag_.builder().build());

        // Hide Skip/Done button.
        showSkipButton(false);
        setProgressButtonEnabled(false);

        // Fix action bar color
        getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(
                        ContextCompat.getColor(
                                this,
                                R.color.colorPrimaryDark
                        )
                )
        );
    }
}
