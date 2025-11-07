package alv.splash.browser;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;

import java.util.ArrayList;
import java.util.List;

import alv.splash.browser.model.TabItem;
import alv.splash.browser.util.GeckoSessionPool;
import alv.splash.browser.viewmodel.TabViewModel;

/**
 * Kelas singleton yang bertindak sebagai adapter antara kode lama
 * dan TabViewModel baru untuk transisi ke MVVM
 */
public class TabManager {
    private static TabManager instance;
    private TabViewModel viewModel;
    private List<TabChangeListener> listeners = new ArrayList<>();
    private static final String TAG = "TabManager";

    public interface TabChangeListener {
        void onTabsChanged();
        void onActiveTabChanged(TabItem tab);
    }

    private TabManager() {
        // Kosong karena viewModel harus di-set dari luar
    }

    public static synchronized TabManager getInstance() {
        if (instance == null) {
            instance = new TabManager();
        }
        return instance;
    }

    /**
     * Mengatur ViewModel yang akan digunakan
     */
    public void setViewModel(TabViewModel viewModel, LifecycleOwner lifecycleOwner) {
        this.viewModel = viewModel;

        // Observasi LiveData untuk perubahan daftar tab
        viewModel.getTabs().observe(lifecycleOwner, tabs -> {
            notifyTabsChanged();
        });

        // Observasi LiveData untuk perubahan tab aktif
        viewModel.getActiveTab().observe(lifecycleOwner, tab -> {
            notifyActiveTabChanged(tab);
        });

        // Observasi LiveData untuk peristiwa tab
        viewModel.getTabEvents().observe(lifecycleOwner, event -> {
            if (event.getType() == TabViewModel.TabEventType.CLOSED) {
                TabItem closedTab = (TabItem) event.getData();
                // Tutup GeckoSession untuk tab yang ditutup
                GeckoSessionPool.getInstance().closeSession(closedTab.getId());
            } else if (event.getType() == TabViewModel.TabEventType.ALL_CLOSED) {
                // Tutup semua GeckoSession
                GeckoSessionPool.getInstance().closeAllSessions();
            }
        });

        Log.d(TAG, "ViewModel set and observers registered");
    }

    /**
     * Mendaftarkan listener untuk perubahan tab
     */
    public void addTabChangeListener(TabChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Menghapus listener perubahan tab
     */
    public void removeTabChangeListener(TabChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Membuat tab baru
     */
    public TabItem addTab(String url) {
        checkViewModel();
        return viewModel.addTab(url);
    }

    /**
     * Menutup tab berdasarkan ID
     */
    public void closeTab(String tabId) {
        checkViewModel();
        viewModel.closeTab(tabId);
    }

    /**
     * Menutup semua tab
     */
    public void closeAllTabs() {
        checkViewModel();
        viewModel.closeAllTabs();
    }

    /**
     * Mengatur tab aktif
     */
    public void setActiveTab(TabItem tab) {
        checkViewModel();
        viewModel.setActiveTab(tab);
    }

    /**
     * Mendapatkan tab aktif
     */
    public TabItem getActiveTab() {
        checkViewModel();
        return viewModel.getActiveTab().getValue();
    }

    /**
     * Mendapatkan semua tab
     */
    public List<TabItem> getAllTabs() {
        checkViewModel();
        List<TabItem> tabs = viewModel.getTabs().getValue();
        return tabs != null ? new ArrayList<>(tabs) : new ArrayList<>();
    }

    /**
     * Memperbarui informasi tab
     */
    public void updateTabInfo(String tabId, String title, String url, Bitmap favicon) {
        checkViewModel();
        viewModel.updateTabInfo(tabId, title, url, favicon);
    }

    /**
     * Memeriksa apakah ViewModel telah diinisialisasi
     */
    private void checkViewModel() {
        if (viewModel == null) {
            throw new IllegalStateException("TabViewModel not initialized. Call setViewModel first.");
        }
    }

    /**
     * Mengirim notifikasi tentang perubahan daftar tab
     */
    private void notifyTabsChanged() {
        for (TabChangeListener listener : listeners) {
            listener.onTabsChanged();
        }
    }

    /**
     * Mengirim notifikasi tentang perubahan tab aktif
     */
    private void notifyActiveTabChanged(TabItem tab) {
        for (TabChangeListener listener : listeners) {
            listener.onActiveTabChanged(tab);
        }
    }
}