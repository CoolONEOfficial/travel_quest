package ru.coolone.travelquest.ui.fragments.quests.details;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import lombok.val;
import ru.coolone.travelquest.ui.adapters.BaseSectionedAdapter;
import ru.coolone.travelquest.ui.adapters.BaseSectionedHeader;
import ru.coolone.travelquest.ui.fragments.quests.details.add.QuestDetailsAddAdapter;
import ru.coolone.travelquest.ui.fragments.quests.details.items.BaseQuestDetailsItem;
import ru.coolone.travelquest.ui.fragments.quests.details.items.QuestDetailsItemRecycler;
import ru.coolone.travelquest.ui.fragments.quests.details.items.QuestDetailsItemText;

/**
 * @author coolone
 * @since 15.04.18
 */
public class FirebaseMethods {
    private static final String TAG = FirebaseMethods.class.getSimpleName();

    private static int startedTasks;

    private static int startedDeleteTasks;

    private static void checkEndTask(SerializeDetailsListener listener) {
        startedTasks--;

        Log.d(TAG, "checkEndTask started, tasks count: " + startedTasks);

        if (startedTasks == 0) {
            Log.d(TAG, "All serialize tasks ended!");
            listener.onSerializeDetailsCompleted();
            listener.onSerializeDetailsSuccess();
        }
    }

    public interface SerializeDetailsListener {
        void onSerializeDetailsSuccess();

        void onSerializeDetailsError(Exception e);

        void onSerializeDetailsCompleted();
    }

    static public void serializeDetails(
            CollectionReference coll,
            RecyclerView recyclerView,
            SerializeDetailsListener listener
    ) {
        startedTasks++;
        coll.get().addOnSuccessListener(
                queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.getDocuments().isEmpty())
                        // Start serialize docs
                        serializeDetails(
                                coll,
                                (BaseSectionedAdapter) recyclerView.getAdapter(),
                                0,
                                listener
                        );
                    else {
                        // Delete all old docs
                        startedDeleteTasks += queryDocumentSnapshots.getDocuments().size();
                        for (val mDoc : queryDocumentSnapshots.getDocuments())
                            mDoc.getReference().delete()
                                    .addOnFailureListener(
                                            e -> {
                                                Log.e(TAG, "Error while delete doc " + mDoc.getId());

                                                listener.onSerializeDetailsCompleted();
                                                listener.onSerializeDetailsError(e);
                                            }
                                    )
                                    .addOnSuccessListener(
                                            task -> {
                                                startedDeleteTasks--;

                                                // Start serialize docs
                                                serializeDetails(
                                                        coll,
                                                        (BaseSectionedAdapter) recyclerView.getAdapter(),
                                                        0,
                                                        listener
                                                );
                                            }
                                    );
                    }
                }
        ).addOnFailureListener(
                e -> Log.e(TAG, "Error while get docs of coll " + coll.getId())
        ).addOnCompleteListener(
                task -> startedTasks--
        );
    }

    static private String getSaltString() {
        val saltChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890abcdefghijklmnopqrstuvwxyz";
        val salt = new StringBuilder();
        val rnd = new Random();
        while (salt.length() < 19) {
            int index = (int) (rnd.nextFloat() * saltChars.length());
            salt.append(saltChars.charAt(index));
        }
        return salt.toString();
    }

    static private void serializeDetails(
            CollectionReference coll,
            BaseSectionedAdapter adapter,
            int startId,
            SerializeDetailsListener listener
    ) {
        Log.d(TAG, "--- Started serialize details ---");

        Log.d(TAG, "Starting sections for");
        for (int mSectionId = 0; mSectionId < adapter.getSectionCount(); mSectionId++) {
            val mSection = (Pair<BaseSectionedHeader, List<BaseQuestDetailsItem>>)
                    adapter.getSection(mSectionId);

            if (mSection.first.getTitle().isEmpty())
                continue;

            Log.d(TAG, "mSection"
                    + '\n' + " id: " + mSectionId
                    + '\n' + " title: " + mSection.first.getTitle()
                    + '\n' + " size: " + mSection.second.size());

            val mDoc = coll.document(
                    Integer.toString(mSectionId + startId) + getSaltString()
            );

            startedTasks++;
            mDoc.set(
                    new HashMap<String, Object>() {{
                        put("title", mSection.first.getTitle());
                    }}
            ).addOnSuccessListener(
                    taskTitleComplete -> Log.d(TAG, "Adding title " + mSection.first.getTitle() + " ended")
            ).addOnFailureListener(
                    e -> {
                        Log.e(TAG, "Create firebase doument with title error", e);
                        listener.onSerializeDetailsCompleted();
                        listener.onSerializeDetailsError(e);
                    }
            ).addOnCompleteListener(
                    task -> checkEndTask(listener)
            );

            val nextColl = mDoc.collection("coll");

            Log.d(TAG, "Starting items for");
            for (int mItemId = 0; mItemId < mSection.second.size(); mItemId++) {
                val mItem = mSection.second.get(mItemId);
                Log.d(TAG, "mItem"
                        + '\n' + " class: " + mItem.getClass().getSimpleName()
                );

                if (mItem instanceof QuestDetailsItemText &&
                        !((QuestDetailsItemText) mItem).getText().isEmpty()) {
                    Log.d(TAG, "mItem is text");

                    startedTasks++;
                    nextColl.document(Integer.toString(mItemId) + getSaltString()).set(
                            new HashMap<String, Object>() {{
                                put(
                                        "text",
                                        ((QuestDetailsItemText) mItem).getText()
                                );
                            }}
                    ).addOnSuccessListener(
                            taskTextComplete -> Log.d(TAG, "Adding text " + ((QuestDetailsItemText) mItem).getText() + " ended")
                    ).addOnFailureListener(
                            e -> {
                                Log.e(TAG, "Create firebase doument with text error", e);
                                listener.onSerializeDetailsCompleted();
                                listener.onSerializeDetailsError(e);
                            }
                    ).addOnCompleteListener(
                            task -> checkEndTask(listener)
                    );
                } else if (mItem instanceof QuestDetailsItemRecycler) {
                    Log.d(TAG, "mItem is recycler");

                    val mItemRecycler = ((QuestDetailsItemRecycler) mItem).getRecyclerView();

                    val mItemAdapter = (BaseSectionedAdapter) mItemRecycler.getAdapter();

                    serializeDetails(
                            nextColl,
                            mItemAdapter,
                            mItemId,
                            listener
                    );
                }
            }
        }

        Log.d(TAG, "--- Ended serialize details ---");
    }

    static public boolean parseDetails(
            QuerySnapshot coll,
            RecyclerView recyclerView,
            Context context
    ) {
        return parseDetailsHeaders(
                coll,
                recyclerView,
                context
        );
    }

    static private boolean parseDetailsHeaders(
            QuerySnapshot coll,
            RecyclerView recyclerView,
            Context context
    ) {
        Log.d(TAG, "--- Started parse details headers ---");

        boolean result = false;

        BaseSectionedAdapter adapter = (BaseSectionedAdapter) recyclerView.getAdapter();

        for (DocumentSnapshot mDoc : coll.getDocuments()) {
            if (mDoc.contains("title")) {
                // Recycler view
                final RecyclerView recycler = new RecyclerView(context);
                setDetailsRecyclerView(recycler, adapter.getClass(), context);

                // Recycler item
                final QuestDetailsItemRecycler itemRecycler = new QuestDetailsItemRecycler(recycler);

                // Section
                final Pair<BaseSectionedHeader, List<BaseQuestDetailsItem>> nextSection = new Pair<>(
                        new BaseSectionedHeader(mDoc.getString("title")),
                        new ArrayList<BaseQuestDetailsItem>() {{
                            add(itemRecycler);
                        }}
                );

                // Add item
                adapter.addSection(
                        nextSection
                );

                mDoc.getReference().collection("coll").get()
                        .addOnCompleteListener(
                                task -> {
                                    if (task.isSuccessful()) {
                                        // Parse details
                                        parseDetailsSections(
                                                task.getResult(),
                                                nextSection,
                                                adapter,
                                                context
                                        );
                                    }
                                }
                        );

                result = true;
            }
        }

        Log.d(TAG, "--- Ended parse details headers ---");

        return result;
    }

    static private void parseDetailsSections(
            QuerySnapshot coll,
            Pair<BaseSectionedHeader, List<BaseQuestDetailsItem>> section,
            BaseSectionedAdapter parentAdapter,
            Context context) {
        Log.d(TAG, "--- Started parse details sections ---");

        for (DocumentSnapshot mDoc : coll.getDocuments()) {
            Log.d(TAG, "mDoc id: " + mDoc.getId());

            BaseQuestDetailsItem mItem = null;

            if (mDoc.contains("text")) {
                String mDocText = mDoc.getString("text");
                Log.d(TAG, "mDoc is text (" + mDocText + ")");

                mItem = new QuestDetailsItemText(
                        mDocText
                );
            } else if (mDoc.contains("title")) {
                final String mDocTitle = mDoc.getString("title");
                Log.d(TAG, "mDoc is title (" + mDocTitle + ")");

                // Recycler view
                final RecyclerView recycler = new RecyclerView(context);
                setDetailsRecyclerView(recycler, parentAdapter.getClass(), context);
                final BaseSectionedAdapter adapter = (BaseSectionedAdapter) recycler.getAdapter();

                // Recycler item
                mItem = new QuestDetailsItemRecycler(
                        recycler
                );

                // Next section
                final Pair<BaseSectionedHeader, List<BaseQuestDetailsItem>> nextSection = new Pair<>(
                        new BaseSectionedHeader(mDocTitle),
                        new ArrayList<>()
                );

                // Add section
                adapter.addSection(nextSection);

                // Get collection
                mDoc.getReference().collection("coll").get()
                        .addOnCompleteListener(
                                task -> {
                                    if (task.isSuccessful()) {
                                        // Parse inner sections
                                        parseDetailsSections(
                                                task.getResult(),
                                                nextSection,
                                                parentAdapter,
                                                context
                                        );
                                    }
                                }
                        )
                        .addOnFailureListener(
                                e -> {
                                    // TODO: FAILURE
                                }
                        );
            } else {
                Log.e(TAG, "Unknown doc! (" + mDoc.getId() + ") " + mDoc);
            }

            if (mItem != null) {
                Log.d(TAG, "Item " + mItem.getClass().getSimpleName() + " added to section");
                section.second.add(mItem);

                parentAdapter.notifyDataSetChanged();
            }
        }

        Log.d(TAG, "--- Ended parse details sections ---");
    }

    static public void setDetailsRecyclerView(
            RecyclerView recyclerView,
            Class<? extends BaseSectionedAdapter> adapterClass,
            Context context
    ) {
        recyclerView.setNestedScrollingEnabled(false);

        // Recycler view
        recyclerView.setHasFixedSize(false);

        // Layout manager
        val detailsLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(detailsLayoutManager);

        // Adapter
        try {
            val adapter = (BaseSectionedAdapter)
                    (recyclerView.getAdapter() != null
                            ? (BaseSectionedAdapter) recyclerView.getAdapter() :
                            (adapterClass == QuestDetailsAddAdapter.class)
                                    ? adapterClass.getDeclaredConstructor
                                    (
                                            Context.class
                                    ).newInstance(context)
                                    : adapterClass.getConstructor()
                                    .newInstance()
                    );
            adapter.shouldShowHeadersForEmptySections(true);
            recyclerView.setAdapter(adapter);
        } catch (IllegalAccessException
                | java.lang.InstantiationException
                | NoSuchMethodException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
