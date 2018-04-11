package ru.coolone.travelquest.ui.fragments.quests.details.items;

import android.support.v7.widget.RecyclerView;

/**
 * Created by coolone on 15.11.17.
 */

public class QuestDetailsItemRecycler extends BaseQuestDetailsItem {
    public QuestDetailsItemRecycler(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    private RecyclerView recyclerView;

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public int getListItemId() {
        return Id.ITEM_RECYCLER.ordinal();
    }
}
