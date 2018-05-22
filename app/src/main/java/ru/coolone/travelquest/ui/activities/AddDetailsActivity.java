package ru.coolone.travelquest.ui.activities;

import android.annotation.SuppressLint;
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
import ru.coolone.travelquest.ui.adapters.BaseSectionedAdapter;
import ru.coolone.travelquest.ui.adapters.BaseSectionedHeader;
import ru.coolone.travelquest.ui.fragments.places.details.FirebaseMethods;
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
        implements FirebaseMethods.TaskListener, PlaceDetailsAddFrag.Listener {
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
    ViewPager viewPager;
    PlaceDetailsAddPagerAdapter pagerAdapter;

    // Tab layout
    @ViewById(R.id.add_details_sliding_tabs)
    TabLayout tabLayout;

    ProgressBar progressBar;

    PlaceDetailsAddFrag[] frags;

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
                                    mSectionItemText.getText()
                            )
                    );
                } else if (mSectionItem instanceof QuestDetailsItemRecycler) {
                    val mSectionItemRecycler = (QuestDetailsItemRecycler) mSectionItem;
                    val mSectionItemRecyclerAdapter = (PlaceDetailsAddAdapter) mSectionItemRecycler.getRecyclerAdapter();

                    val mNewSectionItemRecyclerAdapter = new PlaceDetailsAddAdapter(
                            mSectionItemRecyclerAdapter.context
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

    void translateDetails(
            PlaceDetailsAddFrag from,
            PlaceDetailsAddFrag to
    ) {
        Log.d(TAG, "Started transalte " + from.lang.lang + " to " + to.lang.lang);

        val fromAdapter = (PlaceDetailsAddAdapter) from.recycler.getAdapter();
        val toAdapter = (PlaceDetailsAddAdapter) to.recycler.getAdapter();

        // Copy sections
        copySections(
                fromAdapter.getSections(),
                toAdapter.getSections(),
                to
        );

        // Translate
        translateDetailAdapters(
                toAdapter,
                from.lang,
                to.lang
        );

        to.translated = true;
        to.translatedChanged = false;

        untranslatedChanges = false;
    }

    void translateDetailAdapters(
            PlaceDetailsAddAdapter adapter,
            MainActivity.SupportLang from,
            MainActivity.SupportLang to
    ) {
        for (val mSection : adapter.getSections()) {
            translateHeader(
                    adapter,
                    mSection.first,
                    from,
                    to
            );

            for (val mItem : mSection.second) {
                if (mItem instanceof QuestDetailsItemRecycler) {
                    val mRecyclerItem = (QuestDetailsItemRecycler) mItem;

                    translateDetailAdapters(
                            (PlaceDetailsAddAdapter) mRecyclerItem.getRecyclerAdapter(),
                            from,
                            to
                    );
                } else if (mItem instanceof QuestDetailsItemText) {
                    val mTextItem = (QuestDetailsItemText) mItem;
                    translateItemText(
                            adapter,
                            mTextItem,
                            from,
                            to
                    );
                    translateItemText(
                            adapter,
                            mTextItem,
                            from,
                            to
                    );
                }
            }
        }
    }

    RequestQueue queue;

    Pair<FrameLayout, SwitchIconView> tabs[] = new Pair[MainActivity.SupportLang.values().length];

    @Override
    public void onSectionsLoaded() {
        val dismissText = getString(R.string.add_details_intro_dismiss_button);
        val frag = pagerAdapter.getItem(viewPager.getCurrentItem());

        frag.recycler.post(
                () -> {
                    if (!introStarted &&
                            ((PlaceDetailsAddAdapter) frag.recycler.getAdapter()).getSectionCount() != 0) {

                        val firstHolder = frag.recycler.findViewHolderForAdapterPosition(0).itemView;

                        // Intro
                        ShowcaseConfig config = new ShowcaseConfig();
                        config.setDelay(100);

                        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(AddDetailsActivity.this, TAG);

                        sequence.setConfig(config);

                        sequence.addSequenceItem(
                                frag.addSectionButton,
                                getString(R.string.add_details_intro_add_header),
                                dismissText
                        );

                        FrameLayout translateButtonLayout = null;
                        for (val mTab : tabs)
                            if (mTab.first.getVisibility() == View.VISIBLE)
                                translateButtonLayout = mTab.first;

                        if (translateButtonLayout != null)
                            sequence.addSequenceItem(
                                    translateButtonLayout,
                                    getString(R.string.add_details_intro_translate),
                                    dismissText
                            );

                        sequence.addSequenceItem(
                                firstHolder.findViewById(R.id.add_details_head_add),
                                getString(R.string.add_details_intro_add),
                                dismissText
                        );

                        sequence.addSequenceItem(
                                firstHolder.findViewById(R.id.add_details_head_remove),
                                getString(R.string.add_details_intro_remove),
                                dismissText
                        );

                        sequence.addSequenceItem(
                                sendView,
                                getString(R.string.add_details_intro_send),
                                dismissText
                        );

                        sequence.addSequenceItem(
                                restoreView,
                                getString(R.string.add_details_intro_restore),
                                dismissText
                        );

                        introStarted = true;
                        sequence.start();
                    }
                }
        );
    }

    @Override
    public void onSectionsChanged(MainActivity.SupportLang fragLang) {
        unsavedChanges = true;

        if (fragLang == MainActivity.getLocale(AddDetailsActivity.this))
            untranslatedChanges = true;
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
        void onSuccess(String translatedText);
    }

    interface TranslateErrorListener {
        void onApiError(JSONObject errorResponse);

        void onNetworkError(VolleyError error);
    }

    interface Translatable {
        String getText();

        void setText(String text);
    }

    void translateItemText(
            RecyclerView.Adapter adapter,
            QuestDetailsItemText itemText,
            MainActivity.SupportLang fromLang,
            MainActivity.SupportLang toLang
    ) {
        translateTranslatable(
                new Translatable() {
                    @Override
                    public String getText() {
                        return itemText.getText();
                    }

                    @Override
                    public void setText(String text) {
                        itemText.setText(text);
                        adapter.notifyDataSetChanged();
                    }
                },
                fromLang,
                toLang
        );
    }

    void translateHeader(
            PlaceDetailsAddAdapter adapter,
            BaseSectionedHeader header,
            MainActivity.SupportLang fromLang,
            MainActivity.SupportLang toLang
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
                        runOnUiThread(adapter::notifyDataSetChanged);
                    }
                },
                fromLang,
                toLang
        );
    }

    @Background
    void translateTranslatable(
            Translatable translatable,
            MainActivity.SupportLang fromLang,
            MainActivity.SupportLang toLang
    ) {
        val text = translatable.getText();

        if (!text.equals(getString(R.string.add_details_translate_progress))) {

            Log.d(TAG, "Translating text: " + text);
            runOnUiThread(
                    () -> translatable.setText(getString(R.string.add_details_translate_progress))
            );

            val listener = (TranslateListener) translatable::setText;

            translateText(
                    text,
                    fromLang,
                    toLang,
                    listener,
                    new TranslateErrorListener() {
                        @SneakyThrows
                        @Override
                        public void onApiError(JSONObject errorResponse) {
                            listener.onSuccess(getString(R.string.add_details_translate_error));

                            Toast.makeText(
                                    AddDetailsActivity.this,
                                    "Translate http request code: " + errorResponse.getInt("code"),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        @Override
                        public void onNetworkError(VolleyError error) {
                            listener.onSuccess(getString(R.string.add_details_translate_error));

                            Toast.makeText(
                                    AddDetailsActivity.this,
                                    error.getLocalizedMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
        }
    }

    void translateText(
            String text,
            MainActivity.SupportLang fromLang,
            MainActivity.SupportLang toLang,
            TranslateListener listener,
            TranslateErrorListener errorListener
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
                        fromLang.yaTranslateLang + '-' + toLang.yaTranslateLang
                )
                .build();
        val uriStr = uri.toString();
        Log.d(TAG, "Translate request to " + uriStr + "...");

        val request = new JsonObjectRequest(
                uriStr,
                null,
                response -> {
                    try {
                        if (response.getInt("code") == HttpURLConnection.HTTP_OK) {
                            listener.onSuccess(response.getJSONArray("text").getString(0));
                        } else if (errorListener != null)
                            errorListener.onApiError(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (errorListener != null)
                            errorListener.onNetworkError(new VolleyError(e));
                    }
                },
                error -> {
                    if (errorListener != null)
                        errorListener.onNetworkError(error);
                }
        );

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
        if (frags != null) {
            pagerAdapter.setTabFragments(frags);
        }
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);

                        val frag = pagerAdapter.getItem(position);
                        val tab = tabs[frag.lang.ordinal()];
                        if (tab.first.getVisibility() == View.VISIBLE &&
                                tab.second.isIconEnabled() &&
                                (
                                        untranslatedChanges ||
                                                ((PlaceDetailsAddAdapter) frag.recycler.getAdapter()).getSectionCount() == 0)
                                ) {
                            translateDetails(
                                    pagerAdapter.getItem(MainActivity.getLocale(AddDetailsActivity.this).ordinal()),
                                    pagerAdapter.getItem(position)
                            );
                        }
                    }
                }
        );
        viewPager.setOffscreenPageLimit(MainActivity.SupportLang.values().length);

        // Listen all frags
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
                                    pagerAdapter.getItem(MainActivity.getLocale(AddDetailsActivity.this).ordinal()),
                                    mFrag
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
                        .setCancelable(false)
                        .setPositiveButton(
                                getString(R.string.add_details_action_alert_confirm),
                                (dialog, which) -> {
                                    setProgressVisible(true);

                                    val defaultVals = new HashMap<String, Object>();
                                    defaultVals.put("score", new ArrayList<String>());

                                    for (int mFragId = 0; mFragId < pagerAdapter.getCount(); mFragId++) {
                                        val mFrag = pagerAdapter.getItem(mFragId);

                                        val mLang = mFrag.lang.lang;
                                        val docRef = MainActivity.getQuestsRoot(mLang)
                                                .collection(placeId)
                                                .document(
                                                        MainActivity.firebaseUser.getUid() +
                                                                '_' +
                                                                MainActivity.firebaseUser.getDisplayName()
                                                );

                                        docRef.set(defaultVals)
                                                .addOnSuccessListener(
                                                        aVoid -> {
                                                            serializeDetails(
                                                                    docRef.collection("coll"),
                                                                    mFrag.recycler,
                                                                    this
                                                            );
                                                        }
                                                ).addOnFailureListener(
                                                e -> {
                                                    onFailure(e);
                                                    onCompleted();
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

        // Restore frags
        if (savedInstanceState != null) {
            frags = new PlaceDetailsAddFrag[MainActivity.SupportLang.values().length];
            for (val mLang : MainActivity.SupportLang.values()) {
                frags[mLang.ordinal()] = (PlaceDetailsAddFrag) getSupportFragmentManager().getFragment(
                        savedInstanceState,
                        mLang.lang
                );
            }
            if (pagerAdapter != null) {
                pagerAdapter.setTabFragments(frags);
            }
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

    @Override
    public void onSuccess() {
        setResult(RESULT_OK);
        finish();
    }

    public void setProgressVisible(
            boolean visible
    ) {
        val visibility = visible
                ? View.VISIBLE
                : View.GONE;
        tabLayout.setVisibility(visibility);
        viewPager.setVisibility(visibility);
        if (visible)
            rootLayout.addView(progressBar);
        else
            rootLayout.removeView(progressBar);
    }

    @Override
    public void onCompleted() {
        setProgressVisible(false);
    }

    @Override
    public void onFailure(Exception e) {
        Toast.makeText(
                AddDetailsActivity.this,
                e.getLocalizedMessage(),
                Toast.LENGTH_SHORT
        ).show();
    }
}
