package ru.coolone.travelquest.ui.fragments.quests.details.add;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import ru.coolone.travelquest.ui.activities.MainActivity.SupportLang;

/**
 * @author coolone
 * @since 30.03.18
 */
public class QuestDetailsAddPagerAdapter extends FragmentPagerAdapter {
    private QuestDetailsAddFragment tabFragments[] =
            new QuestDetailsAddFragment[SupportLang.values().length];
    private Context context;

    public QuestDetailsAddPagerAdapter(FragmentManager fm, String placeId, Context context) {
        super(fm);
        this.context = context;

        for (int mTabFragmentId = 0; mTabFragmentId < tabFragments.length; mTabFragmentId++) {
            tabFragments[mTabFragmentId] = QuestDetailsAddFragment_.builder()
                    .lang(SupportLang.values()[mTabFragmentId])
                    .placeId(placeId)
                    .build();
        }
    }

    @Override
    public int getCount() {
        return tabFragments.length;
    }

    @Override
    public Fragment getItem(int position) {
        return tabFragments[position];
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return context.getString(SupportLang.values()[position].titleId);
    }
}
