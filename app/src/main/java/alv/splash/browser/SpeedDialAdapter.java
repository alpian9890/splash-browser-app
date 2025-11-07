package alv.splash.browser;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SpeedDialAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<SpeedDialItem> items;
    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_ADD = 1;
    private final Runnable onAddClickListener;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    // Interface untuk mendengarkan event tekan lama pada item
    public interface OnItemLongClickListener {
        void onItemLongClick(SpeedDialItem item, int position);
    }
    // Interface untuk mendengarkan event klik pada item
    public interface OnItemClickListener {
        void onItemClick(SpeedDialItem item);
    }

    // Konstruktor untuk adapter
    public SpeedDialAdapter(List<SpeedDialItem> items, Runnable onAddClickListener) {
        this.items = new ArrayList<>(items);
        this.onAddClickListener = onAddClickListener;
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    // Menetapkan listener untuk tekan lama
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ADD) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_add_button, parent, false);
            return new AddButtonViewHolder(view, onAddClickListener);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_speed_dial, parent, false);
            return new ItemViewHolder(view, onItemClickListener, onItemLongClickListener);  // Tambahkan listener untuk item
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ((ItemViewHolder) holder).bind(items.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return items.size() + 1; // +1 untuk tombol "add"
    }

    @Override
    public int getItemViewType(int position) {
        return position == items.size() ? VIEW_TYPE_ADD : VIEW_TYPE_ITEM;
    }

    // Menambahkan item baru
    public void addItem(SpeedDialItem item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    // Mengupdate item pada posisi tertentu
    public void updateItem(int position, SpeedDialItem newItem) {
        items.set(position, newItem);
        notifyItemChanged(position);
    }

    public List<SpeedDialItem> getItems() {
        return new ArrayList<>(items);
    }

    public void removeItem(int position) {
        items.remove(position); // Menghapus item dari daftar
        notifyItemRemoved(position); // Memberitahu adapter bahwa item telah dihapus
        notifyItemRangeChanged(position, items.size()); // Update posisi item lainnya
    }

    // ViewHolder untuk item biasa
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView iconView;
        TextView titleView;

        ItemViewHolder(View itemView, OnItemClickListener clickListener, OnItemLongClickListener longClickListener) {
            super(itemView);
            iconView = itemView.findViewById(R.id.gambarHalaman);
            titleView = itemView.findViewById(R.id.judulHalaman);

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onItemClick((SpeedDialItem) itemView.getTag());
                }
            });

            // Menambahkan listener untuk event tekan lama pada item
            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onItemLongClick((SpeedDialItem) itemView.getTag(), getAdapterPosition());
                }
                return true; // Menandakan bahwa event sudah diproses
            });
        }

        // Menghubungkan data ke tampilan
        void bind(SpeedDialItem item) {
            titleView.setText(item.getName());
            itemView.setTag(item);  // Menyimpan item dalam tag untuk digunakan saat klik lama
        }
    }

    // ViewHolder untuk tombol "add"
    static class AddButtonViewHolder extends RecyclerView.ViewHolder {
        AddButtonViewHolder(View itemView, Runnable onClickListener) {
            super(itemView);
            itemView.setOnClickListener(v -> onClickListener.run());  // Menambahkan listener klik pada tombol tambah
        }
    }
}