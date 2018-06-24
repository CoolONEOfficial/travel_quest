package ru.coolone.travelquest.ui.fragments.places.details.add;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.Layout;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.fragments.places.details.PlaceDetailsAdapter.ListItem;
import ru.coolone.travelquest.ui.fragments.places.details.adapters.BaseSectionedAdapter;
import ru.coolone.travelquest.ui.fragments.places.details.adapters.BaseSectionedHeader;
import ru.coolone.travelquest.ui.fragments.places.details.adapters.BaseSectionedViewHolder;
import ru.coolone.travelquest.ui.fragments.places.details.items.BaseQuestDetailsItem;
import ru.coolone.travelquest.ui.fragments.places.details.items.QuestDetailsItemRecycler;
import ru.coolone.travelquest.ui.fragments.places.details.items.QuestDetailsItemText;

import static android.webkit.URLUtil.isValidUrl;
import static ru.coolone.travelquest.ui.fragments.places.details.FirebaseMethods.initDetailsRecyclerView;

/**
 * @author coolone
 * @since 06.04.18
 */
@RequiredArgsConstructor
public class PlaceDetailsAddAdapter extends BaseSectionedAdapter<
        BaseSectionedHeader, BaseSectionedViewHolder,
        BaseQuestDetailsItem, BaseSectionedViewHolder> {
    private static final String TAG = PlaceDetailsAddFrag.class.getSimpleName();

    public static int PLACE_PICKER_REQUEST = 1;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    // Context
    public final Activity activity;

    @Setter
    private OnClickListener<BaseQuestDetailsItem, BaseSectionedViewHolder> itemClickListener;

    @Override
    public BaseSectionedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "Vh type: " + String.valueOf(viewType));

        if (viewType == ListItem.Id.HEADER_TEXT.ordinal())
            return new HeaderHolder(inflateLayout(parent, R.layout.add_details_header));
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

    static String unescapeHtml4(String str) {
        val jsoup = Jsoup.clean(str,
                new Whitelist()
                        .addTags("a")

                        .addAttributes("a", "href")

                        .addProtocols("a", "href", "ftp", "http", "https", "mailto")
        ).replace("\n", "");

        return jsoup;
    }

    interface PlaceSelectedListener {
        void onPlaceSelected(Place place);
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
                        val builder = new AlertDialog.Builder(activity);
                        builder.setTitle(activity.getString(R.string.add_details_add_dialog_title));

                        builder.setItems(
                                new String[]{
                                        activity.getString(R.string.add_details_add_dialog_section),
                                        activity.getString(R.string.add_details_add_dialog_text)
                                }, (dialog, item) -> {
                                    final Pair<BaseSectionedHeader, ArrayList<BaseQuestDetailsItem>> section =
                                            getSection(getRelativePosition().section());

                                    BaseQuestDetailsItem detailsItem = null;

                                    switch (item) {
                                        case 0:
                                            RecyclerView recycler = new RecyclerView(activity);
                                            initDetailsRecyclerView(
                                                    recycler,
                                                    PlaceDetailsAddAdapter.class,
                                                    activity
                                            );
                                            val adapter = ((PlaceDetailsAddAdapter) recycler.getAdapter());
                                            adapter.addSection(
                                                    new Pair<>(
                                                            new BaseSectionedHeader(),
                                                            new ArrayList<>()
                                                    )
                                            );
                                            adapter.setListener(listener);

                                            detailsItem = new QuestDetailsItemRecycler((BaseSectionedAdapter) recycler.getAdapter());
                                            break;
                                        case 1:
                                            detailsItem = new QuestDetailsItemText() {{
                                                userEditable = true;
                                            }};
                                            break;
                                    }

                                    section.second.add(detailsItem);
                                    notifyDataSetChanged();
                                    onSectionsChanged();
                                }
                        );
                        builder.setCancelable(true);
                        builder.show();
                    }
            );

            buttonRemove.setOnClickListener(
                    v12 -> {
                        val itemCount = getItemCount(getRelativePosition().section());
                        if (itemCount <= 0) {
                            removeSection(getRelativePosition().section());
                            notifyDataSetChanged();
                            onSectionsChanged();
                        } else new AlertDialog.Builder(activity)
                                .setTitle(R.string.add_details_remove_section_dialog_title)
                                .setMessage(
                                        activity.getString(R.string.add_details_remove_section_dialog_text)
                                                .replace("X", Integer.toString(itemCount))
                                )
                                .setPositiveButton(
                                        android.R.string.ok,
                                        (dialog, which) -> {
                                            removeSection(getRelativePosition().section());
                                            notifyDataSetChanged();
                                            onSectionsChanged();
                                        }
                                )
                                .setNegativeButton(
                                        android.R.string.cancel,
                                        (dialog, which) -> dialog.dismiss()
                                )
                                .create().show();
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

        PlaceSelectedListener placeSelectedListener;

        ItemHolderText(View v) {
            super(v, itemClickListener, PlaceDetailsAddAdapter.this);

            text = v.findViewById(R.id.add_details_item_text);
            buttonRemove = v.findViewById(R.id.add_details_item_text_remove);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                text.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
            }
            text.setCustomSelectionActionModeCallback(
                    new android.view.ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
                            MenuInflater inflater = mode.getMenuInflater();
                            inflater.inflate(R.menu.activity_add_details_text_select_actions, menu);

                            return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
                            return false;
                        }

                        @Override
                        @SneakyThrows
                        public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
                            val textHtml = unescapeHtml4(Html.toHtml(text.getText()));
                            val textSpan = text.getText();

                            val startIndexSpan = text.getSelectionStart();
                            val endIndexSpan = text.getSelectionEnd();

                            val token = text.getText().subSequence(startIndexSpan, endIndexSpan);

                            int tokenIndexId = 0;
                            int mIndex = 0;
                            while (mIndex < startIndexSpan) {
                                mIndex = textSpan.toString().indexOf(token.toString(), mIndex);
                                tokenIndexId++;
                            }
                            if (tokenIndexId == 0)
                                tokenIndexId = 1;

                            int mStartIndexHtml = 0;
                            for (int mNextIndexId = 0; mNextIndexId < tokenIndexId; mNextIndexId++) {
                                mStartIndexHtml = textHtml.indexOf(token.toString(), mStartIndexHtml);
                            }
                            val startIndexHtml = mStartIndexHtml;
                            val endIndexHtml = startIndexHtml + token.length();

                            val textItem = (QuestDetailsItemText) getItem(getRelativePosition().section(), getRelativePosition().relativePos());
                            val sb = new StringBuilder(textHtml);

                            switch (item.getItemId()) {
                                case R.id.text_link:
                                    val editUri = new EditText(activity);
                                    editUri.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);

                                    new AlertDialog.Builder(activity)
                                            .setTitle(R.string.add_details_text_link_dialog_title)
                                            .setView(editUri)
                                            .setPositiveButton(
                                                    activity.getString(android.R.string.ok),
                                                    (dialog, which) -> {
                                                        if (isValidUrl(editUri.getText().toString())) {

                                                            sb.delete(startIndexHtml, endIndexHtml);
                                                            sb.insert(
                                                                    startIndexHtml,
                                                                    "<a href=\""
                                                                            + editUri.getText()
                                                                            + "\">"
                                                                            + token
                                                                            + "</a>"
                                                            );

                                                            val resultHtml = sb.toString();
                                                            val result = Html.fromHtml(resultHtml);
                                                            text.setText(result);
                                                            textItem.setText(result);
                                                            textItem.setHtml(resultHtml);
                                                        } else Toast.makeText(
                                                                activity,
                                                                R.string.add_details_text_link_invalid,
                                                                Toast.LENGTH_SHORT
                                                        ).show();
                                                    }
                                            )
                                            .setNegativeButton(
                                                    activity.getString(android.R.string.cancel),
                                                    (dialog, which) -> dialog.dismiss()
                                            )
                                            .show();
                                    return true;
                                case R.id.text_place:
                                    placeSelectedListener = place -> {

                                        sb.delete(startIndexHtml, endIndexHtml);
                                        sb.insert(
                                                startIndexHtml,
                                                "<a href=\""
                                                        + new Uri.Builder()
                                                        .scheme("http")
                                                        .authority("www.coolone.ru")
                                                        .appendPath("travelquest")
                                                        .appendQueryParameter("lat", String.valueOf(place.getLatLng().latitude))
                                                        .appendQueryParameter("lng", String.valueOf(place.getLatLng().longitude))
                                                        .build()
                                                        + "\">"
                                                        + token
                                                        + "</a>"
                                        );

                                        val resultHtml = sb.toString();
                                        val result = Html.fromHtml(resultHtml);
                                        text.setText(result);
                                        textItem.setText(result);
                                        textItem.setHtml(resultHtml);
                                    };
                                    activity.startActivityForResult(
                                            new PlacePicker.IntentBuilder()
                                                    .build(activity),
                                            PLACE_PICKER_REQUEST
                                    );
                                    return true;
                            }

                            return false;
                        }

                        @Override
                        public void onDestroyActionMode(android.view.ActionMode mode) {

                        }
                    }
            );
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
                            val item = ((QuestDetailsItemText) getItem(getLayoutPosition()));
                            item.setText(s);
                            item.setHtml(unescapeHtml4(Html.toHtml(s)));
                        }
                    }
            );

            buttonRemove.setOnClickListener(
                    v1 -> {
                        getSection(getRelativePosition().section())
                                .second
                                .remove(getRelativePosition().relativePos());
                        notifyDataSetChanged();
                        onSectionsChanged();
                    }
            );

            Log.d(TAG, "Item text holder created:" + this.text.getText());
        }

        @Override
        public void bind(QuestDetailsItemText item) {
            val itemText = item.getText();

            text.setEnabled(item.userEditable);
            text.setText(
                    itemText != null && !itemText.toString().trim().isEmpty()
                            ? itemText
                            : ""
            );
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