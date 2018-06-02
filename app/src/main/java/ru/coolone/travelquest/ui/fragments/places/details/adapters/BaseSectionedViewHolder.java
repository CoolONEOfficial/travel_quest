package ru.coolone.travelquest.ui.fragments.places.details.adapters;

import android.view.View;

import com.afollestad.sectionedrecyclerview.SectionedViewHolder;

import lombok.val;

/**
 * Created by radiationx on 14.09.17.
 */

public class BaseSectionedViewHolder<T>
        extends SectionedViewHolder
        implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = BaseSectionedViewHolder.class.getSimpleName();

    public BaseSectionedAdapter.OnClickListener onClickListener;
    public BaseSectionedAdapter baseAdapter;

    public BaseSectionedViewHolder(View v) {
        super(v);

        // Handle clicks
        v.setOnClickListener(this);
        v.setOnLongClickListener(this);
    }

    public BaseSectionedViewHolder(
            View v,
            BaseSectionedAdapter.OnClickListener onClickListener,
            BaseSectionedAdapter baseAdapter
    ) {
        super(v);

        // Handle clicks
        v.setOnClickListener(this);
        v.setOnLongClickListener(this);

        this.onClickListener = onClickListener;
        this.baseAdapter = baseAdapter;
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
        if (onClickListener != null) {
            val item = baseAdapter.getItem(getLayoutPosition());
            if (item != null) {
                onClickListener.onClick(item, this,  getRelativePosition().section());
            }
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (onClickListener != null) {
            val item = baseAdapter.getItem(getLayoutPosition());
            if (item != null) {
                onClickListener.onLongClick(item, this, getRelativePosition().section());
            }
            return true;
        }
        return false;
    }
}
