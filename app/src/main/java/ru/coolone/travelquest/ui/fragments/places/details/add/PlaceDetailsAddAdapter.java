package ru.coolone.travelquest.ui.fragments.places.details.add;

import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.adapters.BaseSectionedAdapter;
import ru.coolone.travelquest.ui.adapters.BaseSectionedHeader;
import ru.coolone.travelquest.ui.adapters.BaseSectionedViewHolder;
import ru.coolone.travelquest.ui.fragments.places.details.PlaceDetailsAdapter.ListItem;
import ru.coolone.travelquest.ui.fragments.places.details.items.BaseQuestDetailsItem;
import ru.coolone.travelquest.ui.fragments.places.details.items.QuestDetailsItemRecycler;
import ru.coolone.travelquest.ui.fragments.places.details.items.QuestDetailsItemText;

import static ru.coolone.travelquest.ui.fragments.places.details.FirebaseMethods.initDetailsRecyclerView;

/**
 * @author coolone
 * @since 06.04.18
 */
@RequiredArgsConstructor
public class PlaceDetailsAddAdapter extends BaseSectionedAdapter<
        BaseSectionedHeader, BaseSectionedViewHolder,
        BaseQuestDetailsItem, BaseSectionedViewHolder> {
    private static final String TAG = PlaceDetailsAddFragment.class.getSimpleName();

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    // Context
    private final Context context;

    @Setter
    private OnClickListener<BaseQuestDetailsItem, BaseSectionedViewHolder> itemClickListener;

    @Setter
    private OnClickListener<BaseSectionedHeader, PlaceDetailsAddAdapter.HeaderHolder> headerClickListener;

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

    public class HeaderHolder
            extends BaseSectionedViewHolder<BaseSectionedHeader>
            implements View.OnClickListener, View.OnLongClickListener, Serializable {
        EditText title;
        ImageButton buttonAdd;
        ImageButton buttonRemove;

        public HeaderHolder(View v) {
            super(v, itemClickListener, PlaceDetailsAddAdapter.this);
            title = v.findViewById(R.id.add_details_head_text);
            buttonAdd = v.findViewById(R.id.add_details_head_add);
            buttonRemove = v.findViewById(R.id.add_details_head_remove);

            title.addTextChangedListener(
                    new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            getHeader(getRelativePosition().section())
                                    .setTitle(s.toString());
                        }
                    }
            );

            buttonAdd.setOnClickListener(
                    v1 -> {
                        val builder = new AlertDialog.Builder(context);
                        builder.setTitle(context.getString(R.string.add_details_add_dialog_title));

                        builder.setItems(
                                new String[]{
                                        context.getString(R.string.add_details_add_dialog_section),
                                        context.getString(R.string.add_details_add_dialog_text)
                                }, (dialog, item) -> {
                                    final Pair<BaseSectionedHeader, ArrayList<BaseQuestDetailsItem>> section =
                                            getSection(getRelativePosition().section());

                                    BaseQuestDetailsItem detailsItem = null;

                                    switch (item) {
                                        case 0:
                                            RecyclerView recycler = new RecyclerView(context);
                                            initDetailsRecyclerView(
                                                    recycler,
                                                    PlaceDetailsAddAdapter.class,
                                                    context
                                            );
                                            ((BaseSectionedAdapter) recycler.getAdapter())
                                                    .addSection(
                                                            new Pair<>(
                                                                    new BaseSectionedHeader(),
                                                                    new ArrayList()
                                                            )
                                                    );

                                            detailsItem = new QuestDetailsItemRecycler((BaseSectionedAdapter) recycler.getAdapter());
                                            break;
                                        case 1:
                                            detailsItem = new QuestDetailsItemText();
                                            break;
                                    }

                                    section.second.add(detailsItem);
                                    notifyDataSetChanged();
                                }
                        );
                        builder.setCancelable(true);
                        builder.show();
                    }
            );

            buttonRemove.setOnClickListener(
                    v12 -> {
                        removeSection(getRelativePosition().section());
                        notifyDataSetChanged();
                    }
            );
        }

        @Override
        public void bind(int section) {
            title.setText(sections.get(section).first.getTitle());
        }
    }

    public class ItemHolderText
            extends BaseSectionedViewHolder<QuestDetailsItemText>
            implements Serializable {
        EditText text;
        ImageButton buttonRemove;

        ItemHolderText(View v) {
            super(v, itemClickListener, PlaceDetailsAddAdapter.this);

            text = v.findViewById(R.id.add_details_item_text);
            buttonRemove = v.findViewById(R.id.add_details_item_text_remove);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                text.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
            }
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
                            ((QuestDetailsItemText) getItem(getLayoutPosition()))
                                    .setText(s.toString());
                        }
                    }
            );

            buttonRemove.setOnClickListener(
                    v1 -> {
                        getSection(getRelativePosition().section())
                                .second
                                .remove(getRelativePosition().relativePos());
                        notifyDataSetChanged();
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
            super(v, itemClickListener, PlaceDetailsAddAdapter.this);

            this.recyclerView = v.findViewById(R.id.add_details_item_recycler);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            Log.d(TAG, "Item recycler holder created:\n\tsize: "
                    + String.valueOf(recyclerView.getChildCount())
                    + "\n\titem view:" + String.valueOf(v)
                    + "\n\trecyclerAdapter" + String.valueOf(recyclerView.getAdapter()));
        }

        @Override
        public void bind(QuestDetailsItemRecycler recycler) {
            Log.d(TAG, "item recycler binded!\n\tItem: "
                    + String.valueOf(recycler));
            recyclerView.setAdapter(recycler.getRecyclerAdapter());
            recyclerView.invalidate();
        }
    }
}