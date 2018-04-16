package ru.coolone.travelquest.ui.fragments.quests.details.items;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by coolone on 14.11.17.
 */

@AllArgsConstructor
@NoArgsConstructor
public class QuestDetailsItemText extends BaseQuestDetailsItem {
    @Getter
    @Setter
    private String text;

    @Override
    public int getListItemId() {
        return Id.ITEM_TEXT.ordinal();
    }
}
