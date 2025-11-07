package alv.splash.browser;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private List<HistoryItem> historyItems;
    private OnHistoryItemClickListener listener;

    public interface OnHistoryItemClickListener {
        void onHistoryItemClick(HistoryItem item);
        void onHistoryItemDelete(HistoryItem item);
    }

    public HistoryAdapter(List<HistoryItem> historyItems, OnHistoryItemClickListener listener) {
        this.historyItems = historyItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = historyItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    public void updateItems(List<HistoryItem> items) {
        this.historyItems = items;
        notifyDataSetChanged();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView url;
        private TextView timestamp;
        private ImageButton deleteButton;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.history_title);
            url = itemView.findViewById(R.id.history_url);
            timestamp = itemView.findViewById(R.id.history_timestamp);
            deleteButton = itemView.findViewById(R.id.history_delete);
        }

        public void bind(HistoryItem item) {
            title.setText(item.getTitle());
            url.setText(item.getUrl());
            timestamp.setText(item.getFormattedDate());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onHistoryItemClick(item);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onHistoryItemDelete(item);
                }
            });
        }
    }
}