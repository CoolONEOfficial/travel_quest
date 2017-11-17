package ru.coolone.travelquest.ui.fragments.quests.details.items;

/**
 * Created by coolone on 14.11.17.
 */

public class QuestDetailsItemText extends BaseQuestDetailsItem {
    private String mText;

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
    }

    @Override
    public int getListItemId() {
        return Id.ITEM_TEXT.ordinal();
    }
}
