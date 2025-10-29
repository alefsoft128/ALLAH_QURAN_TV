package ir.fena.allah_quran_tv;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Button;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    // translator paths as you provided
    private static final String[] TRANSLATOR_PATHS = new String[]{
            "zh_jian.xml",
            "de_zaidan.xml",
            "en_yusufali.xml",
            "es_cortes.xml",
            "fa_fooladvand.xml",
            "ru_porokhova.xml",
            "tr_yazir.xml"
    };

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("quran_settings", MODE_PRIVATE);

        // load UI from stored values (SharedPreferences -> General1)
        loadSettings();

        // save on button
        Button saveBtn = findViewById(R.id.saveSettings);
        saveBtn.setOnClickListener(v -> {
            saveSettings();
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // auto-save to avoid losing changes when leaving activity
        saveSettings();
    }

    public void loadSettings() {
        // First: load persisted values into General1 (if exist)
        General1.tartilName = prefs.getString("tartilName", General1.tartilName);
        General1.fontNameIndex = prefs.getInt("fontNameIndex", General1.fontNameIndex);
        General1.fontSize = prefs.getInt("fontSize", General1.fontSize);
        General1.ayaRepeat = prefs.getInt("ayaRepeat", General1.ayaRepeat);
        General1.styleArabicText = prefs.getInt("styleArabicText", General1.styleArabicText);
        General1.styleTranslateText = prefs.getInt("styleTranslateText", General1.styleTranslateText);
        General1.playerSpeed = prefs.getInt("playerSpeed", General1.playerSpeed);
        General1.translatorIndex = prefs.getInt("translatorIndex", General1.translatorIndex);
        General1.animateStyleIndex = prefs.getInt("animateStyleIndex", General1.animateStyleIndex);

        // Now reflect General1 values into UI safely (with bounds checks)

        // Tartil
        RadioButton rbAfasy = (RadioButton) findViewById(R.id.radioAfasy);
        RadioButton rbShatri = (RadioButton) findViewById(R.id.radioShatri);
        if ("afasy".equals(General1.tartilName)) {
            rbAfasy.setChecked(true);
        } else {
            rbShatri.setChecked(true);
        }

        // Font index (6 options: radioFont0..radioFont5)
        RadioGroup fontGroup = findViewById(R.id.radioGroupFont);
        int fIdx = clamp(General1.fontNameIndex, 0, fontGroup.getChildCount() - 1);
        ((RadioButton) fontGroup.getChildAt(fIdx)).setChecked(true);

        // Font size
        EditText etFontSize = findViewById(R.id.editTextFontSize);
        etFontSize.setText(String.valueOf(General1.fontSize));

        // Aya repeat
        EditText etAyaRepeat = findViewById(R.id.editTextAyaRepeat);
        etAyaRepeat.setText(String.valueOf(General1.ayaRepeat));

        // Arabic style (6 options)
        RadioGroup arabicGroup = findViewById(R.id.radioGroupArabicStyle);
        int aIdx = clamp(General1.styleArabicText, 0, arabicGroup.getChildCount() - 1);
        ((RadioButton) arabicGroup.getChildAt(aIdx)).setChecked(true);

        // Translate style (6 options)
        RadioGroup transStyleGroup = findViewById(R.id.radioGroupTranslateStyle);
        int tIdx = clamp(General1.styleTranslateText, 0, transStyleGroup.getChildCount() - 1);
        ((RadioButton) transStyleGroup.getChildAt(tIdx)).setChecked(true);

        // Player speed (6 options)
        RadioGroup speedGroup = findViewById(R.id.radioGroupSpeed);
        int sIdx = clamp(General1.playerSpeed, 0, speedGroup.getChildCount() - 1);
        ((RadioButton) speedGroup.getChildAt(sIdx)).setChecked(true);

        // Translator (7 options)
        RadioGroup translatorGroup = findViewById(R.id.radioGroupTranslator);
        int trIdx = clamp(General1.translatorIndex, 0, translatorGroup.getChildCount() - 1);
        ((RadioButton) translatorGroup.getChildAt(trIdx)).setChecked(true);

        // Animation (6 options)
        RadioGroup animationGroup = findViewById(R.id.radioAnim);
        int anIdx = clamp(General1.animateStyleIndex, 0, animationGroup.getChildCount() - 1);
        ((RadioButton) animationGroup.getChildAt(anIdx)).setChecked(true);
    }

    public void saveSettings() {
        try {
            // Tartil
            RadioGroup tartilGroup = findViewById(R.id.radioGroupTartil);
            int tartilChecked = tartilGroup.getCheckedRadioButtonId();
            if (tartilChecked == R.id.radioAfasy) General1.tartilName = "afasy";
            else General1.tartilName = "shatri";

            // Font index
            RadioGroup fontGroup = findViewById(R.id.radioGroupFont);
            int fontCheckedId = fontGroup.getCheckedRadioButtonId();
            General1.fontNameIndex = indexOfChildSafe(fontGroup, fontCheckedId, 0);

            // Font size (validate)
            EditText etFontSize = findViewById(R.id.editTextFontSize);
            String fontSizeText = etFontSize.getText().toString().trim();
            if (!fontSizeText.isEmpty()) {
                try {
                    General1.fontSize = Integer.parseInt(fontSizeText);
                } catch (NumberFormatException ex) {
                    General1.fontSize = 12; // fallback
                }
            }

            // Aya repeat
            EditText etAyaRepeat = findViewById(R.id.editTextAyaRepeat);
            String ayaRepeatText = etAyaRepeat.getText().toString().trim();
            if (!ayaRepeatText.isEmpty()) {
                try {
                    General1.ayaRepeat = Integer.parseInt(ayaRepeatText);
                } catch (NumberFormatException ex) {
                    General1.ayaRepeat = 2; // fallback
                }
            }

            // Arabic style
            RadioGroup arabicGroup = findViewById(R.id.radioGroupArabicStyle);
            General1.styleArabicText = indexOfChildSafe(arabicGroup, arabicGroup.getCheckedRadioButtonId(), 0);

            // Translate style
            RadioGroup transStyleGroup = findViewById(R.id.radioGroupTranslateStyle);
            General1.styleTranslateText = indexOfChildSafe(transStyleGroup, transStyleGroup.getCheckedRadioButtonId(), 0);

            // Player speed
            RadioGroup speedGroup = findViewById(R.id.radioGroupSpeed);
            General1.playerSpeed = indexOfChildSafe(speedGroup, speedGroup.getCheckedRadioButtonId(), 0);

            // Translator index (map to your provided list)
            RadioGroup translatorGroup = findViewById(R.id.radioGroupTranslator);
            General1.translatorIndex = indexOfChildSafe(translatorGroup, translatorGroup.getCheckedRadioButtonId(), 0);
            // (If you want to use file path: TRANSLATOR_PATHS[General1.translatorIndex])

            // Animation
            RadioGroup animGroup = findViewById(R.id.radioAnim);
            General1.animateStyleIndex = indexOfChildSafe(animGroup, animGroup.getCheckedRadioButtonId(), 0);


            // Persist to SharedPreferences
            SharedPreferences.Editor e = prefs.edit();
            e.putString("tartilName", General1.tartilName);
            e.putInt("fontNameIndex", General1.fontNameIndex);
            e.putInt("fontSize", General1.fontSize);
            e.putInt("ayaRepeat", General1.ayaRepeat);
            e.putInt("styleArabicText", General1.styleArabicText);
            e.putInt("styleTranslateText", General1.styleTranslateText);
            e.putInt("playerSpeed", General1.playerSpeed);
            e.putInt("translatorIndex", General1.translatorIndex);
            e.putInt("animateStyleIndex", General1.animateStyleIndex);
            e.putInt("ayaIndex", General1.ayaIndex);
            e.putInt("suraIndex", General1.suraIndex);
            e.apply();
        } catch (Exception ex) {
            // don't crash the activity on unexpected error
            ex.printStackTrace();
        }
    }

    // helper: clamp index between min and max
    private int clamp(int val, int min, int max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }

    // helper: find index of checked child safely; defaultIndex on failure
    private int indexOfChildSafe(RadioGroup group, int checkedId, int defaultIndex) {
        if (checkedId == -1) return defaultIndex;
        try {
            int idx = group.indexOfChild(findViewById(checkedId));
            if (idx < 0) return defaultIndex;
            return idx;
        } catch (Exception ex) {
            return defaultIndex;
        }
    }
}
