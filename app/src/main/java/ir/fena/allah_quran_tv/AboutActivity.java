package ir.fena.allah_quran_tv;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ScrollView;
import android.widget.TextView;

public class AboutActivity extends Activity {

    private TextView tvAbout;
    private ScrollView scrollView;
    private Handler handler = new Handler();
    private int index = 0;
    private int typingDelay = 25; // میلی‌ثانیه

    // متن کامل داخل کلاس
    private String aboutText = "﴿ \uD83C\uDF38 بِسْمِ اللَّـهِ الرَّ\u200Cحْمَـٰنِ الرَّ\u200Cحِيمِ \uD83C\uDF38 ﴾\n\n"
            + "أَلَمْ تَرَوْا أَنَّ اللَّهَ سَخَّرَ لَكُمْ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ وَأَسْبَغَ عَلَيْكُمْ نِعَمَهُ ظَاهِرَةً وَبَاطِنَةً\n"
            + "وَمِنَ النَّاسِ مَنْ يُجَادِلُ فِي اللَّهِ بِغَيْرِ عِلْمٍ وَلَا هُدًى وَلَا كِتَابٍ مُنِيرٍ ﴿۲۰﴾\n\n"
            + "الهم صل علی محمد و آله\n🕊️ About\n\n"
            + "This app is dedicated to:\n'بقیه‌الله فی الأرضه' —\n"
            + "and to my parents, my spouse, and all believers.\n\n"
            + "📩 Contact:\nalefsoft@protonmail.com\n\n"
            + "Very Thanks To\n\n"
            + "🌸 Open AI 🌸\nhttps://openai.com/\n\n"
            + "🌸 King Fahd Complex 🌸\nhttps://qurancomplex.gov.sa/\n\n"
            + "🌸 SIL International 🌸\nhttps://software.sil.org/scheherazade/\n\n"
            + "🌸 Google Android Team 🌸\nhttps://www.android.com/\n\n"
            + "🌸 Tanzil Team 🌸\nhttps://tanzil.net/docs/quran_metadata\n\n"
            + "🌸 Zekr Project 🌸\nhttps://sourceforge.net/projects/zekr/\n\n"
            + "🌸 Quran.com Team 🌸\nhttps://quran.com/\n\n"
            + "🌸 Noor Soft Teams 🌸\nhttps://www.noorsoft.org/en/Default\n\n"
            + "🌸 Motion Backgrounds Teams 🌸\nhttps://motionbgs.com/\n\n"
            + "🌸 Amiri Project 🌸\nhttps://github.com/aliftype/amiri\n\n"
            + "🌸 Google Gemini and AI team 🌸\nhttps://gemini.google.com/\n\n"
            + "🌸 and many others 🌸\n\n\n\n"
            + "🌸 Free and Opensource: https://github.com/alefsoft128/ALLAH_QURAN_TV/ 🌸\n"
            + "📩 Contact 📩\nalefsoft@protonmail.com\n\n"
            ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        tvAbout = findViewById(R.id.tvAbout);
        scrollView = findViewById(R.id.scrollAboutRoot);

        startTyping();
    }

    private void startTyping() {
        index = 0;
        tvAbout.setText("");
        handler.postDelayed(typingRunnable, typingDelay);
    }

    private Runnable typingRunnable = new Runnable() {
        @Override
        public void run() {
            if (index <= aboutText.length()) {
                tvAbout.setText(aboutText.substring(0, index));
                index++;

                // Scroll به پایین
                scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));

                handler.postDelayed(this, typingDelay);
            }
        }
    };
}
