package ir.fena.allah_quran_tv;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HistoryActivity extends AppCompatActivity {

    private ScrollView scrollRoot;
    private LinearLayout historyContainer; // ظرف برای دکمه‌های تاریخچه

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        scrollRoot = findViewById(R.id.scrollRoot);
        historyContainer = findViewById(R.id.historyContainer);

        // ✅ اگر فایل وجود داشت → بخوان و داخل General1.History بریز
        if (General1.History == null || General1.History.size() == 0) {
            General1.History = HistoryManager.load(this);
        }

        // ✅ ایجاد دکمه‌ها
        showHistoryOnScreen();

        int childCount = historyContainer.getChildCount();
        if (childCount > 0) {
            // اندیس آخرین المان = تعداد کل المان‌ها - ۱
            View lastButton = historyContainer.getChildAt(childCount - 1);
            lastButton.post(() -> {
                lastButton.requestFocus();
                // اسکرول به پایین تا آخرین دکمه فوکوس‌شده دیده شود
                scrollRoot.smoothScrollTo(0, lastButton.getTop());
            });
        }
    }

    /**
     * متد برای ایجاد دکمه‌های پویا از روی لیست General1.History
     */
    private void showHistoryOnScreen() {
        historyContainer.removeAllViews();
        Context context = this;

        for (String historyItem : General1.History) {
            Button historyBtn = new Button(context);

            // ✅ عنوان دکمه: متن همان آیتم لیست + " | Start Here"
            historyBtn.setText(historyItem + " | Start Here");

            // تنظیمات ظاهری دکمه
            historyBtn.setTextSize(18f);
            historyBtn.setTextColor(getResources().getColor(android.R.color.black));
            //historyBtn.setBackgroundColor(getResources().getColor(android.R.color.white));
            historyBtn.setBackgroundResource(R.drawable.history_button_selector);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 10, 0, 10);
            historyBtn.setLayoutParams(params);

            // قابلیت فوکوس
            historyBtn.setFocusable(true);
            historyBtn.setFocusableInTouchMode(true);

            // ✅ منطق کلیک دکمه
            historyBtn.setOnClickListener(v -> {
                // ۱. تجزیه رشته (داخل پرانتز و جدا شده با |) و تنظیم General1.suraIndex و General1.ayaIndex
                if (parseHistoryItemAndSetIndexes(historyItem)) {
                    saveHistory();

                    // ۲. اجرای منطق شروع پلی‌لیست با مقادیر جدید
                    startPlaylistAndDetailsActivity(General1.suraIndex, General1.ayaIndex);
                } else {
                    Toast.makeText(context, "Cannot parse history item: " + historyItem, Toast.LENGTH_LONG).show();
                }
            });

            historyContainer.addView(historyBtn);
        }
    }

    /**
     * تجزیه رشته تاریخچه برای استخراج SuraIndex و AyaIndex.
     */
    private boolean parseHistoryItemAndSetIndexes(String historyItem) {
        try {
            int startIndex = historyItem.lastIndexOf('(');
            int endIndex = historyItem.lastIndexOf(')');

            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                String contentInParentheses = historyItem.substring(startIndex + 1, endIndex).trim();
                String[] parts = contentInParentheses.split("\\|");

                if (parts.length == 2) {
                    int suraIndex = Integer.parseInt(parts[0].trim());
                    int ayaIndex = Integer.parseInt(parts[1].trim());

                    General1.suraIndex = suraIndex;
                    General1.ayaIndex = ayaIndex;

                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * پیاده‌سازی منطق شروع پلی‌لیست و رفتن به DetailsActivity
     */
    private void startPlaylistAndDetailsActivity(int suraIndex, int ayaIndex) {
        if (General1.playlistMain != null) {
            General1.playlistMain.clear();
        }

        // ✅ اجرای دقیق منطق چک و تنظیم مجدد شما
        if (suraIndex > 113 || suraIndex < 0) {
            suraIndex = 0;
            ayaIndex = 0;
        }

        // ✅ اجرای فراخوانی generatePlaylist (با فرض suraIndex بر مبنای 0)
        if(ayaIndex>0)
        QuranPlaylistGenerator.generatePlaylist(1001, suraIndex-1, ayaIndex-1, General1.tartilName, General1.ayaRepeat);
        else
        QuranPlaylistGenerator.generatePlaylist(1001, suraIndex-1, ayaIndex, General1.tartilName, General1.ayaRepeat);

        Intent intent = new Intent(HistoryActivity.this, DetailsActivity.class);
        startActivity(intent);
    }

    private void saveHistory() {
        HistoryManager.save(this, General1.History);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveHistory();
    }
}