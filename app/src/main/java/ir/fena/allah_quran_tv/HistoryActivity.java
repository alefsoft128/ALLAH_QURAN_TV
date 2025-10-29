package ir.fena.allah_quran_tv;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HistoryActivity extends AppCompatActivity {

    private ScrollView scrollRoot;
    private TextView historyContent;
    private Button continueBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        scrollRoot = findViewById(R.id.scrollRoot);
        historyContent = findViewById(R.id.historyContent);
        continueBtn = findViewById(R.id.continueBtn);

        // ✅ اگر فایل وجود داشت → بخوان و داخل General1.History بریز
        if (General1.History == null || General1.History.size() == 0) {
            General1.History = HistoryManager.load(this);
        }

        // ✅ نمایش روی صفحه
        showHistoryOnScreen();

        // ✅ فوکوس اولیه روی دکمه
        continueBtn.post(() -> {
            continueBtn.requestFocus();
            scrollRoot.smoothScrollTo(0, continueBtn.getTop());
        });

        continueBtn.setOnClickListener(v -> {
            saveHistory();
            finish();
        });
    }

    private void showHistoryOnScreen() {
        StringBuilder b = new StringBuilder();
        for (String item : General1.History) {
            b.append(item).append("\n");
        }
        historyContent.setText(b.toString());
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
