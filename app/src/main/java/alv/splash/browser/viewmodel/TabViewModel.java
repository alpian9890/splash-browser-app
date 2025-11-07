package alv.splash.browser.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import alv.splash.browser.CosmicExplorer;
import alv.splash.browser.model.TabItem;
import alv.splash.browser.util.TabStorage;

/**
 * ViewModel untuk mengelola data tab browser dan status terkait
 */
public class TabViewModel extends AndroidViewModel {
    private static final String TAG = "TabViewModel";

    private TabStorage tabStorage;

    // LiveData untuk daftar tab
    private final MutableLiveData<List<TabItem>> tabsLiveData = new MutableLiveData<>(new ArrayList<>());

    // LiveData untuk tab aktif
    private final MutableLiveData<TabItem> activeTabLiveData = new MutableLiveData<>();

    // LiveData untuk kejadian tab (misal: tab dibuka, ditutup)
    private final MutableLiveData<TabEvent> tabEventLiveData = new MutableLiveData<>();

    // State untuk mengetahui apakah tab sedang dimuat
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public TabViewModel(@NonNull Application application) {
        super(application);
        tabStorage = new TabStorage(application);

        // Coba pulihkan tab saat ViewModel dibuat
        restoreSavedTabs();

        Log.d(TAG, "TabViewModel initialized with tab storage");
    }

    /**
     * Menyimpan state tab saat ini ke penyimpanan
     */
    public void saveTabState() {
        List<TabItem> tabs = getCurrentTabs();

        if (tabs.isEmpty()) {
            Log.d(TAG, "No tabs to save");
            return;
        }

        TabItem activeTab = activeTabLiveData.getValue();
        String activeTabId = activeTab != null ? activeTab.getId() : "";

        tabStorage.saveTabs(tabs, activeTabId);
        Log.d(TAG, "Saved " + tabs.size() + " tabs to storage");
    }

    /**
     * Memulihkan tab yang disimpan dari penyimpanan
     */
    public void restoreSavedTabs() {
        // Pulihkan tab dari penyimpanan
        List<TabItem> savedTabs = tabStorage.restoreTabs();

        if (savedTabs.isEmpty()) {
            Log.d(TAG, "No saved tabs to restore");
            return;
        }

        // Setel tab yang dipulihkan ke LiveData
        tabsLiveData.setValue(savedTabs);

        // Pulihkan tab aktif
        String activeTabId = tabStorage.restoreActiveTabId();
        if (!activeTabId.isEmpty()) {
            for (TabItem tab : savedTabs) {
                if (tab.getId().equals(activeTabId)) {
                    tab.setActive(true);
                    activeTabLiveData.setValue(tab);

                    // Beri tahu observer tentang tab yang dipulihkan
                    tabEventLiveData.setValue(new TabEvent(TabEventType.RESTORED, savedTabs));

                    Log.d(TAG, "Restored active tab: " + tab.getId() + ", URL: " + tab.getUrl());
                    break;
                }
            }
        } else if (!savedTabs.isEmpty()) {
            // Jika tidak ada tab aktif yang disimpan, aktifkan tab pertama
            TabItem firstTab = savedTabs.get(0);
            firstTab.setActive(true);
            activeTabLiveData.setValue(firstTab);

            Log.d(TAG, "No active tab found, activated first tab: " + firstTab.getId());
        }

        Log.d(TAG, "Restored " + savedTabs.size() + " tabs from storage");
    }

    /**
     * Mendapatkan daftar tab sebagai LiveData untuk observasi
     */
    public LiveData<List<TabItem>> getTabs() {
        return tabsLiveData;
    }

    /**
     * Mendapatkan tab aktif sebagai LiveData untuk observasi
     */
    public LiveData<TabItem> getActiveTab() {
        return activeTabLiveData;
    }

    /**
     * Mendapatkan peristiwa tab sebagai LiveData untuk observasi
     */
    public LiveData<TabEvent> getTabEvents() {
        return tabEventLiveData;
    }

    /**
     * Mendapatkan status loading sebagai LiveData
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Membuat tab baru dengan URL tertentu
     */
    public TabItem addTab(String url) {
        List<TabItem> currentTabs = getCurrentTabs();
        TabItem newTab = new TabItem(url);
        currentTabs.add(newTab);
        tabsLiveData.setValue(currentTabs);
        setActiveTab(newTab);

        // Beri tahu observer tentang tab baru
        tabEventLiveData.setValue(new TabEvent(TabEventType.ADDED, newTab));

        Log.d(TAG, "New tab added: " + newTab.getId() + ", URL: " + url);
        return newTab;
    }

    /**
     * Menutup tab berdasarkan ID
     */
    public void closeTab(String tabId) {
        List<TabItem> currentTabs = getCurrentTabs();
        TabItem tabToRemove = null;
        int tabIndex = -1;

        // Temukan tab yang akan ditutup
        for (int i = 0; i < currentTabs.size(); i++) {
            TabItem tab = currentTabs.get(i);
            if (tab.getId().equals(tabId)) {
                tabToRemove = tab;
                tabIndex = i;
                break;
            }
        }

        if (tabToRemove != null) {
            boolean wasActive = tabToRemove.isActive();

            // Hapus profil tab jika mode profil diaktifkan
            if (CosmicExplorer.getInstance().isProfileModeEnabled()) {
                CosmicExplorer.getInstance().clearTabProfile(tabId);
            }

            // Hapus tab dari daftar
            currentTabs.remove(tabToRemove);
            tabsLiveData.setValue(currentTabs);

            // Beri tahu observer tentang tab yang ditutup
            tabEventLiveData.setValue(new TabEvent(TabEventType.CLOSED, tabToRemove));

            // Jika tidak ada tab tersisa, buat tab home baru
            if (currentTabs.isEmpty()) {
                TabItem homeTab = addTab("about:home");
                setActiveTab(homeTab);
                Log.d(TAG, "Created new home tab as all tabs were closed");
            }
            // Jika tab yang ditutup adalah tab aktif, aktifkan tab berikutnya
            else if (wasActive) {
                int newIndex = Math.min(tabIndex, currentTabs.size() - 1);
                setActiveTab(currentTabs.get(newIndex));
                Log.d(TAG, "Set active tab to: " + currentTabs.get(newIndex).getId());
            }
        } else {
            Log.w(TAG, "Attempted to close non-existent tab: " + tabId);
        }
    }

    /**
     * Menutup semua tab dan membuat tab home baru
     */
    public void closeAllTabs() {
        List<TabItem> currentTabs = getCurrentTabs();

        // Buat salinan tabs untuk dikirim sebagai event
        ArrayList<TabItem> closedTabs = new ArrayList<>(currentTabs);

        // Hapus semua tab
        currentTabs.clear();
        tabsLiveData.setValue(currentTabs);

        // Beri tahu observer tentang semua tab yang ditutup
        tabEventLiveData.setValue(new TabEvent(TabEventType.ALL_CLOSED, closedTabs));

        // Buat tab home baru
        TabItem homeTab = addTab("about:home");
        Log.d(TAG, "All tabs closed and new home tab created");
    }

    /**
     * Mengatur tab aktif
     */
    public void setActiveTab(TabItem tab) {
        if (tab == null) {
            Log.w(TAG, "Attempted to set null tab as active");
            return;
        }

        // Jika tab sudah aktif, tidak perlu melakukan apa-apa
        TabItem currentActive = activeTabLiveData.getValue();
        if (currentActive != null && currentActive.getId().equals(tab.getId())) {
            Log.d(TAG, "Tab already active: " + tab.getId());
            return;
        }

        List<TabItem> currentTabs = getCurrentTabs();

        // Non-aktifkan tab yang saat ini aktif
        if (currentActive != null) {
            for (TabItem t : currentTabs) {
                if (t.getId().equals(currentActive.getId())) {
                    t.setActive(false);
                }
            }
        }

        // Aktifkan tab baru
        for (TabItem t : currentTabs) {
            if (t.getId().equals(tab.getId())) {
                t.setActive(true);
                activeTabLiveData.setValue(t);
                Log.d(TAG, "Active tab set to: " + t.getId());

                // Beri tahu observer tentang perubahan tab aktif
                tabEventLiveData.setValue(new TabEvent(TabEventType.ACTIVE_CHANGED, t));
                break;
            }
        }

        tabsLiveData.setValue(currentTabs);
    }

    /**
     * Memperbarui informasi tab (judul, URL, favicon)
     */
    public void updateTabInfo(String tabId, String title, String url, Bitmap favicon) {
        List<TabItem> currentTabs = getCurrentTabs();
        boolean tabFound = false;

        for (TabItem tab : currentTabs) {
            if (tab.getId().equals(tabId)) {
                boolean updated = false;

                if (title != null && !title.isEmpty() && !title.equals(tab.getTitle())) {
                    tab.setTitle(title);
                    updated = true;
                }

                if (url != null && !url.equals(tab.getUrl())) {
                    tab.setUrl(url);
                    updated = true;
                }

                if (favicon != null) {
                    tab.setFavicon(favicon);
                    updated = true;
                }

                if (updated) {
                    tabsLiveData.setValue(currentTabs);

                    // Jika tab yang diperbarui adalah tab aktif, perbarui juga activeTabLiveData
                    if (tab.isActive()) {
                        activeTabLiveData.setValue(tab);
                    }

                    // Beri tahu observer tentang tab yang diperbarui
                    tabEventLiveData.setValue(new TabEvent(TabEventType.UPDATED, tab));
                }

                tabFound = true;
                break;
            }
        }

        if (!tabFound) {
            Log.w(TAG, "Attempted to update non-existent tab: " + tabId);
        }
    }

    /**
     * Mendapatkan daftar tab saat ini
     */
    private List<TabItem> getCurrentTabs() {
        List<TabItem> currentTabs = tabsLiveData.getValue();
        return currentTabs != null ? new ArrayList<>(currentTabs) : new ArrayList<>();
    }

    /**
     * Mengatur status loading
     */
    public void setLoading(boolean loading) {
        isLoading.setValue(loading);
    }

    /**
     * Enum untuk tipe peristiwa tab
     */
    public enum TabEventType {
        ADDED,
        CLOSED,
        ALL_CLOSED,
        ACTIVE_CHANGED,
        UPDATED,
        RESTORED
    }

    /**
     * Kelas untuk menampung informasi peristiwa tab
     */
    public static class TabEvent {
        private final TabEventType type;
        private final Object data;

        public TabEvent(TabEventType type, Object data) {
            this.type = type;
            this.data = data;
        }

        public TabEventType getType() {
            return type;
        }

        public Object getData() {
            return data;
        }
    }
}