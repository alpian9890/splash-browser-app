package alv.splash.browser;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;


public class HistoryBrowsingFragment extends Fragment {
    private RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;
    private HistoryManager historyManager;
    private SearchView searchView;

    public HistoryBrowsingFragment() {
        // Required empty public constructor
    }


    public static HistoryBrowsingFragment newInstance(String param1, String param2) {
        HistoryBrowsingFragment fragment = new HistoryBrowsingFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        historyManager = HistoryManager.getInstance(requireContext());
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_browsing, container, false);

        historyRecyclerView = view.findViewById(R.id.historyRecyclerView);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchView = view.findViewById(R.id.historySearchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterHistory(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterHistory(newText);
                return true;
            }
        });

        Button clearButton = view.findViewById(R.id.btnClearHistory);
        clearButton.setOnClickListener(v -> showClearHistoryConfirmation());

        loadHistory();

        return view;
    }

    private void loadHistory() {
        List<HistoryItem> historyItems = historyManager.getAllHistoryItems();
        historyAdapter = new HistoryAdapter(historyItems, new HistoryAdapter.OnHistoryItemClickListener() {
            @Override
            public void onHistoryItemClick(HistoryItem item) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).loadUrl(item.getUrl());
                }
                Log.d("HistoryBrowsingFragment", "History item clicked: " + item.getUrl());
            }

            @Override
            public void onHistoryItemDelete(HistoryItem item) {
                historyManager.deleteHistoryItem(item.getId());
                loadHistory();
                Log.d("HistoryBrowsingFragment", "History item deleted: " + item.getUrl());
            }
        });
        historyRecyclerView.setAdapter(historyAdapter);
    }

    private void filterHistory(String query) {
        if (query.isEmpty()) {
            loadHistory();
        } else {
            List<HistoryItem> filteredItems = historyManager.searchHistory(query);
            historyAdapter.updateItems(filteredItems);
        }
    }

    private void showClearHistoryConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Clear Browsing History")
                .setMessage("Are you sure you want to clear all browsing history?")
                .setPositiveButton("Clear", (dialog, which) -> {
                    historyManager.clearHistory();
                    loadHistory();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

}