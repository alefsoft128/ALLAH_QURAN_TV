package ir.fena.allah_quran_tv;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.GridLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import java.util.List;

public class IntermediateActivity extends AppCompatActivity {

    private Movie selectedMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_intermediate);

        // گرفتن Movie اصلی از General1 یا Intent
        //selectedMovie = General1.selectedMovie; // مطمئن شو قبل از باز کردن این Activity ست شده باشد

        GridLayout gridLayout = findViewById(R.id.grid_button_container);
        //int numberOfAyas = getNumberOfAyas(General1.suraIndex+1);
        int numberOfAyas = getNumberOfAyas(General1.suraIndex+1);

        for (int i = 1; i <= numberOfAyas; i++) {
            Button ayaButton = new Button(this);
            ayaButton.setText(String.valueOf(i));
            ayaButton.setAllCaps(false);
            ayaButton.setGravity(Gravity.CENTER);
            ayaButton.setTextSize(40f);
            ayaButton.setMinWidth(200);
            ayaButton.setMinHeight(200);
            ayaButton.setFocusable(true);

            ayaButton.setFocusableInTouchMode(true);
            ayaButton.setBackgroundResource(R.drawable.button_background);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = GridLayout.LayoutParams.WRAP_CONTENT;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.setMargins(4, 4, 4, 4);
            ayaButton.setLayoutParams(params);

            int finalI = i;
            ayaButton.setOnClickListener(v -> {
                General1.ayaIndex = finalI-1;

                //General1.playlistMain.clear();

                if (General1.playlistMain != null) {
                    General1.playlistMain.clear();
                }
                //public static void generatePlaylist(int limit, int suraStartIndex, int ayaStartIndex, String tartilName, int ayaRepeat);
                if (General1.suraIndex>113 || General1.suraIndex<0) {General1.suraIndex = 0; General1.ayaIndex = 0;}
                QuranPlaylistGenerator.generatePlaylist(1001, General1.suraIndex, General1.ayaIndex, General1.tartilName,General1.ayaRepeat);

                Intent intent = new Intent(IntermediateActivity.this, DetailsActivity.class);
                intent.putExtra(DetailsActivity.MOVIE, selectedMovie);

                // Shared Element Transition
                ActivityOptionsCompat bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        IntermediateActivity.this,
                        ayaButton,
                        DetailsActivity.SHARED_ELEMENT_NAME
                );

                startActivity(intent, bundle.toBundle());
            });

            gridLayout.addView(ayaButton);
            if (i==1) {
                ayaButton.requestFocus();
                ayaButton.requestFocusFromTouch();
            }

        }
    }

    private int getNumberOfAyas(int suraIndex) {
        int[] ayasPerSura = {
                7,286,200,176,120,165,206,75,129,109,123,111,43,52,99,128,111,110,98,135,
                112,78,118,64,77,227,93,88,69,60,34,30,73,54,45,83,182,88,75,85,54,53,89,59,
                37,35,38,29,18,45,60,49,62,55,78,96,29,22,24,13,14,11,11,18,12,12,30,52,52,
                44,28,28,20,56,40,31,50,40,46,42,29,19,36,25,22,17,19,26,30,20,15,21,11,8,8,19,
                5,8,8,11,11,8,8,3,9,5,4,7,3,6,3,5,5,6
        };
        if (suraIndex >= 1 && suraIndex <= 114) {
            return ayasPerSura[suraIndex - 1];
        } else {
            return 1;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        General1.suraIndex--;
        QuranPlaylistPlayer.stopPlaylist();
    }

}
