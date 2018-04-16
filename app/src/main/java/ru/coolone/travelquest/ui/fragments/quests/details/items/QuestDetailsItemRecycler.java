package ru.coolone.travelquest.ui.fragments.quests.details.items;

import android.support.v7.widget.RecyclerView;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Created by coolone on 15.11.17.
 */

@RequiredArgsConstructor
public class QuestDetailsItemRecycler extends BaseQuestDetailsItem {
    @Getter
    @Setter
    final private RecyclerView recyclerView;

    @Override
    public int getListItemId() {
        return Id.ITEM_RECYCLER.ordinal();
    }
}
