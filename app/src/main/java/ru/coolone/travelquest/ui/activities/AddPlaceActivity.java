package ru.coolone.travelquest.ui.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.adapters.BaseSectionedAdapter;
import ru.coolone.travelquest.ui.adapters.BaseSectionedHeader;
import ru.coolone.travelquest.ui.fragments.quests.details.QuestDetailsAdapter;
import ru.coolone.travelquest.ui.fragments.quests.details.add.QuestDetailsAddAdapter;
import ru.coolone.travelquest.ui.fragments.quests.details.add.QuestDetailsAddPagerAdapter;

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
    int placeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        placeId = getIntent().getIntExtra(ArgKeys.PLACE_ID.toString(), -1);
        if(placeId == -1)
            Log.e(TAG, "Place id not found!");

        ViewPager viewPager = findViewById(R.id.add_details_viewpager);
        viewPager.setAdapter(
                new QuestDetailsAddPagerAdapter(
                        getSupportFragmentManager(),
                        this
                )
        );

        TabLayout tabLayout = findViewById(R.id.add_details_sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }
}