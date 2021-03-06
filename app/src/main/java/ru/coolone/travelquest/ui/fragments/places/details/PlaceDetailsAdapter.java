package ru.coolone.travelquest.ui.fragments.places.details;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.fragments.places.details.adapters.BaseSectionedAdapter;
import ru.coolone.travelquest.ui.fragments.places.details.adapters.BaseSectionedHeader;
import ru.coolone.travelquest.ui.fragments.places.details.adapters.BaseSectionedViewHolder;
import ru.coolone.travelquest.ui.fragments.places.details.items.BaseQuestDetailsItem;
import ru.coolone.travelquest.ui.fragments.places.details.items.QuestDetailsItemRecycler;
import ru.coolone.travelquest.ui.fragments.places.details.items.QuestDetailsItemText;

@RequiredArgsConstructor
public class PlaceDetailsAdapter
        extends BaseSectionedAdapter<
        BaseSectionedHeader, BaseSectionedViewHolder,
        BaseQuestDetailsItem, BaseSectionedViewHolder> {
    private static final String TAG = PlaceDetailsAdapter.class.getSimpleName();
    @Setter
    protected OnClickListener<BaseQuestDetailsItem, BaseSectionedViewHolder> itemClickListener;

    // Context
    public final Activity activity;

    @Setter
    protected OnClickListener<BaseSectionedHeader, PlaceDetailsAdapter.HeaderHolder> headerClickListener;

    @Override
    public BaseSectionedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "Vh type: " + String.valueOf(viewType));

        // Header
        if (viewType == ListItem.Id.HEADER_TEXT.ordinal())
            return new HeaderHolder(inflateLayout(parent, R.layout.details_header));

            // Item
        else if (viewType == ListItem.Id.ITEM_TEXT.ordinal())
            return new ItemHolderText(inflateLayout(parent, R.layout.details_item_text));
        else if (viewType == ListItem.Id.ITEM_RECYCLER.ordinal())
            return new ItemHolderRecycler(inflateLayout(parent, R.layout.details_item_recycler), parent.getContext());
        else if (viewType == VIEW_TYPE_HEADER)
            return new HeaderHolder(inflateLayout(parent, R.layout.details_header));

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

    @Override
    public int getItemViewType(int section, int relativePosition, int absolutePosition) {
        return getItem(section, relativePosition).getListItemId();
    }

    public interface ListItem extends Serializable {
        int getListItemId();

        enum Id {
            HEADER_TEXT,
            ITEM_TEXT,
            ITEM_RECYCLER
        }
    }

    class HeaderHolder
            extends BaseSectionedViewHolder<BaseSectionedHeader>
            implements View.OnClickListener, View.OnLongClickListener, Serializable {
        ImageView caret;
        TextView title;

        public HeaderHolder(View v) {
            super(v, headerClickListener, PlaceDetailsAdapter.this);
            title = v.findViewById(R.id.details_head_text);
            title.setSelected(true);
            caret = v.findViewById(R.id.details_head_caret);

            caret.setOnClickListener(this);
            caret.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            toggleSectionExpanded(getRelativePosition().section());
            super.onClick(view);
        }

        @Override
        public void bind(int section) {
            title.setText(sections.get(section).first.getTitle());
        }
    }

    public class ItemHolderText
            extends BaseSectionedViewHolder<QuestDetailsItemText>
            implements Serializable {
        TextView text;

        ItemHolderText(View v) {
            super(v, itemClickListener, PlaceDetailsAdapter.this);

            this.text = v.findViewById(R.id.details_item_text);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                text.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
            text.setMovementMethod(LinkMovementMethod.getInstance());
            text.addTextChangedListener(
                    new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (s.charAt(0) != '\t')
                                s.insert(0, "\t");
                        }
                    }
            );

            Log.d(TAG, "Item text holder created:" + this.text.getText());
        }

        @Override
        public void bind(QuestDetailsItemText item) {
            text.setText(item.getText());
        }
    }

    public class ItemHolderRecycler
            extends BaseSectionedViewHolder<QuestDetailsItemRecycler>
            implements Serializable {

        RecyclerView recyclerView;

        ItemHolderRecycler(View v, Context context) {
            super(v, itemClickListener, PlaceDetailsAdapter.this);

            this.recyclerView = v.findViewById(R.id.details_item_recycler);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            Log.d(TAG, "Item recyclerAdapter holder created:\n\tsize: "
                    + String.valueOf(recyclerView.getChildCount())
                    + "\n\titem view:" + String.valueOf(v)
                    + "\n\tadapter" + String.valueOf(recyclerView.getAdapter()));
        }

        @Override
        public void bind(QuestDetailsItemRecycler recycler) {
            Log.d(TAG, "item recyclerAdapter binded!\n\tItem: "
                    + String.valueOf(recycler));
            recyclerView.setAdapter(recycler.getRecyclerAdapter());
            recyclerView.invalidate();
        }
    }
}
