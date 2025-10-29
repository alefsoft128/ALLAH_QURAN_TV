package ir.fena.allah_quran_tv;

import android.content.Context;
import android.media.MediaPlayer;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ObbSoundPlayer {

    // ⚠️ متغیر static برای ردیابی Player فعلی
    private static MediaPlayer currentMediaPlayer = null;

    // ⚠️ متغیر static برای ردیابی فایل موقت فعلی جهت حذف
    private static File currentTempFile = null;

    public static void playObbSound(Context context, String obbPath, String internalPath) {

        // 1. آزادسازی پلیر قبلی قبل از شروع پخش جدید
        if (currentMediaPlayer != null) {
            currentMediaPlayer.release();
            currentMediaPlayer = null;
        }

        // 2. پاک کردن فایل موقت قبلی در صورت وجود
        if (currentTempFile != null) {
            currentTempFile.delete();
            currentTempFile = null;
        }

        File obbFile = new File(obbPath);
        if (!obbFile.exists()) {
            Toast.makeText(context, "OBB file not found", Toast.LENGTH_SHORT).show();
            return;
        }

        File tempFile = null;

        try {
            // استخراج فایل به temp (منطق اصلی شما)
            tempFile = new File(context.getCacheDir(), new File(internalPath).getName());

            // ⚠️ ذخیره مرجع tempFile
            currentTempFile = tempFile;

            ZipFile zip = new ZipFile(obbFile);
            ZipEntry entry = zip.getEntry(internalPath);
            if (entry == null) {
                Toast.makeText(context, "File not found in OBB: " + internalPath, Toast.LENGTH_LONG).show();
                zip.close();
                currentTempFile = null; // فایل موقتی ساخته نشده
                return;
            }

            InputStream is = zip.getInputStream(entry);
            FileOutputStream fos = new FileOutputStream(tempFile);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            is.close();
            zip.close();

            // پخش فایل
            MediaPlayer mediaPlayer = new MediaPlayer();

            // ⚠️ ذخیره پلیر در متغیر static
            currentMediaPlayer = mediaPlayer;

            // ⚠️ اضافه کردن OnCompletionListener برای آزادسازی منابع و حذف فایل موقت
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                if (currentTempFile != null) {
                    currentTempFile.delete();
                }
                currentMediaPlayer = null;
                currentTempFile = null;
            });

            mediaPlayer.setDataSource(tempFile.getAbsolutePath());
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) mp.setPlaybackParams(mp.getPlaybackParams().setSpeed(General1.speedOptions[General1.playerSpeed]));
                    mp.start();
                }
            });
            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Cannot play sound", Toast.LENGTH_SHORT).show();

            // تمیزکاری در صورت خطا
            if (currentMediaPlayer != null) {
                currentMediaPlayer.release();
                currentMediaPlayer = null;
            }
            if (currentTempFile != null) {
                currentTempFile.delete();
                currentTempFile = null;
            }
        }
    }

    /**
     * بررسی می‌کند آیا Player در حال پخش است. (برای استفاده در QuranPlaylistPlayer)
     */
    public static boolean isPlaying() {
        try {
            // پلیر باید وجود داشته باشد و در حال پخش باشد
            return currentMediaPlayer != null && currentMediaPlayer.isPlaying();
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * توقف موقت
     */
    public static void pause() {
        if (currentMediaPlayer != null && currentMediaPlayer.isPlaying()) {
            currentMediaPlayer.pause();
        }
    }

    /**
     * ادامه پخش
     */
    public static void resume() {
        if (currentMediaPlayer != null && !currentMediaPlayer.isPlaying()) {
            currentMediaPlayer.start();
        }
    }

    /**
     * توقف کامل و آزادسازی
     */
    public static void stop() {
        if (currentMediaPlayer != null) {
            currentMediaPlayer.release();
            currentMediaPlayer = null;
        }
        if (currentTempFile != null) {
            currentTempFile.delete();
            currentTempFile = null;
        }
    }

    public static void setSpeed(float speed) {
        if (currentMediaPlayer != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                currentMediaPlayer.setPlaybackParams(currentMediaPlayer.getPlaybackParams().setSpeed(speed));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}