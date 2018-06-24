package ru.coolone.travelquest.ui.fragments.places.details;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.util.Pair;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import lombok.SneakyThrows;
import lombok.val;
import ru.coolone.travelquest.R;
import ru.coolone.travelquest.TasksCounter;
import ru.coolone.travelquest.ui.fragments.places.details.adapters.BaseSectionedAdapter;
import ru.coolone.travelquest.ui.fragments.places.details.adapters.BaseSectionedHeader;
import ru.coolone.travelquest.ui.fragments.places.details.items.BaseQuestDetailsItem;
import ru.coolone.travelquest.ui.fragments.places.details.items.QuestDetailsItemRecycler;
import ru.coolone.travelquest.ui.fragments.places.details.items.QuestDetailsItemText;

/**
 * @author coolone
 * @since 15.04.18
 */
public class FirebaseMethods {
    private static final String TAG = FirebaseMethods.class.getSimpleName();

    public interface TaskListener {
        void onTaskSuccess();

        void onTaskError(Exception e);

        void onTaskCompleted();
    }

    static public void serializeDetails(
            CollectionReference coll,
            RecyclerView recyclerView,
            TaskListener listener
    ) {
        val serializeCounter = new TasksCounter(
                "serialize",
                () -> {
                    Log.d(TAG, "All serialize tasks ended!");
                    listener.onTaskCompleted();
                    listener.onTaskSuccess();
                }
        );
        serializeCounter.onStartTask();

        coll.get().addOnSuccessListener(
                queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.getDocuments().isEmpty())
                        // Start serialize docs
                        serializeDetails(
                                coll,
                                (BaseSectionedAdapter) recyclerView.getAdapter(),
                                0,
                                listener,
                                serializeCounter
                        );
                    else {
                        val deleteCounter = new TasksCounter(
                                "delete",
                                () -> {
                                    // Start serialize docs
                                    serializeDetails(
                                            coll,
                                            (BaseSectionedAdapter) recyclerView.getAdapter(),
                                            0,
                                            listener,
                                            serializeCounter
                                    );
                                }
                        );

                        // Delete all old docs
                        deleteCounter.onStartTasks(queryDocumentSnapshots.getDocuments().size());
                        for (val mDoc : queryDocumentSnapshots.getDocuments())
                            mDoc.getReference().delete()
                                    .addOnFailureListener(
                                            e -> {
                                                Log.e(TAG, "Error while delete doc " + mDoc.getId());

                                                listener.onTaskCompleted();
                                                listener.onTaskError(e);
                                            }
                                    )
                                    .addOnSuccessListener(
                                            task -> deleteCounter.onEndTask()
                                    );
                    }
                }
        ).addOnFailureListener(
                e -> Log.e(TAG, "Error while get docs of coll " + coll.getId())
        ).addOnCompleteListener(
                task -> serializeCounter.onEndTask()
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

    static String addNullsPrefix(int num, int max) {
        val strIdBuilder = new StringBuilder();
        for (int mNullPrefix = 0;
             mNullPrefix < Integer.toString(max).length() - Integer.toString(num).length();
             mNullPrefix++) {
            strIdBuilder.append('0');
        }
        strIdBuilder.append(Integer.toString(num));

        return strIdBuilder.toString();
    }

    static private void serializeDetails(
            CollectionReference coll,
            BaseSectionedAdapter adapter,
            int startId,
            TaskListener listener,
            TasksCounter serializeCounter
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
                    addNullsPrefix(
                            mSectionId + startId,
                            adapter.getSectionCount() + startId
                    ) + getSaltString()
            );

            serializeCounter.onStartTask();
            mDoc.set(
                    new HashMap<String, Object>() {{
                        put("title", mSection.first.getTitle());
                    }}
            ).addOnSuccessListener(
                    taskTitleComplete -> Log.d(TAG, "Adding title " + mSection.first.getTitle() + " ended")
            ).addOnFailureListener(
                    e -> {
                        Log.e(TAG, "Create firebase document with title error", e);
                        listener.onTaskCompleted();
                        listener.onTaskError(e);
                    }
            ).addOnCompleteListener(
                    task -> serializeCounter.onEndTask()
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

                    if (!mItemText.getText().toString().trim().isEmpty()) {
                        // Serialize
                        serializeCounter.onStartTask();
                        nextColl.document(
                                addNullsPrefix(mItemId, mSection.second.size())
                                        + getSaltString()
                        ).set(
                                new HashMap<String, Object>() {{
                                    put(
                                            "text",
                                            mItemText.getHtml()
                                    );
                                }}
                        ).addOnSuccessListener(
                                taskTextComplete -> Log.d(TAG, "Adding text " + mItemText.getText() + " ended")
                        ).addOnFailureListener(
                                e -> {
                                    Log.e(TAG, "Create firebase doument with text error", e);
                                    listener.onTaskCompleted();
                                    listener.onTaskError(e);
                                }
                        ).addOnCompleteListener(
                                task -> serializeCounter.onEndTask()
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
                            listener,
                            serializeCounter
                    );
                }
            }
        }

        Log.d(TAG, "--- Ended serialize details ---");
    }

    public static void parseDetailsCards(
            QuerySnapshot coll,
            PlaceCardDetailsAdapter adapter,
            Activity activity,
            TaskListener listener
    ) {
        coll.getQuery()
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(
                        collRef -> {
                            val docs = collRef.getDocuments();

                            if (docs.isEmpty()) {
                                listener.onTaskCompleted();
                                listener.onTaskError(new Exception("No cards"));
                                Log.d(TAG, "No cards");
                            } else for (val mDoc : docs) {
                                val recycler = new RecyclerView(activity);
                                val nextAdapter = new PlaceDetailsAdapter(activity);
                                recycler.setAdapter(nextAdapter);

                                val mDocId = mDoc.getId();
                                val delimIndex = mDocId.indexOf('_');

                                val mDocIdWithName = mDocId.matches("([0-9A-Za-z]+)(_)([^_]+)( )([^_]+)");

                                adapter.dataset.add(
                                        new PlaceCardDetailsAdapter.Item(
                                                mDocIdWithName
                                                        ? mDocId.substring(delimIndex + 1) // name
                                                        : activity.getString(R.string.details_unnamed_user)
                                                        + mDocId, // user uid,
                                                recycler,
                                                mDoc,
                                                mDocIdWithName
                                                        ? mDocId.substring(0, delimIndex) // 0 to delim is id
                                                        : mDocId // all is id
                                        )
                                );
                                adapter.notifyDataSetChanged();

                                mDoc.getReference().collection("coll").get()
                                        .addOnSuccessListener(
                                                collDetails -> {
                                                    if (!parseDetailsHeaders(
                                                            collDetails,
                                                            nextAdapter,
                                                            true,
                                                            activity,
                                                            listener::onTaskSuccess
                                                    ))
                                                        listener.onTaskError(new Exception("Error while parsing card details"));
                                                }
                                        )
                                        .addOnFailureListener(
                                                listener::onTaskError
                                        )
                                        .addOnCompleteListener(
                                                task -> listener.onTaskCompleted()
                                        );

                            }
                        }
                )
                .addOnFailureListener(
                        e -> {
                            listener.onTaskCompleted();
                            listener.onTaskError(e);
                            Log.e(TAG, "Error while getting cards order by score", e);
                        }
                );
    }

    public static boolean parseDetailsHeaders(
            QuerySnapshot coll,
            BaseSectionedAdapter adapter,
            boolean collapseSection,
            Activity activity,
            TasksCounter.TaskListener listener
    ) {
        boolean result = false;

        Log.d(TAG, "--- Started parse details headers ---");

        val parseCounter = new TasksCounter("parse", listener);

        val docs = coll.getDocuments();

        if (!docs.isEmpty()) {
            parseCounter.onStartTasks(docs.size());

            for (DocumentSnapshot mDoc : docs) {
                if (mDoc.contains("title")) {
                    // Recycler view
                    val recycler = new RecyclerView(activity);
                    initDetailsRecyclerView(recycler, adapter.getClass(), activity);

                    // Section
                    val nextSection = new Pair<BaseSectionedHeader, List<BaseQuestDetailsItem>>(
                            new BaseSectionedHeader(mDoc.getString("title")),
                            new ArrayList<>()
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
                                                collapseSection,
                                                activity,
                                                parseCounter
                                        );
                                    }
                            );

                    result = true;
                } else {
                    Log.e(TAG, "mDoc not contain title!");
                }

                parseCounter.onEndTask();
            }

            if (collapseSection) {
                adapter.collapseAllSections();
                adapter.expandSection(0);
            }
        } else result = true;

        Log.d(TAG, "--- Ended parse details headers ---");

        return result;
    }

    static private void parseDetailsSections(
            QuerySnapshot coll,
            Pair<BaseSectionedHeader, List<BaseQuestDetailsItem>> section,
            BaseSectionedAdapter parentAdapter,
            boolean collapseSection,
            Activity activity,
            TasksCounter parseCounter
    ) {
        Log.d(TAG, "--- Started parse details sections ---");

        parseCounter.onStartTasks(coll.getDocuments().size());
        for (DocumentSnapshot mDoc : coll.getDocuments()) {
            Log.d(TAG, "mDoc id: " + mDoc.getId());

            BaseQuestDetailsItem mItem = null;

            if (mDoc.contains("text")) {
                String mDocText = mDoc.getString("text");
                Log.d(TAG, "mDoc is text (" + mDocText + ")");

                mItem = new QuestDetailsItemText(
                        Html.fromHtml(mDocText),
                        mDocText
                );
            } else if (mDoc.contains("title")) {
                val mDocTitle = mDoc.getString("title");
                Log.d(TAG, "mDoc is title (" + mDocTitle + ")");

                // Recycler view
                val recycler = new RecyclerView(activity);
                initDetailsRecyclerView(recycler, parentAdapter.getClass(), activity);
                val adapter = (BaseSectionedAdapter) recycler.getAdapter();
                adapter.setListener(parentAdapter.getListener());

                // Recycler item
                mItem = new QuestDetailsItemRecycler((BaseSectionedAdapter) recycler.getAdapter());

                // Next section
                val nextSection = new Pair<BaseSectionedHeader, List<BaseQuestDetailsItem>>(
                        new BaseSectionedHeader(mDocTitle),
                        new ArrayList<>()
                );

                // Add and collapse section
                adapter.addSection(nextSection);
                if (collapseSection)
                    adapter.collapseAllSections();

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
                                                collapseSection,
                                                activity,
                                                parseCounter
                                        );
                                    }
                                }
                        )
                        .addOnFailureListener(
                                e -> Log.e(TAG, "Error while getting coll in mDoc", e)
                        );
            } else Log.e(TAG, "Unknown doc! (" + mDoc.getId() + ") " + mDoc);

            if (mItem != null) {
                Log.d(TAG, "Item " + mItem.getClass().getSimpleName() + " added to section");
                section.second.add(mItem);

                parentAdapter.notifyDataSetChanged();
            }

            parseCounter.onEndTask();
        }

        Log.d(TAG, "--- Ended parse details sections ---");
    }

    @SneakyThrows
    static public void initDetailsRecyclerView(
            RecyclerView recyclerView,
            Class<? extends RecyclerView.Adapter> adapterClass,
            Activity activity
    ) {
        // Recycler view
        recyclerView.setHasFixedSize(true);

        // Layout manager
        val detailsLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(detailsLayoutManager);

        // Adapter

        val adapter = (RecyclerView.Adapter)
                (recyclerView.getAdapter() != null
                        ? (BaseSectionedAdapter) recyclerView.getAdapter() :
                        adapterClass.getDeclaredConstructor(Activity.class)
                                .newInstance(activity)
                );

        if (adapter instanceof SectionedRecyclerViewAdapter) {
            val sectionedAdapter = (SectionedRecyclerViewAdapter) adapter;
            sectionedAdapter.shouldShowHeadersForEmptySections(true);
        }

        recyclerView.setAdapter(adapter);
    }
}
