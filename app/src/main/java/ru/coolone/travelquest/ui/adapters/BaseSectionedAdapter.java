package ru.coolone.travelquest.ui.adapters;

import android.support.annotation.LayoutRes;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

import ru.coolone.travelquest.ui.fragments.quests.details.add.QuestDetailsAddAdapter;

/**
 * Created by radiationx on 14.09.17.
 */

public class BaseSectionedAdapter<
        H extends BaseSectionedHeader, HVH extends BaseSectionedViewHolder,
        I, IVH extends BaseSectionedViewHolder>
        extends SectionedRecyclerViewAdapter<IVH> {
    protected List<Pair<H, List<I>>> sections = new ArrayList<>();

    public void addSection(int section, H header, List<I> items) {
        sections.add(section, new Pair<>(header, items));
    }

    public void addSection(H header, List<I> items) {
        sections.add(new Pair<>(header, items));
    }

    public void addSection(Pair<H, List<I>> item) {
        sections.add(item);
    }

    public void clear() {
        for (Pair<H, List<I>> section : sections)
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

    public Pair<H, List<I>> getSection(int section) {
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
}
