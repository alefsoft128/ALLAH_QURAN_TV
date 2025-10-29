package ir.fena.allah_quran_tv;

import android.content.Context;
import org.xmlpull.v1.XmlPullParser;

public class QuranReader {

    private final Context context;

    public QuranReader(Context context) {
        this.context = context;
    }

    /**
     * دریافت متن آیه با شماره سوره و شماره آیه
     */
    public String getAyaText(int suraIndex, int ayaIndex) {
        XmlPullParser parser = context.getResources().getXml(ir.fena.allah_quran_tv.R.xml.quran_simple);
        int eventType;

        int currentSuraIndex = -1;

        try {
            eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();

                    if ("sura".equals(tagName)) {
                        String indexAttr = parser.getAttributeValue(null, "index");
                        if (indexAttr != null) {
                            currentSuraIndex = Integer.parseInt(indexAttr);
                        }
                    } else if ("aya".equals(tagName)) {
                        if (currentSuraIndex == suraIndex) {
                            String ayaAttr = parser.getAttributeValue(null, "index");
                            if (ayaAttr != null && Integer.parseInt(ayaAttr) == ayaIndex) {
                                return parser.getAttributeValue(null, "text");
                            }
                        }
                    }
                }
                eventType = parser.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // اگر آیه پیدا نشد
    }
}
