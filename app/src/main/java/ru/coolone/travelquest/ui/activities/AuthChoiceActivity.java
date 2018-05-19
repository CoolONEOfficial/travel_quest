package ru.coolone.travelquest.ui.activities;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.fragments.AuthChoiceFrag_;

@SuppressLint("Registered")
@EActivity
public class AuthChoiceActivity extends AppCompatActivity {

    @ViewById(R.id.auth_choice_container)
    FrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_choice);

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

    @AfterViews
    void afterViews() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.auth_choice_container, AuthChoiceFrag_.builder().build())
                .commit();
    }
}
