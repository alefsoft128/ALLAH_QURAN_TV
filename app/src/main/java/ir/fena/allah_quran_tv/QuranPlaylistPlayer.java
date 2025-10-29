package ir.fena.allah_quran_tv;

import android.content.Context;
import java.util.List;

public class QuranPlaylistPlayer {

    private static List<String> playlist = null;
    private static int currentIndex = -1;
    private static Context ctx = null;
    private static String obbPathGlobal = null;

    private static Thread monitorThread = null;
    private static boolean monitoring = false;

    public static void playPlaylist(Context context, String obbPath, List<String> internalPaths) {
        if (internalPaths == null || internalPaths.isEmpty()) return;

        ctx = context.getApplicationContext();
        obbPathGlobal = obbPath;
        playlist = internalPaths;
        currentIndex = 0;

        playCurrent();

        // آغاز مانیتور برای رفتن به ترک بعد
        startMonitor();
    }

    private static void playCurrent() {
        if (playlist == null || currentIndex < 0 || currentIndex >= playlist.size()) return;
        String internalPath = playlist.get(currentIndex);

        ObbSoundPlayer.playObbSound(ctx, obbPathGlobal, internalPath);
        General1.currentIndexMain = currentIndex;

        int[] result1 = General1.getAyaAndSura(VideoDetailsFragment.tracks[currentIndex]);

        General1.suraIndex = result1[0];
        General1.ayaIndex  =  result1[1];



        // محتوای جدید
        String newAyaText = General1.getAyaText(ctx,General1.suraIndex, General1.ayaIndex);//
        if(General1.suraIndex>0 && General1.ayaIndex>1) {
            newAyaText = newAyaText + "<span class='smallT1'>" + " ﴿" + General1.ayaIndex + " | " + General1.ayaCountMain[General1.suraIndex - 1] + "﴾" + "</span>";
        }
        String translateAyaText = "";// "﴿ قُلْ هُوَ اللَّهُ أَحَدٌ ﴾";
        if (General1.translatorIndex<7 && General1.translatorIndex>-1) {
            //String newAyaText = General1.getAyaText(ctx, General1.suraIndex, General1.ayaIndex);//"﴿ قُلْ هُوَ اللَّهُ أَحَدٌ ﴾";
            translateAyaText = General1.getAyaTranslation(ctx, General1.suraIndex,General1.ayaIndex,General1.translatorIndex);
        }

// ساخت HTML کامل با استفاده از متد کمکی ثابت
        String newHtml = General1.getInitialHtml(newAyaText, translateAyaText);

// به‌روزرسانی WebView در VideoDetailsFragment
        VideoDetailsFragment.updateWebViewContent(newHtml);
    }





    private static void startMonitor() {
        monitoring = true;
        monitorThread = new Thread(() -> {
            while (monitoring) {
                try {
                    Thread.sleep(200); // هر ۰.۲ ثانیه چک
                    if (!ObbSoundPlayer.isPlaying() && playlist != null) {
                        // ترک فعلی تمام شده، برو ترک بعد
                        currentIndex++;
                        if (currentIndex < playlist.size()) {
                            playCurrent();
                        } else {
                            // پلی‌لیست تمام شد
                            monitoring = false;
                        }
                    }
                } catch (Exception ignored) {}
            }
        });
        monitorThread.start();
    }

    public static void pause() {
        ObbSoundPlayer.pause();
    }

    public static void resume() {
        ObbSoundPlayer.resume();
    }

    public static void stopPlaylist() {
        monitoring = false;
        ObbSoundPlayer.stop();
        playlist = null;
        currentIndex = -1;
    }

    public static boolean isPlaying() {
        return ObbSoundPlayer.isPlaying();
    }
}
