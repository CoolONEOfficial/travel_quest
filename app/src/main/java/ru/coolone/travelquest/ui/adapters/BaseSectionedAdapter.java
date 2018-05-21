package ru.coolone.travelquest.ui.adapters;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.LayoutRes;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import ru.coolone.travelquest.ui.fragments.places.details.add.PlaceDetailsAddAdapter;
import ru.coolone.travelquest.ui.fragments.places.details.items.QuestDetailsItemRecycler;

/**
 * Created by radiationx on 14.09.17.
 */

@NoArgsConstructor
public class BaseSectionedAdapter<
        H extends BaseSectionedHeader, HVH extends BaseSectionedViewHolder,
        I extends Parcelable, IVH extends BaseSectionedViewHolder>
        extends SectionedRecyclerViewAdapter<IVH>
        implements Parcelable {
    public BaseSectionedAdapter(Parcel parcel) {
        for (int mSectionId = 0; mSectionId < parcel.readInt(); mSectionId++) {
            sections.add(
                    new Pair<>(
                            (H) parcel.readSerializable(),
                            new ArrayList<>(Arrays.asList(
                                    (I[]) parcel.readParcelableArray(I.ClassLoaderCreator.class.getClassLoader())
                            ))
                    )
            );
        }
    }

    @Getter
    protected ArrayList<Pair<H, ArrayList<I>>> sections = new ArrayList<>();

    @Getter
    protected Listener listener;

    public void setSections(
            ArrayList<Pair<H, ArrayList<I>>> sections,
            Activity activity
    ) {
        this.sections = sections;
        activity.runOnUiThread(
                () -> notifyDataSetChanged()
        );
    }

    public void setSections(ArrayList<Pair<H, ArrayList<I>>> sections) {
        this.sections = sections;
        notifyDataSetChanged();
    }

    public void addSection(Pair<H, ArrayList<I>> item) {
        addSection(sections.size(), item);
    }

    public void addSection(int section, Pair<H, ArrayList<I>> item) {
        sections.add(section, item);
        notifyDataSetChanged();
    }

    public void removeSection(int section) {
        sections.remove(section);
        notifyDataSetChanged();
    }

    public void clear() {
        for (Pair<H, ArrayList<I>> section : sections)
            section.second.clear();
        sections.clear();
    }

    public int[] getItemPosition(int layPos) {
        int result[] = new int[]{-1, -1};
        int sumPrevSections = 0;
        for (int i = 0; i < getSectionCount(); i++) {
            result[0] = i;
            result[1] = layPos - i - sumPrevSections - 1;
            sumPrevSections += getItemCount(i);
            if (sumPrevSections + i >= layPos) break;
        }
        if (result[1] < 0) {
            result[0] = -1;
            result[1] = -1;
        }
        return result;
    }

    public I getItem(int layPos) {
        int position[] = getItemPosition(layPos);
        if (position[0] == -1) {
            return null;
        }
        return sections.get(position[0]).second.get(position[1]);
    }

    public List<I> getItems(int section) {
        return getSection(section).second;
    }

    public I getItem(int section, int relativePosition) {
        return getItems(section).get(relativePosition);
    }

    public Pair<H, ArrayList<I>> getSection(int section) {
        return sections.get(section);
    }

    public H getHeader(int section) {
        return getSection(section).first;
    }

    @Override
    public int getSectionCount() {
        return sections.size();
    }

    @Override
    public int getItemCount(int section) {
        return sections.get(section).second.size();
    }

    @Override
    public void onBindHeaderViewHolder(IVH IVH, int i, boolean b) {
    }

    @Override
    public void onBindFooterViewHolder(IVH IVH, int i) {
    }

    @Override
    public void onBindViewHolder(IVH IVH, int i, int i1, int i2) {
    }

    @Override
    public IVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    protected View inflateLayout(ViewGroup parent, @LayoutRes int id) {
        return LayoutInflater.from(parent.getContext()).inflate(id, parent, false);
    }

    public interface OnClickListener<ME, MVH> {
        void onClick(ME i, MVH i2, int section);

        boolean onLongClick(ME i, MVH i2, int section);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(sections.size());
        for (val mSection : sections) {
            dest.writeSerializable(mSection.first);
            dest.writeArray(mSection.second.toArray());
        }
    }

    public static final Parcelable.Creator<BaseSectionedAdapter> CREATOR = new Parcelable.Creator<BaseSectionedAdapter>() {

        @Override
        public BaseSectionedAdapter createFromParcel(Parcel source) {
            return new BaseSectionedAdapter(source);
        }

        @Override
        public BaseSectionedAdapter[] newArray(int size) {
            return new BaseSectionedAdapter[size];
        }
    };

    public void setListener(Listener listener) {
        this.listener = listener;

        for (val mSection : sections) {
            for (val mItem : mSection.second) {
                if (mItem instanceof QuestDetailsItemRecycler)
                    (
                            (PlaceDetailsAddAdapter) (
                                    (QuestDetailsItemRecycler) mItem
                            ).getRecyclerAdapter()
                    ).setListener(listener);
            }
        }
    }

    protected void onSectionsChanged() {
        if (listener != null)
            listener.sectionsChanged();
    }

    public interface Listener {
        void sectionsChanged();
    }
}
