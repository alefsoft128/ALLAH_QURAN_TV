package ir.fena.allah_quran_tv;

import android.graphics.drawable.Drawable;

import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an Image CardView
 */
public class CardPresenter extends Presenter {
    private static final String TAG = "CardPresenter";

    private static final int CARD_WIDTH = 555;
    private static final int CARD_HEIGHT = 313;
    private static int sSelectedBackgroundColor;
    private static int sDefaultBackgroundColor;
    private Drawable mDefaultCardImage;

    private static void updateCardBackgroundColor(ImageCardView view, boolean selected) {
        int color = selected ? sSelectedBackgroundColor : sDefaultBackgroundColor;
        // Both background colors should be set because the view"s background is temporarily visible
        // during animations.
        view.setBackgroundColor(color);
        view.setInfoAreaBackgroundColor(color);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Log.d(TAG, "onCreateViewHolder");

        sDefaultBackgroundColor =
                ContextCompat.getColor(parent.getContext(), R.color.default_background);
        sSelectedBackgroundColor =
                ContextCompat.getColor(parent.getContext(), R.color.selected_background);
        /*
         * This template uses a default image in res/drawable, but the general case for Android TV
         * will require your resources in xhdpi. For more information, see
         * https://developer.android.com/training/tv/start/layouts.html#density-resources
         */
        mDefaultCardImage = ContextCompat.getDrawable(parent.getContext(), R.drawable.movie);

        ImageCardView cardView =
                new ImageCardView(parent.getContext()) {
                    @Override
                    public void setSelected(boolean selected) {
                        updateCardBackgroundColor(this, selected);
                        super.setSelected(selected);
                    }
                };

        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        updateCardBackgroundColor(cardView, false);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        Movie movie = (Movie) item;
        ImageCardView cardView = (ImageCardView) viewHolder.view;

        Log.d(TAG, "onBindViewHolder");

        if (movie.getCardImageUrl() != null) {

            // -------------------------
            // 1. متن پایین (info area)
            // -------------------------
            cardView.setTitleText(movie.getTitle());
            cardView.setContentText(movie.getStudio());
            cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);

            // -------------------------
            // 2. بارگذاری تصویر با Glide
            // -------------------------
            Glide.with(cardView.getContext())
                    .load(movie.getCardImageUrl())
                    .centerCrop()
                    .error(mDefaultCardImage)
                    .into(cardView.getMainImageView());

            // -------------------------
            // 3. اضافه کردن متن overlay روی عکس
            // -------------------------
            ViewGroup imageContainer = (ViewGroup) cardView.getMainImageView().getParent();
            android.widget.TextView overlayText = null;

            // بررسی اینکه TextView قبلاً اضافه شده یا نه
            for (int i = 0; i < imageContainer.getChildCount(); i++) {
                View child = imageContainer.getChildAt(i);
                if (child.getTag() != null && child.getTag().equals("overlayTitle")) {
                    overlayText = (android.widget.TextView) child;
                    break;
                }
            }

            if (overlayText == null) {
                overlayText = new android.widget.TextView(cardView.getContext());
                overlayText.setTag("overlayTitle");
                overlayText.setTextColor(android.graphics.Color.WHITE);
                overlayText.setTextSize(16);
                overlayText.setBackgroundColor(0x66000000); // نیمه شفاف مشکی
                overlayText.setPadding(20, 10, 20, 10);

                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                imageContainer.addView(overlayText, params);

                // استفاده از final برای lambda
                final android.widget.TextView overlayTextFinal = overlayText;
                overlayTextFinal.post(() -> {
                    overlayTextFinal.setY(cardView.getMainImageView().getHeight() - overlayTextFinal.getHeight() - 10);
                    overlayTextFinal.setX(0);
                });
            }

            // مقدار متن overlay همیشه از title پایین کارت گرفته شود
            overlayText.setText(cardView.getTitleText());

            // -------------------------
            // 4. انیمیشن و فیلتر رنگ هنگام فوکوس
            // -------------------------
            cardView.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    // افکت تغییر تدریجی alpha
                    android.animation.ObjectAnimator.ofFloat(
                                    cardView.getMainImageView(), "alpha", 1f, 0.8f)
                            .setDuration(250).start();

                    // فیلتر طلایی/زرد
                    cardView.getMainImageView().setColorFilter(
                            0x99FFD700, android.graphics.PorterDuff.Mode.OVERLAY);
                } else {
                    // برگشت به حالت اولیه با انیمیشن
                    android.animation.ObjectAnimator.ofFloat(
                                    cardView.getMainImageView(), "alpha", 0.8f, 1f)
                            .setDuration(250).start();

                    // حذف فیلتر
                    cardView.getMainImageView().clearColorFilter();
                }
            });
        }
    }





    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        Log.d(TAG, "onUnbindViewHolder");
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        // Remove references to images so that the garbage collector can free up memory
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }
}