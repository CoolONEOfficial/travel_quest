package ru.coolone.travelquest.ui.activities;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.firestore.FirebaseFirestore;

import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.fragments.quests.details.add.QuestDetailsAddFragment;
import ru.coolone.travelquest.ui.fragments.quests.details.add.QuestDetailsAddPagerAdapter;

import static ru.coolone.travelquest.ui.fragments.quests.details.FirebaseMethods.serializeDetails;

public class AddPlaceActivity extends AppCompatActivity {
    private static final String TAG = AddPlaceActivity.class.getSimpleName();

    // Arguments
    public enum ArgKeys {
        PLACE_ID("place_id");

        private final String val;

        ArgKeys(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return val;
        }
    }

    // Google map place id
    String placeId;

    QuestDetailsAddPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        placeId = getIntent().getStringExtra(ArgKeys.PLACE_ID.toString());
        if (placeId == null)
            Log.e(TAG, "Place id not found!");

        ViewPager viewPager = findViewById(R.id.add_details_viewpager);
        pagerAdapter = new QuestDetailsAddPagerAdapter(
                getSupportFragmentManager(),
                placeId,
                this
        );
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = findViewById(R.id.add_details_sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.add_details_action_send:
                applyDetails();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_add_place_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void applyDetails() {
        FirebaseFirestore db = FirebaseFirestore
                .getInstance();

        for (int mFragId = 0; mFragId < pagerAdapter.getCount(); mFragId++) {
            QuestDetailsAddFragment mFrag = (QuestDetailsAddFragment) pagerAdapter.getItem(mFragId);

            serializeDetails(
                    db
                            .collection(mFrag.lang.lang)
                            .document("quests")
                            .collection(placeId),
                    mFrag.recycler
            );
        }
    }
}
