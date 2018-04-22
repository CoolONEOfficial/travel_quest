package ru.coolone.travelquest.ui.activities;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import ru.coolone.travelquest.ui.fragments.quests.details.FirebaseMethods;
import ru.coolone.travelquest.ui.fragments.quests.details.add.PlaceDetailsAddFragment;
import ru.coolone.travelquest.ui.fragments.quests.details.add.PlaceDetailsAddPagerAdapter;

import static ru.coolone.travelquest.ui.fragments.quests.details.FirebaseMethods.serializeDetails;

@SuppressLint("Registered")
@EActivity
@OptionsMenu(R.menu.activity_add_place_actions)
public class AddDetailsActivity extends AppCompatActivity implements FirebaseMethods.FirestoreListener {
    private static final String TAG = AddDetailsActivity.class.getSimpleName();

    // Arguments
    public enum ArgKeys {
        PLACE_ID("placeId");

        private final String val;

        ArgKeys(String val) {
            this.val = val;
        }

        @Override
        final public String toString() {
            return val;
        }
    }

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

    @AfterViews
    void afterViews() {
        // View pager
        pagerAdapter = new PlaceDetailsAddPagerAdapter(
                getSupportFragmentManager(),
                placeId,
                this
        );
        viewPager.setAdapter(pagerAdapter);

        // Tab layout
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
    }

    @OptionsItem(android.R.id.home)
    void homeSelected() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @OptionsItem(R.id.add_details_action_send)
    void sendSelected() {
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
