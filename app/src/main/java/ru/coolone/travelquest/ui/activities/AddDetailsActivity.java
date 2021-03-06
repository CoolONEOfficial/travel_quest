package ru.coolone.travelquest.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.zagum.switchicon.SwitchIconView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import lombok.SneakyThrows;
import lombok.val;
import ru.coolone.travelquest.R;
import ru.coolone.travelquest.TasksCounter;
import ru.coolone.travelquest.ui.MyViewPager;
import ru.coolone.travelquest.ui.fragments.places.details.FirebaseMethods;
import ru.coolone.travelquest.ui.fragments.places.details.adapters.BaseSectionedAdapter;
import ru.coolone.travelquest.ui.fragments.places.details.adapters.BaseSectionedHeader;
import ru.coolone.travelquest.ui.fragments.places.details.add.PlaceDetailsAddAdapter;
import ru.coolone.travelquest.ui.fragments.places.details.add.PlaceDetailsAddFrag;
import ru.coolone.travelquest.ui.fragments.places.details.add.PlaceDetailsAddPagerAdapter;
import ru.coolone.travelquest.ui.fragments.places.details.items.BaseQuestDetailsItem;
import ru.coolone.travelquest.ui.fragments.places.details.items.QuestDetailsItemRecycler;
import ru.coolone.travelquest.ui.fragments.places.details.items.QuestDetailsItemText;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static ru.coolone.travelquest.ui.fragments.places.details.FirebaseMethods.serializeDetails;

@SuppressLint("Registered")
@EActivity
@OptionsMenu(R.menu.activity_add_details_actions)
public class AddDetailsActivity extends AppCompatActivity
        implements FirebaseMethods.TaskListener,
        PlaceDetailsAddFrag.Listener {
    private static final String TAG = AddDetailsActivity.class.getSimpleName();

    // Toolbar views
    ImageButton sendView;
    ImageButton restoreView;

    // Google map place id
    @Extra
    String placeId;

    // Root layout
    @ViewById(R.id.add_details_root_layout)
    LinearLayout rootLayout;

    // View pager
    @ViewById(R.id.add_details_viewpager)
    MyViewPager viewPager;
    PlaceDetailsAddPagerAdapter pagerAdapter;

    // Tab layout
    @ViewById(R.id.add_details_sliding_tabs)
    TabLayout tabLayout;

    ProgressBar progressBar;

    PlaceDetailsAddFrag[] restoredFrags;

    static boolean introStarted = false;

    boolean unsavedChanges = false;

    boolean untranslatedChanges = false;

    void copySections(
            ArrayList<Pair<BaseSectionedHeader, ArrayList<BaseQuestDetailsItem>>> fromSections,
            ArrayList<Pair<BaseSectionedHeader, ArrayList<BaseQuestDetailsItem>>> toSections,
            BaseSectionedAdapter.Listener newListener
    ) {
        toSections.clear();

        for (val mSection : fromSections) {
            val mSectionHeader = mSection.first;
            val mNewSectionHeader = new BaseSectionedHeader(mSectionHeader.getTitle());

            val mSectionItems = mSection.second;
            val mNewSectionItems = new ArrayList<BaseQuestDetailsItem>(mSectionItems.size());

            for (val mSectionItem : mSectionItems) {

                if (mSectionItem instanceof QuestDetailsItemText) {
                    val mSectionItemText = (QuestDetailsItemText) mSectionItem;

                    mNewSectionItems.add(
                            new QuestDetailsItemText(
                                    mSectionItemText.getText(),
                                    mSectionItemText.getHtml()
                            )
                    );
                } else if (mSectionItem instanceof QuestDetailsItemRecycler) {
                    val mSectionItemRecycler = (QuestDetailsItemRecycler) mSectionItem;
                    val mSectionItemRecyclerAdapter = (PlaceDetailsAddAdapter) mSectionItemRecycler.getRecyclerAdapter();

                    val mNewSectionItemRecyclerAdapter = new PlaceDetailsAddAdapter(
                            mSectionItemRecyclerAdapter.activity
                    );
                    mNewSectionItemRecyclerAdapter.setListener(newListener);

                    copySections(
                            ((PlaceDetailsAddAdapter) mSectionItemRecycler.getRecyclerAdapter())
                                    .getSections(),
                            mNewSectionItemRecyclerAdapter.getSections(),
                            newListener
                    );

                    val mNewSectionItemRecycler = new QuestDetailsItemRecycler(mNewSectionItemRecyclerAdapter);

                    mNewSectionItems.add(
                            mNewSectionItemRecycler
                    );
                }
            }

            val mNewSection = new Pair<BaseSectionedHeader, ArrayList<BaseQuestDetailsItem>>(
                    mNewSectionHeader,
                    mNewSectionItems
            );

            toSections.add(mNewSection);
        }
    }

    public interface TranslateFragListener {
        void onTranslateFragSuccess(PlaceDetailsAddFrag frag);

        void onTranslateFragError(PlaceDetailsAddFrag frag, Exception e);

        void onTranslateFragCompleted(PlaceDetailsAddFrag frag);
    }

    final TranslateFragListener fragListener = new TranslateFragListener() {
        @Override
        public void onTranslateFragSuccess(PlaceDetailsAddFrag frag) {
        }

        @Override
        public void onTranslateFragError(PlaceDetailsAddFrag frag, Exception e) {
            if (e != null && e.getLocalizedMessage() != null &&
                    !e.getLocalizedMessage().trim().isEmpty())
                Toast.makeText(
                        AddDetailsActivity.this,
                        e.getLocalizedMessage(),
                        Toast.LENGTH_SHORT
                ).show();
        }

        @Override
        public void onTranslateFragCompleted(PlaceDetailsAddFrag frag) {
            frag.hideProgressBar();
        }
    };

    void translateDetails(
            PlaceDetailsAddFrag fromFrag,
            PlaceDetailsAddFrag toFrag,
            TranslateFragListener fragListener
    ) {
        toFrag.showProgressBar(getString(R.string.add_details_progress_translate));

        Log.d(TAG, "Started transalte " + fromFrag.lang.lang + " to " + toFrag.lang.lang);

        toFrag.translated = true;
        toFrag.translatedChanged = false;
        toFrag.translateInProgress = true;
        setUserEditable(toFrag, false);

        untranslatedChanges = false;

        val fromAdapter = (PlaceDetailsAddAdapter) fromFrag.recycler.getAdapter();
        val toAdapter = (PlaceDetailsAddAdapter) toFrag.recycler.getAdapter();

        // Copy sections
        copySections(
                fromAdapter.getSections(),
                toAdapter.getSections(),
                toFrag
        );

        // Translate
        translateDetailAdapters(
                toAdapter,
                fromFrag,
                toFrag,
                fragListener
        );
    }

    void setUserEditable(
            PlaceDetailsAddFrag frag,
            boolean userEditable
    ) {
        viewPager.setSwipeable(userEditable);

        frag.addSectionButton.setEnabled(userEditable);

        applyUserEditableSections(frag.recyclerAdapter.getSections(), userEditable);
        frag.recyclerAdapter.notifyDataSetChanged();
    }

    private void applyUserEditableSections(
            ArrayList<Pair<BaseSectionedHeader, ArrayList<BaseQuestDetailsItem>>> sections,
            boolean userEditable
    ) {
        for (val mSection : sections) {
            for (val mItem : mSection.second) {
                if (mItem instanceof QuestDetailsItemText) {
                    val mItemText = (QuestDetailsItemText) mItem;

                    mItemText.userEditable = userEditable;
                } else if (mItem instanceof QuestDetailsItemRecycler) {
                    val mItemRecycler = (QuestDetailsItemRecycler) mItem;

                    applyUserEditableSections(
                            mItemRecycler.getRecyclerAdapter().getSections(),
                            userEditable
                    );
                }
            }
        }
    }

    void translateDetailAdapters(
            PlaceDetailsAddAdapter adapter,
            PlaceDetailsAddFrag fromFrag,
            PlaceDetailsAddFrag toFrag,
            TranslateFragListener fragListener
    ) {
        val translateCounter = new TasksCounter(
                "translate",
                () -> {
                    fragListener.onTranslateFragCompleted(toFrag);
                    fragListener.onTranslateFragSuccess(toFrag);
                    toFrag.translateInProgress = false;
                    setUserEditable(toFrag, true);
                }
        );

        translateCounter.onStartTasks(adapter.getSectionCount());

        for (val mSection : adapter.getSections()) {
            translateHeader(
                    adapter,
                    mSection.first,
                    fromFrag,
                    toFrag,
                    fragListener,
                    translateCounter
            );

            translateCounter.onStartTasks(mSection.second.size());

            for (val mItem : mSection.second) {
                if (mItem instanceof QuestDetailsItemRecycler) {
                    val mRecyclerItem = (QuestDetailsItemRecycler) mItem;

                    translateDetailAdapters(
                            (PlaceDetailsAddAdapter) mRecyclerItem.getRecyclerAdapter(),
                            fromFrag,
                            toFrag,
                            fragListener
                    );
                } else if (mItem instanceof QuestDetailsItemText) {
                    val mTextItem = (QuestDetailsItemText) mItem;
                    if (!mTextItem.getText().toString().trim().isEmpty()) {
                        translateItemText(
                                adapter,
                                mTextItem,
                                fromFrag,
                                toFrag,
                                fragListener,
                                translateCounter
                        );
                    }
                }

                translateCounter.onEndTask();
            }

            translateCounter.onEndTask();
        }
    }

    RequestQueue queue;

    Pair<FrameLayout, SwitchIconView> tabs[] = new Pair[MainActivity.SupportLang.values().length];

    @Override
    public void onSectionsLoaded(MainActivity.SupportLang fragLang) {
        setUserEditable(
                pagerAdapter.getItem(fragLang.ordinal()),
                true
        );

        if (fragLang == pagerAdapter.getItem(tabLayout.getSelectedTabPosition()).lang) {
            val dismissText = getString(R.string.add_details_intro_dismiss_button);
            val frag = pagerAdapter.getItem(viewPager.getCurrentItem());

            val sequence = new MaterialShowcaseSequence(AddDetailsActivity.this, TAG);

            if (!sequence.hasFired() && !introStarted) {
                introStarted = true;

                if (frag.recyclerAdapter.getSections().isEmpty())
                    frag.onAddHeaderClick();

                frag.recycler.post(
                        () -> {
                            MainActivity.sequenceItems.clear();

                            if (frag.recycler.findViewHolderForAdapterPosition(0) == null) {
                                Log.d(TAG, "");
                            } else Log.d(TAG, "");

                            val firstHolder = frag.recycler.findViewHolderForAdapterPosition(0).itemView;

                            MainActivity.addIntroItem(
                                    this,
                                    frag.addSectionButton,
                                    getString(R.string.add_details_intro_add_header),
                                    dismissText
                            );

                            // Find translate button
                            FrameLayout translateButtonLayout = null;
                            for (val mTab : tabs)
                                if (mTab.first.getVisibility() == View.VISIBLE)
                                    translateButtonLayout = mTab.first;

                            if (translateButtonLayout != null)
                                MainActivity.addIntroItem(
                                        this,
                                        translateButtonLayout,
                                        getString(R.string.add_details_intro_translate),
                                        dismissText
                                );

                            MainActivity.addIntroItem(
                                    this,
                                    firstHolder.findViewById(R.id.add_details_head_add),
                                    getString(R.string.add_details_intro_add),
                                    dismissText
                            );

                            MainActivity.addIntroItem(
                                    this,
                                    firstHolder.findViewById(R.id.add_details_head_remove),
                                    getString(R.string.add_details_intro_remove),
                                    dismissText
                            );

                            MainActivity.addIntroItem(
                                    this,
                                    sendView,
                                    getString(R.string.add_details_intro_send),
                                    dismissText
                            );

                            MainActivity.addIntroItem(
                                    this,
                                    restoreView,
                                    getString(R.string.add_details_intro_restore),
                                    dismissText
                            );

                            // Intro
                            ShowcaseConfig config = new ShowcaseConfig();
                            config.setDelay(100);

                            sequence.setConfig(config);

                            for (val mItem : MainActivity.sequenceItems)
                                sequence.addSequenceItem(mItem);

                            sequence.start();
                        }
                );
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!MainActivity.sequenceItems.isEmpty()) {
            for (val mItem : MainActivity.sequenceItems)
                mItem.removeFromWindow();
            MainActivity.sequenceItems.clear();
        } else
            homeSelected();
    }

    @Override
    public void onSectionsChanged(MainActivity.SupportLang fragLang) {
        unsavedChanges = true;

        if (fragLang == MainActivity.getLocale(AddDetailsActivity.this)) {
            untranslatedChanges = true;

            for (int mFragId = 0; mFragId < pagerAdapter.getCount(); mFragId++) {
                val mFrag = pagerAdapter.getItem(mFragId);
                if (mFrag.translated)
                    mFrag.translatedChanged = true;
            }
        }
    }

    @Override
    public FrameLayout getTranslateLayout(MainActivity.SupportLang fragLang) {
        return tabs[fragLang.ordinal()].first;
    }

    @Override
    public SwitchIconView getTranslateIcon(MainActivity.SupportLang fragLang) {
        return tabs[fragLang.ordinal()].second;
    }

    interface TranslateListener {
        void onTranslateSuccess(String translatedText);
    }

    interface TranslateErrorListener {
        void onTranslateApiError(JSONObject errorResponse);

        void onTranslateNetworkError(VolleyError error);
    }

    interface Translatable {
        String getText();

        void setText(String text);
    }

    void translateItemText(
            RecyclerView.Adapter adapter,
            QuestDetailsItemText itemText,
            PlaceDetailsAddFrag fromFrag,
            PlaceDetailsAddFrag toFrag,
            TranslateFragListener fragListener,
            TasksCounter translateCounter
    ) {
        translateTranslatable(
                new Translatable() {
                    @Override
                    public String getText() {
                        return itemText.getHtml();
                    }

                    @Override
                    public void setText(String text) {
                        itemText.setText(new SpannableString(Html.fromHtml(text)));
                        itemText.setHtml(text);
                        adapter.notifyDataSetChanged();

                        Log.d(TAG, "Translate text setted: " + text);
                    }
                },
                fromFrag,
                toFrag,
                fragListener,
                translateCounter
        );
    }

    void translateHeader(
            PlaceDetailsAddAdapter adapter,
            BaseSectionedHeader header,
            PlaceDetailsAddFrag fromFrag,
            PlaceDetailsAddFrag toFrag,
            TranslateFragListener fragListener,
            TasksCounter translateCounter
    ) {
        translateTranslatable(
                new Translatable() {
                    @Override
                    public String getText() {
                        return header.getTitle();
                    }

                    @Override
                    public void setText(String text) {
                        header.setTitle(text);
                        adapter.notifyDataSetChanged();
                    }
                },
                fromFrag,
                toFrag,
                fragListener,
                translateCounter
        );
    }

    @Background
    void translateTranslatable(
            Translatable translatable,
            PlaceDetailsAddFrag fromFrag,
            PlaceDetailsAddFrag toFrag,
            TranslateFragListener fragListener,
            TasksCounter translateCounter
    ) {
        val text = translatable.getText();

        if (!text.isEmpty()) {

            Log.d(TAG, "Translating text: " + text);
            runOnUiThread(
                    () -> translatable.setText(getString(R.string.add_details_translate_progress))
            );

            val listener = (TranslateListener) translatable::setText;

            translateText(
                    text,
                    fromFrag,
                    toFrag,
                    listener,
                    new TranslateErrorListener() {
                        @SneakyThrows
                        @Override
                        public void onTranslateApiError(JSONObject errorResponse) {
                            listener.onTranslateSuccess(getString(R.string.add_details_translate_error));

                            Toast.makeText(
                                    AddDetailsActivity.this,
                                    "Translate http request code: " + errorResponse.getInt("code"),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        @Override
                        public void onTranslateNetworkError(VolleyError error) {
                            listener.onTranslateSuccess(getString(R.string.add_details_translate_error));

                            if (error != null &&
                                    error.getLocalizedMessage() != null &&
                                    !error.getLocalizedMessage().trim().isEmpty())
                                Toast.makeText(
                                        AddDetailsActivity.this,
                                        error.getLocalizedMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                        }
                    },
                    fragListener,
                    translateCounter
            );
        }
    }

    void translateText(
            String text,
            PlaceDetailsAddFrag fromFrag,
            PlaceDetailsAddFrag toFrag,
            TranslateListener listener,
            TranslateErrorListener errorListener,
            TranslateFragListener fragListener,
            TasksCounter translateCounter
    ) {
        if (queue == null)
            queue = Volley.newRequestQueue(this);

        val uri = new Uri.Builder()
                .scheme("https")
                .authority("translate.yandex.net")
                .appendPath("api")
                .appendPath("v1.5")
                .appendPath("tr.json")
                .appendPath("translate")
                .appendQueryParameter("key", getString(R.string.YANDEX_TRANSLATE_API_KEY))
                .appendQueryParameter("text", text)
                .appendQueryParameter("lang",
                        fromFrag.lang.yaTranslateLang + '-' + toFrag.lang.yaTranslateLang
                );
        val uriStr = uri.toString();
        Log.d(TAG, "Translate request to " + uriStr + "...");

        val request = new JsonObjectRequest(
                uriStr,
                null,
                response -> {
                    try {
                        if (response.getInt("code") == HttpURLConnection.HTTP_OK) {
                            listener.onTranslateSuccess(response.getJSONArray("text").getString(0));
                        } else if (errorListener != null) {
                            errorListener.onTranslateApiError(response);
                            fragListener.onTranslateFragError(toFrag, new Exception("Translate api error"));
                            fragListener.onTranslateFragCompleted(toFrag);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (errorListener != null) {
                            errorListener.onTranslateNetworkError(new VolleyError(e));
                            fragListener.onTranslateFragError(toFrag, e);
                            fragListener.onTranslateFragCompleted(toFrag);
                        }
                    }

                    translateCounter.onEndTask();
                },
                error -> {
                    if (errorListener != null) {
                        errorListener.onTranslateNetworkError(error);
                        fragListener.onTranslateFragError(toFrag, error);
                        fragListener.onTranslateFragCompleted(toFrag);
                    }
                }
        );

        translateCounter.onStartTask();

        queue.add(request);
    }

    @AfterViews
    void afterViews() {
        // View pager
        pagerAdapter = new PlaceDetailsAddPagerAdapter(
                getSupportFragmentManager(),
                placeId,
                this
        );
        if (restoredFrags != null) {
            pagerAdapter.setTabFragments(restoredFrags);
        }
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(MainActivity.getLocale(this).ordinal());
        viewPager.addOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);

                        val fromFrag = pagerAdapter.getItem(MainActivity.getLocale(AddDetailsActivity.this).ordinal());
                        val frag = pagerAdapter.getItem(position);
                        val tab = tabs[frag.lang.ordinal()];
                        if (tab.first.getVisibility() == View.VISIBLE &&
                                tab.second.isIconEnabled() &&
                                fromFrag.recyclerAdapter.getSectionCount() > 0 &&
                                (untranslatedChanges ||
                                        ((PlaceDetailsAddAdapter) frag.recycler.getAdapter())
                                                .getSectionCount() == 0) &&
                                !frag.translateInProgress) {
                            translateDetails(
                                    fromFrag,
                                    pagerAdapter.getItem(position),
                                    fragListener
                            );
                        }
                    }
                }
        );
        viewPager.setOffscreenPageLimit(MainActivity.SupportLang.values().length);

        // Listen all restoredFrags
        for (int mFragId = 0; mFragId < pagerAdapter.getCount(); mFragId++) {
            val mFrag = pagerAdapter.getItem(mFragId);
            mFrag.setListener(this);
        }

        // Tab layout
        tabLayout.setupWithViewPager(viewPager);

        for (int mTabId = 0; mTabId < tabLayout.getTabCount(); mTabId++) {
            val mTab = tabLayout.getTabAt(mTabId);

            val tabLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.frag_add_details_page_tab, null);
            val tabTitle = (TextView) tabLayout.findViewById(R.id.details_tab_title);
            tabTitle.setText(mTab.getText());

            val mFrag = pagerAdapter.getItem(mTabId);

            val tabTranslate = (SwitchIconView) tabLayout.findViewById(R.id.details_tab_translate);
            val tabTranslateLayout = (FrameLayout) tabLayout.findViewById(R.id.details_tab_translate_layout);
            tabs[mTabId] = new Pair<>(
                    tabTranslateLayout,
                    tabTranslate
            );
            if (pagerAdapter.getItem(mTabId).lang != null)
                tabTranslateLayout.setVisibility(
                        pagerAdapter.getItem(mTabId).lang == MainActivity.getLocale(this)
                                ? View.GONE
                                : View.VISIBLE
                );
            else postRestoreListener = this::afterViews;

            tabTranslateLayout.setOnClickListener(
                    v -> {
                        val nextState = !tabTranslate.isIconEnabled();
                        tabTranslate.setIconEnabled(nextState);

                        if (
                                nextState &&
                                        mFrag.lang.ordinal() == viewPager.getCurrentItem() &&
                                        (
                                                mFrag.translatedChanged ||
                                                        ((PlaceDetailsAddAdapter) mFrag.recycler.getAdapter())
                                                                .getSectionCount() == 0
                                        )
                                )
                            translateDetails(
                                    pagerAdapter.getItem(
                                            MainActivity.getLocale(AddDetailsActivity.this).ordinal()
                                    ),
                                    mFrag,
                                    fragListener
                            );
                    }
            );

            mTab.setCustomView(tabLayout);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        sendView = (ImageButton) menu.findItem(R.id.add_details_action_send).getActionView();
        sendView.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_check));
        sendView.getBackground().setAlpha(0);
        sendView.setOnClickListener(
                v -> new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.add_details_action_send_alert_title))
                        .setMessage(getString(R.string.add_details_action_send_alert_text))
                        .setCancelable(true)
                        .setPositiveButton(
                                getString(R.string.add_details_action_alert_confirm),
                                (dialog, which) -> {
                                    setProgressVisible(true);

                                    val defaultVals = new HashMap<String, Object>();
                                    defaultVals.put("score", new ArrayList<String>());

                                    for (int mFragId = 0; mFragId < pagerAdapter.getCount(); mFragId++) {
                                        val mFrag = pagerAdapter.getItem(mFragId);

                                        val mLang = mFrag.lang.lang;
                                        val docPath = new StringBuilder(MainActivity.firebaseUser.getUid());
                                        val userName = MainActivity.firebaseUser.getDisplayName();
                                        if (userName != null && !userName.isEmpty())
                                            docPath.append('_').append(userName);
                                        val docRef = MainActivity.getPlacesRoot(mLang)
                                                .collection(placeId)
                                                .document(docPath.toString());

                                        docRef.set(defaultVals)
                                                .addOnSuccessListener(
                                                        aVoid -> serializeDetails(
                                                                docRef.collection("coll"),
                                                                mFrag.recycler,
                                                                this
                                                        )
                                                ).addOnFailureListener(
                                                e -> {
                                                    onTaskError(e);
                                                    onTaskCompleted();
                                                }
                                        );
                                    }
                                }
                        )
                        .setNegativeButton(
                                getString(R.string.add_details_action_alert_cancel),
                                (dialog, which) -> dialog.dismiss()
                        )
                        .create()
                        .show()
        );

        restoreView = (ImageButton) menu.findItem(R.id.add_details_action_restore).getActionView();
        restoreView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_restore));
        restoreView.getBackground().setAlpha(0);
        restoreView.setOnClickListener(
                v -> new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.add_details_action_restore_alert_title))
                        .setMessage(getString(R.string.add_details_action_restore_alert_text))
                        .setCancelable(true)
                        .setPositiveButton(
                                getString(R.string.add_details_action_alert_confirm),
                                (dialog, which) -> {
                                    for (int mFragId = 0; mFragId < pagerAdapter.getCount(); mFragId++) {
                                        val mFrag = pagerAdapter.getItem(mFragId);

                                        mFrag.restoreDetails();
                                    }

                                    unsavedChanges = false;
                                    untranslatedChanges = false;
                                }
                        )
                        .setNegativeButton(
                                getString(R.string.add_details_action_alert_cancel),
                                (dialog, which) -> dialog.dismiss()
                        )
                        .create()
                        .show()
        );

        return super.onCreateOptionsMenu(menu);
    }

    Runnable postRestoreListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_details);
        val bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        // Progress bar
        progressBar = new ProgressBar(this);
        val params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        params.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(params);

        // Fix action bar color
        getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(
                        ContextCompat.getColor(
                                this,
                                R.color.colorPrimary
                        )
                )
        );

        // Restore restoredFrags
        if (savedInstanceState != null) {
            restoredFrags = new PlaceDetailsAddFrag[MainActivity.SupportLang.values().length];
            for (val mLang : MainActivity.SupportLang.values()) {
                restoredFrags[mLang.ordinal()] = (PlaceDetailsAddFrag) getSupportFragmentManager().getFragment(
                        savedInstanceState,
                        mLang.lang
                );
            }
            if (pagerAdapter != null) {
                pagerAdapter.setTabFragments(restoredFrags);
            }

            if (postRestoreListener != null)
                postRestoreListener.run();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        for (val mLang : MainActivity.SupportLang.values()) {
            getSupportFragmentManager().putFragment(
                    outState,
                    mLang.lang,
                    pagerAdapter.getItem(mLang.ordinal())
            );
        }
    }

    @OptionsItem(android.R.id.home)
    void homeSelected() {

        if (unsavedChanges) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.add_details_action_cancel_alert_title))
                    .setMessage(getString(R.string.add_details_action_cancel_alert_text))
                    .setCancelable(true)
                    .setPositiveButton(
                            getString(R.string.add_details_action_alert_confirm),
                            (dialog, which) -> {
                                setResult(RESULT_CANCELED);
                                finish();
                            }
                    )
                    .setNegativeButton(
                            getString(R.string.add_details_action_alert_cancel),
                            (dialog, which) -> dialog.dismiss()
                    )
                    .create()
                    .show();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    public void setProgressVisible(
            boolean visible
    ) {
        val visibility = visible
                ? View.GONE
                : View.VISIBLE;
        tabLayout.setVisibility(visibility);
        viewPager.setVisibility(visibility);
        if (visible)
            rootLayout.addView(progressBar);
        else
            rootLayout.removeView(progressBar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        for (int mFragId = 0; mFragId < pagerAdapter.getCount(); mFragId++) {
            val mFrag = pagerAdapter.getItem(mFragId);

            mFrag.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onTaskSuccess() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onTaskError(Exception e) {
        if (e != null && e.getLocalizedMessage() != null &&
                !e.getLocalizedMessage().trim().isEmpty())
            Toast.makeText(
                    AddDetailsActivity.this,
                    e.getLocalizedMessage(),
                    Toast.LENGTH_SHORT
            ).show();
    }

    @Override
    public void onTaskCompleted() {
        setProgressVisible(false);
    }
}
