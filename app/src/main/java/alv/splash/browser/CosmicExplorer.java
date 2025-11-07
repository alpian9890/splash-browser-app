package alv.splash.browser;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.mozilla.geckoview.BuildConfig;
import org.mozilla.geckoview.ContentBlocking;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.GeckoSessionSettings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CosmicExplorer extends Application {
    private static volatile GeckoRuntime geckoRuntime;
    private static CosmicExplorer instance;
    private SharedPreferences sharedPreferences;
    private boolean profileModeEnabled;
    private final Map<String, GeckoSessionSettings> tabProfiles = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialize preferences
        sharedPreferences = getSharedPreferences("browser_preferences", MODE_PRIVATE);
        profileModeEnabled = sharedPreferences.getBoolean("profile_mode_enabled", false);

        // Initialize GeckoRuntime as a singleton
        //initializeGeckoRuntime();

        // Initialize other components
        //TabManager.getInstance();
    }

    private void initializeGeckoRuntime() {
//        if (geckoRuntime == null) {
//            try {
//                GeckoRuntimeSettings.Builder runtimeSettingsBuilder = new GeckoRuntimeSettings.Builder()
//                        .aboutConfigEnabled(true)
//                        .contentBlocking(new ContentBlocking.Settings.Builder()
//                                .antiTracking(ContentBlocking.AntiTracking.DEFAULT)
//                                .strictSocialTrackingProtection(true)
//                                .cookieBehavior(ContentBlocking.CookieBehavior.ACCEPT_FIRST_PARTY)
//                                .enhancedTrackingProtectionLevel(ContentBlocking.EtpLevel.STRICT)
//                                .build())
//                        .consoleOutput(true)
//                        .debugLogging(true)  // Aktifkan debug logging
//                        .remoteDebuggingEnabled(true);  // Aktifkan remote debugging
//
//                geckoRuntime = GeckoRuntime.create(this, runtimeSettingsBuilder.build());
//                Log.d("GeckoRuntime", "GeckoRuntime initialized");
//
//            } catch (Exception e) {
//                Log.e("GeckoRuntime", "Error initializing GeckoRuntime", e);
//                e.printStackTrace();
//            }
//        }
    }

    public static CosmicExplorer getInstance() {
        Log.d("CosmicExplorer", "getInstance called");
        return instance;
    }

    public static GeckoRuntime getGeckoRuntime() {
        Log.d("CosmicExplorer", "getGeckoRuntime called");
        return geckoRuntime;
    }

    public static synchronized GeckoRuntime getGeckoRuntime(Context context) {
        if (geckoRuntime == null) {
            synchronized (CosmicExplorer.class) {
                if (geckoRuntime == null) {
                    try {
                        Context appContext = context.getApplicationContext();
                       final GeckoRuntimeSettings.Builder runtimeSettingsBuilder = new GeckoRuntimeSettings.Builder()
                                .aboutConfigEnabled(true)
                                .contentBlocking(new ContentBlocking.Settings.Builder()
                                        .antiTracking(ContentBlocking.AntiTracking.DEFAULT)
                                        .strictSocialTrackingProtection(true)
                                        .cookieBehavior(ContentBlocking.CookieBehavior.ACCEPT_FIRST_PARTY)
                                        .enhancedTrackingProtectionLevel(ContentBlocking.EtpLevel.STRICT)
                                        .build())
                                .consoleOutput(true)
                                .debugLogging(true)
                                .remoteDebuggingEnabled(true);
                        geckoRuntime = GeckoRuntime.create(appContext, runtimeSettingsBuilder.build());
                        Log.d("GeckoRuntime", "GeckoRuntime initialized successfully");
                    } catch (Exception e) {
                        Log.e("GeckoRuntime", "Critical error initializing GeckoRuntime", e);
                        // Pertimbangkan untuk menangani kegagalan dengan lebih baik
                        return null;
                    }
                }
            }
        }
        return geckoRuntime;
    }

    public boolean isProfileModeEnabled() {
        Log.d("CosmicExplorer", "isProfileModeEnabled called");
        return profileModeEnabled;
    }

    public void setProfileModeEnabled(boolean enabled) {
        Log.d("CosmicExplorer", "setProfileModeEnabled called with enabled=" + enabled);
        profileModeEnabled = enabled;
        sharedPreferences.edit().putBoolean("profile_mode_enabled", enabled).apply();
    }

    public GeckoSessionSettings getTabProfile(String tabId) {
        if (!profileModeEnabled) {
            Log.d("CosmicExplorer", "Profile mode is disabled, returning default settings");
            // Return default profile settings
            return new GeckoSessionSettings.Builder().build();
        }

        if (!tabProfiles.containsKey(tabId)) {
            // Create a new profile for this tab
            Log.d("CosmicExplorer", "Creating new profile for tab: " + tabId);
            GeckoSessionSettings profile = new GeckoSessionSettings.Builder()
                    .usePrivateMode(false)
                    .build();
            tabProfiles.put(tabId, profile);
        }

        return tabProfiles.get(tabId);
    }
    public void clearTabProfile(String tabId) {
        tabProfiles.remove(tabId);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= TRIM_MEMORY_MODERATE) {
            Log.d("CosmicExplorer", "Trimming memory due to system pressure");
            // Pertimbangkan untuk membebaskan sumber daya yang tidak penting
        }
    }

    public void onTerminate() {
        super.onTerminate();
        if (geckoRuntime != null) {
            geckoRuntime.shutdown();
            geckoRuntime = null;
        }
    }

}
