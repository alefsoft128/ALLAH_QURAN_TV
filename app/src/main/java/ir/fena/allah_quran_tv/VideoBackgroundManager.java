package ir.fena.allah_quran_tv;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.widget.VideoView;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoBackgroundManager {

    private static final String TAG = "VideoBackgroundManager";

    private Context context;
    private VideoView videoView;
    private List<String> videoPaths = new ArrayList<>();
    private int currentIndex = 0;

    private boolean isLooping = true;
    private Handler handler = new Handler();

    public VideoBackgroundManager(Context context, VideoView videoView) {
        if (videoView == null) {
            throw new IllegalArgumentException("VideoView cannot be null!");
        }

        this.context = context;
        this.videoView = videoView;

        this.videoView.setOnCompletionListener(mp -> playNext());
    }

    /**
     * بارگذاری همه ویدئوها از مسیر مشخص
     */
    public void loadVideosFromFolder(String folderPath) {
        File folder = new File(folderPath);
        videoPaths.clear();
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile() && f.getName().endsWith(".mp4")) {
                        videoPaths.add(f.getAbsolutePath());
                    }
                }
            }
        }
        Log.d(TAG, "Loaded " + videoPaths.size() + " videos from folder.");
    }

    /**
     * پخش ویدئوهای انتخابی
     */
    public void setVideoList(List<String> selectedVideos) {
        videoPaths.clear();
        if (selectedVideos != null) {
            videoPaths.addAll(selectedVideos);
        }
        currentIndex = 0;
    }

    /**
     * شروع پخش
     */
    public void start() {
        if (videoPaths.isEmpty()) {
            Log.w(TAG, "No videos to play.");
            return;
        }
        currentIndex = 0;
        playCurrent();
    }

    private void playCurrent() {
        if (videoPaths.isEmpty() || videoView == null) return;

        String path = videoPaths.get(currentIndex);
        File file = new File(path);
        if (!file.exists()) {
            Log.w(TAG, "Video file does not exist: " + path);
            playNext();
            return;
        }

        Log.d(TAG, "Playing video: " + path);
        videoView.setVideoURI(Uri.fromFile(file));
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(false); // لوپ بین ویدئوها خودمان مدیریت می‌کنیم
            videoView.start();
        });
    }

    private void playNext() {
        if (videoPaths.isEmpty() || videoView == null) return;

        currentIndex++;
        if (currentIndex >= videoPaths.size()) {
            if (isLooping) {
                currentIndex = 0;
            } else {
                return;
            }
        }
        playCurrent();
    }

    /**
     * برای تغییر مسیر ویدئوهای پس‌زمینه در طول اجرا
     */
    public void updateVideos(List<String> newVideos, boolean startImmediately) {
        setVideoList(newVideos);
        if (startImmediately) start();
    }

    public void setLooping(boolean looping) {
        this.isLooping = looping;
    }
}
