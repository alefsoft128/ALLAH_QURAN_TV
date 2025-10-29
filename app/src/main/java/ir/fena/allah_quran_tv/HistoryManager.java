package ir.fena.allah_quran_tv;

import android.content.Context;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {

    private static final String FILE_NAME = "history.db";

    // ذخیره لیست در فایل
    public static void save(Context ctx, List<String> list) {
        try {
            FileOutputStream fos = ctx.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            for (String item : list) {
                osw.write(item + "\n");
            }
            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // خواندن لیست از فایل
    public static List<String> load(Context ctx) {
        List<String> result = new ArrayList<>();
        try {
            FileInputStream fis = ctx.openFileInput(FILE_NAME);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
            br.close();
        } catch (Exception ignored) {
            // اگر فایل نبود خطا نمی‌گیریم
        }
        return result;
    }
}
