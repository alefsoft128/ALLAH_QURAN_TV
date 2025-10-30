package ir.fena.allah_quran_tv;

import java.util.ArrayList;
import java.util.List;

public class QuranPlaylistGenerator1 {

    // تعداد آیات هر سوره
    private static final int[] ayaCount = {
            7,286,200,176,120,165,206,75,129,109,123,111,43,52,99,128,111,110,98,135,
            112,78,118,64,77,227,93,88,69,60,34,30,73,54,45,83,182,88,75,85,54,53,89,59,
            37,35,38,29,18,45,60,49,62,55,78,96,29,22,24,13,14,11,11,18,12,12,30,52,52,
            44,28,28,20,56,40,31,50,40,46,42,29,19,36,25,22,17,19,26,30,20,15,21,11,8,8,19,
            5,8,8,11,11,8,8,3,9,5,4,7,3,6,3,5,5,6
    };

    // سوره‌های استثنا (بر اساس شماره سوره ۱-based)
    private static final int[] excludedSuras = {9};

    /**
     * بررسی می‌کند که آیا سوره (۱-based) جزء سوره‌های استثنا است یا خیر.
     */
    private static boolean isExcluded(int suraNumber) {
        for (int ex : excludedSuras) {
            if (ex == suraNumber) return true;
        }
        return false;
    }

    /**
     * مسیر فایل صوتی «بسم الله الرحمن الرحیم» (سوره ۱، آیه ۱) را ایجاد می‌کند.
     */
    private static String getBasmalahPath(String tartilName) {
        // بسم الله همیشه سوره ۱ آیه ۱ است (۱-based)
        return General1.getAudioFilePath("sounds/" + tartilName + "/1", 1, 1);
    }

    /**
     * مسیر فایل صوتی آیه مشخص شده (۱-based) را ایجاد می‌کند.
     */
    private static String getAyaPath(String tartilName, int suraNumber, int ayaNumber) {
        return General1.getAudioFilePath("sounds/" + tartilName + "/1", suraNumber, ayaNumber);
    }

    /**
     * یک گروه (لیست داخلی) از مسیرهای صوتی تکرار شده ایجاد می‌کند.
     */
    private static List<Object> createRepeatedGroup(String path, int repeatCount) {
        List<Object> group = new ArrayList<>();
        // تکرار باید حداقل ۱ باشد
        int validRepeat = (repeatCount <= 0) ? 1 : repeatCount;
        for (int r = 0; r < validRepeat; r++) {
            group.add(path);
        }
        return group;
    }

    /**
     * لیست پخش اصلی (General1.playlistMain) را بر اساس پارامترهای ورودی ایجاد می‌کند.
     * * @param limit تعداد کل *رشته‌های* صوتی که باید در لیست پخش نهایی وجود داشته باشد (با احتساب تکرارها).
     * @param suraStartIndex ایندکس سوره شروع (0 تا 113).
     * @param ayaStartIndex ایندکس آیه شروع (0 تا N-1).
     * @param tartilName نام قاری یا پوشه ترتیل.
     * @param ayaRepeat تعداد تکرار هر آیه یا بسم‌الله.
     */
    public static void generatePlaylist(int limit, int suraStartIndex, int ayaStartIndex, String tartilName, int ayaRepeat) {

        // --- ۱. آماده‌سازی و اعتبارسنجی ورودی‌ها ---
        if (General1.playlistMain == null) {
            General1.playlistMain = new ArrayList<>();
        } else {
            General1.playlistMain.clear();
        }

        // استفاده از پارامترهای متد به جای متغیرهای General1
        int currentSuraIdx = suraStartIndex;
        int currentAyaIdx = ayaStartIndex;
        int repeatCount = ayaRepeat;
        // String tartilName = tartilName; // متغیر محلی است.

        // اعتبارسنجی و نرمال‌سازی
        if (currentSuraIdx < 0 || currentSuraIdx > 113) {
            currentSuraIdx = 0;
        }
        int maxAyaInCurrentSura = ayaCount[currentSuraIdx];
        if (currentAyaIdx < 0 || currentAyaIdx >= maxAyaInCurrentSura) {
            currentAyaIdx = 0;
        }
        if (repeatCount <= 0) {
            repeatCount = 1;
        }
        int totalStringCount = limit; // نام limit را به totalStringCount تغییر می‌دهیم تا با منطق داخلی یکسان شود.

        int stringsAdded = 0;
        String basmalahPath = getBasmalahPath(tartilName);

        // --- ۲. حلقه اصلی تولید لیست پخش ---

        while (stringsAdded < totalStringCount) {

            // تبدیل ایندکس 0-based به شماره 1-based
            int suraNumber = currentSuraIdx + 1;
            int ayaNumber = currentAyaIdx + 1;

            // --- ۳. افزودن بسم الله (در صورت لزوم) ---
            if (currentAyaIdx == 0 && !isExcluded(suraNumber)) {
                List<Object> basmalahGroup = createRepeatedGroup(basmalahPath, repeatCount);
                General1.playlistMain.add(basmalahGroup);
                stringsAdded += repeatCount;

                // بررسی توقف
                if (stringsAdded >= totalStringCount) {
                    break;
                }
            }

            // --- ۴. افزودن آیه فعلی (با تکرار) ---
            String ayaPath = getAyaPath(tartilName, suraNumber, ayaNumber);
            List<Object> ayaGroup = createRepeatedGroup(ayaPath, repeatCount);
            General1.playlistMain.add(ayaGroup);
            stringsAdded += repeatCount;

            // بررسی توقف
            if (stringsAdded >= totalStringCount) {
                break;
            }

            // --- ۵. رفتن به آیه بعدی و گردش ---
            currentAyaIdx++;

            if (currentAyaIdx >= maxAyaInCurrentSura) {
                currentSuraIdx++;
                currentAyaIdx = 0;

                // منطق گردش: اگر به سوره ۱۱۴ رسیدیم، به سوره ۱ برگرد
                if (currentSuraIdx > 113) {
                    currentSuraIdx = 0;
                }

                // به‌روزرسانی تعداد آیات برای سوره جدید
                maxAyaInCurrentSura = ayaCount[currentSuraIdx];
            }
        }
    }
}

/*

public static void generatePlaylist(int limit, int suraStartIndex, int ayaStartIndex, String tartilName, int ayaRepeat);

QuranPlaylistGenerator.generatePlaylist(50, 1, 0, "minshawi", 3);


        // دسترسی به خروجی نهایی:
        System.out.println("پلی‌لیست تولید شده. تعداد گروه‌های آیتم: " + General1.playlistMain.size());
        // ... (ادامه کد برای استفاده از General1.playlistMain)
 */