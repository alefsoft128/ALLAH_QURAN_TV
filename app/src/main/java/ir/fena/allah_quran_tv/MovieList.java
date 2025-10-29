package ir.fena.allah_quran_tv;

import java.util.ArrayList;
import java.util.List;

public final class MovieList {
    public static final String MOVIE_CATEGORY[] = {
            "SpeedDial 1",
            "SpeedDial 2",
            "SpeedDial 3",
            "SpeedDial 4",
            "SpeedDial 5",
            "SpeedDial 6",
            "SpeedDial 7",
            "SpeedDial 8",
            "SpeedDial 9",
            "SpeedDial 10",
            "SpeedDial 11",
            "SpeedDial 12"
    };

    private static List<Movie> list;
    private static long count = 0;

    public static List<Movie> getList() {
        if (list == null) {
            list = setupMovies();
        }
        return list;
    }

    public static List<Movie> setupMovies() {
        list = new ArrayList<>();
        String title[] = {
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

        String description = "";

        // studio[] پر از رشتهٔ خالی، به اندازهٔ title
        String studio[] = new String[title.length];
        for (int i = 0; i < studio.length; i++) {
            studio[i] = "";
        }

        // videoUrl دقیقا برابر title (بدون تغییر)
        String[] videoUrl = title;

        // مسیرهای لوکال تکراری
        String bgImageUrl[] = new String[title.length];
        String cardImageUrl[] = new String[title.length];
        for (int i = 0; i < title.length; i++) {
            bgImageUrl[i] = "bg1.png";
            cardImageUrl[i] = "btn2.png";
        }

        for (int index = 0; index < title.length; ++index) {
            list.add(
                    buildMovieInfo(
                            title[index],
                            description,
                            studio[index],
                            videoUrl[index],
                            cardImageUrl[index],
                            bgImageUrl[index]));
        }

        return list;
    }


    private static Movie buildMovieInfo(
            String title,
            String description,
            String studio,
            String videoUrl,
            String cardImageUrl,
            String backgroundImageUrl) {
        Movie movie = new Movie();
        movie.setId(count++);
        movie.setTitle(title);
        movie.setDescription(description);
        movie.setStudio(studio);
        movie.setCardImageUrl(cardImageUrl);
        movie.setBackgroundImageUrl(backgroundImageUrl);
        movie.setVideoUrl(videoUrl);
        return movie;
    }
}