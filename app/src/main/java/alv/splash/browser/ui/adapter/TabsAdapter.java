package alv.splash.browser.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

import java.util.List;

import alv.splash.browser.R;
import alv.splash.browser.model.TabItem;

public class TabsAdapter extends RecyclerView.Adapter<TabsAdapter.TabViewHolder> {
    private List<TabItem> tabs;
    private OnTabClickListener listener;

    public interface OnTabClickListener {
        void onTabClick(TabItem tab);
        void onTabClose(TabItem tab);
    }

    public TabsAdapter(List<TabItem> tabs, OnTabClickListener listener) {
        this.tabs = tabs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tab, parent, false);
        return new TabViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TabViewHolder holder, int position) {
        TabItem tab = tabs.get(position);
        holder.bind(tab);
    }

    @Override
    public int getItemCount() {
        return tabs.size();
    }

    public void updateTabs(List<TabItem> tabs) {
        this.tabs = tabs;
        notifyDataSetChanged();
    }

    class TabViewHolder extends RecyclerView.ViewHolder {
        private ImageView favicon;
        private TextView title;
        private TextView url;
        private ImageButton closeButton;
        private CardView tabCard;
        private Drawable active_tab = ContextCompat.getDrawable(itemView.getContext(), R.drawable.cosmic_gradient_bg3);
        private Drawable inactive_tab = ContextCompat.getDrawable(itemView.getContext(), R.drawable.cosmic_gradient_bg);
        public TabViewHolder(@NonNull View itemView) {
            super(itemView);
            favicon = itemView.findViewById(R.id.tab_favicon);
            title = itemView.findViewById(R.id.tab_title);
            url = itemView.findViewById(R.id.tab_url);
            closeButton = itemView.findViewById(R.id.tab_close);
            tabCard = itemView.findViewById(R.id.tab_card);
        }

        public void bind(TabItem tab) {
            title.setText(tab.getTitle());
            url.setText(tab.getUrl());

            if (tab.getFavicon() != null) {
                favicon.setImageBitmap(tab.getFavicon());
                Log.d("TabsAdapter", "Favicon loaded for URL: " + tab.getUrl());
            } else {
                favicon.setImageResource(R.drawable.ic_globe_32);
                Log.d("TabsAdapter", "No favicon found for URL: " + tab.getUrl());
            }

            // Highlight active tab
            if (tab.isActive()) {
                tabCard.setBackground(active_tab);
            } else {
                tabCard.setBackground(inactive_tab);
            }

            tabCard.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTabClick(tab);
                }
                Log.d("TabsAdapter", "onTabClick " + (listener != null ? " Listener berfungsi" : " Listener null"));
            });

            closeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTabClose(tab);
                }
                Log.d("TabsAdapter", "onTabClose " + (listener != null ? " Listener berfungsi" : " Listener null"));
            });
        }
    }
}
