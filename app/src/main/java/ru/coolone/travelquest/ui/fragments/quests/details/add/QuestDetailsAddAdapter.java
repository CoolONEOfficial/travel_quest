package ru.coolone.travelquest.ui.fragments.quests.details.add;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.adapters.BaseSectionedAdapter;
import ru.coolone.travelquest.ui.adapters.BaseSectionedHeader;
import ru.coolone.travelquest.ui.adapters.BaseSectionedViewHolder;
import ru.coolone.travelquest.ui.fragments.quests.details.items.BaseQuestDetailsItem;
import ru.coolone.travelquest.ui.fragments.quests.details.items.QuestDetailsItemRecycler;
import ru.coolone.travelquest.ui.fragments.quests.details.items.QuestDetailsItemText;

/**
 * @author coolone
 * @since 06.04.18
 */
public class QuestDetailsAddAdapter extends BaseSectionedAdapter<
        BaseSectionedHeader, BaseSectionedViewHolder,
        BaseQuestDetailsItem, BaseSectionedViewHolder> {

    private static final String TAG = QuestDetailsAddFragment.class.getSimpleName();
    private OnClickListener<BaseQuestDetailsItem, BaseSectionedViewHolder> itemClickListener;
    private OnClickListener<BaseSectionedHeader, QuestDetailsAddAdapter.HeaderHolder> headerClickListener;

    @Override
    public BaseSectionedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "Vh type: " + String.valueOf(viewType));

        // Header
        if (viewType == ListItem.Id.HEADER_TEXT.ordinal())
            return new HeaderHolder(inflateLayout(parent, R.layout.add_details_header));

            // Item
        else if (viewType == ListItem.Id.ITEM_TEXT.ordinal())
            return new ItemHolderText(inflateLayout(parent, R.layout.add_details_item_text));
        else if (viewType == ListItem.Id.ITEM_RECYCLER.ordinal())
            return new ItemHolderRecycler(inflateLayout(parent, R.layout.add_details_item_recycler), parent.getContext());
        else if (viewType == VIEW_TYPE_HEADER)
            return new HeaderHolder(inflateLayout(parent, R.layout.add_details_header));

        Log.e(TAG, "Create vh wrong type: " + String.valueOf(viewType));
        return null;
    }

    @Override
    public void onBindHeaderViewHolder(
            BaseSectionedViewHolder holder,
            int section,
            boolean expanded
    ) {
        holder.bind(section);
        // Set caret image
        ((HeaderHolder) holder).caret.setImageResource(
                expanded
                        ? R.drawable.ic_arrow_up
                        : R.drawable.ic_arrow_down
        );
    }

    @Override
    public void onBindViewHolder(
            BaseSectionedViewHolder holder,
            int section,
            int relPos,
            int absPos
    ) {
        ((BaseSectionedViewHolder<BaseQuestDetailsItem>) holder).bind(getItem(section, relPos));
    }

    public void setHeaderClickListener(OnClickListener<BaseSectionedHeader, HeaderHolder> headerClickListener) {
        this.headerClickListener = headerClickListener;
    }

    public void setItemClickListener(OnClickListener<BaseQuestDetailsItem, BaseSectionedViewHolder> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public int getItemViewType(int section, int relativePosition, int absolutePosition) {
        return getItem(section, relativePosition).getListItemId();
    }

    public interface ListItem {
        int getListItemId();

        enum Id {
            HEADER_TEXT,
            ITEM_TEXT,
            ITEM_RECYCLER
        }
    }

    public class HeaderHolder
            extends BaseSectionedViewHolder<BaseSectionedHeader>
            implements View.OnClickListener, View.OnLongClickListener {
        ImageView caret;
        TextView title;

        public HeaderHolder(View v) {
            super(v);
            title = v.findViewById(R.id.add_details_head_text);
            caret = v.findViewById(R.id.add_details_head_caret);

            // Handle clicks
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void bind(int section) {
            title.setText(sections.get(section).first.getTitle());
        }

        @Override
        public void onClick(View view) {
            if (headerClickListener != null) {
                int section = getRelativePosition().section();
                BaseSectionedHeader header = getHeader(section);
                if (header != null) {
                    headerClickListener.onClick(header, this, section);
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (headerClickListener != null) {
                int section = getRelativePosition().section();
                BaseSectionedHeader header = getHeader(section);
                if (header != null) {
                    headerClickListener.onClick(header, this, section);
                }
                return true;
            }
            return false;
        }
    }

    public class ItemHolderText
            extends BaseSectionedViewHolder<QuestDetailsItemText> {
        TextView text;

        ItemHolderText(View v) {
            super(v);

            this.text = v.findViewById(R.id.add_details_item_text);

            // Handle clicks
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);

            Log.d(TAG, "Item text holder created:" + this.text.getText());
        }

        @Override
        public void bind(QuestDetailsItemText item) {
            text.setText(item.getText());
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                int section = getRelativePosition().section();
                BaseQuestDetailsItem item = getItem(getLayoutPosition());
                if (item != null) {
                    itemClickListener.onClick(item, this, section);
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (itemClickListener != null) {
                int section = getRelativePosition().section();
                BaseQuestDetailsItem item = getItem(getLayoutPosition());
                if (item != null) {
                    itemClickListener.onLongClick(item, this, section);
                }
                return true;
            }
            return false;
        }
    }

    public class ItemHolderRecycler
            extends BaseSectionedViewHolder<QuestDetailsItemRecycler> {
        RecyclerView recyclerView;

        ItemHolderRecycler(View v, Context context) {
            super(v);

            this.recyclerView = v.findViewById(R.id.add_details_item_recycler);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            // Handle clicks
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);

            Log.d(TAG, "Item recycler holder created:\n\tsize: "
                    + String.valueOf(recyclerView.getChildCount())
                    + "\n\titem view:" + String.valueOf(v)
                    + "\n\tadapter" + String.valueOf(recyclerView.getAdapter()));
        }

        @Override
        public void bind(QuestDetailsItemRecycler recycler) {
            Log.d(TAG, "item recycler binded!\n\tItem: "
                    + String.valueOf(recycler));
            recyclerView.setAdapter(recycler.getRecyclerView().getAdapter());
            recyclerView.invalidate();
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                int section = getRelativePosition().section();
                BaseQuestDetailsItem item = getItem(getLayoutPosition());
                if (item != null) {
                    itemClickListener.onClick(item, this, section);
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (itemClickListener != null) {
                int section = getRelativePosition().section();
                BaseQuestDetailsItem item = getItem(getLayoutPosition());
                if (item != null) {
                    itemClickListener.onLongClick(item, this, section);
                }
                return true;
            }
            return false;
        }
    }
}