package ru.coolone.travelquest.ui.adapters;

import android.view.View;

import com.afollestad.sectionedrecyclerview.SectionedViewHolder;

/**
 * Created by radiationx on 14.09.17.
 */

public class BaseSectionedViewHolder<T>
        extends SectionedViewHolder
        implements View.OnClickListener, View.OnLongClickListener {

    static final String TAG = BaseSectionedViewHolder.class.getSimpleName();

    public BaseSectionedViewHolder(View v) {
        super(v);

        // Handle clicks
        v.setOnClickListener(this);
        v.setOnLongClickListener(this);
    }

    public void bind(T item, int section, int relativePosition, int absolutePosition) {
    }

    public void bind(T item, int section) {
    }

    public void bind(T item) {
    }

    public void bind(int section) {
    }

    public void bind() {
    }

    @Override
    public void onClick(View view) {
    }

    @Override
    public boolean onLongClick(View view) {
        return false;
    }
}
