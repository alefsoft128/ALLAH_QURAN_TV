package ir.fena.allah_quran_tv;

import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.leanback.app.DetailsSupportFragment;
import androidx.leanback.app.DetailsSupportFragmentBackgroundController;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * VideoDetailsFragment - بازنویسی کامل
 * - پخش playlist از QuranPlaylistPlayer
 * - پخش ویدئو پس‌زمینه از ObbVideoPlayer (با TextureView)
 * - نمایش متن آیه در WebView
 * - کنترل Pause/Resume با دکمه OK/ENTER ریموت
 */
public class VideoDetailsFragment1 extends DetailsSupportFragment {

    private static final String TAG = "VideoDetailsFragment";

    // رسانه‌ها / پلیرها
    private ObbVideoPlayer obbVideoPlayer;

    // UI
    private WebView mWebView;
    public static WebView sCurrentWebView; // برای دسترسی از QuranPlaylistPlayer.update

    // playlist tracks (از General1 گرفته میشود)
    public static String[] tracks;

    // background controller (برای leanback)
    private DetailsSupportFragmentBackgroundController mDetailsBackground;

    // overlay برای pause
    private FrameLayout pauseOverlayLayout;
    private TextView pausePlayButton;
    private boolean isPausedByUser = false;

    // handler برای اجرای کارها در UI thread
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public VideoDetailsFragment1() {
        // سازنده خالی
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // آماده‌سازی لیست پخش از General1
        try {
            if (General1.ayaRepeat > 1) {
                General1.playlistMain = General1.createRepeatedPlaylist(General1.ayaRepeat);
            }
            tracks = new String[General1.playlistMain.size()];
            for (int i = 0; i < tracks.length; i++) {
                tracks[i] = (String) General1.playlistMain.get(i).get(0);
            }

            List<String> tracks1 = Arrays.asList(tracks);
            // شروع پخش (QuranPlaylistPlayer مسئول مدیریت play/monitor است)
            QuranPlaylistPlayer.playPlaylist(getContext(), General1.obbPathMain, tracks1);

        } catch (Exception ex) {
            Log.e(TAG, "onCreate: error preparing playlist: " + ex.getMessage(), ex);
        }

        mDetailsBackground = new DetailsSupportFragmentBackgroundController(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 1) پیدا کردن TextureView در لایه اکتیویتی برای پخش ویدئو پس‌زمینه
        try {
            View rootActivityView = getActivity() != null ? getActivity().findViewById(R.id.details_fragment) : null;
            TextureView backgroundTextureView = null;
            if (rootActivityView != null) {
                backgroundTextureView = rootActivityView.findViewById(R.id.backgroundTextureView);
            }

            if (backgroundTextureView != null) {
                String obbPath = getContext().getObbDir() + File.separator + "main.1." + getContext().getPackageName() + ".obb";
                obbVideoPlayer = new ObbVideoPlayer(getContext(), backgroundTextureView, obbPath);
                obbVideoPlayer.playAllSequentially(3); // یا هر تنظیمی که نیاز داری
            } else {
                Log.w(TAG, "onActivityCreated: background TextureView not found (id: backgroundTextureView).");
            }
        } catch (Exception ex) {
            Log.e(TAG, "onActivityCreated: error setting up ObbVideoPlayer: " + ex.getMessage(), ex);
        }

        // 2) ایجاد WebView و قرار دادن آن در ریشه fragment (اگر قبلاً ایجاد نشده)
        try {
            if (mWebView == null) {
                mWebView = new WebView(getContext());
                mWebView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                mWebView.getSettings().setJavaScriptEnabled(false);
                mWebView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                View rootView = getView();
                if (rootView instanceof ViewGroup) {
                    ((ViewGroup) rootView).addView(mWebView, 1); // انداختن بالاتر از محتوای اصلی
                }
                sCurrentWebView = mWebView;
            }

            // بارگذاری HTML اولیه
            String initialText;
            if (General1.suraIndex != 9 && General1.suraIndex != 1) {
                initialText = General1.getInitialHtml(
                        "﴿ بِسْمِ اللَّـهِ الرَّحْمَـٰنِ الرَّحِيمِ ﴾" + "<br>" + System.lineSeparator() + General1.getAyaText(getContext(), General1.suraIndex, General1.ayaIndex),
                        General1.getAyaTranslation(getContext(), General1.suraIndex, General1.ayaIndex, General1.translatorIndex)
                );
            } else {
                initialText = General1.getInitialHtml(
                        General1.getAyaText(getContext(), General1.suraIndex, General1.ayaIndex),
                        General1.getAyaTranslation(getContext(), General1.suraIndex, General1.ayaIndex, General1.translatorIndex)
                );
            }
            updateWebViewContent(initialText);

            // انیمیشن ساده fade-in
            mWebView.post(() -> {
                mWebView.setAlpha(0f);
                mWebView.animate().alpha(1f).setDuration(400).start();
            });

        } catch (Exception ex) {
            Log.e(TAG, "onActivityCreated: error setting up WebView: " + ex.getMessage(), ex);
        }

        // 3) ساخت overlay و دکمه Play برای حالت Pause
        try {
            ViewGroup activityRoot = (ViewGroup) getActivity().findViewById(android.R.id.content);

            pauseOverlayLayout = new FrameLayout(getContext());
            pauseOverlayLayout.setBackground(new ColorDrawable(0x88000000)); // نیمه شفاف
            pauseOverlayLayout.setClickable(true);
            pauseOverlayLayout.setFocusable(true);
            pauseOverlayLayout.setVisibility(View.GONE);

            pausePlayButton = new TextView(getContext());
            pausePlayButton.setText("▶");
            pausePlayButton.setTextSize(72);
            pausePlayButton.setTypeface(Typeface.DEFAULT_BOLD);
            pausePlayButton.setGravity(Gravity.CENTER);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            pausePlayButton.setLayoutParams(lp);

            pauseOverlayLayout.addView(pausePlayButton);
            // به صورت بالای همه ویوها اضافه می‌کنیم
            activityRoot.addView(pauseOverlayLayout, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));

            // کلیک لمسی روی دکمه هم Resume می‌کند
            pausePlayButton.setOnClickListener(v -> {
                if (isPausedByUser) doResumeFromUser();
            });

        } catch (Exception ex) {
            Log.e(TAG, "onActivityCreated: error adding pause overlay: " + ex.getMessage(), ex);
        }

        // 4) دریافت رویدادهای کلید (OK/ENTER) از ریشه fragment
        try {
            View rootView = getView();
            if (rootView != null) {
                rootView.setFocusableInTouchMode(true);
                rootView.requestFocus();
                rootView.setOnKeyListener((v, keyCode, event) -> {
                    if (event.getAction() == KeyEvent.ACTION_DOWN &&
                            (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)) {

                        if (!isPausedByUser) {
                            doPauseFromUser();
                        } else {
                            doResumeFromUser();
                        }
                        return true;
                    }
                    return false;
                });
            } else {
                Log.w(TAG, "onActivityCreated: root view is null (cannot set OnKeyListener).");
            }
        } catch (Exception ex) {
            Log.e(TAG, "onActivityCreated: error setting OnKeyListener: " + ex.getMessage(), ex);
        }
    }

    /**
     * آپدیت ایمن WebView از سایر کلاس‌ها
     */
    public static void updateWebViewContent(final String htmlContent) {
        if (sCurrentWebView != null) {
            sCurrentWebView.post(() -> sCurrentWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null));
        } else {
            Log.w(TAG, "updateWebViewContent: WebView not ready");
        }
    }

    /**
     * عملیات Pause که کاربر از طریق ریموت می‌خواهد
     */
    private void doPauseFromUser() {
        try {
            int ssura = 0;
            int ssAya = 0;

            if (General1.suraIndex>0) ssura = General1.suraIndex-1; else ssura = General1.suraIndex;
            if (General1.ayaIndex>0) ssAya = General1.ayaIndex-1; else ssAya = General1.ayaIndex;

            QuranPlaylistGenerator.generatePlaylist(1001, ssura, ssAya, General1.tartilName,General1.ayaRepeat);

            QuranPlaylistPlayer.stopPlaylist();
        } catch (Throwable t) {
            Log.w(TAG, "doPauseFromUser: QuranPlaylistPlayer.pause failed: " + t.getMessage(), t);
        }


        // نمایش overlay در UI thread
        mainHandler.post(() -> {
            if (pauseOverlayLayout != null) pauseOverlayLayout.setVisibility(View.VISIBLE);
            isPausedByUser = true;
        });
    }

    /**
     * عملیات Resume که کاربر از طریق ریموت می‌خواهد
     */
    private void doResumeFromUser() {
        try {


                if (General1.ayaRepeat > 1) {
                    General1.playlistMain = General1.createRepeatedPlaylist(General1.ayaRepeat);
                }
                tracks = new String[General1.playlistMain.size()];
                for (int i = 0; i < tracks.length; i++) {
                    tracks[i] = (String) General1.playlistMain.get(i).get(0);
                }

                List<String> tracks1 = Arrays.asList(tracks);

                QuranPlaylistPlayer.playPlaylist(getContext(), General1.obbPathMain, tracks1);

        } catch (Throwable t) {
            Log.w(TAG, "doResumeFromUser: QuranPlaylistPlayer.resume failed: " + t.getMessage(), t);
        }


        // مخفی‌سازی overlay در UI thread
        mainHandler.post(() -> {
            if (pauseOverlayLayout != null) pauseOverlayLayout.setVisibility(View.GONE);
            isPausedByUser = false;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // جلوگیری از خاموش شدن صفحه
        if (getView() != null) getView().setKeepScreenOn(true);

        // اگر به دلیل lifecycle پخش قطع شده بود و کاربر Pause نزده، دوباره از پلیر بخواه پخش کند
        try {
            if (!isPausedByUser) {
                // اگر ObbVideoPlayer قبلاً ساخته شده، ensure it's playing
                if (obbVideoPlayer != null) {
                    obbVideoPlayer.playAllSequentially(15); // فرضی؛ اگر ندارید خط را بردارید یا به play() تغییر دهید
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "onResume: error resuming obbVideoPlayer: " + t.getMessage(), t);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getView() != null) getView().setKeepScreenOn(false);

        // وقتی fragment به background می‌رود، مطابق درخواست قبلی شما، Playlist را متوقف کن
        try {
            QuranPlaylistPlayer.stopPlaylist();
        } catch (Throwable t) {
            Log.w(TAG, "onPause: QuranPlaylistPlayer.stopPlaylist failed: " + t.getMessage(), t);
        }

        // همچنین ویدیو را متوقف کن


        // پاکسازی overlay
        if (pauseOverlayLayout != null) pauseOverlayLayout.setVisibility(View.GONE);
        isPausedByUser = false;

        long timestamp = System.currentTimeMillis();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dateTime = sdf.format(new Date(timestamp));

        General1.History.add("Sura ="+ General1.titles[General1.suraIndex-1] +" ||| Aya = "+General1.ayaIndex + " ||| Time = " + dateTime);
        HistoryManager.save(getContext(), General1.History);
        //General1.History = HistoryManager.load(this);

    }

    @Override
    public void onStop() {
        super.onStop();

        // اطمینان از توقف کامل playlist و آزاد کردن منابع مربوطه
        try {
            QuranPlaylistPlayer.stopPlaylist();
        } catch (Throwable t) {
            Log.w(TAG, "onStop: QuranPlaylistPlayer.stopPlaylist failed: " + t.getMessage(), t);
        }

        // توقف و آزادسازی ویدیو پلیر
        try {
            if (obbVideoPlayer != null) {
                obbVideoPlayer.stop();
                obbVideoPlayer.releaseAll();
                obbVideoPlayer = null;
            }
        } catch (Throwable t) {
            Log.w(TAG, "onStop: obbVideoPlayer stop/release failed: " + t.getMessage(), t);
        }

        // پاکسازی WebView مرجع
        try {
            if (mWebView != null) {
                mWebView.stopLoading();
                mWebView.loadUrl("about:blank");
                mWebView.removeAllViews();
                mWebView.destroy();
                mWebView = null;
                sCurrentWebView = null;
            }
        } catch (Throwable t) {
            Log.w(TAG, "onStop: WebView cleanup failed: " + t.getMessage(), t);
        }

        // پاکسازی overlay از درخت ویو (ایمن)
        try {
            if (pauseOverlayLayout != null && getActivity() != null) {
                ViewGroup activityRoot = (ViewGroup) getActivity().findViewById(android.R.id.content);
                if (activityRoot != null) activityRoot.removeView(pauseOverlayLayout);
                pauseOverlayLayout = null;
                pausePlayButton = null;
            }
        } catch (Throwable t) {
            Log.w(TAG, "onStop: overlay removal failed: " + t.getMessage(), t);
        }
    }
}
