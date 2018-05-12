package ru.coolone.travelquest.ui.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.zagum.switchicon.SwitchIconView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lombok.val;
import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.fragments.places.details.FirebaseMethods;
import ru.coolone.travelquest.ui.fragments.places.details.add.PlaceDetailsAddFragment;
import ru.coolone.travelquest.ui.fragments.places.details.add.PlaceDetailsAddPagerAdapter;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static ru.coolone.travelquest.ui.fragments.places.details.FirebaseMethods.serializeDetails;

@SuppressLint("Registered")
@EActivity
@OptionsMenu(R.menu.activity_add_details_actions)
public class AddDetailsActivity extends AppCompatActivity implements FirebaseMethods.TaskListener {
    private static final String TAG = AddDetailsActivity.class.getSimpleName();

    // Toolbar views
    ImageButton sendView;
    ImageButton restoreView;
    SwitchIconView translateView;
    FrameLayout translateViewLayout;

    // Google map place id
    @Extra
    String placeId;

    // Root layout
    @ViewById(R.id.add_details_root_layout)
    LinearLayout rootLayout;

    // View pager
    @ViewById(R.id.add_details_viewpager)
    ViewPager viewPager;
    PlaceDetailsAddPagerAdapter pagerAdapter;

    // Tab layout
    @ViewById(R.id.add_details_sliding_tabs)
    TabLayout tabLayout;

    ProgressBar progressBar;

    PlaceDetailsAddFragment[] frags;

    @AfterViews
    void afterViews() {
        // View pager
        pagerAdapter = new PlaceDetailsAddPagerAdapter(
                getSupportFragmentManager(),
                placeId,
                this
        );
        if (frags != null) {
            pagerAdapter.tabFragments = frags;
        }
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(MainActivity.SupportLang.values().length);
        ((PlaceDetailsAddFragment) pagerAdapter.getItem(viewPager.getCurrentItem()))
                .addListener(
                        () -> {
                            val dismissText = getString(R.string.add_details_intro_dismiss_button);
                            val frag = (PlaceDetailsAddFragment) pagerAdapter.getItem(viewPager.getCurrentItem());

                            frag.recycler.post(
                                    () -> {
                                        val firstHolder = frag.recycler.findViewHolderForAdapterPosition(0).itemView;

                                        // Intro
                                        ShowcaseConfig config = new ShowcaseConfig();
                                        config.setDelay(100);

                                        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(AddDetailsActivity.this, TAG);

                                        sequence.setConfig(config);

                                        sequence.addSequenceItem(
                                                frag.addSectionButton,
                                                getString(R.string.add_details_intro_add_header),
                                                dismissText
                                        );

                                        sequence.addSequenceItem(
                                                firstHolder.findViewById(R.id.add_details_head_add),
                                                getString(R.string.add_details_intro_add),
                                                dismissText
                                        );

                                        sequence.addSequenceItem(
                                                firstHolder.findViewById(R.id.add_details_head_remove),
                                                getString(R.string.add_details_intro_remove),
                                                dismissText
                                        );

                                        sequence.addSequenceItem(
                                                sendView,
                                                getString(R.string.add_details_intro_send),
                                                dismissText
                                        );

                                        sequence.addSequenceItem(
                                                restoreView,
                                                getString(R.string.add_details_intro_restore),
                                                dismissText
                                        );

                                        sequence.start();
                                    }
                            );
                        }
                );

        // Tab layout
        tabLayout.setupWithViewPager(viewPager);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        sendView = (ImageButton) menu.findItem(R.id.add_details_action_send).getActionView();
        sendView.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_check));
        sendView.getBackground().setAlpha(0);
        sendView.setOnClickListener(
                v -> new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.add_details_action_send_alert_title))
                        .setMessage(getString(R.string.add_details_action_send_alert_text))
                        .setCancelable(true)
                        .setPositiveButton(
                                getString(R.string.add_details_action_send_alert_confirm),
                                (dialog, which) -> {
                                    tabLayout.setVisibility(View.GONE);
                                    viewPager.setVisibility(View.GONE);
                                    rootLayout.addView(progressBar);

                                    Map<String, Object> defaultVals = new HashMap<>();
                                    defaultVals.put("score", new ArrayList<String>());

                                    for (int mFragId = 0; mFragId < pagerAdapter.getCount(); mFragId++) {
                                        PlaceDetailsAddFragment mFrag = (PlaceDetailsAddFragment) pagerAdapter.getItem(mFragId);

                                        val mLang = mFrag.lang.lang;
                                        val docRef = MainActivity.getQuestsRoot(mLang)
                                                .collection(placeId)
                                                .document(
                                                        MainActivity.firebaseUser.getUid() +
                                                                '_' +
                                                                MainActivity.firebaseUser.getDisplayName()
                                                );

                                        docRef.set(defaultVals)
                                                .addOnSuccessListener(
                                                        aVoid -> serializeDetails(
                                                                docRef.collection("coll"),
                                                                mFrag.recycler,
                                                                this
                                                        )
                                                ).addOnFailureListener(
                                                e -> {
                                                    onFailure(e);
                                                    onCompleted();
                                                }
                                        );
                                    }
                                }
                        )
                        .setNegativeButton(
                                getString(R.string.add_details_action_send_alert_cancel),
                                (dialog, which) -> dialog.dismiss()
                        )
                        .create()
                        .show()
        );

        translateView = new SwitchIconView(this);
        translateView.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.ic_translate)
        );
        val params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        translateView.setLayoutParams(
                params
        );
        translateView.setColorFilter(Color.WHITE);

        translateViewLayout = (FrameLayout) menu.findItem(R.id.add_details_action_translate).getActionView();
        translateViewLayout.setOnClickListener(
                v -> translateView.setIconEnabled(!translateView.isIconEnabled())
        );
        val padding = (int) getResources().getDimension(R.dimen.content_inset) * 2;
        translateViewLayout.setPadding(
                padding, padding, padding, padding
        );
        translateViewLayout.addView(translateView);
        translateView.setOnClickListener(
                v -> translateViewLayout.callOnClick()
        );

        restoreView = (ImageButton) menu.findItem(R.id.add_details_action_restore).getActionView();
        restoreView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_restore));
        restoreView.getBackground().setAlpha(0);
        restoreView.setOnClickListener(
                v -> {
                    for (int mFragId = 0; mFragId < pagerAdapter.getCount(); mFragId++) {
                        val mFrag = (PlaceDetailsAddFragment) pagerAdapter.getItem(mFragId);

                        mFrag.restoreDetails();
                    }
                }
        );

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_details);
        val bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        // Progress bar
        progressBar = new ProgressBar(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        params.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(params);

        // Fix action bar color
        getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(
                        ContextCompat.getColor(
                                this,
                                R.color.colorPrimary
                        )
                )
        );

        // Restore frags
        if (savedInstanceState != null) {
            frags = new PlaceDetailsAddFragment[MainActivity.SupportLang.values().length];
            for (val mLang : MainActivity.SupportLang.values()) {
                frags[mLang.ordinal()] = (PlaceDetailsAddFragment) getSupportFragmentManager().getFragment(
                        savedInstanceState,
                        mLang.lang
                );
            }
            if (pagerAdapter != null) {
                pagerAdapter.tabFragments = frags;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        for (val mLang : MainActivity.SupportLang.values()) {
            getSupportFragmentManager().putFragment(
                    outState,
                    mLang.lang,
                    pagerAdapter.getItem(mLang.ordinal())
            );
        }
    }

    @OptionsItem(android.R.id.home)
    void homeSelected() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onSuccess() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onCompleted() {
        rootLayout.removeView(progressBar);
        tabLayout.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFailure(Exception e) {
        Toast.makeText(
                AddDetailsActivity.this,
                e.getLocalizedMessage(),
                Toast.LENGTH_SHORT
        ).show();
    }
}
