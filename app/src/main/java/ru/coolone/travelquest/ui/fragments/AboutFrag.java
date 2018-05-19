package ru.coolone.travelquest.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.widget.ImageView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import ru.coolone.travelquest.R;

@EFragment(R.layout.frag_about)
public class AboutFrag extends Fragment {
    @ViewById(R.id.about_logo)
    ImageView logo;

    @AfterViews
    void afterViews() {
        logo.setOnClickListener(
                view -> {
                    Intent browserIntent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://www.coolone.ru")
                    );
                    startActivity(browserIntent);
                }
        );
    }
}
