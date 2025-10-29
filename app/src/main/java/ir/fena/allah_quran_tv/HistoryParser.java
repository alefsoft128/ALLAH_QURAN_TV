package ir.fena.allah_quran_tv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryParser {

    public static class HistoryItem {
        public String dateTime;
        public String suraTitle;
        public int ayaIndex;
        public String tartilName;
    }

    public static List<HistoryItem> readHistory(File file) {
        List<HistoryItem> list = new ArrayList<>();
        if (!file.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length != 4) continue;
                long timestamp = Long.parseLong(parts[0]);
                int suraIndex = Integer.parseInt(parts[1]);
                int ayaIndex = Integer.parseInt(parts[2]);
                String tartil = parts[3];

                HistoryItem item = new HistoryItem();
                Date date = new Date(timestamp);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
                item.dateTime = sdf.format(date);

                item.suraTitle = (suraIndex >=0 && suraIndex < General1.titles.length) ? General1.titles[suraIndex] : "Unknown";
                item.ayaIndex = ayaIndex;
                item.tartilName = tartil;

                list.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
