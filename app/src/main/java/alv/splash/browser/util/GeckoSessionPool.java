package alv.splash.browser.util;

import android.util.Log;

import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoSessionSettings;

import java.util.HashMap;
import java.util.Map;

import alv.splash.browser.CosmicExplorer;

/**
 * Pool untuk mengelola dan menggunakan kembali instansi GeckoSession
 * untuk meningkatkan performa dan mengurangi penggunaan memori
 */
public class GeckoSessionPool {
    private static final String TAG = "GeckoSessionPool";
    private static GeckoSessionPool instance;

    // Peta untuk menyimpan sesi berdasarkan ID tab
    private final Map<String, GeckoSession> sessionMap = new HashMap<>();

    private GeckoSessionPool() {
        // Konstruktor private untuk singleton
    }

    /**
     * Mendapatkan instance GeckoSessionPool (singleton)
     */
    public static synchronized GeckoSessionPool getInstance() {
        if (instance == null) {
            instance = new GeckoSessionPool();
        }
        return instance;
    }

    /**
     * Mendapatkan sesi untuk tab tertentu. Jika sesi sudah ada,
     * akan mengembalikan sesi yang ada. Jika tidak, membuat sesi baru.
     */
    public GeckoSession getSession(String tabId, GeckoRuntime runtime) {
        // Cek apakah sesi sudah ada di pool
        GeckoSession session = sessionMap.get(tabId);

        if (session == null) {
            // Buat sesi baru jika belum ada
            GeckoSessionSettings settings = CosmicExplorer.getInstance().getTabProfile(tabId);
            session = new GeckoSession(settings);

            sessionMap.put(tabId, session);
            Log.d(TAG, "Created new GeckoSession for tab: " + tabId);
        } else {
            Log.d(TAG, "Reusing existing GeckoSession for tab: " + tabId);
        }

        // Pastikan sesi terbuka
        if (!session.isOpen()) {
            try {
                session.open(runtime);
                Log.d(TAG, "Opened GeckoSession for tab: " + tabId);
            } catch (Exception e) {
                Log.e(TAG, "Error opening GeckoSession for tab: " + tabId, e);
            }
        }

        return session;
    }

    /**
     * Menutup dan menghapus sesi untuk tab tertentu
     */
    public void closeSession(String tabId) {
        GeckoSession session = sessionMap.remove(tabId);

        if (session != null) {
            try {
                session.close();
                Log.d(TAG, "Closed and removed GeckoSession for tab: " + tabId);
            } catch (Exception e) {
                Log.e(TAG, "Error closing GeckoSession for tab: " + tabId, e);
            }
        }
    }

    /**
     * Memeriksa apakah sesi untuk tab tertentu sudah ada di pool
     */
    public boolean hasSession(String tabId) {
        return sessionMap.containsKey(tabId);
    }

    /**
     * Mengambil session tanpa membuka, digunakan untuk memasukkan
     * sesi yang sudah ada sebelumnya ke dalam pool
     */
    public GeckoSession getExistingSession(String tabId) {
        return sessionMap.get(tabId);
    }

    /**
     * Menempatkan sesi yang sudah ada ke dalam pool
     */
    public void putSession(String tabId, GeckoSession session) {
        sessionMap.put(tabId, session);
        Log.d(TAG, "Added existing GeckoSession to pool for tab: " + tabId);
    }

    /**
     * Menutup semua sesi
     */
    public void closeAllSessions() {
        for (Map.Entry<String, GeckoSession> entry : sessionMap.entrySet()) {
            try {
                entry.getValue().close();
                Log.d(TAG, "Closed GeckoSession for tab: " + entry.getKey());
            } catch (Exception e) {
                Log.e(TAG, "Error closing GeckoSession for tab: " + entry.getKey(), e);
            }
        }

        sessionMap.clear();
        Log.d(TAG, "All GeckoSessions closed and removed from pool");
    }
}