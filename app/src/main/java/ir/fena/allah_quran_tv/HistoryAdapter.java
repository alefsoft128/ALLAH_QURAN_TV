package ir.fena.allah_quran_tv;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryVH> {

    private final List<HistoryParser.HistoryItem> items;

    public HistoryAdapter(List<HistoryParser.HistoryItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public HistoryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new HistoryVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryVH holder, int position) {
        HistoryParser.HistoryItem item = items.get(position);
        holder.tv1.setText(item.dateTime + " - " + item.suraTitle + " : " + item.ayaIndex);
        holder.tv2.setText("Tartil: " + item.tartilName);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HistoryVH extends RecyclerView.ViewHolder {
        TextView tv1, tv2;
        HistoryVH(@NonNull View itemView) {
            super(itemView);
            tv1 = itemView.findViewById(android.R.id.text1);
            tv2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
