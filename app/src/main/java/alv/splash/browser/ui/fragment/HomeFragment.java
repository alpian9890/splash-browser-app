package alv.splash.browser.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDragHandleView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import java.util.List;

import alv.splash.browser.MainActivity;
import alv.splash.browser.R;
import alv.splash.browser.SpeedDialAdapter;
import alv.splash.browser.SpeedDialItem;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView homeRecyclerView;

    private SpeedDialAdapter adapter;

    private SharedPreferences sharedPreferences;

    private static final String PREF_NAME = "SpeedDialPrefs";

    private static final String KEY_ITEMS = "speedDialItems";

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        homeRecyclerView = view.findViewById(R.id.homeRecyclerView);
        homeRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 5));

        sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Inisialisasi adapter dengan item yang disimpan
        List<SpeedDialItem> items = loadItems();
        adapter = new SpeedDialAdapter(items, this::showAddItemBottomSheet);

        adapter.setOnItemClickListener(item -> openUrl(item.getUrl()));
        // Menetapkan listener untuk event tekan lama
        adapter.setOnItemLongClickListener(this::showEditItemBottomSheet);

        homeRecyclerView.setAdapter(adapter);

        return view;
    }

    private List<SpeedDialItem> loadItems() {
        String jsonItems = sharedPreferences.getString(KEY_ITEMS, "[]");
        Type type = new TypeToken<List<SpeedDialItem>>(){}.getType();
        return new Gson().fromJson(jsonItems, type);
    }

    private void saveItems(List<SpeedDialItem> items) {
        String jsonItems = new Gson().toJson(items);
        sharedPreferences.edit().putString(KEY_ITEMS, jsonItems).apply();
    }

    private void showAddItemBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_add_item, null);

        TextInputEditText nameInput = bottomSheetView.findViewById(R.id.nameInput);
        TextInputEditText urlInput = bottomSheetView.findViewById(R.id.urlInput);
        Button saveButton = bottomSheetView.findViewById(R.id.saveButton);
        BottomSheetDragHandleView bottomDragHV = bottomSheetView.findViewById(R.id.bottomSDHV_Add);

        nameInput.setSingleLine(true);
        urlInput.setSingleLine(true);
        nameInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        urlInput.setImeOptions(EditorInfo.IME_ACTION_DONE);

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String url = urlInput.getText().toString().trim();

            if (name.isEmpty() || url.isEmpty()) {
                Toast.makeText(requireContext(), "Wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!url.startsWith("https://")) {
                url = "https://" + url;
            }

            SpeedDialItem newItem = new SpeedDialItem(name, url);
            adapter.addItem(newItem);
            saveItems(adapter.getItems());

            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    // Menampilkan bottom sheet untuk mengedit item yang dipilih
    private void showEditItemBottomSheet(SpeedDialItem item, int position) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetEdit = getLayoutInflater().inflate(R.layout.bottom_sheet_edit_item, null);
        View bottomSheetAdd = getLayoutInflater().inflate(R.layout.bottom_sheet_add_item, null);

        TextInputEditText nameInput = bottomSheetAdd.findViewById(R.id.nameInput);
        TextInputEditText urlInput = bottomSheetAdd.findViewById(R.id.urlInput);

        TextInputEditText editNameInput = bottomSheetEdit.findViewById(R.id.editNameInput);
        TextInputEditText editUrlInput = bottomSheetEdit.findViewById(R.id.editUrlInput);

        Button saveEdit = bottomSheetEdit.findViewById(R.id.saveEdit);
        Button deleteButton = bottomSheetEdit.findViewById(R.id.deleteItem);

        // Mengisi EditText dengan data item yang dipilih
        editNameInput.setText(item.getName());
        editUrlInput.setText(item.getUrl());

        saveEdit.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String url = urlInput.getText().toString().trim();

            String eName = editNameInput.getText().toString().trim();
            String eUrl = editUrlInput.getText().toString().trim();

            if (eName.isEmpty() || eUrl.isEmpty()) {
                Toast.makeText(requireContext(), "silahkan diisi dulu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!url.startsWith("https://")) {
                url = "https://" + url;
            }

            // Mengupdate item dengan data baru
            SpeedDialItem updatedItem = new SpeedDialItem(name, url);
            adapter.updateItem(position, updatedItem);
            saveItems(adapter.getItems());

            bottomSheetDialog.dismiss();
        });

        deleteButton.setOnClickListener(v -> {
            adapter.removeItem(position); // Menghapus item menggunakan metode adapter
            saveItems(adapter.getItems()); // Menyimpan perubahan data

            bottomSheetDialog.dismiss(); // Menutup dialog bottom sheet
        });

        bottomSheetDialog.setContentView(bottomSheetEdit);
        bottomSheetDialog.show();
    }

    private void openUrl(String url) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).loadUrl(url);
        }
    }

}