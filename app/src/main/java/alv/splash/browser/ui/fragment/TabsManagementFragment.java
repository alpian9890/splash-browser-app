package alv.splash.browser.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import alv.splash.browser.MainActivity;
import alv.splash.browser.R;
import alv.splash.browser.model.TabItem;
import alv.splash.browser.ui.adapter.TabsAdapter;
import alv.splash.browser.viewmodel.TabViewModel;

public class TabsManagementFragment extends Fragment {
    private RecyclerView tabsRecyclerView;
    private TabsAdapter tabsAdapter;
    private TabViewModel tabViewModel;
    private Button btnAddNewTab;
    private Button btnCloseAllTabs;
    private static final String TAG = "TabsManagementFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inisialisasi ViewModel
        tabViewModel = new ViewModelProvider(requireActivity()).get(TabViewModel.class);

        Log.d(TAG, "Fragment created and ViewModel initialized");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tabs_management, container, false);

        // Setup RecyclerView
        tabsRecyclerView = view.findViewById(R.id.tabsRecyclerView);
        tabsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Setup Add Tab Button
        btnAddNewTab = view.findViewById(R.id.btnAddNewTab);
        btnAddNewTab.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).createNewTab("about:home");
                Log.d(TAG, "New tab button clicked");
            }
        });

        // Add Close All Tabs button if it exists
        btnCloseAllTabs = view.findViewById(R.id.btnCloseAllTabs);
        if (btnCloseAllTabs != null) {
            btnCloseAllTabs.setOnClickListener(v -> {
                tabViewModel.closeAllTabs();
                Log.d(TAG, "Close all tabs button clicked");
            });
        } else {
            // Jika button tidak ada di layout, bisa ditambahkan ke layout
            Log.d(TAG, "btnCloseAllTabs not found in layout");
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize TabsAdapter
        tabsAdapter = new TabsAdapter(tabViewModel.getTabs().getValue(), new TabsAdapter.OnTabClickListener() {
            @Override
            public void onTabClick(TabItem tab) {
                tabViewModel.setActiveTab(tab);
                Log.d(TAG, "Tab clicked: " + tab.getId());

                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showTab(tab);
                }
            }

            @Override
            public void onTabClose(TabItem tab) {
                tabViewModel.closeTab(tab.getId());
                Log.d(TAG, "Tab closed: " + tab.getId());
            }
        });

        tabsRecyclerView.setAdapter(tabsAdapter);

        // Observe tab changes
        tabViewModel.getTabs().observe(getViewLifecycleOwner(), tabs -> {
            tabsAdapter.updateTabs(tabs);
            Log.d(TAG, "Tab list updated: " + (tabs != null ? tabs.size() : 0) + " tabs");
        });
    }
}
