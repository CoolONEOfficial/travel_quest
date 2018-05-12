package ru.coolone.travelquest.ui.fragments.places.details;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import lombok.SneakyThrows;
import lombok.val;
import ru.coolone.travelquest.ui.adapters.BaseSectionedAdapter;
import ru.coolone.travelquest.ui.adapters.BaseSectionedHeader;
import ru.coolone.travelquest.ui.fragments.places.details.add.PlaceDetailsAddAdapter;
import ru.coolone.travelquest.ui.fragments.places.details.items.BaseQuestDetailsItem;
import ru.coolone.travelquest.ui.fragments.places.details.items.QuestDetailsItemRecycler;
import ru.coolone.travelquest.ui.fragments.places.details.items.QuestDetailsItemText;

/**
 * @author coolone
 * @since 15.04.18
 */
public class FirebaseMethods {
    private static final String TAG = FirebaseMethods.class.getSimpleName();

    private static int startedTasks;
    private static int startedDeleteTasks;

    private static void checkEndTask(TaskListener listener) {
        startedTasks--;

        Log.d(TAG, "checkEndTask started, tasks count: " + startedTasks);

        if (startedTasks == 0) {
            Log.d(TAG, "All serialize tasks ended!");
            listener.onCompleted();
            listener.onSuccess();
        }
    }

    public interface TaskListener {
        void onSuccess();

        void onFailure(Exception e);

        void onCompleted();
    }

    static public void serializeDetails(
            CollectionReference coll,
            RecyclerView recyclerView,
            TaskListener listener
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

                                                listener.onCompleted();
                                                listener.onFailure(e);
                                            }
                                    )
                                    .addOnSuccessListener(
                                            task -> {
                                                startedDeleteTasks--;

                                                if (startedDeleteTasks == 0)
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
            TaskListener listener
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
                        listener.onCompleted();
                        listener.onFailure(e);
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

                if (mItem instanceof QuestDetailsItemText) {
                    val mItemText = (QuestDetailsItemText) mItem;
                    Log.d(TAG, "mItem is text");

                    if (!mItemText.getText().isEmpty()) {
                        startedTasks++;
                        nextColl.document(Integer.toString(mItemId) + getSaltString()).set(
                                new HashMap<String, Object>() {{
                                    put(
                                            "text",
                                            mItemText.getText()
                                    );
                                }}
                        ).addOnSuccessListener(
                                taskTextComplete -> Log.d(TAG, "Adding text " + mItemText.getText() + " ended")
                        ).addOnFailureListener(
                                e -> {
                                    Log.e(TAG, "Create firebase doument with text error", e);
                                    listener.onCompleted();
                                    listener.onFailure(e);
                                }
                        ).addOnCompleteListener(
                                task -> checkEndTask(listener)
                        );
                    }
                } else if (mItem instanceof QuestDetailsItemRecycler) {
                    val mItemRecycler = (QuestDetailsItemRecycler) mItem;
                    Log.d(TAG, "mItem is recyclerAdapter");

                    val mItemAdapter = (BaseSectionedAdapter) mItemRecycler.getRecyclerAdapter();

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

    public static void parseDetailsCards(
            QuerySnapshot coll,
            PlaceCardDetailsAdapter adapter,
            Context context,
            TaskListener listener
    ) {
        coll.getQuery()
                .orderBy("score")
                .limit(5)
                .get()
                .addOnSuccessListener(
                        collRef -> {
                            if (collRef.getDocuments().isEmpty()) {
                                listener.onCompleted();
                                listener.onFailure(new Exception("No cards"));
                                Log.d(TAG, "No cards");
                            }

                            for (val mDoc : collRef.getDocuments()) {
                                val recycler = new RecyclerView(context);
                                val nextAdapter = new PlaceDetailsAdapter();
                                recycler.setAdapter(nextAdapter);

                                int delimIndex = mDoc.getId().indexOf('_');

                                adapter.dataset.add(
                                        new PlaceCardDetailsAdapter.Item(
                                                mDoc.getId().substring(delimIndex + 1), // delim to end is name
                                                recycler,
                                                mDoc,
                                                mDoc.getId().substring(0, delimIndex) // 0 to delim is id
                                        )
                                );
                                adapter.notifyDataSetChanged();

                                mDoc.getReference().collection("coll").get()
                                        .addOnSuccessListener(
                                                collDetails -> {
                                                    if (parseDetailsHeaders(
                                                            collDetails,
                                                            nextAdapter,
                                                            context
                                                    ))
                                                        listener.onSuccess();
                                                    else
                                                        listener.onFailure(new Exception("Error while parsing card details"));
                                                }
                                        )
                                        .addOnFailureListener(
                                                listener::onFailure
                                        )
                                        .addOnCompleteListener(
                                                task -> listener.onCompleted()
                                        );

                            }
                        }
                )
                .addOnFailureListener(
                        e -> {
                            listener.onCompleted();
                            listener.onFailure(e);
                            Log.e(TAG, "Error while getting cards order by score", e);
                        }
                );
    }

    public static boolean parseDetailsHeaders(
            QuerySnapshot coll,
            BaseSectionedAdapter adapter,
            Context context
    ) {
        boolean result = false;

        Log.d(TAG, "--- Started parse details headers ---");

        for (DocumentSnapshot mDoc : coll.getDocuments()) {
            if (mDoc.contains("title")) {
                // Recycler view
                val recycler = new RecyclerView(context);

                initDetailsRecyclerView(recycler, adapter.getClass(), context);

                // Recycler item

                val itemRecycler = new QuestDetailsItemRecycler((BaseSectionedAdapter) recycler.getAdapter());

                // Section
                val nextSection = new Pair<BaseSectionedHeader, List<BaseQuestDetailsItem>>(
                        new BaseSectionedHeader(mDoc.getString("title")),
                        new ArrayList<BaseQuestDetailsItem>() {{
                            add(itemRecycler);
                        }}
                );

                // Add item
                adapter.addSection(
                        nextSection
                );
                adapter.notifyDataSetChanged();

                mDoc.getReference().collection("coll").get()
                        .addOnSuccessListener(
                                task -> {
                                    // Parse details
                                    parseDetailsSections(
                                            task,
                                            nextSection,
                                            adapter,
                                            context
                                    );
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
                initDetailsRecyclerView(recycler, parentAdapter.getClass(), context);
                final BaseSectionedAdapter adapter = (BaseSectionedAdapter) recycler.getAdapter();

                // Recycler item
                mItem = new QuestDetailsItemRecycler((BaseSectionedAdapter) recycler.getAdapter());

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
                                e -> Log.e(TAG, "Error while getting coll in mDoc", e)
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

    @SneakyThrows
    static public void initDetailsRecyclerView(
            RecyclerView recyclerView,
            Class<? extends RecyclerView.Adapter> adapterClass,
            Context context
    ) {
        // Recycler view
        recyclerView.setHasFixedSize(true);

        // Layout manager
        val detailsLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(detailsLayoutManager);

        // Adapter

        val adapter = (RecyclerView.Adapter)
                (recyclerView.getAdapter() != null
                        ? (BaseSectionedAdapter) recyclerView.getAdapter() :
                        (adapterClass == PlaceDetailsAddAdapter.class ||
                                adapterClass == PlaceCardDetailsAdapter.class)
                                ? adapterClass.getDeclaredConstructor(Context.class)
                                .newInstance(context)
                                : adapterClass.getConstructor()
                                .newInstance()
                );

        if (adapter instanceof SectionedRecyclerViewAdapter) {
            val sectionedAdapter = (SectionedRecyclerViewAdapter) adapter;
            sectionedAdapter.shouldShowHeadersForEmptySections(true);
        }

        recyclerView.setAdapter(adapter);
    }
}