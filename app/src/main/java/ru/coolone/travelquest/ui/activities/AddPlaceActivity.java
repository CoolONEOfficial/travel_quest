package ru.coolone.travelquest.ui.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.fragments.quests.details.add.QuestDetailsAddPagerAdapter;

public class AddPlaceActivity extends AppCompatActivity {
    private static final String TAG = AddPlaceActivity.class.getSimpleName();

    // Google map place id
    int placeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        ViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(
                new QuestDetailsAddPagerAdapter(
                        getSupportFragmentManager(),
                        this
                )
        );

        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        placeId = getIntent().getIntExtra("placeId", -1);
        if(placeId == -1)
            Log.e(TAG, "Place id not found!");
    }
}
