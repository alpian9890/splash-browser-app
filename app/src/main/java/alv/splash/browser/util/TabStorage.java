package alv.splash.browser.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import alv.splash.browser.model.TabItem;

/**
 * Kelas untuk menyimpan dan memulihkan data tab menggunakan SharedPreferences
 */
public class TabStorage {
    private static final String TAG = "TabStorage";
    private static final String PREFS_NAME = "browser_tab_prefs";
    private static final String KEY_TABS = "saved_tabs";
    private static final String KEY_ACTIVE_TAB_ID = "active_tab_id";

    private final SharedPreferences prefs;
    private final Gson gson;

    public TabStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    /**
     * Menyimpan daftar tab dan ID tab aktif
     */
    public void saveTabs(List<TabItem> tabs, String activeTabId) {
        if (tabs == null) {
            return;
        }

        try {
            // Konversi list Tab ke JSON
            String tabsJson = gson.toJson(tabs);

            // Simpan ke SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_TABS, tabsJson);
            editor.putString(KEY_ACTIVE_TAB_ID, activeTabId);
            editor.apply();

            Log.d(TAG, "Saved " + tabs.size() + " tabs, active tab: " + activeTabId);
        } catch (Exception e) {
            Log.e(TAG, "Error saving tabs: " + e.getMessage(), e);
        }
    }

    /**
     * Memulihkan daftar tab yang disimpan
     */
    public List<TabItem> restoreTabs() {
        try {
            String tabsJson = prefs.getString(KEY_TABS, "");

            if (tabsJson.isEmpty()) {
                Log.d(TAG, "No saved tabs found");
                return new ArrayList<>();
            }

            // Konversi JSON kembali ke list Tab
            Type listType = new TypeToken<ArrayList<TabItem>>(){}.getType();
            List<TabItem> tabs = gson.fromJson(tabsJson, listType);

            Log.d(TAG, "Restored " + tabs.size() + " tabs");
            return tabs;
        } catch (Exception e) {
            Log.e(TAG, "Error restoring tabs: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Memulihkan ID tab aktif
     */
    public String restoreActiveTabId() {
        return prefs.getString(KEY_ACTIVE_TAB_ID, "");
    }

    /**
     * Menghapus semua data tab yang disimpan
     */
    public void clearSavedTabs() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_TABS);
        editor.remove(KEY_ACTIVE_TAB_ID);
        editor.apply();

        Log.d(TAG, "Cleared all saved tabs");
    }
}