package ru.coolone.travelquest.ui.fragments;

import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.support.v7.content.res.AppCompatResources;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import ru.coolone.travelquest.R;

/**
 * @author coolone
 * @since 22.04.18
 */

@EFragment(R.layout.fragment_intro)
public class IntroFragment extends Fragment {
    /**
     * Title
     */
    @ViewById(R.id.intro_title)
    TextView titleView;
    @FragmentArg
    String title;

    /**
     * Text
     */
    @ViewById(R.id.intro_text)
    TextView textView;
    @FragmentArg
    String text;

    /**
     * Image
     */
    @ViewById(R.id.intro_image)
    ImageView imageView;
    @FragmentArg
    @DrawableRes
    int image;

    @AfterViews
    void afterViews() {
        textView.setText(text);
        titleView.setText(title);
        imageView.setImageDrawable(
                AppCompatResources.getDrawable(getContext(), image)
        );
    }
}
