package alv.splash.browser;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CaptchaViewerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CaptchaViewerFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    // Pagination variables
    private int currentPage = 1;
    private int itemsPerPage = 50;
    private int totalPages = 1;
    private int totalItems = 0;
    private CaptchaDataManager captchaDataManager;
    private CaptchaAdapter captchaAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView captchaRecyclerView;
    private TextView
            emptyStateText,
            textTotalItems,
            tvShowingItems,
            textPageIndicator;
    private MaterialButton buttonPrevPage, buttonNextPage;

    public CaptchaViewerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CaptchaViewerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CaptchaViewerFragment newInstance(String param1, String param2) {
        CaptchaViewerFragment fragment = new CaptchaViewerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.captcha_viewer_db, container, false);

        captchaDataManager = new CaptchaDataManager(requireContext());

        // Initialize views
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutCDB);
        captchaRecyclerView = view.findViewById(R.id.captchaRecyclerView);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        textTotalItems = view.findViewById(R.id.textTotalItems);
        tvShowingItems = view.findViewById(R.id.tvShowingItems);
        textPageIndicator = view.findViewById(R.id.textPageIndicator);
        buttonPrevPage = view.findViewById(R.id.buttonPrevPage);
        buttonNextPage = view.findViewById(R.id.buttonNextPage);

        if (captchaRecyclerView != null) {
            captchaRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

            // Initialize adapter with empty list
            captchaAdapter = new CaptchaAdapter(this, new ArrayList<>());
            captchaRecyclerView.setAdapter(captchaAdapter);

            // Set up pagination controls
            setupPaginationControls();

            // Set up swipe refresh
            setupSwipeRefresh();

            // Load data
            loadData();
        }

        return view;
    }

    public CaptchaDataManager getCaptchaDataManager() {
        return captchaDataManager;
    }

    public void reloadData() {
        loadData();
    }

    private void setupPaginationControls() {
        buttonPrevPage.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                loadData();
            }
        });

        buttonNextPage.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadData();
            }
        });

        updatePaginationControls();
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Toast.makeText(requireContext(), "Refreshing data...", Toast.LENGTH_SHORT).show();
            loadData();
        });

        // Set refresh indicator colors
        swipeRefreshLayout.setColorSchemeResources(
                R.color.purple_500,
                R.color.teal_200,
                R.color.purple_700
        );
    }

    public void loadData() {
        // Show refresh indicator
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        // Load data in background
        new Thread(() -> {
            // First get total count for pagination
            int count = captchaDataManager.getEntryCount();

            totalItems = count;
            totalPages = (count == 0) ? 1 : (int) Math.ceil((double) count / itemsPerPage);

            // Ensure current page is within bounds
            if (currentPage > totalPages) {
                currentPage = totalPages > 0 ? totalPages : 1;
            }

            // Calculate offset
            int offset = (currentPage - 1) * itemsPerPage;

            // Get paginated entries
            List<CaptchaDataManager.CaptchaEntry> entries;
                entries = captchaDataManager.getEntriesWithPagination(offset, itemsPerPage);

            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;

                    if (captchaAdapter == null) {
                        captchaAdapter = new CaptchaAdapter(this, entries);
                        captchaRecyclerView.setAdapter(captchaAdapter);
                    } else {
                        captchaAdapter.updateData(entries);
                    }

                    // Show empty state if needed
                    if (emptyStateText != null) {
                        if (totalItems == 0) {
                            emptyStateText.setVisibility(View.VISIBLE);
                            captchaRecyclerView.setVisibility(View.GONE);
                        } else {
                            emptyStateText.setVisibility(View.GONE);
                            captchaRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }

                    // Update pagination controls
                    updatePaginationControls();

                    // Hide refresh indicator
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

        }).start();
    }

    private void updatePaginationControls() {
        buttonPrevPage.setEnabled(currentPage > 1);
        buttonNextPage.setEnabled(currentPage < totalPages);
        textPageIndicator.setText("Page " + currentPage + " of " + totalPages);
        textTotalItems.setText("Total entries: " + totalItems);

        // Hitung rentang item yang ditampilkan
        int startItem = (currentPage - 1) * itemsPerPage + 1;
        int endItem = Math.min(currentPage * itemsPerPage, totalItems);

        // Menangani kasus ketika database kosong
        if (totalItems == 0) {
            startItem = 0;
            endItem = 0;
        }

        String tvShowingItemsText = " | Showing " + startItem + "-" + endItem + " of " + totalItems;
        tvShowingItems.setText(tvShowingItemsText);
    }

}