package ru.coolone.travelquest.ui.fragments.quests.details.add;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import ru.coolone.travelquest.R;

/**
 * @author coolone
 * @since 30.03.18
 */
public class QuestDetailsAddPagerAdapter extends FragmentPagerAdapter {
    private String tabTitles[];
    private Context context;

    public QuestDetailsAddPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
        tabTitles = new String[] {
                context.getString(R.string.add_place_tab_english_title),
                context.getString(R.string.add_place_tab_russian_title)
        };
    }

    @Override public int getCount() {
        return 2;
    }

    @Override public Fragment getItem(int position) {
        return QuestDetailsAddFragment.newInstance(position + 1);
    }

    @Override public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}