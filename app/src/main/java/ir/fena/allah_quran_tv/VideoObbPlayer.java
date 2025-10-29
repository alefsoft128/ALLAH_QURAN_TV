package ir.fena.allah_quran_tv;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class VideoObbPlayer {

    private static final String TAG = "VideoObbPlayer";

    private Context context;
    private VideoView videoView;
    private String obbPath;

    private List<String> videoList = new ArrayList<>();
    private int currentIndex = 0;
    private boolean looping = true;

    public VideoObbPlayer(Context context, VideoView videoView, String obbPath) {
        this.context = context;
        this.videoView = videoView;
        this.obbPath = obbPath;

        if (videoView != null) {
            videoView.setOnCompletionListener(mp -> playNextVideo());
        }
    }

    /**
     * بارگذاری همه ویدئوها از فولدر داخل OBB (مثلا wallpapers/)
     */
    public void loadVideosFromObbFolder(String internalFolder) {
        try {
            File obbFile = new File(obbPath);
            if (!obbFile.exists()) {
                Log.e(TAG, "OBB file not found: " + obbPath);
                return;
            }

            ZipFile zip = new ZipFile(obbFile);
            videoList.clear();
            zip.stream()
                    .filter(e -> !e.isDirectory() && e.getName().startsWith(internalFolder) && e.getName().endsWith(".mp4"))
                    .forEach(e -> videoList.add(e.getName()));
            zip.close();

            Log.d(TAG, "Loaded " + videoList.size() + " videos from OBB folder: " + internalFolder);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setVideoList(List<String> videos) {
        videoList.clear();
        videoList.addAll(videos);
        currentIndex = 0;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public void start() {
        if (videoList.isEmpty()) {
            Log.w(TAG, "No videos to play");
            return;
        }
        currentIndex = 0;
        playCurrentVideo();
    }

    private void playCurrentVideo() {
        if (videoList.isEmpty() || videoView == null) return;
        String internalPath = videoList.get(currentIndex);
        extractAndPlay(internalPath);
    }

    private void playNextVideo() {
        if (videoList.isEmpty() || videoView == null) return;

        currentIndex++;
        if (currentIndex >= videoList.size()) {
            if (looping) currentIndex = 0;
            else return;
        }
        playCurrentVideo();
    }

    private void extractAndPlay(String internalPath) {
        try {
            if (videoView == null) return;

            File obbFile = new File(obbPath);
            if (!obbFile.exists()) return;

            File tempFile = new File(context.getCacheDir(), new File(internalPath).getName());

            // اگر temp وجود ندارد استخراج شود
            if (!tempFile.exists()) {
                ZipFile zip = new ZipFile(obbFile);
                ZipEntry entry = zip.getEntry(internalPath);
                if (entry == null) {
                    Log.e(TAG, "Video not found in OBB: " + internalPath);
                    zip.close();
                    return;
                }

                InputStream is = zip.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(tempFile);
                byte[] buffer = new byte[8192];
                int len;
                while ((len = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                is.close();
                zip.close();
            }

            videoView.setVideoURI(Uri.fromFile(tempFile));
            videoView.setOnPreparedListener(mp -> {
                mp.setLooping(false); // لوپ بین ویدئوها توسط playNextVideo مدیریت می‌شود
                videoView.start();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
