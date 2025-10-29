package ir.fena.allah_quran_tv;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * ObbVideoPlayer
 * - همه عملیات I/O و MediaPlayer در یک HandlerThread با اولویت پس‌زمینه اجرا می‌شوند.
 * - هر ویدئو 10 بار پخش می‌شود، سپس به ویدئوی بعدی می‌رود.
 * - ویدئوی بعدی پیش‌لود می‌شود (nextPlayer) تا هنگام سوآپ لگ به حداقل برسد.
 * - دارای متدهای playAllSequentially، playSpecificVideo، stop/release.
 */
public class ObbVideoPlayer implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        TextureView.SurfaceTextureListener {

    private static final String TAG = "ObbVideoPlayer";

    // لیست منبع ویدیویی داخل OBB (ثابت)
    private static final List<String> OBB_VIDEO_FILES_SOURCE = Arrays.asList(
            "wallpapers/zelda-forest-temple.1920x1080.mp4",
            "wallpapers/a-painting-landscape.1920x1080.mp4",
            "wallpapers/blurred-sunset-while-raining.1920x1080.mp4",
            "wallpapers/cherry-blossoms-branches.1920x1080.mp4",
            "wallpapers/earth-from-space.1920x1080.mp4",
            "wallpapers/fairy-mushroom-house.1920x1080.mp4",
            "wallpapers/golden-temple-ghost-of-tsushima.1920x1080.mp4",
            "wallpapers/japanese-garden.1920x1080.mp4",
            "wallpapers/misty-blue-lake.1920x1080.mp4",
            "wallpapers/river-flowing.1920x1080.mp4",
            "wallpapers/rain-drops-on-window.1920x1080.mp4",
            "wallpapers/snowy-forest.1920x1080.mp4",
            "wallpapers/tranquil-lotus-lake.1920x1080.mp4",
            "wallpapers/road-in-frozen-forest.1920x1080.mp4",
            "wallpapers/the-centenary.1920x1080.mp4",
            "wallpapers/whispering-lights-forest.1920x1080.mp4"
    );

    private final Context context;
    private final TextureView textureView;
    private final String obbPath;

    // Player thread & handler (تمام کارها در این handler اجرا می‌شوند)
    private HandlerThread playerThread;
    private Handler playerHandler;

    // Surface مربوط به TextureView
    private volatile Surface surface;
    private volatile boolean isSurfaceReady = false;

    // MediaPlayers
    private MediaPlayer mediaPlayer;   // پلیر فعلی (در حال پخش)
    private MediaPlayer nextPlayer;    // پلیر پیش‌لود شده برای ویدئوی بعدی
    private volatile boolean nextPlayerReady = false;

    // playlist و شاخص‌ها
    private List<String> playlist;
    private int currentIndex = 0;
    private int videoRepeatCount = 0; // شمارش تکرار یک ویدیو (هر ویدئو 10 بار)
    private int maxPlaylistRepeats = 1; // تعداد دفعات تکرار کل لیست
    private int playlistRepeatCount = 0;

    // حالت‌ها
    private boolean isSequentialMode = true;   // اگر true => لیست پخش متوالی
    private boolean isLoopingSingle = false;   // اگر true => ویدیوی تکی بی‌نهایت loop می‌شود

    // همگام‌سازی ساده (دسترسی‌ها تنها از طریق playerHandler انجام شود)
    // -------------------------------------------------------------

    public ObbVideoPlayer(Context context, TextureView textureView, String obbPath) {
        this.context = context.getApplicationContext();
        this.textureView = textureView;
        this.obbPath = obbPath;

        // لیست اولیه از منبع ثابت
        this.playlist = new ArrayList<>(OBB_VIDEO_FILES_SOURCE);
        Collections.shuffle(this.playlist);

        // تنظیم TextureView listener (UI thread)
        if (this.textureView != null) {
            this.textureView.setSurfaceTextureListener(this);
        }

        // ساخت و راه‌اندازی HandlerThread با اولویت پس‌زمینه
        playerThread = new HandlerThread("ObbVideoPlayerThread", Process.THREAD_PRIORITY_BACKGROUND);
        playerThread.start();
        playerHandler = new Handler(playerThread.getLooper());
    }

    // --------------------- استخراج فایل از OBB (فایل temp در cache) ---------------------
    // این متد باید در playerHandler اجرا شود تا I/O در پس‌زمینه باشد.
    private Uri extractFileFromObbBlocking(String internalPath) throws Exception {
        File obbFile = new File(obbPath);
        if (!obbFile.exists()) {
            throw new Exception("OBB file not found: " + obbPath);
        }

        String fileName = new File(internalPath).getName();
        File tempFile = new File(context.getCacheDir(), fileName);

        // اگر فایل از قبل استخراج شده و سالم است، بازگشت URI
        if (tempFile.exists() && tempFile.length() > 0) {
            return Uri.fromFile(tempFile);
        }

        // استخراج از zip
        try (ZipFile zip = new ZipFile(obbFile)) {
            ZipEntry entry = zip.getEntry(internalPath);
            if (entry == null) {
                throw new Exception("Entry not found in OBB: " + internalPath);
            }

            try (InputStream is = zip.getInputStream(entry);
                 FileOutputStream fos = new FileOutputStream(tempFile)) {

                byte[] buffer = new byte[8192];
                int len;
                while ((len = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.getFD().sync();
            }
        }

        return Uri.fromFile(tempFile);
    }

    // --------------------- شروع پخش یک مسیر (در playerHandler اجرا شود) ---------------------
    // توجه: این متد MediaPlayer جدید ایجاد و prepareAsync فراخوانی می‌کند.
    private void startPlaybackBlocking(final String internalPath) {
        try {
            // استخراج فایل (بلوک)
            Uri uri = extractFileFromObbBlocking(internalPath);

            // آزادسازی پلیر فعلی
            if (mediaPlayer != null) {
                try { mediaPlayer.stop(); } catch (Exception ignored) {}
                try { mediaPlayer.release(); } catch (Exception ignored) {}
                mediaPlayer = null;
            }

            // ایجاد MediaPlayer جدید
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setVolume(0f, 0f);

            // اتصال سطح (اگر آماده است)
            if (isSurfaceReady && surface != null) {
                mediaPlayer.setSurface(surface);
            }

            mediaPlayer.setDataSource(context, uri);
            mediaPlayer.prepareAsync();

            // هم‌زمان سعی می‌کنیم ویدئوی بعدی را پیش‌لود کنیم
            preloadNextBlocking();

        } catch (Exception e) {
            Log.e(TAG, "startPlaybackBlocking error: " + e.getMessage(), e);
            // در صورت خطا، ادامه تلاش برای پخش بعدی
            playerHandler.postDelayed(() -> {
                advanceToNextIndex();
                scheduleStartForCurrent();
            }, 500);
        }
    }

    // --------------------- پیش‌لود ویدئوی بعدی (بدون setSurface) ---------------------
    // اجرا در playerHandler
    private void preloadNextBlocking() {
        try {
            // آزادسازی nextPlayer قبلی اگر وجود دارد
            if (nextPlayer != null) {
                try { nextPlayer.release(); } catch (Exception ignored) {}
                nextPlayer = null;
                nextPlayerReady = false;
            }

            if (playlist == null || playlist.isEmpty()) {
                nextPlayerReady = false;
                return;
            }

            int nextIdx = (currentIndex + 1) % playlist.size();
            String nextPath = playlist.get(nextIdx);

            nextPlayer = new MediaPlayer();
            // دقت: برای preload، surface را وصل نمی‌کنیم تا منابع رندر مصرف نشود
            nextPlayer.setOnPreparedListener(mp -> {
                // این callback ممکن است در thread نامشخص اجرا شود، اما ما فقط علامت می‌زنیم
                // برای ثبات، مقداردهی را در playerHandler انجام می‌دهیم
                playerHandler.post(() -> {
                    nextPlayerReady = true;
                    Log.d(TAG, "nextPlayer prepared and ready for index=" + nextIdx);
                });
            });

            nextPlayer.setOnCompletionListener(mp -> {
                // اگر nextPlayer پخش شد (نباید بدون surface پخش شود)، به ایمن‌ترین شکل release کن
                playerHandler.post(() -> {
                    try { if (nextPlayer != null) nextPlayer.reset(); } catch (Exception ignored) {}
                });
            });

            nextPlayer.setVolume(0f, 0f);
            Uri nextUri = extractFileFromObbBlocking(nextPath);
            nextPlayer.setDataSource(context, nextUri);
            nextPlayer.prepareAsync();

        } catch (Exception e) {
            Log.e(TAG, "preloadNextBlocking error: " + e.getMessage(), e);
            nextPlayerReady = false;
        }
    }

    // --------------------- Listener callbacks ---------------------
    // همه‌ی کارهای سنگین داخل playerHandler اجرا می‌شوند (با post از این callbackها)

    @Override
    public void onPrepared(MediaPlayer mp) {
        // این callback از MediaPlayer فراخوانی می‌شود.
        // برای ایمنی تمام عملیات مربوط به start را روی playerHandler انجام می‌دهیم.
        playerHandler.post(() -> {
            if (mp == mediaPlayer) {
                try {
                    // اگر حالت looping تک ویدئو قرار گرفته باشد، از setLooping استفاده کن
                    if (isLoopingSingle) {
                        mediaPlayer.setLooping(true);
                    }
                    mediaPlayer.start();
                    Log.d(TAG, "mediaPlayer started for index=" + currentIndex);
                } catch (Exception e) {
                    Log.e(TAG, "onPrepared start failed: " + e.getMessage(), e);
                }
            } else if (mp == nextPlayer) {
                // handled in preloadNextBlocking via nextPlayerReady flag
                nextPlayerReady = true;
            }
        });
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // همه منطق مدیریت تکرار و جابجایی در playerHandler انجام می‌شود
        playerHandler.post(() -> {
            try {
                if (mp != mediaPlayer) {
                    // اگر اتفاقی برای nextPlayer افتاده، صرفاً release کن
                    try { if (mp != null) mp.release(); } catch (Exception ignored) {}
                    return;
                }

                // 1) تکرار 10 بار برای هر ویدئو
                videoRepeatCount++;
                if (!isLoopingSingle && videoRepeatCount < 10) {
                    try {
                        mediaPlayer.seekTo(0);
                        mediaPlayer.start();
                        Log.d(TAG, "Restarting same video for repeat: " + videoRepeatCount);
                    } catch (Exception e) {
                        Log.e(TAG, "Restart same video failed: " + e.getMessage(), e);
                    }
                    return;
                }

                // اگر looping برای ویدئوی تک تنظیم شده، MediaPlayer خودش loop می‌کرده؛ اما اگر رسیدیم اینجا،
                // یعنی یا loop=false و 10 بار کامل شد، یا loop true و completion به هر دلیلی صدا زده شد.
                videoRepeatCount = 0;

                // 2) حرکت به ویدئوی بعدی
                advanceToNextIndex();

                // اگر nextPlayer آماده است، آن را swap کن تا بدون لگ شروع شود
                if (nextPlayerReady && nextPlayer != null) {
                    // آزادسازی mediaPlayer فعلی و جایگزینی با nextPlayer
                    try {
                        mediaPlayer.release();
                    } catch (Exception ignored) {}
                    mediaPlayer = nextPlayer;
                    nextPlayer = null;
                    nextPlayerReady = false;

                    // وصل کردن surface به پلیر جدید (اگر سطح آماده‌ست)
                    if (isSurfaceReady && surface != null) {
                        try {
                            mediaPlayer.setSurface(surface);
                        } catch (Exception e) {
                            Log.e(TAG, "setSurface on swapped player failed: " + e.getMessage(), e);
                        }
                    }

                    // listenerها را دوباره تنظیم کن (ممکن است از قبل تنظیم باشند)
                    mediaPlayer.setOnPreparedListener(this);
                    mediaPlayer.setOnCompletionListener(this);

                    // اگر پلیر آماده است، start کن (معمولاً آماده است چون آماده‌سازی در preload انجام شده)
                    try {
                        mediaPlayer.start();
                    } catch (Exception e) {
                        Log.e(TAG, "start swapped player failed: " + e.getMessage(), e);
                    }

                    // پیش‌لود بعدی
                    preloadNextBlocking();
                    return;
                }

                // اگر nextPlayer آماده نبود، مستقیماً startPlayback برای مسیر جدید فراخوانی کن
                scheduleStartForCurrent();

            } catch (Exception e) {
                Log.e(TAG, "onCompletion handling failed: " + e.getMessage(), e);
            }
        });
    }

    // --------------------- مدیریت شاخص playlist ---------------------
    // اجرا در playerHandler
    private void advanceToNextIndex() {
        currentIndex++;
        if (currentIndex >= playlist.size()) {
            playlistRepeatCount++;
            if (playlistRepeatCount >= maxPlaylistRepeats) {
                // تمام تکرارها کامل شد -> متوقف کن و منابع رو آزاد کن
                Log.i(TAG, "Playlist finished after repeats=" + playlistRepeatCount);
                releaseAll();
                return;
            } else {
                // shuffle و برگشت به اول
                Collections.shuffle(playlist);
                currentIndex = 0;
                Log.d(TAG, "Shuffling playlist for next round. round=" + (playlistRepeatCount + 1));
            }
        }
    }

    // --------------------- برنامه‌ریزی شروع پخش برای currentIndex ---------------------
    private void scheduleStartForCurrent() {
        if (playlist == null || playlist.isEmpty()) return;
        if (currentIndex < 0 || currentIndex >= playlist.size()) return;

        final String path = playlist.get(currentIndex);
        playerHandler.post(() -> startPlaybackBlocking(path));
    }

    // --------------------- API عمومی ---------------------

    /**
     * پخش متوالی کل لیست با تعداد تکرار کل لیست = maxRepeats
     * هر ویدیو 10 بار پخش می‌شود.
     */
    public void playAllSequentially(int maxRepeats) {
        playerHandler.post(() -> {
            try {
                // تنظیم حالت‌ها
                isSequentialMode = true;
                isLoopingSingle = false;

                if (playlist == null || playlist.isEmpty()) {
                    playlist = new ArrayList<>(OBB_VIDEO_FILES_SOURCE);
                }
                Collections.shuffle(playlist);

                maxPlaylistRepeats = Math.max(1, maxRepeats);
                playlistRepeatCount = 0;
                currentIndex = 0;
                videoRepeatCount = 0;

                if (isSurfaceReady) {
                    startPlaybackBlocking(playlist.get(currentIndex));
                } else {
                    // صبر کن تا surface آماده شود؛ onSurfaceTextureAvailable آن را شروع خواهد کرد
                    Log.d(TAG, "playAllSequentially queued — waiting for surface");
                }
            } catch (Exception e) {
                Log.e(TAG, "playAllSequentially error: " + e.getMessage(), e);
            }
        });
    }

    /**
     * پخش یک ویدیوی مشخص (شماره از 1 شروع می‌شود)؛ اگر loop=true، ویدیو بی‌نهایت لوپ می‌شود.
     */
    public void playSpecificVideo(int videoNumber, boolean loop) {
        playerHandler.post(() -> {
            try {
                isSequentialMode = false;
                isLoopingSingle = loop;

                int idx = Math.max(0, Math.min(videoNumber - 1, OBB_VIDEO_FILES_SOURCE.size() - 1));
                playlist = new ArrayList<>();
                playlist.add(OBB_VIDEO_FILES_SOURCE.get(idx));
                Collections.shuffle(playlist); // بی‌ضرر است؛ فقط یک مورد است

                currentIndex = 0;
                videoRepeatCount = 0;
                maxPlaylistRepeats = 1;
                playlistRepeatCount = 0;

                if (isSurfaceReady) {
                    startPlaybackBlocking(playlist.get(currentIndex));
                } else {
                    Log.d(TAG, "playSpecificVideo queued — waiting for surface");
                }
            } catch (Exception e) {
                Log.e(TAG, "playSpecificVideo error: " + e.getMessage(), e);
            }
        });
    }

    /**
     * توقف پخش فعلی (اما آماده‌سازی/پریزرویشن cache حفظ می‌شود).
     */
    public void stop() {
        playerHandler.post(() -> {
            try {
                if (mediaPlayer != null) {
                    try { mediaPlayer.stop(); } catch (Exception ignored) {}
                }
                // nextPlayer را نگه می‌داریم چون ممکن است دوباره بخواهیم play کنیم
            } catch (Exception e) {
                Log.e(TAG, "stop error: " + e.getMessage(), e);
            }
        });
    }

    /**
     * آزادسازی کامل منابع — بعد از این باید نمونه جدید بسازی.
     */
    public void releaseAll() {
        playerHandler.post(() -> {
            try {
                if (mediaPlayer != null) {
                    try { mediaPlayer.stop(); } catch (Exception ignored) {}
                    try { mediaPlayer.release(); } catch (Exception ignored) {}
                    mediaPlayer = null;
                }
                if (nextPlayer != null) {
                    try { nextPlayer.release(); } catch (Exception ignored) {}
                    nextPlayer = null;
                    nextPlayerReady = false;
                }
                if (surface != null) {
                    try { surface.release(); } catch (Exception ignored) {}
                    surface = null;
                }
                isSurfaceReady = false;

                // متوقف کردن thread
                try {
                    if (playerThread != null) {
                        playerThread.quitSafely();
                        playerThread = null;
                    }
                } catch (Exception ignored) {}

                Log.i(TAG, "releaseAll completed.");
            } catch (Exception e) {
                Log.e(TAG, "releaseAll error: " + e.getMessage(), e);
            }
        });
    }

    // --------------------- TextureView.SurfaceTextureListener ---------------------
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        // این callback در UI thread اجرا می‌شود، ولی ما فقط surface می‌سازیم و به playerHandler اطلاع می‌دهیم.
        playerHandler.post(() -> {
            try {
                if (surface != null) {
                    try { surface.release(); } catch (Exception ignored) {}
                    surface = null;
                }
                surface = new Surface(surfaceTexture);
                isSurfaceReady = true;
                Log.d(TAG, "Surface ready. starting playback if queued.");

                // اگر قبلاً play فراخوانی شده، شروع پخش currentIndex
                scheduleStartForCurrent();

            } catch (Exception e) {
                Log.e(TAG, "onSurfaceTextureAvailable error: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // no-op
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        // Surface UI دارد آزاد می‌شود؛ ما در playerHandler وضعیت را پاک می‌کنیم.
        playerHandler.post(() -> {
            try {
                isSurfaceReady = false;
                if (surface != null) {
                    try { surface.release(); } catch (Exception ignored) {}
                    surface = null;
                }
                // بهتر است پلیرها را نگه داریم تا در صورت بازگشت سریع resume شود،
                // اما اگر می‌خواهی فوراً آزاد شود، می‌توانی releaseAll صدا بزنی.
                // releaseAll();
            } catch (Exception e) {
                Log.e(TAG, "onSurfaceTextureDestroyed error: " + e.getMessage(), e);
            }
        });
        return true; // surface توسط سیستم آزاد شود
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // no-op
    }
}
