package ru.coolone.travelquest.ui.fragments.places.details.items;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Created by coolone on 14.11.17.
 */

@RequiredArgsConstructor
@NoArgsConstructor
public class QuestDetailsItemText extends BaseQuestDetailsItem {
    public boolean userEditable = false;
    @Getter
    @Setter
    @NonNull
    Spanned text;
    @Getter
    @Setter
    @NonNull
    String html;

    public QuestDetailsItemText(Parcel parcel) {
        text = (Spanned) parcel.readSerializable();
    }

    @Override
    public int getListItemId() {
        return Id.ITEM_TEXT.ordinal();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable((Serializable) text);
    }

    public static final Parcelable.Creator<QuestDetailsItemText> CREATOR = new Parcelable.Creator<QuestDetailsItemText>() {

        @Override
        public QuestDetailsItemText createFromParcel(Parcel source) {
            return new QuestDetailsItemText(source);
        }

        @Override
        public QuestDetailsItemText[] newArray(int size) {
            return new QuestDetailsItemText[size];
        }
    };
}
