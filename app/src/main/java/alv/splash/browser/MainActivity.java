package alv.splash.browser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.Manifest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alv.splash.browser.model.TabItem;
import alv.splash.browser.ui.fragment.GeckoViewFragment;
import alv.splash.browser.ui.fragment.HomeFragment;
import alv.splash.browser.ui.fragment.TabsManagementFragment;
import alv.splash.browser.util.GeckoSessionPool;
import alv.splash.browser.viewmodel.TabViewModel;

public class MainActivity extends AppCompatActivity {



    private static final int FILE_CHOOSER_REQUEST_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int REQUEST_CODE_102 = 102;
    ValueCallback<Uri[]> filePathCallback;
    private boolean storagePermission = false;
    private SharedPreferences preferences;
    private static final String APP_PREFERENCES = "AppPreferences";
    private static final String STORAGE_PERMISSION_KEY = "storage_permission_granted";

    CaptchaDataManager captchaDataManager;
    private String basePath = ""; //Environment.getExternalStorageDirectory() + "/Datasets";
    private String imagesPath = "";// basePath + "/Images";

    DrawerLayout mainDrawer;
    FrameLayout fMainContainer;

    NavigationView navigationView;

    SlidingUpPanelLayout slidingLayout;
    private AddressBarUtils addressBarUtils;


    private TextInputEditText editAddressBar;

    private TabManager tabManager;
    private TabViewModel tabViewModel;
    private Map<String, Fragment> fragmentCache = new HashMap<>();
    private FragmentManager fragmentManager;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Inisialisasi SharedPreferences
        preferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);

        // Ambil nilai permission yang tersimpan
        storagePermission = preferences.getBoolean(STORAGE_PERMISSION_KEY, false);

        setupStorage();
        //nav_view
        navigationView = findViewById(R.id.nav_view);
        mainDrawer = findViewById(R.id.mainDrawer);

        // Toolbar
        //myMenu = findViewById(R.id.myMenu);
        //myHome = findViewById(R.id.myHome);

        slidingLayout = findViewById(R.id.sliding_layout);
        fMainContainer = findViewById(R.id.fMainContainer);

        setupSlidingPanel();

        initializeAddressBar();
        initializeSubToolbar();

        // Inisialisasi FragmentManager
        fragmentManager = getSupportFragmentManager();

        // Inisialisasi ViewModel
        tabViewModel = new ViewModelProvider(this).get(TabViewModel.class);

        // Konfigurasi TabManager untuk menggunakan ViewModel
        tabManager = TabManager.getInstance();
        tabManager.setViewModel(tabViewModel, this);
        // Observasi perubahan tab
        setupTabObservers();
        // Cek apakah ada tab yang perlu dibuat
        if (savedInstanceState == null) {
            // Cek apakah ada tab yang dimuat dari penyimpanan
            List<TabItem> existingTabs = tabViewModel.getTabs().getValue();
            // Ada tab yang dipulihkan, tampilkan tab aktif
            TabItem activeTab = tabViewModel.getActiveTab().getValue();

            if (existingTabs == null || existingTabs.isEmpty()) {
                // Tidak ada tab yang dipulihkan, buat tab home baru
                createNewTab("about:home");
                if (activeTab != null) {
                    showTab(activeTab);
                    Log.d(TAG, "No saved tabs, created new home tab");
                }
                Log.d(TAG, "No saved tabs, created initial home tab");
            } else {
                if (activeTab != null) {
                    showTab(activeTab);
                    Log.d(TAG, "Showing restored active tab: " + activeTab.getId());
                } else {
                    // Jika tidak ada tab aktif, buat tab home baru
                    createNewTab("about:home");
                    if (activeTab != null) {
                        showTab(activeTab);
                    }
                    Log.d(TAG, "No active tab, created new home tab");
                }
            }

            // Tampilkan fragment manajemen tab
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentSubContent, new TabsManagementFragment())
                    .commit();
        }


    }//akhir onCreate

    /**
     * Mengatur observer untuk LiveData dalam TabViewModel
     */
    private void setupTabObservers() {
        // Observer untuk daftar tab
        tabViewModel.getTabs().observe(this, tabs -> {
            Log.d(TAG, "Tab list updated: " + (tabs != null ? tabs.size() : 0) + " tabs");
        });

        // Observer untuk tab aktif
        tabViewModel.getActiveTab().observe(this, tab -> {
            if (tab != null) {
                Log.d(TAG, "Active tab changed to: " + tab.getId() + ", URL: " + tab.getUrl());
            }
        });

        // Observer untuk loading state
        tabViewModel.getIsLoading().observe(this, isLoading -> {
            // Anda bisa menampilkan indikator loading di sini jika diinginkan
            Log.d(TAG, "Loading state changed to: " + isLoading);
        });

        // Observer untuk peristiwa tab
        tabViewModel.getTabEvents().observe(this, event -> {
            switch (event.getType()) {
                case ADDED:
                    TabItem newTab = (TabItem) event.getData();
                    Log.d(TAG, "Tab added event: " + newTab.getId());
                    // No additional action needed here as the tab is already added to the ViewModel
                    break;

                case CLOSED:
                    TabItem closedTab = (TabItem) event.getData();
                    onTabClosed(closedTab.getId());
                    Log.d(TAG, "Tab closed event: " + closedTab.getId());
                    break;

                case ALL_CLOSED:
                    // Hapus semua fragment dari cache
                    for (String tabId : new HashMap<>(fragmentCache).keySet()) {
                        onTabClosed(tabId);
                    }
                    Log.d(TAG, "All tabs closed event");
                    break;

                case ACTIVE_CHANGED:
                    TabItem activeTab = (TabItem) event.getData();
                    // showTab will be called by TabManager.onActiveTabChanged
                    Log.d(TAG, "Active tab changed event: " + activeTab.getId());
                    break;

                case UPDATED:
                    TabItem updatedTab = (TabItem) event.getData();
                    // Update address bar if this is the active tab
                    if (updatedTab.isActive()) {
                        updateAddressBarInfo(updatedTab.getTitle(), true); // Assuming secure for now
                    }
                    Log.d(TAG, "Tab updated event: " + updatedTab.getId());
                    break;

                case RESTORED:
                    // Ketika tab dipulihkan, perbarui fragmentCache
                    List<TabItem> restoredTabs = (List<TabItem>) event.getData();
                    Log.d(TAG, "Tab restore event with " + restoredTabs.size() + " tabs");

                    // Bersihkan cache fragment lama
                    fragmentCache.clear();

                    // Tampilkan tab aktif jika ada
                    TabItem activeTabRestored = tabViewModel.getActiveTab().getValue();
                    if (activeTabRestored != null) {
                        showTab(activeTabRestored);
                    }
                    break;
            }
        });
    }

    /**
     * Setup untuk sliding panel (implementasi tetap sama seperti sebelumnya)
     */
    private void setupSlidingPanel() {
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        slidingLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
            }
        });

        slidingLayout.setFadeOnClickListener(view -> {
            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("MainActivity", "Saving instance state");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("MainActivity", "Restoring instance state");
    }

    @Override
    public void onBackPressed() {
        // Check if address bar is expanded
        if (addressBarUtils.isExpanded()) {
            addressBarUtils.collapseAddressBar();
            return;
        }

        // Check if sliding panel is expanded
        if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return;
        }

        // Check if drawer is open
        if (mainDrawer.isDrawerOpen(GravityCompat.START)) {
            mainDrawer.closeDrawer(GravityCompat.START);
            return;
        }

        // Check if current fragment is GeckoViewFragment and can go back
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.fMainContainer);

        if (currentFragment instanceof GeckoViewFragment && ((GeckoViewFragment) currentFragment).canGoBack()) {
            ((GeckoViewFragment) currentFragment).goBack();
            return;
        }

        // Otherwise, proceed with normal back button behavior
        super.onBackPressed();
    }

    public void updateAddressBarInfo(String title, boolean isSecure) {
        addressBarUtils.updatePageInfo(title, isSecure);
    }
    public void updateEditText(String query) {
        editAddressBar.setText(query);
    }
    private void openWebGecko(String query) {
        String processedUrl = new UrlValidator().processInput(query);
        loadUrl(processedUrl);
        addressBarUtils.collapseAddressBar();
        slidePanelCollapse();
    }

    private void initializeAddressBar() {
        // Initialize components

        ImageButton btnMenu = findViewById(R.id.btnMenu);

        ImageButton btnHome = findViewById(R.id.btnHome);

        ImageButton btnAddTab = findViewById(R.id.btnAddTab);

        ImageButton btnTabs = findViewById(R.id.btnTabs);



        ImageView iconSecurity = findViewById(R.id.iconSecurity);

        TextView txtPageTitle = findViewById(R.id.txtPageTitle);

        CardView addressBarContainer = findViewById(R.id.addressBarContainer);

        editAddressBar = findViewById(R.id.editAddressBar);



        // Initialize AddressBarUtils

        addressBarUtils = new AddressBarUtils(addressBarContainer, iconSecurity, txtPageTitle);



        // Set initial page info

        addressBarUtils.updatePageInfo("Start browsing", true);

        // Setup toggle behavior

        txtPageTitle.setOnClickListener(v -> {
            addressBarUtils.expandAddressBar();
            editAddressBar.requestFocus();
            editAddressBar.selectAll();
            showKeyboard(editAddressBar);
        });



        // Handle address bar keyboard actions

        editAddressBar.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_GO) {

                openWebGecko(editAddressBar.getText().toString());
                hideKeyboard(editAddressBar);
                slidePanelCollapse();
                Log.d("MainActivity", "EditText, IME_ACTION_GO clicked");
                return true;

            }

            return false;

        });

        editAddressBar.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(editAddressBar);
                addressBarUtils.collapseAddressBar();
            }
        });



        // Setup other buttons

        btnMenu.setOnClickListener(v -> {
            if (!mainDrawer.isDrawerOpen(GravityCompat.START)) {
                mainDrawer.openDrawer(GravityCompat.START); // Buka Navigation Drawer di sisi kiri
            }
            addressBarUtils.collapseAddressBar();
            slidePanelCollapse();
            Log.d("MainActivity", "Menu button clicked");
        });



        btnHome.setOnClickListener(v -> {

            // Handle home click
            Log.d("MainActivity", "Home button clicked");
            addressBarUtils.collapseAddressBar();

        });



        btnAddTab.setOnClickListener(v -> {
            createNewTab("about:home");
            Log.d("MainActivity", "Add Tab button clicked");
        });

        btnTabs.setOnClickListener(v -> {
            loadFragment(new TabsManagementFragment());
            Log.d("MainActivity", "Tabs button clicked");
        });
    }

    private void initializeSubToolbar() {
        View headerView = navigationView.getHeaderView(0);
        // Find CardView elements and set click listeners
        CardView startWorking = headerView.findViewById(R.id.startWorking);
        CardView cardTabs = headerView.findViewById(R.id.card_tabs);
        CardView cardHistory = headerView.findViewById(R.id.card_history);
        CardView cardBookmarks = headerView.findViewById(R.id.card_bookmarks);
        CardView cardDownloads = headerView.findViewById(R.id.card_downloads);
        CardView cardExtensions = headerView.findViewById(R.id.card_extensions);
        CardView cardTranslate = headerView.findViewById(R.id.card_translate);
        CardView cardNotes = headerView.findViewById(R.id.card_notes);
        CardView cardDatabase = headerView.findViewById(R.id.card_database);
        CardView cardFiles = headerView.findViewById(R.id.card_files);
        CardView cardKeyboard = headerView.findViewById(R.id.card_keyboard);
        CardView cardCalculator = headerView.findViewById(R.id.card_calculator);
        CardView cardPhotos = headerView.findViewById(R.id.card_photos);
        CardView cardVideos = headerView.findViewById(R.id.card_videos);
        CardView cardMusic = headerView.findViewById(R.id.card_music);
        CardView cardV2ray = headerView.findViewById(R.id.card_v2ray);
        CardView cardConsole = headerView.findViewById(R.id.card_console);
        CardView cardLogout = headerView.findViewById(R.id.card_logout);

        // Set click listeners for each card
        startWorking.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StartWorking.class);
            startActivity(intent);
            finish();
        });
        cardTabs.setOnClickListener(v -> loadFragment(new TabsManagementFragment()));
        cardHistory.setOnClickListener(v -> loadFragment(new HistoryBrowsingFragment()));
        cardBookmarks.setOnClickListener(v -> loadFragment(new BookmarksFragment()));
        cardDownloads.setOnClickListener(v -> loadFragment(new DownloadsFragment()));
        cardExtensions.setOnClickListener(v -> loadFragment(new ExtensionsFragment()));
        cardTranslate.setOnClickListener(v -> loadFragment(new TranslateFragment()));
        cardNotes.setOnClickListener(v -> loadFragment(new NotesFragment()));
        cardDatabase.setOnClickListener(v -> loadFragment(new CaptchaViewerFragment()));
        cardFiles.setOnClickListener(v -> loadFragment(new FilesManagerFragment()));
        cardKeyboard.setOnClickListener(v -> loadFragment(new KeyboardFragment()));
        cardCalculator.setOnClickListener(v -> loadFragment(new CalculatorFragment()));
        cardPhotos.setOnClickListener(v -> loadFragment(new PhotosFragment()));
        cardVideos.setOnClickListener(v -> loadFragment(new VideosFragment()));
        cardMusic.setOnClickListener(v -> loadFragment(new MusicFragment()));
        cardV2ray.setOnClickListener(v -> loadFragment(new V2RayFragment()));
        cardConsole.setOnClickListener(v -> loadFragment(new ConsoleFragment()));
        cardLogout.setOnClickListener(v -> finish());

    }

    public void slidePanelCollapse() {
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentSubContent, fragment)
                .commit();
        closeDrawer();
        if (slidingLayout.getPanelState() != SlidingUpPanelLayout.PanelState.EXPANDED ||
                slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        }
    }

    private void closeDrawer() {
        if (mainDrawer.isDrawerOpen(GravityCompat.START)) {
            mainDrawer.closeDrawer(GravityCompat.START);
        }
    }


    public void showKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }
    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Metode showTab yang diperbarui
    public void showTab(TabItem tab) {
        if (tab == null) {
            Log.e(TAG, "Trying to show null tab");
            return;
        }

        Log.d(TAG, "Showing tab: " + tab.getId() + ", URL: " + tab.getUrl());

        // Pastikan tab ini disetel sebagai tab aktif di ViewModel
        tabViewModel.setActiveTab(tab);

        // Cari fragment di cache berdasarkan ID tab
        Fragment fragment = fragmentCache.get(tab.getId());

        if (fragment == null) {

            boolean isRestoredTab = tabViewModel.getTabEvents().getValue() != null &&
                    tabViewModel.getTabEvents().getValue().getType() == TabViewModel.TabEventType.RESTORED;

            // Fragment tidak ditemukan dalam cache, buat fragment baru
            if (tab.getUrl().equals("about:home")) {
                fragment = new HomeFragment();
                Log.d(TAG, "Created new HomeFragment for tab: " + tab.getId());
            } else {
                fragment = GeckoViewFragment.newInstance(tab.getId(), tab.getUrl(), isRestoredTab);
                Log.d(TAG, "Created new GeckoViewFragment for tab: " + tab.getId() + ", isRestored: " + isRestoredTab);

            }

            // Tambahkan ke cache
            fragmentCache.put(tab.getId(), fragment);

            // Tambahkan ke FragmentManager dalam keadaan tersembunyi
            fragmentManager.beginTransaction()
                    .add(R.id.fMainContainer, fragment, tab.getId())
                    .hide(fragment)
                    .commitNow();

            Log.d(TAG, "Added new fragment to FragmentManager: " + tab.getId());
        } else {
            Log.d(TAG, "Found fragment in cache: " + tab.getId());

            // Periksa apakah fragment sudah ada di FragmentManager
            Fragment existingFragment = fragmentManager.findFragmentByTag(tab.getId());
            if (existingFragment == null) {
                // Jika tidak ada di FragmentManager, tambahkan kembali
                fragmentManager.beginTransaction()
                        .add(R.id.fMainContainer, fragment, tab.getId())
                        .hide(fragment)
                        .commitNow();

                Log.d(TAG, "Re-added cached fragment to FragmentManager: " + tab.getId());
            }
        }

        // Cari fragment yang saat ini ditampilkan
        Fragment currentVisibleFragment = getCurrentVisibleFragment();

        // Jika fragment yang terlihat berbeda dari yang ingin ditampilkan
        if (currentVisibleFragment != null && currentVisibleFragment != fragment) {
            Log.d(TAG,
                    "Hiding current visible fragment: " + currentVisibleFragment.getClass().getSimpleName() +
                            ", Tag: " + currentVisibleFragment.getTag());

            // Sembunyikan fragment saat ini dan tampilkan fragment yang baru
            fragmentManager.beginTransaction()
                    .hide(currentVisibleFragment)
                    .show(fragment)
                    .commitNow();

            Log.d(TAG, "Showed fragment: " + fragment.getClass().getSimpleName() + ", Tag: " + tab.getId());
        } else if (currentVisibleFragment == null) {
            // Tidak ada fragment yang terlihat, tampilkan fragment baru
            fragmentManager.beginTransaction()
                    .show(fragment)
                    .commitNow();

            Log.d(TAG, "No visible fragment, showed: " + fragment.getClass().getSimpleName());
        } else {
            // Fragment yang ingin ditampilkan sudah terlihat
            Log.d(TAG, "Fragment already visible: " + fragment.getClass().getSimpleName());
        }

        // Update address bar
        if (tab.getUrl().equals("about:home")) {
            addressBarUtils.updatePageInfo("Start browsing", true);
        } else {
            addressBarUtils.updatePageInfo(tab.getTitle(), true);
        }

        slidePanelCollapse();
    }

    // Metode untuk mencari fragment yang saat ini terlihat
    private Fragment getCurrentVisibleFragment() {
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible() &&
                    fragment.getId() == R.id.fMainContainer) {
                return fragment;
            }
        }
        return null;
    }

    // Metode getCurrentFragment yang diperbarui
    private Fragment getCurrentFragment() {
        // Coba cari fragment yang visible terlebih dahulu
        Fragment visibleFragment = getCurrentVisibleFragment();
        if (visibleFragment != null) {
            return visibleFragment;
        }

        // Coba cari berdasarkan tab yang aktif
        TabItem activeTab = tabViewModel.getActiveTab().getValue();
        if (activeTab != null) {
            Fragment fragment = fragmentManager.findFragmentByTag(activeTab.getId());
            if (fragment != null) {
                return fragment;
            }
        }

        // Fallback ke metode original
        return fragmentManager.findFragmentById(R.id.fMainContainer);
    }

    // Metode onTabClosed yang diperbarui
    public void onTabClosed(String tabId) {
        Fragment fragment = fragmentCache.remove(tabId);
        if (fragment != null) {
            // Pastikan fragment dihapus dari FragmentManager
            try {
                fragmentManager.beginTransaction()
                        .remove(fragment)
                        .commitNow();
                Log.d(TAG, "Removed fragment for tab: " + tabId);
            } catch (Exception e) {
                Log.e(TAG, "Error removing fragment: " + e.getMessage(), e);
            }
        }
    }

    // Metode createNewTab yang diperbarui
    public void createNewTab(String url) {
        TabItem newTab = tabViewModel.addTab(url);
        Log.d(TAG, "New tab created with ID: " + newTab.getId());
        // TabManager.onActiveTabChanged akan memanggil showTab
    }

    // Metode loadUrl yang diperbarui
    public void loadUrl(String url) {
        TabItem activeTab = tabViewModel.getActiveTab().getValue();
        if (activeTab == null) {
            createNewTab(url);
            return;
        }

        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof GeckoViewFragment) {
            ((GeckoViewFragment) currentFragment).loadUrl(url);

            // Update URL di TabViewModel
            tabViewModel.updateTabInfo(activeTab.getId(), activeTab.getTitle(), url, activeTab.getFavicon());
        } else {

            boolean isRestoredTab = tabViewModel.getTabEvents().getValue() != null &&
                    tabViewModel.getTabEvents().getValue().getType() == TabViewModel.TabEventType.RESTORED;
            // Jika tab saat ini adalah HomeFragment, ganti dengan GeckoViewFragment
            GeckoViewFragment geckoViewFragment = GeckoViewFragment.newInstance(activeTab.getId(), url, isRestoredTab);

            // Update fragment di cache
            fragmentCache.put(activeTab.getId(), geckoViewFragment);

            // Ganti fragment
            fragmentManager.beginTransaction()
                    .replace(R.id.fMainContainer, geckoViewFragment, activeTab.getId())
                    .commitNow();

            // Update URL di TabViewModel
            tabViewModel.updateTabInfo(activeTab.getId(), activeTab.getTitle(), url, activeTab.getFavicon());
        }

        slidePanelCollapse();
    }

    private void setupStorage() {
        // Log untuk debugging
        Log.d("StorageDebug", "setupStorage() dipanggil");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ - Gunakan direktori yang dikelola aplikasi
            File externalFilesDir = getExternalFilesDir(null);
            if (externalFilesDir != null) {
                basePath = externalFilesDir.getAbsolutePath() + "/Datasets";
                Log.d("StorageDebug", "Path untuk Android 10+: " + basePath);
            } else {
                Log.e("StorageDebug", "getExternalFilesDir() mengembalikan null");
                //updateConsoleLog("[!] Error: Tidak dapat mendapatkan direktori eksternal");
            }
        } else {
            // Android 9 dan di bawahnya
            basePath = Environment.getExternalStorageDirectory() + "/Datasets";
            Log.d("StorageDebug", "Path untuk Android < 10: " + basePath);
        }

        imagesPath = basePath + "/Images";
        //csvPath = basePath + "/labels.csv";

        // Log info path
        Log.d("StorageDebug", "imagesPath: " + imagesPath);
        //Log.d("StorageDebug", "csvPath: " + csvPath);

        // Buat direktori jika belum ada
        //createDirectories();

        // Minta izin penyimpanan saat aplikasi dibuka
        requestStoragePermissions();
    }

    private void requestStoragePermissions() {
        Log.d("StorageDebug", "requestStoragePermissions(), SDK: " + Build.VERSION.SDK_INT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+)
            boolean hasAllFilesPermission = Environment.isExternalStorageManager();
            Log.d("StorageDebug", "Android 11+, hasAllFilesPermission: " + hasAllFilesPermission);

            if (!hasAllFilesPermission) {
                //updateConsoleLog("[ Meminta izin MANAGE_EXTERNAL_STORAGE untuk Android 11+ ]");
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, REQUEST_CODE_102);
                } catch (Exception e) {
                    Log.e("StorageDebug", "Error meminta MANAGE_EXTERNAL_STORAGE: " + e.getMessage(), e);
                    //updateConsoleLog("[!] Error meminta izin penyimpanan: " + e.getMessage());

                    // Fallback jika cara spesifik gagal
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, REQUEST_CODE_102);
                }
            } else {
                Log.d("StorageDebug", "MANAGE_EXTERNAL_STORAGE sudah diberikan");
                //updateConsoleLog("[ Izin MANAGE_EXTERNAL_STORAGE sudah diberikan ]");
                storagePermission = true;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(STORAGE_PERMISSION_KEY, true);
                editor.apply();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6+ (API 23+)
            int writePermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            Log.d("StorageDebug", "Android 6-10, writePermission status: " +
                    (writePermission == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED"));

            if (writePermission != PackageManager.PERMISSION_GRANTED) {
                //updateConsoleLog("[ Meminta izin WRITE_EXTERNAL_STORAGE ]");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            } else {
                Log.d("StorageDebug", "WRITE_EXTERNAL_STORAGE sudah diberikan");
                //updateConsoleLog("[ Izin WRITE_EXTERNAL_STORAGE sudah diberikan ]");
                // Izin sudah diberikan, update jika belum tersimpan
                if (!storagePermission) {
                    storagePermission = true;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(STORAGE_PERMISSION_KEY, true);
                    editor.apply();
                }
            }
        } else {
            // Android 5.1 dan di bawahnya
            Log.d("StorageDebug", "SDK < 23, izin dianggap sudah diberikan");
           // updateConsoleLog("[ Izin penyimpanan otomatis diberikan untuk Android 5.1- ]");
            storagePermission = true;
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            try {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!storagePermission) {
                        // Update nilai di variabel
                        storagePermission = true;

                        // Simpan nilai ke SharedPreferences
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean(STORAGE_PERMISSION_KEY, true);
                        editor.apply();

                        //updateConsoleLog("[ Izin penyimpanan diberikan ]");
                        Toast.makeText(this, "Izin penyimpanan diberikan", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (storagePermission) {
                        // Update nilai di variabel
                        storagePermission = false;

                        // Simpan nilai ke SharedPreferences
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean(STORAGE_PERMISSION_KEY, false);
                        editor.apply();

                        Toast.makeText(this, "Aplikasi membutuhkan izin penyimpanan", Toast.LENGTH_LONG).show();
                        //updateConsoleLog("[ Aplikasi membutuhkan izin penyimpanan ]");
                    }
                }
            } catch (Exception e) {
                Log.e("StorageDebug", "Error: " + e.getMessage(), e);
                //updateConsoleLog("[!] Error: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (filePathCallback != null) {
                Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
                filePathCallback.onReceiveValue(result);
                filePathCallback = null;
            }
        }

        if (requestCode == REQUEST_CODE_102) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                boolean hasAllFilesPermission = Environment.isExternalStorageManager();
                Log.d("StorageDebug", "onActivityResult, hasAllFilesPermission: " + hasAllFilesPermission);

                if (hasAllFilesPermission) {
                    //updateConsoleLog("[ Izin MANAGE_EXTERNAL_STORAGE diberikan ]");
                    storagePermission = true;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(STORAGE_PERMISSION_KEY, true);
                    editor.apply();

                    // Buat direktori setelah izin diberikan
                    //createDirectories();
                } else {
                    //updateConsoleLog("[!] Izin MANAGE_EXTERNAL_STORAGE ditolak");
                    storagePermission = false;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(STORAGE_PERMISSION_KEY, false);
                    editor.apply();
                }
            }
        }

    }

    private void createDirectories() {
        /*
        try {
            File baseDir = new File(basePath);
            if (!baseDir.exists()) {
                boolean baseDirCreated = baseDir.mkdirs();
                Log.d("StorageDebug", "Membuat basePath: " + basePath + " - Hasil: " + baseDirCreated);
                //updateConsoleLog("[ Mencoba membuat direktori base: " + (baseDirCreated ? "berhasil" : "gagal") + " ]");
            }

            File imagesDir = new File(imagesPath);
            if (!imagesDir.exists()) {
                boolean imagesDirCreated = imagesDir.mkdirs();
                Log.d("StorageDebug", "Membuat imagesPath: " + imagesPath + " - Hasil: " + imagesDirCreated);
                //updateConsoleLog("[ Mencoba membuat direktori images: " + (imagesDirCreated ? "berhasil" : "gagal") + " ]");
            }
        } catch (Exception e) {
            Log.e("StorageDebug", "Error membuat direktori: " + e.getMessage(), e);
            //updateConsoleLog("[!] Error membuat direktori: " + e.getMessage());
        }
        */
    }

    private  void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fMainContainer, fragment);
        fragmentTransaction.commit();
    }

    // Fungsi untuk menampilkan Toast
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity onResume called");

        // Refresh tab aktif untuk memastikan UI sinkron dengan state
        TabItem activeTab = tabViewModel.getActiveTab().getValue();
        if (activeTab != null) {
            // Dapatkan fragment dari cache
            Fragment fragment = fragmentCache.get(activeTab.getId());

            // Jika fragment adalah GeckoViewFragment, pastikan session aktif
            if (fragment instanceof GeckoViewFragment) {
                // Pastikan fragment terlihat dan sesi aktif
                ((GeckoViewFragment) fragment).refreshSession();

                // Perbarui tampilan address bar
                updateAddressBarInfo(activeTab.getTitle(), true);

                Log.d(TAG, "Refreshed active tab session: " + activeTab.getId());
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Simpan status tab ke penyimpanan
        tabViewModel.saveTabState();

        Log.d(TAG, "Activity stopped, tab state saved");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (captchaDataManager != null) {
            captchaDataManager.close();
        }
        // Cleanup jika Activity benar-benar dihancurkan (bukan karena konfigurasi)
        if (isFinishing()) {
            // Tutup semua GeckoSession untuk mencegah memory leak
            GeckoSessionPool.getInstance().closeAllSessions();

            Log.d(TAG, "Activity finishing, closed all sessions");
        }
    }

}// akhir class