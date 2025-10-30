package ir.fena.allah_quran_tv;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.SearchOrbView;
import androidx.leanback.widget.TitleViewAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainFragment extends BrowseSupportFragment {

    private static final String TAG = "MainFragment";
    private static final int BACKGROUND_UPDATE_DELAY = 2000;
    private static final int GRID_ITEM_WIDTH = 555;
    private static final int GRID_ITEM_HEIGHT = 313;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private String mBackgroundUri;
    private BackgroundManager mBackgroundManager;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // پیدا کردن مسیر OBB به صورت استاندارد
        File obbDir = getActivity().getObbDir(); // مسیر رسمی OBB برنامه
        if (obbDir != null) {
            File obbFile = new File(obbDir, "main.1." + requireContext().getPackageName() + ".obb");
            if (obbFile.exists()) {
                General1.obbPathMain = obbFile.getAbsolutePath();
            } else {
                Toast.makeText(requireContext(),
                        "OBB file not found in official directory.\nPlease make sure it exists.",
                        Toast.LENGTH_LONG).show();
                Log.w(TAG, "OBB file not found: " + obbFile.getAbsolutePath());
            }
        } else {
            Toast.makeText(requireContext(), "OBB directory not found", Toast.LENGTH_LONG).show();
        }

        prepareBackgroundManager();
        setupUIElements();
        loadRows();
        setupEventListeners();

        SharedPreferences prefs = requireContext().getSharedPreferences("quran_settings", Context.MODE_PRIVATE);
        General1.loadSettings(prefs);
        General1.History = HistoryManager.load(getContext());
        General1.trimHistory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBackgroundTimer != null) mBackgroundTimer.cancel();
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());

        mDefaultBackground = ContextCompat.getDrawable(getActivity(), R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        setTitle(getString(R.string.browse_title));
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        setBrandColor(ContextCompat.getColor(getActivity(), R.color.fastlane_background));
        setSearchAffordanceColor(ContextCompat.getColor(getActivity(), R.color.default_background));

        try {
            // 1. آداپتور عنوان را دریافت می کنیم
            TitleViewAdapter titleAdapter = getTitleViewAdapter();

            // 2. اگر آداپتور موجود است و دارای ویوی جستجو است
            if (titleAdapter != null) {
                View searchView = titleAdapter.getSearchAffordanceView();

                // 3. ویو را به SearchOrbView کست کرده و آیکن را تنظیم می کنیم
                if (searchView instanceof SearchOrbView) {
                    SearchOrbView searchOrb = (SearchOrbView) searchView;
                    // از requireContext() استفاده کنید
                    searchOrb.setOrbIcon(ContextCompat.getDrawable(requireContext(), R.drawable.history));
                }
            }
        } catch (IllegalStateException e) {
            // اگر getTitleViewAdapter() قبل از ایجاد ویو فراخوانی شود، این خطا رخ می دهد
            // معمولاً نباید در setupUIElements رخ دهد، اما برای ایمنی بهتر است
            e.printStackTrace();
        }

    }

    private void setupEventListeners() {
        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());

        setOnSearchClickedListener(new View.OnClickListener() {



            @Override

            public void onClick(View view) {

                //Toast.makeText(getActivity(), "Implement your own in-app search", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(requireContext(), HistoryActivity.class);
                startActivity(intent);

            }

        });

    }

    private void loadRows() {
        List<Movie> list = MovieList.getList();
        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        CardPresenter cardPresenter = new CardPresenter();

        int total = list.size();
        int itemsPerRow = 10;
        int numRows = (int) Math.ceil(total / 10.0);

        for (int i = 0; i < numRows; i++) {
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
            int start = i * itemsPerRow;
            int end = Math.min(start + itemsPerRow, total);
            for (int j = start; j < end; j++) listRowAdapter.add(list.get(j));

            if (i == numRows - 1) {
                Movie settings = new Movie();
                settings.setId(1000);
                settings.setTitle("Settings 🛠️");
                settings.setDescription("Customize your app preferences");
                settings.setCardImageUrl("btn2.png");
                settings.setBackgroundImageUrl("bg1.png");
                settings.setVideoUrl("settings");

                Movie about = new Movie();
                about.setId(1001);
                about.setTitle("About & Contact ❗");
                about.setDescription("Learn more or reach us");
                about.setCardImageUrl("btn2.png");
                about.setBackgroundImageUrl("bg1.png");
                about.setVideoUrl("about");

                listRowAdapter.add(settings);
                listRowAdapter.add(about);
            }

            String headerTitle = (i < MovieList.MOVIE_CATEGORY.length) ? MovieList.MOVIE_CATEGORY[i] : "SpeedDial " + (i + 1);
            rowsAdapter.add(new ListRow(new HeaderItem(i, headerTitle), listRowAdapter));
        }

        setAdapter(rowsAdapter);
    }



    private void startBackgroundTimer() {
        if (mBackgroundTimer != null) mBackgroundTimer.cancel();
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(() -> updateBackground(mBackgroundUri));
            }
        }, BACKGROUND_UPDATE_DELAY);
    }

    private void updateBackground(String uri) {
        if (uri == null) return;
        Glide.with(getActivity())
                .load(uri)
                .centerCrop()
                .error(mDefaultBackground)
                .into(new SimpleTarget<Drawable>(mMetrics.widthPixels, mMetrics.heightPixels) {
                    @Override
                    public void onResourceReady(@NonNull Drawable drawable,
                                                @Nullable Transition<? super Drawable> transition) {
                        mBackgroundManager.setDrawable(drawable);
                    }
                });
        if (mBackgroundTimer != null) mBackgroundTimer.cancel();
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Movie) {
                Movie movie = (Movie) item;
                General1.suraIndex = General1.getSuraIndex(movie.getTitle(), General1.titles);
                General1.ayaIndex = 1;

                if ("settings".equals(movie.getVideoUrl())) {
                    startActivity(new Intent(getActivity(), SettingsActivity.class));
                    return;
                }

                if ("about".equals(movie.getVideoUrl())) {
                    startActivity(new Intent(getActivity(), AboutActivity.class));
                    return;
                }

                startActivity(new Intent(getActivity(), IntermediateActivity.class));
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Movie) {
                mBackgroundUri = ((Movie) item).getBackgroundImageUrl();
                startBackgroundTimer();
            }
        }
    }

    public static void loadSettings(SharedPreferences prefs) {
        General1.tartilName = prefs.getString("tartilName", General1.tartilName);
        General1.fontNameIndex = prefs.getInt("fontNameIndex", General1.fontNameIndex);
        General1.fontSize = prefs.getInt("fontSize", General1.fontSize);
        General1.ayaRepeat = prefs.getInt("ayaRepeat", General1.ayaRepeat);
        General1.styleArabicText = prefs.getInt("styleArabicText", General1.styleArabicText);
        General1.styleTranslateText = prefs.getInt("styleTranslateText", General1.styleTranslateText);
        General1.playerSpeed = prefs.getInt("playerSpeed", General1.playerSpeed);
        General1.translatorIndex = prefs.getInt("translatorIndex", General1.translatorIndex);
        General1.animateStyleIndex = prefs.getInt("animateStyleIndex", General1.animateStyleIndex);
        General1.ayaIndex = prefs.getInt("ayaIndex", General1.ayaIndex);
        General1.suraIndex = prefs.getInt("suraIndex", General1.suraIndex);
    }
}
