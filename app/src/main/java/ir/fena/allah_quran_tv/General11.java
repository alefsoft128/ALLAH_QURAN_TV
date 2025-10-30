package ir.fena.allah_quran_tv;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

public class General11 {
    public static int getSuraIndex(String query, String[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].contains(query)) {  // یا equals اگر دقیق بخوای
                return i;
            }
        }
        return -1;  // اگر پیدا نشد
    }

    public static String getAudioFilePath(String customBase, int customSura, int customAya) {
        String suraStr = String.format("%03d", customSura);
        String ayaStr = String.format("%03d", customAya);
        return customBase + "/" + suraStr + "/" + suraStr + ayaStr + ".mp3";
    }
    //String message =General1.getAudioFilePath("sounds/afasy/1",General1.suraIndex,General1.ayaIndex);
    //String p1 = General1.getAudioFilePath("sounds/"+General1.tartilName+"/1",General1.suraIndex,General1.ayaIndex);
    //Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

    public static  List<List<Object>> playlistMain;
    // شماره سوره (۰ تا ۱۱۳)
    public static int suraIndex = 0;

    // شماره آیه (۱ تا تعداد آیات هر سوره)
    public static int ayaIndex = 0;
    public static int currentIndexMain = -1;
    public static int maxAyaIndexMain = -1;





    public static String tartilName="afasy";
    public static int fontNameIndex= 0;
    public static int fontSize = 40;
    public static int ayaRepeat = 1;
    public static int styleArabicText = 0;
    public static int styleTranslateText = 0;
    public static int playerSpeed = 2;

    public static final float[] speedOptions = {0.5f, 0.75f, 1.0f,1.25f, 1.5f, 2.0f};


    public static int translatorIndex = 7;
    public static int animateStyleIndex = 5;


    public static List<String> History = new ArrayList<String>();


    //private Context context;
    public static String obbPathMain =  "/main.1.ir.fena.allah_quran_tv.obb";

    public static void trimHistory() {
        // اگر لیست null نیست و تعدادش بیشتر از 200 است
        if (General11.History != null && General11.History.size() > 313) {
            // حذف 100 خط اول
            for (int i = 0; i < 100; i++) {
                General11.History.remove(0);
            }
        }
    }

    public static List<List<Object>> createRepeatedPlaylist(int repeatCount) {
        // 1. بررسی نال بودن General1.playlistMain
        if (General11.playlistMain == null) {
            System.err.println("خطا: General1.playlistMain نال است.");
            return new ArrayList<>();
        }

        // 2. بررسی ضریب تکرار
        if (repeatCount <= 0) {
            System.err.println("خطا: تعداد تکرار باید یک عدد مثبت باشد.");
            return new ArrayList<>();
        }

        // 3. لیست جدیدی با ساختار List<List<Object>> برای ذخیره نتایج تکرار شده
        List<List<Object>> repeatedPlaylist = new ArrayList<>();

        // 4. پیمایش لیست اصلی
        for (List<Object> originalItem : General11.playlistMain) {

            // اطمینان از معتبر بودن آیتم (حداقل حاوی عنصر 0 به عنوان نام آهنگ)
            if (originalItem == null || originalItem.size() == 0) {
                continue; // از آیتم‌های خالی یا نال صرف نظر کن
            }

            // 5. تکرار هر آیتم (List<Object> کامل) به تعداد مشخص شده
            for (int i = 0; i < repeatCount; i++) {
                // نکته: اگر می‌خواهید آیتم را با همان داده‌های دیگر (مثلاً زمان، هنرمند) تکرار کنید،
                // باید یک کپی (Deep Copy) از originalItem بگیرید.
                // در اینجا فرض می‌کنیم یک کپی ساده کافی است:
                repeatedPlaylist.add(new ArrayList<>(originalItem));
            }
        }

        return repeatedPlaylist;
    }

    //String newAyaText = General1.getAyaText(ctx, 2, 3);
    public static String getAyaText(Context context, int suraIndex, int ayaIndex) {
        try {
            XmlResourceParser parser =
                    context.getResources().getXml(R.xml.quran_simple);

            int eventType = parser.getEventType();
            boolean inTargetSura = false;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tag = parser.getName();

                    if (tag.equals("sura")) {
                        int currentSura = Integer.parseInt(parser.getAttributeValue(null, "index"));
                        inTargetSura = (currentSura == suraIndex);
                    } else if (inTargetSura && tag.equals("aya")) {
                        int currentAya = Integer.parseInt(parser.getAttributeValue(null, "index"));
                        if (currentAya == ayaIndex) {
                            String text = parser.getAttributeValue(null, "text");
                            parser.close();
                            return text;
                        }
                    }
                }
                eventType = parser.next();
            }

            parser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static final String[] TRANSLATOR_PATHS = new String[]{
            "zh_jian.xml",
            "de_zaidan.xml",
            "en_yusufali.xml",
            "es_cortes.xml",
            "fa_fooladvand.xml",
            "ru_porokhova.xml",
            "tr_yazir.xml"
    };

    public static String getAyaTranslation(Context context, int suraIndex, int ayaIndex, int translatorIndex) {
        // اگر translatorIndex معتبر نیست، رشته خالی بده
        if (translatorIndex < 0 || translatorIndex >= TRANSLATOR_PATHS.length) {
            return "";
        }

        // نام فایل XML بدون پسوند
        String fileName = TRANSLATOR_PATHS[translatorIndex].replace(".xml", "");

        try {
            // بارگذاری parser از res/xml
            int resId = context.getResources().getIdentifier(fileName, "xml", context.getPackageName());
            if (resId == 0) return "";

            XmlResourceParser parser = context.getResources().getXml(resId);

            int eventType = parser.getEventType();
            boolean inTargetSura = false;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tag = parser.getName();

                    if ("sura".equals(tag)) {
                        String idxStr = parser.getAttributeValue(null, "index");
                        if (idxStr != null) {
                            int currentSura = Integer.parseInt(idxStr);
                            inTargetSura = (currentSura == suraIndex);
                        }
                    } else if (inTargetSura && "aya".equals(tag)) {
                        String idxStr = parser.getAttributeValue(null, "index");
                        if (idxStr != null && Integer.parseInt(idxStr) == ayaIndex) {
                            String text = parser.getAttributeValue(null, "text");
                            parser.close();
                            return text != null ? text : "";
                        }
                    }
                }
                eventType = parser.next();
            }

            parser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }


    public static boolean isSuraChanged(int aya, int sura){
        return aya > ayaCountMain[sura - 1];
    }

    public static final int[] ayaCountMain = {
            7,286,200,176,120,165,206,75,129,109,123,111,43,52,99,128,111,110,98,135,
            112,78,118,64,77,227,93,88,69,60,34,30,73,54,45,83,182,88,75,85,54,53,89,59,
            37,35,38,29,18,45,60,49,62,55,78,96,29,22,24,13,14,11,11,18,12,12,30,52,52,
            44,28,28,20,56,40,31,50,40,46,42,29,19,36,25,22,17,19,26,30,20,15,21,11,8,8,19,
            5,8,8,11,11,8,8,3,9,5,4,7,3,6,3,5,5,6
    };

    public static List<String> repeatPlayList(List<String> originalTracks, int repeatCount) {
        // 1. بررسی نال نبودن لیست ورودی
        if (originalTracks == null) {
            System.out.println("لیست ورودی (originalTracks) نال است. یک لیست خالی برگردانده می‌شود.");
            return new ArrayList<>(); // برگرداندن یک لیست خالی به جای نال
        }

        // 2. بررسی معتبر بودن عدد تکرار
        if (repeatCount <= 0) {
            System.out.println("تعداد تکرار (repeatCount) باید یک عدد مثبت باشد. یک لیست خالی برگردانده می‌شود.");
            return new ArrayList<>();
        }

        // 3. ایجاد لیست جدید برای ذخیره نتایج تکرار شده
        List<String> repeatedList = new ArrayList<>();

        // 4. پیمایش لیست اصلی
        for (String track : originalTracks) {
            // 5. تکرار هر خط به تعداد مشخص شده
            for (int i = 0; i < repeatCount; i++) {
                repeatedList.add(track);
            }
        }

        return repeatedList;
    }

    // آرایه فونت‌ها
    public static String[] fontFiles = {
            "file:///android_asset/fonts/ScheherazadeNew-Regular.ttf",
            "file:///android_asset/fonts/Noor_Zar.ttf",
            "file:///android_asset/fonts/AmiriQuran-Regular.ttf",
            "file:///android_asset/fonts/kfgqpchafsuthmanicscript_regula.otf",
            "file:///android_asset/fonts/NotoNaskhArabic-VariableFont_wght.ttf",
            "file:///android_asset/fonts/pdms-saleem-quranfont.otf"
    };

    // نام فونت‌ها برای font-family
    public static String[] fontNames = {
            "ScheherazadeNew",
            "NoorZar",
            "AmiriQuran",
            "UthmanicScript",
            "NotoNaskhArabic",
            "PDMS_Saleem"
    };

    public static String getMainCss() {
        // اطمینان از ایندکس معتبر
        int index = fontNameIndex;
        if (index < 0 || index >= fontFiles.length) index = 0;

        String fontFile = fontFiles[index];
        String fontName = fontNames[index];

        // تشخیص فرمت فونت برای CSS
        String format;
        if (fontFile.endsWith(".ttf")) format = "truetype";
        else if (fontFile.endsWith(".otf")) format = "opentype";
        else format = "truetype";

        // تولید CSS فونت پایه + کلاس‌ها
        return "@font-face {" +
                "font-family: '" + fontName + "';" +
                "src: url('" + fontFile + "') format('" + format + "');" +
                "}" +
                "body{background-color: transparent !important; text-align: center; font-size: "+String.valueOf(fontSize)+"px;}"+
                ".ar1 {" +
                "    font-family: '" + fontName + "';" +  // فونت عربی
                "    font-size: " + fontSize + "px;" +
                "    text-align: center;" +
                "    direction: rtl;" +
                "}" +
                ".tr1 {" +
                "    font-family: 'Roboto', sans-serif;" +  // فونت ترجمه انگلیسی
                "    font-size: " + (fontSize - (fontSize/5)) + "px;" +
                "    text-align: center;" +
                "    direction: ltr;" +
                "}";
    }



        public static String[] ANIMATION_STYLES = {
                "animate__animated animate__fadeInUp",
                "animate__animated animate__fadeIn",
                "animate__animated animate__bounceIn",
                "animate__animated animate__zoomIn",
                "animate__animated animate__slideInLeft",
                "animate__animated animate__flipInX"
        };


    public static String getInitialHtml(String quranText,String translateText) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/css/animate.min.css\" />" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/css/myStyles.css\" />" +
                "<style type=\"text/css\">" +
                getMainCss() + // فونت پایه از متغیر گلوبال
                "body { margin:0; padding:0; background-color: transparent; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='"+ANIMATION_STYLES[animateStyleIndex]+" animate__slow'>" +
                // خط اصلاح شده برای متن عربی:
                "<div class='ar1 qqqq" + styleArabicText + "'>" + quranText + "</div>" +
                // خط اصلاح شده برای متن ترجمه:
                "<div class='tr1 tttt" + styleTranslateText + "'>" + translateText + "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }


    public static String titles[] = {
                "1 - الفاتحة | Al-Fatiha",
                "2 - البقرة | Al-Baqarah",
                "3 - آل عمران | Aal-E-Imran",
                "4 - النساء | An-Nisa",
                "5 - المائدة | Al-Ma'idah",
                "6 - الأنعام | Al-An'am",
                "7 - الأعراف | Al-A'raf",
                "8 - الأنفال | Al-Anfal",
                "9 - التوبة | At-Tawbah",
                "10 - يونس | Yunus",
                "11 - هود | Hud",
                "12 - يوسف | Yusuf",
                "13 - الرعد | Ar-Ra'd",
                "14 - إبراهيم | Ibrahim",
                "15 - الحجر | Al-Hijr",
                "16 - النحل | An-Nahl",
                "17 - الإسراء | Al-Isra",
                "18 - الكهف | Al-Kahf",
                "19 - مريم | Maryam",
                "20 - طه | Taha",
                "21 - الأنبياء | Al-Anbiya",
                "22 - الحج | Al-Hajj",
                "23 - المؤمنون | Al-Mu’minun",
                "24 - النور | An-Nur",
                "25 - الفرقان | Al-Furqan",
                "26 - الشعراء | Ash-Shu'ara",
                "27 - النمل | An-Naml",
                "28 - القصص | Al-Qasas",
                "29 - العنكبوت | Al-Ankabut",
                "30 - الروم | Ar-Rum",
                "31 - لقمان | Luqman",
                "32 - السجدة | As-Sajda",
                "33 - الأحزاب | Al-Ahzab",
                "34 - سبأ | Saba",
                "35 - فاطر | Fatir",
                "36 - يس | Ya-Sin",
                "37 - الصافات | As-Saffat",
                "38 - ص | Sad",
                "39 - الزمر | Az-Zumar",
                "40 - غافر | Ghafir",
                "41 - فصلت | Fussilat",
                "42 - الشورى | Ash-Shura",
                "43 - الزخرف | Az-Zukhruf",
                "44 - الدخان | Ad-Dukhan",
                "45 - الجاثية | Al-Jathiya",
                "46 - الأحقاف | Al-Ahqaf",
                "47 - محمد | Muhammad",
                "48 - الفتح | Al-Fath",
                "49 - الحجرات | Al-Hujurat",
                "50 - ق | Qaf",
                "51 - الذاريات | Adh-Dhariyat",
                "52 - الطور | At-Tur",
                "53 - النجم | An-Najm",
                "54 - القمر | Al-Qamar",
                "55 - الرحمن | Ar-Rahman",
                "56 - الواقعة | Al-Waqi'a",
                "57 - الحديد | Al-Hadid",
                "58 - المجادلة | Al-Mujadila",
                "59 - الحشر | Al-Hashr",
                "60 - الممتحنة | Al-Mumtahina",
                "61 - الصف | As-Saff",
                "62 - الجمعة | Al-Jumu'a",
                "63 - المنافقون | Al-Munafiqun",
                "64 - التغابن | At-Taghabun",
                "65 - الطلاق | At-Talaq",
                "66 - التحريم | At-Tahrim",
                "67 - الملك | Al-Mulk",
                "68 - القلم | Al-Qalam",
                "69 - الحاقة | Al-Haqqah",
                "70 - المعارج | Al-Ma'arij",
                "71 - نوح | Nuh",
                "72 - الجن | Al-Jinn",
                "73 - المزمل | Al-Muzzammil",
                "74 - المدثر | Al-Muddaththir",
                "75 - القيامة | Al-Qiyama",
                "76 - الإنسان | Al-Insan",
                "77 - المرسلات | Al-Mursalat",
                "78 - النبأ | An-Naba",
                "79 - النازعات | An-Nazi'at",
                "80 - عبس | Abasa",
                "81 - الإنشقاق | Al-Inshiqaq",
                "82 - الإنفطار | Al-Infitar",
                "83 - المطففين | Al-Mutaffifin",
                "84 - الإنشراح | Ash-Sharh",
                "85 - البروج | Al-Buruj",
                "86 - الطارق | At-Tariq",
                "87 - الأعلى | Al-A'la",
                "88 - الغاشية | Al-Ghashiyah",
                "89 - الفجر | Al-Fajr",
                "90 - البلد | Al-Balad",
                "91 - الشمس | Ash-Shams",
                "92 - الليل | Al-Lail",
                "93 - الضحى | Ad-Duha",
                "94 - الإنشراح | Ash-Sharh",
                "95 - التين | At-Tin",
                "96 - العلق | Al-Alaq",
                "97 - القدر | Al-Qadr",
                "98 - البينة | Al-Bayyina",
                "99 - الزلزلة | Az-Zalzalah",
                "100 - العاديات | Al-Adiyat",
                "101 - القارعة | Al-Qari'ah",
                "102 - التكاثر | At-Takathur",
                "103 - العصر | Al-Asr",
                "104 - الهمزة | Al-Humazah",
                "105 - الفيل | Al-Fil",
                "106 - قريش | Quraish",
                "107 - الماعون | Al-Ma'un",
                "108 - الكوثر | Al-Kawthar",
                "109 - الكافرون | Al-Kafirun",
                "110 - النصر | An-Nasr",
                "111 - المسد | Al-Masad",
                "112 - الإخلاص | Al-Ikhlas",
                "113 - الفلق | Al-Falaq",
                "114 - الناس | An-Nas"
        };

    public static final Object[][] SURAHS = {
            {"الفاتحة", 7},
            {"البقرة", 286},
            {"آل عمران", 200},
            {"النساء", 176},
            {"المائدة", 120},
            {"الأنعام", 165},
            {"الأعراف", 206},
            {"الأنفال", 75},
            {"التوبة", 129},
            {"يونس", 109},
            {"هود", 123},
            {"يوسف", 111},
            {"إبراهيم", 52},
            {"الحجر", 99},
            {"النحل", 128},
            {"الإسراء", 111},
            {"الكهف", 110},
            {"مريم", 98},
            {"طه", 135},
            {"الأنبياء", 112},
            {"الحج", 78},
            {"المؤمنون", 118},
            {"النّور", 64},
            {"الفرقان", 77},
            {"الشعراء", 227},
            {"النمل", 93},
            {"القصص", 88},
            {"العنكبوت", 69},
            {"الروم", 60},
            {"لقمان", 34},
            {"السجدة", 30},
            {"الأحزاب", 73},
            {"سبأ", 54},
            {"فاطر", 45},
            {"يس", 83},
            {"الصافات", 182},
            {"ص", 88},
            {"الزمر", 75},
            {"غافر", 85},
            {"فصلت", 54},
            {"الشورى", 53},
            {"الزخرف", 89},
            {"الدّخان", 59},
            {"الجاثية", 37},
            {"الأحقاف", 35},
            {"محمد", 38},
            {"الفتح", 29},
            {"الحجرات", 18},
            {"ق", 45},
            {"الذاريات", 60},
            {"الطور", 49},
            {"النجم", 62},
            {"القمر", 55},
            {"الرحمن", 78},
            {"الواقعة", 96},
            {"الحديد", 29},
            {"المجادلة", 22},
            {"الحشر", 24},
            {"الممتحنة", 13},
            {"الصف", 14},
            {"الجمعة", 11},
            {"المنافقون", 11},
            {"التغابن", 18},
            {"الطلاق", 12},
            {"التحريم", 12},
            {"الملك", 30},
            {"القلم", 52},
            {"الحاقة", 52},
            {"المعارج", 44},
            {"نوح", 28},
            {"الجن", 28},
            {"المزّمّل", 20},
            {"المدّثر", 56},
            {"القيامة", 40},
            {"الإنسان", 31},
            {"المرسلات", 50},
            {"النبأ", 40},
            {"النازعات", 46},
            {"عبس", 42},
            {"التكوير", 29},
            {"الانفطار", 19},
            {"المطفّفين", 36},
            {"الانشقاق", 25},
            {"البروج", 22},
            {"الطارق", 17},
            {"الأعلى", 19},
            {"الغاشية", 26},
            {"الفجر", 30},
            {"البلد", 20},
            {"الشمس", 15},
            {"الليل", 21},
            {"الضحى", 11},
            {"الشرح", 8},
            {"التين", 8},
            {"العلق", 19},
            {"القدر", 5},
            {"البينة", 8},
            {"الزلزلة", 8},
            {"العاديات", 11},
            {"القارعة", 11},
            {"التكاثر", 8},
            {"العصر", 3},
            {"الهمزة", 9},
            {"الفيل", 5},
            {"قريش", 4},
            {"الماعون", 7},
            {"الكوثر", 3},
            {"الكافرون", 6},
            {"النصر", 3},
            {"المسد", 5},
            {"الإخلاص", 4},
            {"الفلق", 5},
            {"الناس", 6}
    };


    public static int[] getAyaAndSura(String path) {
        // گرفتن نام فایل بدون پسوند
        String fileName = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));

        // فرض بر اینکه همیشه 6 کاراکتر داره (مثل 001002)
        String part1 = fileName.substring(0, 3);
        String part2 = fileName.substring(3, 6);

        // تبدیل به int و برگرداندن آرایه
        return new int[]{Integer.parseInt(part1), Integer.parseInt(part2)};
    }


    public static void loadSettings(SharedPreferences prefs) {
        General11.tartilName = prefs.getString("tartilName", General11.tartilName);
        General11.fontNameIndex = prefs.getInt("fontNameIndex", General11.fontNameIndex);
        General11.fontSize = prefs.getInt("fontSize", General11.fontSize);
        General11.ayaRepeat = prefs.getInt("ayaRepeat", General11.ayaRepeat);
        General11.styleArabicText = prefs.getInt("styleArabicText", General11.styleArabicText);
        General11.styleTranslateText = prefs.getInt("styleTranslateText", General11.styleTranslateText);
        General11.playerSpeed = prefs.getInt("playerSpeed", General11.playerSpeed);
        General11.translatorIndex = prefs.getInt("translatorIndex", General11.translatorIndex);
        General11.animateStyleIndex = prefs.getInt("animateStyleIndex", General11.animateStyleIndex);
        General11.ayaIndex = prefs.getInt("ayaIndex", General11.ayaIndex);
        General11.suraIndex = prefs.getInt("suraIndex", General11.suraIndex);
    }




}

