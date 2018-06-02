package ru.coolone.travelquest.ui.fragments.places.details.items;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.coolone.travelquest.ui.fragments.places.details.adapters.BaseSectionedAdapter;

/**
 * Created by coolone on 15.11.17.
 */

@NoArgsConstructor
@RequiredArgsConstructor
public class QuestDetailsItemRecycler extends BaseQuestDetailsItem {

    public QuestDetailsItemRecycler(Parcel parcel) {
        recyclerAdapter = parcel.readParcelable(BaseSectionedAdapter.class.getClassLoader());
    }

    @Getter
    @Setter
    @NonNull
    private BaseSectionedAdapter recyclerAdapter;

    @Override
    public int getListItemId() {
        return Id.ITEM_RECYCLER.ordinal();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(recyclerAdapter, flags);
    }

    public static final Parcelable.Creator<QuestDetailsItemRecycler> CREATOR = new Parcelable.Creator<QuestDetailsItemRecycler>() {

        @Override
        public QuestDetailsItemRecycler createFromParcel(Parcel source) {
            return new QuestDetailsItemRecycler(source);
        }

        @Override
        public QuestDetailsItemRecycler[] newArray(int size) {
            return new QuestDetailsItemRecycler[size];
        }
    };
}
