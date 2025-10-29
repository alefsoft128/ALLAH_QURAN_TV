package ir.fena.allah_quran_tv;

import android.content.Context;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;

/**
 * خواندن ترجمه قرآن از فایل‌های xml
 * ساختار فایل مشابه QuranReader است:
 * <quran>
 *   <sura index="1" name="">
 *       <aya index="1" text="..."/>
 *   </sura>
 * </quran>
 *
 * فایل‌ها داخل res/xml/translations/ قرار دارند.
 */
public class TranslationReader {

    private final Context context;

    public TranslationReader(Context context) {
        this.context = context;
    }

    /**
     * گرفتن متن ترجمه آیه بر اساس نام فایل، شماره سوره و شماره آیه
     *
     * @param fileName نام فایل xml بدون پسوند (مثلا "en_saheeh")
     * @param suraIndex شماره سوره
     * @param ayaIndex شماره آیه
     * @return متن ترجمه یا null اگر پیدا نشد
     */
    public String getAyaTranslation(String fileName, int suraIndex, int ayaIndex) {
        // گرفتن resource id از نام فایل
        int resId = context.getResources().getIdentifier(
                fileName, "xml", context.getPackageName()
        );

        if (resId == 0) return null; // فایل پیدا نشد

        XmlResourceParser parser = context.getResources().getXml(resId);
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
