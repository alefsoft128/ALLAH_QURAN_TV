package ir.fena.allah_quran_tv;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * فرگمنت ساده و مستقل برای نمایش اطلاعات فیلم
 * فقط عنوان فیلم را وسط صفحه نشان می‌دهد
 */
public class VideoDetailsFragmentSimple extends Fragment {

    private Movie movie;

    public static VideoDetailsFragmentSimple newInstance(Movie movie) {
        VideoDetailsFragmentSimple fragment = new VideoDetailsFragmentSimple();
        Bundle args = new Bundle();
        args.putSerializable("movie", movie);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        if (getArguments() != null) {
            movie = (Movie) getArguments().getSerializable("movie");
        }

        TextView textView = new TextView(getActivity());
        textView.setText(movie != null ? movie.getTitle() : "فیلم انتخاب نشده است");
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(24);
        textView.setTextColor(0xFFFFFFFF);
        textView.setBackgroundColor(0xFF000000);
        textView.setPadding(40, 40, 40, 40);

        return textView;
    }
}
