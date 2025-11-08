package alv.splash.browser.ui.fragment;

import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.ContentBlocking;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoSessionSettings;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.WebResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alv.splash.browser.BookmarkManager;
import alv.splash.browser.CosmicExplorer;
import alv.splash.browser.GlideFaviconFetcher;
import alv.splash.browser.HistoryItem;
import alv.splash.browser.HistoryManager;
import alv.splash.browser.LoginCredential;
import alv.splash.browser.MainActivity;
import alv.splash.browser.PasswordManager;
import alv.splash.browser.R;
import alv.splash.browser.TabManager;
import alv.splash.browser.UrlValidator;
import alv.splash.browser.util.GeckoSessionPool;
import alv.splash.browser.viewmodel.TabViewModel;

public class GeckoViewFragment extends Fragment {
    private String tabId;
    private GeckoView geckoView;
    private GeckoSession session;
    private GeckoSession.SessionState sessionState;
    private static final String BUNDLE_KEY = "gecko";
    private static final String HOME_PAGE = "about:home";
    private SwipeRefreshLayout swipeRefreshGecko;
    private ProgressBar progressBar;
    private String initialUrl;
    private boolean canGoBack = false;
    private boolean canGoForward = false;
    private String currentUrl = "";
    private String pageTitle = "";
    private boolean pageSecure = false;
    private Bitmap favicon;
    private HistoryManager historyManager;
    private PopupMenu contextMenu;
    private BookmarkManager bookmarkManager;
    private PasswordManager passwordManager;
    private TabViewModel tabViewModel;
    private boolean isSessionInitialized = false;
    private static final String TAG = "GeckoViewFragment";

    // Singleton pool untuk GeckoSession
    private static final Map<String, GeckoSession> sessionPool = new HashMap<>();


    public static GeckoViewFragment newInstance(String tabId, String url, boolean isRestored) {
        GeckoViewFragment fragment = new GeckoViewFragment();
        Bundle args = new Bundle();
        args.putString("tabId", tabId);
        args.putString("url", url);
        args.putBoolean("isRestored", isRestored);
        fragment.setArguments(args);
        Log.d(TAG, "newInstance() called with tabId: " + tabId + ", url: " + url + ", isRestored: " + isRestored);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Dapatkan ViewModel dari activity
        tabViewModel = new ViewModelProvider(requireActivity()).get(TabViewModel.class);

        boolean isRestored = false;
        if (getArguments() != null) {
            tabId = getArguments().getString("tabId");
            initialUrl = getArguments().getString("url");
            isRestored = getArguments().getBoolean("isRestored", false);
        }

        historyManager = HistoryManager.getInstance(requireContext());
        bookmarkManager = BookmarkManager.getInstance(requireContext());
        passwordManager = PasswordManager.getInstance(requireContext());
        if (savedInstanceState != null) {
            tabId = savedInstanceState.getString("tabId");
            initialUrl = savedInstanceState.getString("url");
            sessionState = savedInstanceState.getParcelable(BUNDLE_KEY);
        }
        if (savedInstanceState != null) {
            tabId = savedInstanceState.getString("tabId");
            initialUrl = savedInstanceState.getString("url");
            sessionState = savedInstanceState.getParcelable(BUNDLE_KEY);
        }

        Log.d(TAG, "onCreate() for tab: " + tabId);
        if (isRestored) {
            Log.d(TAG, "Tab is being restored: " + tabId + ", URL: " + initialUrl);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_gecko, container, false);

        geckoView = view.findViewById(R.id.webGeckoView);
        progressBar = view.findViewById(R.id.progressBarWeb);
        swipeRefreshGecko = view.findViewById(R.id.swipeRefreshGecko);

        swipeRefreshGecko.setOnRefreshListener(this::reload);


        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("GeckoViewFragment", "onViewCreated() called");

        setupGeckoSession();
        setupContextMenu();
    }

    private void setupGeckoSession() {
        GeckoRuntime runtime = CosmicExplorer.getGeckoRuntime(requireContext());
        if (runtime == null) {
            Log.e("GeckoViewFragment", "Failed to get GeckoRuntime - cannot continue");
            Toast.makeText(requireContext(), "Error initializing browser engine", Toast.LENGTH_LONG).show();
            return;
        }

        // Cek apakah session sudah ada di pool
        boolean sessionExists = GeckoSessionPool.getInstance().hasSession(tabId);
        if (sessionExists) {
            // Gunakan session yang sudah ada dari pool
            session = GeckoSessionPool.getInstance().getSession(tabId, runtime);
            Log.d(TAG, "Reusing existing session from pool for tab: " + tabId);
        } else {
            // Buat session baru
            GeckoSessionSettings settings = CosmicExplorer.getInstance().getTabProfile(tabId);

            if (sessionState != null) {
                // Restore dari state yang disimpan
                session = new GeckoSession(settings);
                session.restoreState(sessionState);
                Log.d(TAG, "Created session from saved state for tab: " + tabId);
            } else {
                // Buat session baru dari awal
                session = new GeckoSession(settings);
                Log.d(TAG, "Created brand new session for tab: " + tabId);
            }

            // Setup delegates sebelum menyimpan ke pool
            setupSessionDelegates();

            // Tambahkan ke pool untuk penggunaan selanjutnya
            GeckoSessionPool.getInstance().putSession(tabId, session);

            // Buka session jika belum dibuka
            if (!session.isOpen()) {
                try {
                    session.open(runtime);
                    Log.d(TAG, "Opened session for tab: " + tabId);

                    // Jika ini tab yang dipulihkan dan URL bukan about:home,
                    // kita perlu memuat ulang URL setelah session dibuka
                    boolean isRestored = getArguments() != null &&
                            getArguments().getBoolean("isRestored", false);

                    if (isRestored && initialUrl != null && !initialUrl.equals("about:home")) {
                        // Untuk tab yang dipulihkan, selalu muat ulang URL untuk memastikan konten benar
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            loadUrl(initialUrl);
                            Log.d(TAG, "Reloading URL for restored tab: " + initialUrl);
                        }, 100); // Sedikit delay untuk memastikan session siap
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error opening session: " + e.getMessage(), e);
                }
            }
        }

        // Pasang session ke view
        geckoView.setSession(session);
        geckoView.setSaveEnabled(true);
        isSessionInitialized = true;

        // Load URL awal jika ada dan session baru dibuat (bukan reused)
        if (initialUrl != null && !initialUrl.isEmpty() && !sessionExists) {
            loadUrl(initialUrl);
            Log.d(TAG, "Initial URL loaded: " + initialUrl + " for tab: " + tabId);
        }
    }

    private void setupSessionDelegates() {
        // Set up all delegates
        session.setProgressDelegate(new ProgressDelegate());
        session.setContentDelegate(new ContentDelegate());
        session.setNavigationDelegate(new NavigationDelegate());
        session.setContentBlockingDelegate(new ContentBlockingDelegate());
        session.setPromptDelegate(new PromptDelegate());
        session.setHistoryDelegate(new HistoryDelegate());

        Log.d("GeckoViewFragment", "All delegates set for tab: " + tabId);
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (session != null) {
            try {
                // Save session state
                outState.putParcelable(BUNDLE_KEY, sessionState);

                Log.d("GeckoViewFragment", "Saved session state for tab: " + tabId);
            } catch (Exception e) {
                Log.e("GeckoViewFragment", "Error saving session state: " + e.getMessage(), e);
            }
        }

        outState.putString("tabId", tabId);
        outState.putString("url", initialUrl);
    }

    private void setupContextMenu() {
        geckoView.setOnLongClickListener(view -> {
            // This will be handled by GeckoView context callbacks
            return false;
        });
    }

    private void showContextMenu(int x, int y, GeckoSession.ContentDelegate.ContextElement element) {
        if (contextMenu != null) {
            contextMenu.dismiss();
            Log.d("GeckoViewFragment", "Context menu dismissed");
        }

        contextMenu = new PopupMenu(requireContext(), geckoView);
        Menu menu = contextMenu.getMenu();

        if (element.linkUri != null) {
            // Link context menu
            menu.add(Menu.NONE, 1, Menu.NONE, "Open in New Tab");
            menu.add(Menu.NONE, 2, Menu.NONE, "Open in Private Tab");
            menu.add(Menu.NONE, 3, Menu.NONE, "Copy Link Address");
            menu.add(Menu.NONE, 4, Menu.NONE, "Copy Link Text");
            menu.add(Menu.NONE, 5, Menu.NONE, "Add to Bookmarks");

            contextMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 1:
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).createNewTab(element.linkUri);
                        }
                        return true;
                    case 2:
                        // Open in private tab
                        // Implement private browsing
                        return true;
                    case 3:
                        copyToClipboard("Link Address", element.linkUri);
                        return true;
                    case 4:
                        copyToClipboard("Link Text", element.textContent);
                        return true;
                    case 5:
                        bookmarkManager.addBookmark(element.linkUri, element.textContent);
                        Toast.makeText(requireContext(), "Bookmark added", Toast.LENGTH_SHORT).show();
                        return true;
                }
                return false;
            });
        } else if (element.srcUri != null) {
            // Image context menu
            menu.add(Menu.NONE, 6, Menu.NONE, "View Image");
            menu.add(Menu.NONE, 7, Menu.NONE, "Download Image");
            menu.add(Menu.NONE, 8, Menu.NONE, "Copy Image Address");

            contextMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 6:
                        loadUrl(element.srcUri);
                        return true;
                    case 7:
                        // Download image
                        downloadImage(element.srcUri);
                        return true;
                    case 8:
                        copyToClipboard("Image Address", element.srcUri);
                        return true;
                }
                return false;
            });
        } else {
            // General context menu
            menu.add(Menu.NONE, 9, Menu.NONE, "Back");
            menu.add(Menu.NONE, 10, Menu.NONE, "Forward");
            menu.add(Menu.NONE, 11, Menu.NONE, "Reload");
            menu.add(Menu.NONE, 12, Menu.NONE, "Add to Bookmarks");

            contextMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 9:
                        if (canGoBack) goBack();
                        return true;
                    case 10:
                        if (canGoForward) goForward();
                        return true;
                    case 11:
                        reload();
                        return true;
                    case 12:
                        bookmarkManager.addBookmark(currentUrl, pageTitle);
                        Toast.makeText(requireContext(), "Bookmark added", Toast.LENGTH_SHORT).show();
                        return true;
                }
                return false;
            });
        }

        contextMenu.show();
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(requireContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void downloadImage(String imageUrl) {
        // Check for permission first
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            return;
        }

        // Start download
        DownloadManager downloadManager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(imageUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        String fileName = uri.getLastPathSegment();
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        downloadManager.enqueue(request);
        Toast.makeText(requireContext(), "Download started", Toast.LENGTH_SHORT).show();
    }

    public void loadUrl(String url) {
        String validatedUrl = new UrlValidator().processInput(url);
        session.loadUri(validatedUrl);
        Log.d("GeckoViewFragment", "loadUrl() called with URL: " + validatedUrl);
        currentUrl = validatedUrl;
    }
    private
    void saveToHistory(String url, String title) {
        if (url == null || url.isEmpty()) return;

        long timestamp = System.currentTimeMillis();
        HistoryItem historyItem = new HistoryItem(url, title, timestamp);
        historyManager.addHistoryItem(historyItem);
        Log.d("GeckoViewFragment",
                "History item saved: " + historyItem.getUrl() +
                        "Title: " + historyItem.getTitle() +
                        " Timestamp: " + historyItem.getTimestamp());

    }
    private void updateHistoryTitle(String url, String title) {
        // Metode tambahan untuk memperbarui judul halaman yang sudah disimpan
        // Jika judul baru berbeda dan bermakna
        if (title != null && !title.isEmpty()) {
            HistoryItem item = new HistoryItem(url, title, System.currentTimeMillis());
            historyManager.updateHistoryTitle(url, title);
            Log.d("GeckoViewFragment",
                    "History title updated: " + item.getUrl() +
                            "Title: " + item.getTitle() +
                            " Timestamp: " + item.getTimestamp());
        }
    }

    public void reload() {
        session.reload();
        swipeRefreshGecko.setRefreshing(false);
    }

    public boolean canGoBack() {
        return canGoBack;
    }

    public void goBack() {
        if (canGoBack) {
            session.goBack();
        }
    }

    public boolean canGoForward() {
        return canGoForward;
    }

    public void goForward() {
        if (canGoForward) {
            session.goForward();
        }
    }

    public String getCurrentUrl() {
        return currentUrl;
    }

    /**
     * Memperbarui dan mengaktifkan ulang session saat aplikasi di-resume
     * untuk memastikan tampilan visual sinkron dengan state
     */
    public void refreshSession() {
        if (session != null && geckoView != null) {
            try {
                // PERBAIKAN: Hanya set active tanpa release/reattach untuk menghindari crash
                // saat view sedang di-attach ke window
                session.setActive(true);
                
                // Hanya refresh jika geckoView tidak sedang di-attach dan session sudah terpasang
                if (geckoView.getSession() == session && isVisible()) {
                    // Session sudah terpasang dengan benar, tidak perlu refresh ulang
                    Log.d(TAG, "Session already attached correctly for tab: " + tabId);
                } else if (geckoView.getSession() == null && isVisible()) {
                    // Jika session belum terpasang, pasang sekarang
                    geckoView.setSession(session);
                    Log.d(TAG, "Session attached to GeckoView for tab: " + tabId);
                }
                
                Log.d(TAG, "Session activated for tab: " + tabId + ", URL: " + currentUrl);
            } catch (Exception e) {
                Log.e(TAG, "Error refreshing session: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (session != null) {
            try {
                // When pausing, make the session inactive but keep it in memory
                session.setActive(false);
                Log.d("GeckoViewFragment", "Session set inactive for tab: " + tabId);
            } catch (Exception e) {
                Log.e("GeckoViewFragment", "Error deactivating session: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (session != null && geckoView != null) {
            try {
                // Aktifkan session
                session.setActive(true);

                // Periksa apakah ini fragment yang terlihat
                if (isVisible()) {
                    // PERBAIKAN: Tidak perlu release/reattach session
                    // Hanya pastikan session terpasang jika belum
                    if (geckoView.getSession() == null) {
                        geckoView.setSession(session);
                        Log.d(TAG, "Session attached to visible GeckoView: " + tabId);
                    }

                    // Pastikan URL/judul ditampilkan dengan benar
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).updateAddressBarInfo(pageTitle, pageSecure);
                    }

                    Log.d(TAG, "Session activated for visible tab: " + tabId);
                } else {
                    Log.d(TAG, "Session activated for non-visible tab: " + tabId);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error activating session: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Important: Don't close the session here anymore, as we're reusing sessions
        // Just keep it in the pool for later reuse

        // We only want to close sessions when the tab is actually closed
        // This is now handled in closeTab() in TabViewModel via tabEventLiveData

        Log.d("GeckoViewFragment", "onDestroy() called for tab: " + tabId + ", leaving session in pool for reuse");
    }

    /**
     * Method to be called when the tab is actually closed
     * This should be called from the TabViewModel when a tab is removed
     */
    public static void closeSession(String tabId) {
        GeckoSession session = sessionPool.remove(tabId);
        if (session != null) {
            try {
                session.close();
                Log.d("GeckoViewFragment", "Session closed and removed from pool for tab: " + tabId);
            } catch (Exception e) {
                Log.e("GeckoViewFragment", "Error closing session for tab: " + tabId, e);
            }
        }
    }

    // Inner classes for GeckoSession delegates
    private class ProgressDelegate implements GeckoSession.ProgressDelegate {
        @Override
        public void onSessionStateChange(@NonNull GeckoSession session, @NonNull GeckoSession.SessionState geckoSessionState) {
            sessionState = geckoSessionState;
        }

        @Override
        public void onPageStart(GeckoSession session, String url) {
            currentUrl = url;
            updateEditText(currentUrl);

            // Notify ViewModel that page is loading
            tabViewModel.setLoading(true);

            // Update tab info in TabViewModel
            String fetchDomain = Uri.parse(currentUrl != null && !currentUrl.isEmpty() ? currentUrl : "https://google.com").getHost();
            if (fetchDomain == null) fetchDomain = "google.com";

            String finalFetchDomain = fetchDomain;
            GlideFaviconFetcher.fetchFavicon(requireContext(), fetchDomain, new GlideFaviconFetcher.FaviconCallback() {
                @Override
                public void onFaviconLoaded(Bitmap bitmap) {
                    favicon = bitmap;
                    if (isAdded()) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            tabViewModel.updateTabInfo(tabId, pageTitle, currentUrl, favicon);
                        });
                    }
                }

                @Override
                public void onError(Exception e) {
                    if (isAdded()) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            tabViewModel.updateTabInfo(tabId, pageTitle, currentUrl, null);
                        });
                    }
                }
            });
        }

        @Override
        public void onPageStop(GeckoSession session, boolean success) {
            // Notify ViewModel that page finished loading
            tabViewModel.setLoading(false);

            // Update tab info
            new Handler(Looper.getMainLooper()).post(() -> {
                tabViewModel.updateTabInfo(tabId, pageTitle, currentUrl, favicon);
            });

            if (success) {
                saveToHistory(currentUrl, pageTitle);
            }
        }

        @Override
        public void onProgressChange(GeckoSession session, int progress) {
            if (progressBar != null) {
                progressBar.setProgress(progress);

                if (progress > 0 && progress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onSecurityChange(GeckoSession session, SecurityInformation securityInfo) {
            pageSecure = securityInfo.isSecure;
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).updateAddressBarInfo(pageTitle, pageSecure);
            }
        }
    }

    private class ContentDelegate implements GeckoSession.ContentDelegate {
        @Override
        public void onTitleChange(GeckoSession session, String title) {
            pageTitle = title;
            if (currentUrl!= null) {
                updateHistoryTitle(currentUrl, pageTitle);
            }
            TabManager.getInstance().updateTabInfo(tabId, pageTitle, currentUrl, favicon);
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).updateAddressBarInfo(pageTitle, pageSecure);
            }
        }

        @Override
        public void onContextMenu(GeckoSession session, int screenX, int screenY, ContextElement element) {
            new Handler(Looper.getMainLooper()).post(() -> {
                showContextMenu(screenX, screenY, element);
            });
        }

        @Override
        public void onExternalResponse(GeckoSession session, WebResponse response) {
            // Handle downloads
            String mimeType = response.headers.get("Content-Type");
            String url = response.uri;

            if (mimeType == null || mimeType.isEmpty()){
                mimeType = getMimeTypeFromUrl(url);
            }
            // Start the download
            downloadFile(url, mimeType);
        }
    }

    private class NavigationDelegate implements GeckoSession.NavigationDelegate {
        @Override
        public void onCanGoBack(GeckoSession session, boolean canGoBack) {
            GeckoViewFragment.this.canGoBack = canGoBack;
        }

        @Override
        public void onCanGoForward(GeckoSession session, boolean canGoForward) {
            GeckoViewFragment.this.canGoForward = canGoForward;
        }

        @Override
        public void onLocationChange(GeckoSession session, String url, List<GeckoSession.PermissionDelegate.ContentPermission> perms, Boolean hasUserGesture) {
            currentUrl = url;
            updateEditText(currentUrl);
            Log.d("GeckoViewFragment", "onLocationChange() called with URL: " + url);
        }

        @Override
        public GeckoResult<AllowOrDeny> onLoadRequest(GeckoSession session,
                                                      LoadRequest request) {
            // Handle different URL schemes
            Uri uri = Uri.parse(request.uri);
            String scheme = uri.getScheme();

            if ("mailto".equals(scheme)) {
                // Handle mailto links
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(uri);
                startActivity(Intent.createChooser(emailIntent, "Send email"));
                return GeckoResult.allow();
            } else if ("tel".equals(scheme)) {
                // Handle tel links
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(uri);
                startActivity(dialIntent);
                return GeckoResult.allow();
            }

            return GeckoResult.allow();
        }
    }

    private class ContentBlockingDelegate implements ContentBlocking.Delegate {
        @Override
        public void onContentBlocked(GeckoSession session, ContentBlocking.BlockEvent event) {
            // Log content blocking
            Log.d("ContentBlocking", "Blocked: " + event.getSafeBrowsingCategory());
        }
    }

    private class PromptDelegate implements GeckoSession.PromptDelegate {
        @Override
        public GeckoResult<PromptResponse> onAlertPrompt(GeckoSession session, AlertPrompt prompt) {
            GeckoResult<PromptResponse> result = new GeckoResult<>();

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(prompt.title)
                    .setMessage(prompt.message)
                    .setPositiveButton("OK", (dialog, which) -> {

                    })
                    .setOnDismissListener(dialog -> {

                    })
                    .show();

            return result;
        }

        @Override
        public GeckoResult<PromptResponse> onBeforeUnloadPrompt(GeckoSession session, BeforeUnloadPrompt prompt) {
            final GeckoResult<PromptResponse> result = new GeckoResult<>();

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Leave Page")
                    .setMessage("Are you sure you want to leave this page? Changes you made may not be saved.")
                    .setPositiveButton("Leave", (dialog, which) -> {
                        result.complete(prompt.confirm(AllowOrDeny.ALLOW));
                    })
                    .setNegativeButton("Stay", (dialog, which) -> {
                        result.complete(prompt.confirm(AllowOrDeny.DENY));
                    })
                    .setOnDismissListener(dialog -> {
                        result.complete(prompt.confirm(AllowOrDeny.DENY));
                    })
                    .show();

            return result;
        }

        @Override
        public GeckoResult<PromptResponse> onAuthPrompt(GeckoSession session, AuthPrompt prompt) {
            GeckoResult<PromptResponse> result = new GeckoResult<>();

            LayoutInflater inflater = LayoutInflater.from(requireContext());
            View view = inflater.inflate(R.layout.dialog_auth, null);

            EditText username = view.findViewById(R.id.auth_username);
            EditText password = view.findViewById(R.id.auth_password);

            // Try to autofill with saved credentials
            String host = Uri.parse(currentUrl).getHost();
            if (host != null) {
                LoginCredential savedCredential = passwordManager.getCredentialForSite(host);
                if (savedCredential != null) {
                    username.setText(savedCredential.getUsername());
                    password.setText(savedCredential.getPassword());
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(prompt.title)
                    .setMessage(prompt.message)
                    .setView(view)
                    .setPositiveButton("OK", (dialog, which) -> {
                        String user = username.getText().toString();
                        String pass = password.getText().toString();

                        // Save this credential
                        if (host != null) {
                            passwordManager.saveCredential(host, user, pass);
                        }

                        result.complete(prompt.confirm(user, pass));
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        result.complete(prompt.dismiss());
                    })
                    .setOnDismissListener(dialog -> {
                        result.complete(prompt.dismiss());
                    })
                    .show();

            return result;
        }

        @Override
        public GeckoResult<PromptResponse> onButtonPrompt(GeckoSession session, ButtonPrompt prompt) {
            final GeckoResult<PromptResponse> result = new GeckoResult<>();

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(prompt.title)
                    .setMessage(prompt.message)
                    .setPositiveButton(ButtonPrompt.Type.POSITIVE, (dialog, which) -> {
                        result.complete(prompt.confirm(ButtonPrompt.Type.POSITIVE));
                    })
                    .setNegativeButton(ButtonPrompt.Type.NEGATIVE, (dialog, which) -> {
                        result.complete(prompt.confirm(ButtonPrompt.Type.NEGATIVE));
                    });

            builder.setOnDismissListener(dialog -> {
                result.complete(prompt.dismiss());
            }).show();

            return result;
        }

    }

    private class HistoryDelegate implements GeckoSession.HistoryDelegate {
        @Override
        public GeckoResult<Boolean> onVisited(GeckoSession session, String url, String lastVisitedURL, int flags) {
            // Flags GeckoHistoryVisited.RELOAD, REDIRECT_PERMANENT, REDIRECT_TEMPORARY, TOP_LEVEL
            // Bisa digunakan untuk mengontrol perilaku penyimpanan

            // Hanya simpan kunjungan top-level (bukan resource tambahan)
            if ((flags & GeckoSession.HistoryDelegate.VISIT_TOP_LEVEL) != 0) {
                // Filter URL yang tidak perlu disimpan
                if (!url.startsWith("about:") && !url.startsWith("data:") && !url.startsWith("blob:")) {
                    saveToHistory(url, pageTitle);
                }
            }

            // Mengembalikan hasil bahwa link harus ditandai sebagai dikunjungi
            return GeckoResult.fromValue(true);
        }
    }

    private void updateEditText(String query) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateEditText(query);
        }
    }

    private String getMimeTypeFromUrl(String url) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
            if (type != null) {
                return type;
            }
        }
        // Default MIME type jika tidak dapat ditentukan
        return "application/octet-stream";
    }

    private void downloadFile(String url, String mimeType) {
        DownloadManager downloadManager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        String fileName = getFileNameFromUrl(url);

        // Untuk Android 10 (API 29) ke atas, tidak perlu izin WRITE_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        } else {
            // Untuk versi sebelumnya, periksa izin
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                return;
            }
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        }

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setMimeType(mimeType);
        downloadManager.enqueue(request);
        Toast.makeText(requireContext(), "Download dimulai", Toast.LENGTH_SHORT).show();
    }

    private String getFileNameFromUrl(String url) {
        Uri uri = Uri.parse(url);
        String fileName = uri.getLastPathSegment();

        // Jika nama file masih null atau kosong, ambil dari bagian akhir URL
        if (fileName == null || fileName.isEmpty()) {
            String[] segments = url.split("/");
            if (segments.length > 0) {
                fileName = segments[segments.length - 1];
                // Hapus parameter query jika ada
                if (fileName.contains("?")) {
                    fileName = fileName.substring(0, fileName.indexOf("?"));
                }
            }
        }

        // Jika masih kosong, buat nama default
        if (fileName == null || fileName.isEmpty()) {
            fileName = "download_" + System.currentTimeMillis();
        }

        return fileName;
    }
}