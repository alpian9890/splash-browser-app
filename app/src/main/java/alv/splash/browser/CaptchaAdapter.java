package alv.splash.browser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CaptchaAdapter extends RecyclerView.Adapter<CaptchaViewHolder> {
    private List<CaptchaDataManager.CaptchaEntry> entries;
    private CaptchaViewerFragment fragment;

    public CaptchaAdapter(CaptchaViewerFragment fragment, List<CaptchaDataManager.CaptchaEntry> entries) {
        this.fragment = fragment;
        this.entries = entries;
    }

    @NonNull
    @Override
    public CaptchaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_captcha, parent, false);
        return new CaptchaViewHolder(fragment, view);
    }

    @Override
    public void onBindViewHolder(@NonNull CaptchaViewHolder holder, int position) {
        CaptchaDataManager.CaptchaEntry entry = entries.get(position);
        holder.bind(entry);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public void updateData(List<CaptchaDataManager.CaptchaEntry> newEntries) {
        this.entries = newEntries;
        notifyDataSetChanged();
    }
}