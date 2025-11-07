package alv.splash.browser;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.Manifest;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.MotionEvent;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.Switch;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StartWorking extends AppCompatActivity {

    public static final int FOCUS_NONE = 0;
    public static final int FOCUS_WEBVIEW = 1;
    public static final int FOCUS_GECKOVIEW = 2;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int REQUEST_CODE_102 = 102;
    String KBEarn = "https://kolotibablo.com";
    ValueCallback<Uri[]> filePathCallback;
    String getUrl1 = "";
    String getUrl2 ="";
    String pageTitle1 ="";
    String pageTitle2 = "";
    String title_earning = "Money Earning";
    public String getTitleEarning() {
        return title_earning;
    }
    private static final int FILE_CHOOSER_REQUEST_CODE = 100;
    FloatingActionButton fabControl, fabEnter;
    ImageView pageSecure1, pageSecure2, refreshTab1, refreshTab2;
    TextView titleTab1;
    TextView titleTab2;
    ProgressBar pBarTab1, pBarTab2;
    LinearLayout layoutAddressBar1, layoutAddressBar2;
    TextInputLayout etLayout1, etLayout2, etLayoutTitleKB;
    TextInputEditText etSearch1, etSearch2, editTextTitleKB;
    ToggleButton btnPointerVisibility;
    Drawable bg_lavender_rounded;
    Button btnSaveTitle;
    WebView webViewTab1;
    private static GeckoRuntime sRuntime;
    GeckoView viewGecko;
    GeckoSession sessionGecko;
    boolean canGoBack = false;

    // Views for orientation change
    LinearLayout tabContainerStartWorking;
    RelativeLayout tab1, tab2;

    BottomSheetDialog bottomSheetDialog;

    private static final String KEY_POINTER_RIGHT_X = "pointerRightX";
    private static final String KEY_POINTER_RIGHT_Y = "pointerRightY";
    private static final String KEY_POINTER_PAUSE_R_X = "pointerPauseRX";
    private static final String KEY_POINTER_PAUSE_R_Y = "pointerPauseRY";
    private static final String KEY_POINTER_CLOSE_R_X = "pointerCloseRX";
    private static final String KEY_POINTER_CLOSE_R_Y = "pointerCloseRY";

    private static final String KEY_POINTER_LEFT_X = "pointerLeftX";
    private static final String KEY_POINTER_LEFT_Y = "pointerLeftY";
    private static final String KEY_POINTER_PAUSE_L_X = "pointerPauseLX";
    private static final String KEY_POINTER_PAUSE_L_Y = "pointerPauseLY";
    private static final String KEY_POINTER_CLOSE_L_X = "pointerCloseLX";
    private static final String KEY_POINTER_CLOSE_L_Y = "pointerCloseLY";


    float lastX_right, lastY_right, lastX_left, lastY_left,
            lastX_pauseR, lastY_pauseR, lastX_pauseL, lastY_pauseL,
            lastX_closeR, lastY_closeR, lastX_closeL, lastY_closeL;
    ImageView pointerRight, pointerLeft,
            pointerPauseR, pointerPauseL,
            pointerCloseR, pointerCloseL;
    TextView textPointerR, textPointerL;

    private boolean toggleClick = true;

    // Buat instance UrlValidator
    private UrlValidator urlValidator = new UrlValidator();
    private boolean isCalculationMode = false;
    CalculatorSetress calculatorSetress;

    private Handler handler = new Handler();
    private long startTime;
    private int totalKata = 0;
    private boolean isTesting = false;
    private boolean scrapingEnabled = false;
    public boolean isScrapingEnabled() {
        return scrapingEnabled;
    }
    LogView logView;
    private boolean consoleEnabled = false;
    private boolean isLogCopied = false;

    Switch switchBtnConsole, switchBtnScraping;
    ToggleButton btnPlayPauseWPM;
    TextView speedTestWPM_SW, speedTestWPM_BS;
    ImageView closeConsoleLog;

    private final OkHttpClient okClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    // URL server
    private final String SERVER_URL = "http://47.129.145.21:9890";
    private final String API_KEY = "019553d7-9890-0202-2025-7a24b83c935d"; // Kunci API untuk autentikasi
    private String lastBase64Hash = "";
    private String lastBase64HashLocal = "";
    private static final String LAST_BASE64_HASH_KEY = "last_base64_hash";
    private static final String LAST_BASE64_HASH_KEY_LOCAL = "last_base64_hash";


    private String basePath = ""; //Environment.getExternalStorageDirectory() + "/Datasets";
    private String imagesPath = "";// basePath + "/Images";
    private String csvPath = ""; // basePath + "/labels.csv";

    WebAppInterface webAppInterface;
    public boolean imageDataAvailable() {
        return !webAppInterface.ImgBase64.isEmpty() && !webAppInterface.ImgLabel.isEmpty();
    }
    public String isLabelExist() {
        return matchCaptchaImage(webAppInterface.ImgBase64);
    }
    public void saveCaptchaDataset() {
        saveBase64ImageToDb(webAppInterface.ImgBase64, webAppInterface.ImgLabel);
    }

    private CaptchaDataManager captchaDataManager;

    private boolean storagePermission = false;
    private SharedPreferences preferences;
    private static final String APP_PREFERENCES = "AppPreferences";
    private static final String STORAGE_PERMISSION_KEY = "storage_permission_granted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Menghilangkan judul aplikasi
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Membuat layar menjadi fullscreen
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                //WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_start_working);

        logView = findViewById(R.id.logView);
        // Inisialisasi SharedPreferences
        preferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);

        // Ambil nilai permission yang tersimpan
        storagePermission = preferences.getBoolean(STORAGE_PERMISSION_KEY, false);

        calculatorSetress = new CalculatorSetress(this);

        webViewTab1 = findViewById(R.id.webViewTab1);
        new AdBlockerWebView.init(this).initializeWebView(webViewTab1);

        // Inisialisasi dengan kedua constructor
        webAppInterface = new WebAppInterface(this);
        captchaDataManager = new CaptchaDataManager(this);

        fabControl = findViewById(R.id.fab_control);
        fabEnter = findViewById(R.id.fabEnter);
        // FAB click listener
        fabControl.setOnClickListener(v -> showBottomSheet());
        fabEnter.setOnClickListener(v -> sendKeyEvent(KeyEvent.KEYCODE_ENTER, false));
        closeConsoleLog = findViewById(R.id.closeConsoleLog);


        TextView copyLog = findViewById(R.id.copyLog);
        copyLog.setOnClickListener(v -> {
            // TextView consoleLogText = findViewById(R.id.consoleLogText);
            if (!isLogCopied){
                if (logView != null && logView.getLogContent() != null) {
                    if (!logView.getLogContent().isEmpty()) {
                        copyToClipboard(logView.getLogContent());
                        isLogCopied = true;
                        copyLog.setText("Copied");
                        new Handler().postDelayed(() -> {
                            copyLog.setText(" Copy ");
                            isLogCopied = false;
                        }, 1000);
                    }
                }
            }
        });

        TextView clearLog = findViewById(R.id.clearLog);
        clearLog.setOnClickListener(v -> {
			/*TextView consoleLog = findViewById(R.id.consoleLogText);
            consoleLog.setText("");*/
            logView.clear();
        });


        // Initialize views for orientation change
        tabContainerStartWorking = findViewById(R.id.tabContainerStartWorking);
        tab1 = findViewById(R.id.tab1);
        tab2 = findViewById(R.id.tab2);

        // Set initial layout based on current orientation
        updateLayoutForOrientation(getResources().getConfiguration().orientation);
        applyWindowModeForOrientation(getResources().getConfiguration().orientation);

        TextView injectScript = findViewById(R.id.injectScript);
        injectScript.setOnClickListener(v -> {
            updateConsoleLog("Start Injected script: " + webAppInterface.scriptInjectData.length());
            updateConsoleLog("String JS: " + webAppInterface.makeSingleLine(webAppInterface.scriptInjectData));
            webViewTab1.evaluateJavascript(webAppInterface.scriptInjectData, null);
            Log.i("Inject_WebView", "Start Injected script");
        });
        TextView saveData = findViewById(R.id.saveData);
        saveData.setOnClickListener(v -> {
            if (logView != null) logView.saveToFile();
            /*
            new Thread(() -> {
                try {
                    if (getUrl1.contains("kolotibablo.com") && pageTitle1.contains(title_earning)) {
                        updateConsoleLog("Page title: " + pageTitle1);

                        // Validasi data
                        if (webAppInterface.ImgBase64 != null && !webAppInterface.ImgBase64.isEmpty() &&
                                webAppInterface.ImgLabel != null && !webAppInterface.ImgLabel.isEmpty()) {

                            updateConsoleLog("Data ditemukan: Image (" + webAppInterface.ImgBase64.length() + " chars) dan Label: " + webAppInterface.ImgLabel);
                            checkLastHashFromServer();
                            // Buat JSON untuk dikirim ke server
                            JSONObject jsonData = new JSONObject();
                            jsonData.put("image_base64", webAppInterface.ImgBase64);
                            jsonData.put("image_label", webAppInterface.ImgLabel);
                            jsonData.put("api_key", API_KEY);

                            // Mengirim data ke server
                            sendDataToServer(jsonData);

                        } else {
                            updateConsoleLog("Error: Tidak ada data untuk diupload");
                            runOnUiThread(() -> {
                                Toast.makeText(this, "No data to save", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
						runOnUiThread(() -> {
                                Toast.makeText(this, "Not available", Toast.LENGTH_SHORT).show();
                            });
					}
                    Thread.sleep(300);
                } catch (Exception e) {
                    updateConsoleLog("Error: " + e.getMessage());
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    Thread.currentThread().interrupt();
                }
            }).start();
            */
        });

        pBarTab1 = findViewById(R.id.pBarTab1);
        pBarTab2 = findViewById(R.id.pBarTab2);

        bg_lavender_rounded = getResources().getDrawable(R.drawable.edittext);

        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.sw_bottomsheet_control);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.setCanceledOnTouchOutside(true);

        titleTab1 = bottomSheetDialog.findViewById(R.id.titleTab1);
        titleTab2 = bottomSheetDialog.findViewById(R.id.titleTab2);
        pageSecure1 = bottomSheetDialog.findViewById(R.id.pageSecure1);
        pageSecure2 = bottomSheetDialog.findViewById(R.id.pageSecure2);
        refreshTab1 = bottomSheetDialog.findViewById(R.id.refreshTab1);
        refreshTab2 = bottomSheetDialog.findViewById(R.id.refreshTab2);
        etLayout1 = bottomSheetDialog.findViewById(R.id.etLayout1);
        etLayout2 = bottomSheetDialog.findViewById(R.id.etLayout2);
        etSearch1 = bottomSheetDialog.findViewById(R.id.et_search1);
        etSearch2 = bottomSheetDialog.findViewById(R.id.et_search2);

        layoutAddressBar1 = bottomSheetDialog.findViewById(R.id.layoutAddressBar1);
        layoutAddressBar2 = bottomSheetDialog.findViewById(R.id.layoutAddressBar2);

        etLayoutTitleKB = bottomSheetDialog.findViewById(R.id.etLayoutTitleKB);
        editTextTitleKB = bottomSheetDialog.findViewById(R.id.editTextTitleKB);
        btnSaveTitle = bottomSheetDialog.findViewById(R.id.btnSaveTitle);

        editTextTitleKB.setText(title_earning);

        loadTitleKB(title_earning);

        //View layoutSpeedText = LayoutInflater.from(this).inflate(R.layout.speed_text, null);

        speedTestWPM_SW = findViewById(R.id.speedTestWPM);
        speedTestWPM_BS = bottomSheetDialog.findViewById(R.id.speedTestWPM);
        btnPlayPauseWPM = bottomSheetDialog.findViewById(R.id.btnPlayPauseWPM);
        switchBtnConsole = bottomSheetDialog.findViewById(R.id.switchBtnConsole);
        switchBtnScraping = bottomSheetDialog.findViewById(R.id.switchBtnScraping);

        closeConsoleLog.setOnClickListener(v -> {
            RelativeLayout consoleContainer = findViewById(R.id.consoleContainer);
            logView.clear();
            logView.disableLogging();
            consoleContainer.setVisibility(View.GONE);
            switchBtnConsole.setChecked(false);
            consoleEnabled = false;
        });

        btnPlayPauseWPM.setOnCheckedChangeListener((buttonView, isChecked) -> {

            LinearLayout speedTestContainer = findViewById(R.id.speedTestContainer);
            Space space4TextBtn = findViewById(R.id.space4TextBtn);

            if (isChecked) {
                speedTestContainer.setVisibility(View.VISIBLE);
                space4TextBtn.setVisibility(View.VISIBLE);
                // Mulai test
                startTime = System.currentTimeMillis();
                totalKata = 0;
                isTesting = true;
                handler.post(updateWPMLoop); // Mulai perhitungan WPM
            } else {
                speedTestContainer.setVisibility(View.GONE);
                space4TextBtn.setVisibility(View.GONE);
                // Hentikan test
                isTesting = false;
                handler.removeCallbacks(updateWPMLoop);
            }
        });

        switchBtnConsole.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RelativeLayout consoleContainer = findViewById(R.id.consoleContainer);
            consoleContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (isChecked) {
                logView.enableLogging();
            } else {
                logView.disableLogging();
            }
            consoleEnabled = isChecked;
        });

        switchBtnScraping.setOnCheckedChangeListener((buttonView, isChecked) -> {
            scrapingEnabled = isChecked;
        });

        btnSaveTitle.setOnClickListener(v -> {
            title_earning = editTextTitleKB.getText().toString().trim();
            bottomSheetDialog.dismiss();
            Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show();
            saveEditTextTitleKB(title_earning);
        });


        //BottomSheetDialog
        bottomSheetDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                View bottomSheetInternal = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheetInternal != null) {
                    BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheetInternal);
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Munculkan dalam mode penuh
                }
            }
        });

        // Listener untuk mendeteksi saat dialog ditutup
        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // Ubah visibilitas saat dialog ditutup
                etLayout1.setVisibility(View.GONE);
                etLayout2.setVisibility(View.GONE);
                layoutAddressBar1.setBackground(bg_lavender_rounded);
                layoutAddressBar2.setBackground(bg_lavender_rounded);

                pageSecure1.setVisibility(View.VISIBLE);
                titleTab1.setVisibility(View.VISIBLE);
                refreshTab1.setVisibility(View.VISIBLE);

                pageSecure2.setVisibility(View.VISIBLE);
                titleTab2.setVisibility(View.VISIBLE);
                refreshTab2.setVisibility(View.VISIBLE);

            }
        });

        titleTab1.setOnClickListener(v -> switchViewSearch1());
        titleTab2.setOnClickListener(v -> switchViewSearch2());

        // Event saat tombol enter ditekan
        etSearch1.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                            event.getAction() == KeyEvent.ACTION_DOWN)) {

                String userInput = etSearch1.getText().toString();
                String processedUrl = urlValidator.processInput(userInput);

                // Memuat URL yang sudah diproses ke WebView
                webViewTab1.loadUrl(processedUrl);

                // Sembunyikan keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etSearch1.getWindowToken(), 0);
                bottomSheetDialog.dismiss();
                return true;
            }
            return false;
        });
        // Event saat tombol enter ditekan
        etSearch2.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null &&
                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                    event.getAction() == KeyEvent.ACTION_DOWN)) {
                String userInput = etSearch2.getText().toString();
                String processedUrl = urlValidator.processInput(userInput);

                // Memuat URL yang sudah diproses ke WebView
                sessionGecko.loadUri(processedUrl);

                // Sembunyikan keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etSearch2.getWindowToken(), 0);
                bottomSheetDialog.dismiss();
                Log.d("SearchDebug", "ActionId: " + actionId);
                if (event != null) {
                    Log.d("SearchDebug", "KeyCode: " + event.getKeyCode());
                    Log.d("SearchDebug", "Action: " + event.getAction());
                }
                return true;
            }
            return false;
        });
        // Event saat kehilangan fokus
        etSearch1.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                etLayout1.setVisibility(View.GONE);
                layoutAddressBar1.setBackground(bg_lavender_rounded);

                pageSecure1.setVisibility(View.VISIBLE);
                titleTab1.setVisibility(View.VISIBLE);
                refreshTab1.setVisibility(View.VISIBLE);
            }
        });

        // Event saat kehilangan fokus
        etSearch2.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                etLayout2.setVisibility(View.GONE);
                layoutAddressBar2.setBackground(bg_lavender_rounded);

                pageSecure2.setVisibility(View.VISIBLE);
                titleTab2.setVisibility(View.VISIBLE);
                refreshTab2.setVisibility(View.VISIBLE);
            }
        });

        if (pageTitle1.length() > 6) {
            // Judul memiliki lebih dari 10 karakter, atur animasi marquee
            titleTab1.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            titleTab1.setMarqueeRepeatLimit(-1);
            titleTab1.setSelected(true);
        } else {
            // Judul memiliki 10 karakter atau kurang, nonaktifkan animasi marquee
            titleTab1.setEllipsize(TextUtils.TruncateAt.END);
            titleTab1.setSelected(false);
        }

        if (pageTitle2.length() > 6) {
            // Judul memiliki lebih dari 10 karakter, atur animasi marquee
            titleTab2.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            titleTab2.setMarqueeRepeatLimit(-1);
            titleTab2.setSelected(true);
        } else {
            // Judul memiliki 10 karakter atau kurang, nonaktifkan animasi marquee
            titleTab2.setEllipsize(TextUtils.TruncateAt.END);
            titleTab2.setSelected(false);
        }

        refreshTab1.setOnClickListener(v -> webViewTab1.reload());
        refreshTab2.setOnClickListener(v -> sessionGecko.reload());
        //End BottomsheetDialog





        viewGecko = findViewById(R.id.webTab2);
        sessionGecko = new GeckoSession();

        // Workaround for Bug 1758212
        sessionGecko.setContentDelegate(new GeckoSession.ContentDelegate() {});
        setupGeckoSession();

        if (sRuntime == null) {
            // GeckoRuntime can only be initialized once per process
            sRuntime = GeckoRuntime.create(this);
        }

        sessionGecko.open(sRuntime);
        viewGecko.setSession(sessionGecko);


        pointerRight = findViewById(R.id.pointerRight);
        pointerLeft = findViewById(R.id.pointerLeft);
        pointerPauseR = findViewById(R.id.pointerPauseR);
        pointerPauseL = findViewById(R.id.pointerPauseL);
        pointerCloseR = findViewById(R.id.pointerCloseR);
        pointerCloseL = findViewById(R.id.pointerCloseL);

        textPointerR = findViewById(R.id.textPointerR);
        textPointerL = findViewById(R.id.textPointerL);

        btnPointerVisibility = bottomSheetDialog.findViewById(R.id.btnPointerVisibilty);
        btnPointerVisibility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Jika isChecked adalah true, maka set cursor menjadi VISIBLE
                if (isChecked) {
                    pointerRight.setVisibility(View.VISIBLE);
                    pointerLeft.setVisibility(View.VISIBLE);

                    pointerPauseR.setVisibility(View.VISIBLE);
                    pointerPauseL.setVisibility(View.VISIBLE);

                    pointerCloseR.setVisibility(View.VISIBLE);
                    pointerCloseL.setVisibility(View.VISIBLE);
                } else {
                    // Jika isChecked adalah false, maka set cursor menjadi INVISIBLE
                    pointerRight.setVisibility(View.INVISIBLE);
                    pointerLeft.setVisibility(View.INVISIBLE);

                    pointerPauseR.setVisibility(View.INVISIBLE);
                    pointerPauseL.setVisibility(View.INVISIBLE);

                    pointerCloseR.setVisibility(View.INVISIBLE);
                    pointerCloseL.setVisibility(View.INVISIBLE);
                }
            }
        });

        setupWebViewSettings();
        setupWebViewClient();
        setupWebChromeClient();
        webViewTab1.loadUrl(KBEarn);

        sessionGecko.loadUri(KBEarn); // URL...

        View rootViewSW = findViewById(android.R.id.content);
        rootViewSW.post(() -> {
            setupPointerMovement();

            //Pointer right tab
            float[] pointerRightPos = loadPointerPosition(KEY_POINTER_RIGHT_X, KEY_POINTER_RIGHT_Y);
            pointerRight.setX(Math.max(0, Math.min(pointerRightPos[0], viewGecko.getWidth() - pointerRight.getWidth())));
            pointerRight.setY(Math.max(0, Math.min(pointerRightPos[1], viewGecko.getHeight() - pointerRight.getHeight())));

            float[] pointerPauseRPos = loadPointerPosition(KEY_POINTER_PAUSE_R_X, KEY_POINTER_PAUSE_R_Y);
            pointerPauseR.setX(Math.max(0, Math.min(pointerPauseRPos[0], viewGecko.getWidth() - pointerPauseR.getWidth())));
            pointerPauseR.setY(Math.max(0, Math.min(pointerPauseRPos[1], viewGecko.getHeight() - pointerPauseR.getHeight())));

            float[] pointerCloseRPos = loadPointerPosition(KEY_POINTER_CLOSE_R_X, KEY_POINTER_CLOSE_R_Y);
            pointerCloseR.setX(Math.max(0, Math.min(pointerCloseRPos[0], viewGecko.getWidth() - pointerCloseR.getWidth())));
            pointerCloseR.setY(Math.max(0, Math.min(pointerCloseRPos[1], viewGecko.getHeight() - pointerCloseR.getHeight())));

            //Pointer left tab
            float[] pointerLeftPos = loadPointerPosition(KEY_POINTER_LEFT_X, KEY_POINTER_LEFT_Y);
            pointerLeft.setX(Math.max(0, Math.min(pointerLeftPos[0], webViewTab1.getWidth() - pointerLeft.getWidth())));
            pointerLeft.setY(Math.max(0, Math.min(pointerLeftPos[1], webViewTab1.getHeight() - pointerLeft.getHeight())));

            float[] pointerPauseLPos = loadPointerPosition(KEY_POINTER_PAUSE_L_X, KEY_POINTER_PAUSE_L_Y);
            pointerPauseL.setX(Math.max(0, Math.min(pointerPauseLPos[0], webViewTab1.getWidth() - pointerPauseL.getWidth())));
            pointerPauseL.setY(Math.max(0, Math.min(pointerPauseLPos[1], webViewTab1.getHeight() - pointerPauseL.getHeight())));

            float[] pointerCloseLPos = loadPointerPosition(KEY_POINTER_CLOSE_L_X, KEY_POINTER_CLOSE_L_Y);
            pointerCloseL.setX(Math.max(0, Math.min(pointerCloseLPos[0], webViewTab1.getWidth() - pointerCloseL.getWidth())));
            pointerCloseL.setY(Math.max(0, Math.min(pointerCloseLPos[1], webViewTab1.getHeight() - pointerCloseL.getHeight())));

            setupStorage();
            checkLastHashFromServer();

        });

		/*
        webViewTab1.setOnFocusChangeListener((v, hasFocus) -> {
			if (hasFocus) {
				 updateConsoleLog("A WebView hasFocus = " + hasFocus);
			} else {
				 updateConsoleLog("B WebView !hasFocus = " + hasFocus);
			}
        });
        viewGecko.setOnFocusChangeListener((v, hasFocus) -> {
		   if (hasFocus) {
				 updateConsoleLog("A GeckoView hasFocus = " + hasFocus);
			} else {
				 updateConsoleLog("B GeckoView !hasFocus = " + hasFocus);
			}
        }); */

    }// akhir onCreate

    private void setFullscreen(boolean enable) {
        Window window = getWindow();
        View decor = window.getDecorView();
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, decor);

        if (enable) {
            // Konten menggambar ke belakang system bars
            WindowCompat.setDecorFitsSystemWindows(window, false);
            // Sembunyikan status bar (kalau mau sekalian nav bar, tambahkan navigationBars())
            controller.hide(WindowInsetsCompat.Type.statusBars());
            // Geser untuk munculkan sementara
            controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        } else {
            // Tampilkan status bar lagi & tata letak tidak menggambar di baliknya
            controller.show(WindowInsetsCompat.Type.statusBars());
            WindowCompat.setDecorFitsSystemWindows(window, true);
        }
    }

    private void applyWindowModeForOrientation(int orientation) {
        boolean fullscreen = (orientation == Configuration.ORIENTATION_LANDSCAPE);
        setFullscreen(fullscreen);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Update layout based on new orientation
        updateLayoutForOrientation(newConfig.orientation);
        // Update mode layar (full-screen / biasa)
        applyWindowModeForOrientation(newConfig.orientation);
    }

    private void updateLayoutForOrientation(int orientation) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Set layout for Portrait mode
            tabContainerStartWorking.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1.0f
            );
            tab1.setLayoutParams(params1);

            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1.0f
            );
            tab2.setLayoutParams(params2);

        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Set layout for Landscape mode
            tabContainerStartWorking.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1.0f
            );
            tab1.setLayoutParams(params1);

            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1.0f
            );
            tab2.setLayoutParams(params2);
        }
    }


    public void actionWhoIsFocused() {
        int focusState = whoIsFocus();

        switch (focusState) {
            case FOCUS_WEBVIEW:
                Log.d("FocusCheck", "WebView sedang fokus");
                if (pageTitle1.contains(title_earning)&&pageTitle2.contains(title_earning)){
                    if (toggleClick) {
                        // AutoClick untuk pointerRight
                        //webViewTab1.clearFocus();
                        viewGecko.requestFocus(); // Root view batas pointerRight
                        simulateTouch(viewGecko, lastX_right, lastY_right);
                        toggleClick = false;
                    } else {
                        // AutoClick untuk pointerLeft
                        //viewGecko.clearFocus();
                        webViewTab1.requestFocus(); // Root view batas pointerLeft
                        simulateDoubleClick(webViewTab1, lastX_left, lastY_left);
                        toggleClick = true;
                    }
                }
                break;

            case FOCUS_GECKOVIEW:
                // Kode yang dijalankan jika GeckoView mendapatkan fokus
                Log.d("FocusCheck", "GeckoView sedang fokus");
                break;

            case FOCUS_NONE:
                // Kode yang dijalankan jika tidak ada yang mendapatkan fokus
                Log.d("FocusCheck", "Tidak ada view yang fokus");
                break;
        }
    }

    public int whoIsFocus() {
        // Cek apakah webViewTab1 mendapatkan fokus
        if (webViewTab1 != null && webViewTab1.isFocused()) {
            return FOCUS_WEBVIEW;
        }

        // Cek apakah viewGecko mendapatkan fokus
        if (viewGecko != null && viewGecko.isFocused()) {
            return FOCUS_GECKOVIEW;
        }

        // Jika tidak ada yang mendapatkan fokus
        return FOCUS_NONE;
    }

    // Method untuk menghitung hash SHA-256
    private String calculateSHA256(String data) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            return bytesToHex(hash);
        } catch (Exception e) {
            return "";
        }
    }

    // Helper method untuk mengonversi byte array ke string hex
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void sendDataToServer(JSONObject jsonData) {
        try {
            String base64Data = jsonData.getString("image_base64");
            String currentHash = calculateSHA256(base64Data);

            // Cek apakah hash sudah ada
            if (currentHash.equals(lastBase64Hash)) {
                updateConsoleLog("Data sudah ada, tidak perlu mengirim ulang");
                return;
            }

            // Simpan hash baru ke SharedPreferences
            SharedPreferences.Editor editor = preferences.edit();

            lastBase64Hash = currentHash;
            editor.putString(LAST_BASE64_HASH_KEY, lastBase64Hash);
            editor.apply();

            // Buat request body
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonData.toString(), JSON);

            // Buat request
            Request request = new Request.Builder()
                    .url(SERVER_URL + "/upload")
                    .post(body)
                    .build();

            updateConsoleLog("Mengirim data ke server...");

            // Kirim request secara asynchronous
            okClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    updateConsoleLog("Gagal terhubung ke server: " + e.getMessage());
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "Server tidak dapat dijangkau", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            String message = jsonResponse.getString("message");
                            String filename = jsonResponse.optString("filename", "");

                            updateConsoleLog("Server: " + message + (filename.isEmpty() ? "" : ", file: " + filename));
                            runOnUiThread(() -> {
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            });
                        } catch (Exception e) {
                            updateConsoleLog("Error parsing response: " + e.getMessage());
                        }
                    } else {
                        updateConsoleLog("Server error: " + response.code() + " - " + responseBody);
                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        } catch (Exception e) {
            updateConsoleLog("Error saat mengirim data: " + e.getMessage());
        }
    }

    private void checkLastHashFromServer() {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(SERVER_URL + "/last-hash")
                        .build();

                Response response = okClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    String lastHash = response.body().string();
                    runOnUiThread(() -> {
                        lastBase64Hash = lastHash;
                        preferences.edit().putString(LAST_BASE64_HASH_KEY, lastBase64Hash).apply();
                        updateConsoleLog("Updated Last Base64 Hash from Server:\n" + lastBase64Hash);
                    });
                }
            } catch (Exception e) {
                // Jika offline, gunakan hash dari SharedPreferences
                lastBase64Hash = preferences.getString(LAST_BASE64_HASH_KEY, lastBase64Hash);
                lastBase64HashLocal = preferences.getString(LAST_BASE64_HASH_KEY_LOCAL, lastBase64HashLocal);
                updateConsoleLog(e.getMessage());
                updateConsoleLog(" LOCAL: Using Last Base64 Hash from SharedPreferences: 燥\n" + lastBase64Hash);
            }
        }).start();
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
                updateConsoleLog("[!] Error: Tidak dapat mendapatkan direktori eksternal");
            }
        } else {
            // Android 9 dan di bawahnya
            basePath = Environment.getExternalStorageDirectory() + "/Datasets";
            Log.d("StorageDebug", "Path untuk Android < 10: " + basePath);
        }

        imagesPath = basePath + "/Images";
        csvPath = basePath + "/labels.csv";

        // Log info path
        Log.d("StorageDebug", "imagesPath: " + imagesPath);
        Log.d("StorageDebug", "csvPath: " + csvPath);

        // Buat direktori jika belum ada
        createDirectories();

        // Minta izin penyimpanan saat aplikasi dibuka
        requestStoragePermissions();
    }

    private void createDirectories() {
        try {
            File baseDir = new File(basePath);
            if (!baseDir.exists()) {
                boolean baseDirCreated = baseDir.mkdirs();
                Log.d("StorageDebug", "Membuat basePath: " + basePath + " - Hasil: " + baseDirCreated);
                updateConsoleLog("[ Mencoba membuat direktori base: " + (baseDirCreated ? "berhasil" : "gagal") + " ]");
            }

            File imagesDir = new File(imagesPath);
            if (!imagesDir.exists()) {
                boolean imagesDirCreated = imagesDir.mkdirs();
                Log.d("StorageDebug", "Membuat imagesPath: " + imagesPath + " - Hasil: " + imagesDirCreated);
                updateConsoleLog("[ Mencoba membuat direktori images: " + (imagesDirCreated ? "berhasil" : "gagal") + " ]");
            }
        } catch (Exception e) {
            Log.e("StorageDebug", "Error membuat direktori: " + e.getMessage(), e);
            updateConsoleLog("[!] Error membuat direktori: " + e.getMessage());
        }
    }

    private void requestStoragePermissions() {
        Log.d("StorageDebug", "requestStoragePermissions(), SDK: " + Build.VERSION.SDK_INT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+)
            boolean hasAllFilesPermission = Environment.isExternalStorageManager();
            Log.d("StorageDebug", "Android 11+, hasAllFilesPermission: " + hasAllFilesPermission);

            if (!hasAllFilesPermission) {
                updateConsoleLog("[ Meminta izin MANAGE_EXTERNAL_STORAGE untuk Android 11+ ]");
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, REQUEST_CODE_102);
                } catch (Exception e) {
                    Log.e("StorageDebug", "Error meminta MANAGE_EXTERNAL_STORAGE: " + e.getMessage(), e);
                    updateConsoleLog("[!] Error meminta izin penyimpanan: " + e.getMessage());

                    // Fallback jika cara spesifik gagal
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, REQUEST_CODE_102);
                }
            } else {
                Log.d("StorageDebug", "MANAGE_EXTERNAL_STORAGE sudah diberikan");
                updateConsoleLog("[ Izin MANAGE_EXTERNAL_STORAGE sudah diberikan ]");
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
                updateConsoleLog("[ Meminta izin WRITE_EXTERNAL_STORAGE ]");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            } else {
                Log.d("StorageDebug", "WRITE_EXTERNAL_STORAGE sudah diberikan");
                updateConsoleLog("[ Izin WRITE_EXTERNAL_STORAGE sudah diberikan ]");
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
            updateConsoleLog("[ Izin penyimpanan otomatis diberikan untuk Android 5.1- ]");
            storagePermission = true;
        }
    }
    /*
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            }
        }
    }*/

    /**
     * Migrate existing data from files to SQLite
     */
    private void migrateFilesToDatabase() {
        Log.d("Migration", "Starting migration...");
        updateConsoleLog("[ Starting migration to SQLite... ]");

        new Thread(() -> {
            try {
                if (captchaDataManager == null) {
                    captchaDataManager = new CaptchaDataManager(this);
                }

                String imagesPath = getExternalFilesDir("Datasets").getPath() + "/images";
                String csvPath = getExternalFilesDir("Datasets").getPath() + "/labels.csv";

                // captchaDataManager.importFromFileSystem(imagesPath, csvPath);

                runOnUiThread(() -> {
                    updateConsoleLog("[ Migration completed successfully ]");
                });
            } catch (Exception e) {
                Log.e("Migration", "Error during migration: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    updateConsoleLog("[!] Migration error: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Match a CAPTCHA image with database and return label if found
     */
    public String matchCaptchaImage(String base64Data) {
        Log.d("CaptchaMatch", "matchCaptchaImage() dipanggil");

        if (base64Data == null || base64Data.isEmpty()) {
            Log.e("CaptchaMatch", "Data Base64 kosong");
            return null;
        }

        try {
            if (captchaDataManager == null) {
                captchaDataManager = new CaptchaDataManager(this);
            }

            // Dapatkan label yang cocok
            String label = captchaDataManager.getLabelForImage(base64Data);

            if (label != null) {
                Log.d("CaptchaMatch", "Ditemukan CAPTCHA yang cocok: " + label);
                updateConsoleLog("[ Ditemukan kecocokan: " + label + " ]");
                return label;
            } else {
                Log.d("CaptchaMatch", "Tidak ditemukan kecocokan");
                updateConsoleLog("[ Tidak ditemukan kecocokan ]");
                return null;
            }
        } catch (Exception e) {
            Log.e("CaptchaMatch", "Error mencocokkan: " + e.getMessage(), e);
            updateConsoleLog("[!] Error mencocokkan: " + e.getMessage());
            return null;
        }
    }

    private void koloSetTextInputCaptcha (String captchaLabel) {
        String script = "setTextInput(" + JSONObject.quote(captchaLabel) + ")";

        webViewTab1.evaluateJavascript(script, null);
    }
    /**
     * Show CAPTCHA suggestion to user
     */
    private void showCaptchaSuggestion(String suggestedLabel) {
        runOnUiThread(() -> {
            // Create popup with suggestion
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("CAPTCHA Suggestion")
                    .setMessage("Suggested text: " + suggestedLabel)
                    .setPositiveButton("Use", (dialog, id) -> {
                        // Here you would auto-fill the CAPTCHA input field
                        //webAppInterface.fillCaptchaField(suggestedLabel);
                        koloSetTextInputCaptcha(suggestedLabel);
                        //Toast.makeText(getApplicationContext(), "Suggested text: " + suggestedLabel, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", (dialog, id) -> {
                        dialog.dismiss();
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    public void saveBase64ImageToDb(String base64Data, String label) {
        Log.d("StorageDebug", "saveBase64ImageToDb() dipanggil");

        if (!storagePermission) {
            Log.e("StorageDebug", "Izin penyimpanan tidak diberikan");
            updateConsoleLog("[!] Error: Izin penyimpanan tidak diberikan");
            requestStoragePermissions();
            return;
        }

        try {
            if (base64Data == null || base64Data.isEmpty()) {
                Log.e("StorageDebug", "Data Base64 kosong");
                updateConsoleLog("[!] Error: Data Base64 kosong");
                return;
            }

            // Info debug
            updateConsoleLog("Base64 length: 燥\n" + base64Data.length() + " startWith: " +
                    base64Data.substring(0, Math.min(21, base64Data.length())));

            // Inisialisasi manager jika diperlukan
            if (captchaDataManager == null) {
                captchaDataManager = new CaptchaDataManager(this);
            }

            // Simpan ke SQLite dan sistem file
            boolean saved = captchaDataManager.saveCaptchaData(base64Data, label);

            if (saved) {
                updateConsoleLog("[ Captcha berhasil disimpan ke database ]");
            } else {
                updateConsoleLog("[!] Captcha sudah ada di database atau terjadi error");
            }
        } catch (Exception e) {
            Log.e("StorageDebug", "Error tidak terduga: " + e.getMessage(), e);
            updateConsoleLog("[!] Error tidak terduga: " + e.getMessage());
        }
    }

    private void saveBase64Image(String base64Data, String label) {
        Log.d("StorageDebug", "saveBase64Image() dipanggil dengan label: " + label);

        if (!storagePermission) {
            Log.e("StorageDebug", "Izin penyimpanan belum diberikan");
            updateConsoleLog("[!] Error: Izin penyimpanan belum diberikan");
            requestStoragePermissions();
            return;
        }

        try {
            // Cek apakah base64Data valid
            if (base64Data == null || base64Data.isEmpty()) {
                Log.e("StorageDebug", "Data base64 kosong");
                updateConsoleLog("[!] Error: Data base64 kosong");
                return;
            }



            // Debug info
            updateConsoleLog("Panjang base64: 燥\n" + base64Data.length() + " startWith: " + base64Data.substring(0, Math.min(20, base64Data.length())));

            // Pisahkan MIME type dan data base64
            String[] parts;
            try {
                parts = base64Data.split(",");
                if (parts.length < 2) {
                    Log.e("StorageDebug", "Format base64 tidak valid, tidak dapat split dengan ',': " +
                            base64Data.substring(0, Math.min(50, base64Data.length())));
                    updateConsoleLog("[!] Error: Format base64 tidak valid (tidak ada pemisah)");
                    return;
                }
            } catch (Exception e) {
                Log.e("StorageDebug", "Error saat memisahkan base64: " + e.getMessage(), e);
                updateConsoleLog("[!] Error memproses base64: " + e.getMessage());
                return;
            }

            String mimeTypeHeader = parts[0]; // data:image/png;base64
            String base64EncodedData = parts[1];

            // Ekstrak ekstensi file dari MIME type
            String extension = "png"; // Default
            try {
                if (mimeTypeHeader.contains("image/")) {
                    extension = mimeTypeHeader.split("image/")[1].split(";")[0];
                    Log.d("StorageDebug", "Ekstensi terdeteksi: " + extension);
                } else {
                    Log.w("StorageDebug", "Tidak dapat mendeteksi MIME type, menggunakan default: " + extension);
                    updateConsoleLog("[ Peringatan: Format gambar tidak terdeteksi, menggunakan JPEG ]");
                }
            } catch (Exception e) {
                Log.e("StorageDebug", "Error saat ekstrak MIME type: " + e.getMessage(), e);
                updateConsoleLog("[!] Error ekstrak format gambar: " + e.getMessage());
                // Tetap gunakan default
            }

            // Generate nama file dengan UUID dan ekstensi
            String fileName = UUID.randomUUID().toString() + "." + extension;
            Log.d("StorageDebug", "Nama file: " + fileName);

            // Decode data base64
            byte[] decodedBytes;
            try {
                decodedBytes = android.util.Base64.decode(
                        base64EncodedData, android.util.Base64.DEFAULT
                );
                Log.d("StorageDebug", "Panjang bytes setelah decode: " + decodedBytes.length);
            } catch (Exception e) {
                Log.e("StorageDebug", "Error saat decode base64: " + e.getMessage(), e);
                updateConsoleLog("[!] Error decode base64: " + e.getMessage());
                return;
            }

            // Buat direktori jika belum ada
            File dir = new File(imagesPath);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                Log.d("StorageDebug", "Membuat direktori images: " + created);
                if (!created) {
                    Log.e("StorageDebug", "Gagal membuat direktori: " + imagesPath);
                    updateConsoleLog("[!] Error: Gagal membuat direktori " + imagesPath);
                    return;
                }
            }

            // Cek apakah direktori dapat ditulis
            if (!dir.canWrite()) {
                Log.e("StorageDebug", "Direktori tidak dapat ditulis: " + imagesPath);
                updateConsoleLog("[!] Error: Tidak dapat menulis ke direktori " + imagesPath);
                return;
            }

            File file = new File(dir, fileName);
            String relativePath = "Images/" + fileName;
            Log.d("StorageDebug", "Path file lengkap: " + file.getAbsolutePath());

            // Simpan gambar berdasarkan versi Android
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Untuk API 26 ke atas
                    java.nio.file.Files.write(file.toPath(), decodedBytes);
                    Log.d("StorageDebug", "File ditulis dengan Files.write (API 26+)");
                } else {
                    // Untuk API di bawah 26
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(decodedBytes);
                        Log.d("StorageDebug", "File ditulis dengan FileOutputStream (API <26)");
                    }
                }

                // Verifikasi file tertulis
                if (file.exists() && file.length() > 0) {
                    Log.d("StorageDebug", "File berhasil dibuat: " + file.getAbsolutePath() +
                            " dengan ukuran " + file.length() + " bytes");
                } else {
                    Log.e("StorageDebug", "File tidak tertulis dengan benar: " +
                            (file.exists() ? "Ada tapi ukuran 0" : "Tidak ada"));
                    updateConsoleLog("[!] Error: File tidak tersimpan dengan benar");
                    return;
                }

                // Simpan info ke CSV
                saveToCsv(relativePath, label);

                runOnUiThread(() -> {
                    String newHashLocal = calculateSHA256(base64Data);
                    lastBase64HashLocal = newHashLocal;
                    preferences.edit().putString(LAST_BASE64_HASH_KEY_LOCAL, lastBase64HashLocal).apply();
                    updateConsoleLog(" Last Base64 Hash Local: " + lastBase64HashLocal);
                    updateConsoleLog("[ Captcha berhasil disimpan: " + fileName + " ]");
                });
            } catch (Exception e) {
                Log.e("StorageDebug", "Error menyimpan file: " + e.getMessage(), e);
                updateConsoleLog("[!] Error menyimpan gambar: " + file.getAbsolutePath() +
                        " - " + e.getMessage());
                return;
            }
        } catch (Exception e) {
            Log.e("StorageDebug", "Error tidak terduga: " + e.getMessage(), e);
            updateConsoleLog("[!] Error tidak terduga: " + e.getMessage());
        }
    }

    // Metode untuk menyimpan label ke CSV
    private void saveToCsv(String imagePath, String label) {
        Log.d("StorageDebug", "saveToCsv() dipanggil: " + imagePath + ", " + label);

        try {
            File file = new File(csvPath);
            File parentDir = file.getParentFile();

            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                Log.d("StorageDebug", "Membuat direktori CSV parent: " + created);
                if (!created) {
                    Log.e("StorageDebug", "Gagal membuat direktori CSV parent: " + parentDir.getAbsolutePath());
                    updateConsoleLog("[!] Error: Gagal membuat direktori CSV parent");
                    return;
                }
            }

            boolean isNewFile = !file.exists();
            Log.d("StorageDebug", "File CSV " + (isNewFile ? "baru" : "sudah ada"));

            // Buat file jika belum ada
            if (isNewFile) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write("image_path,label\n"); // Header untuk CSV
                    Log.d("StorageDebug", "File CSV baru dibuat dengan header");
                } catch (IOException e) {
                    Log.e("StorageDebug", "Error membuat file CSV: " + e.getMessage(), e);
                    updateConsoleLog("[!] Error membuat file CSV: " + e.getMessage());
                    return;
                }
            }

            // Append data baru ke file CSV
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write(imagePath + "," + label + "\n");
                Log.d("StorageDebug", "Data berhasil ditambahkan ke CSV: " + imagePath + "," + label);
            } catch (IOException e) {
                Log.e("StorageDebug", "Error menambahkan data ke CSV: " + e.getMessage(), e);
                updateConsoleLog("[!] Error menambahkan data ke CSV: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e("StorageDebug", "Error tidak terduga di saveToCsv: " + e.getMessage(), e);
            updateConsoleLog("[!] Error menyimpan label: " + e.getMessage());
        }
    }


    private Runnable updateWPMLoop = new Runnable() {
        @Override
        public void run() {
            if (isTesting) {
                long currentTime = System.currentTimeMillis();
                double elapsedMinutes = (currentTime - startTime) / 60000.0;

                // Hindari pembagian dengan nol (minimal 1 detik)
                if (elapsedMinutes == 0) elapsedMinutes = 1 / 60.0;

                double wpm = totalKata / elapsedMinutes;
                String formattedWPM = String.format(Locale.getDefault(), "%.2f", wpm);
                if(speedTestWPM_SW != null) speedTestWPM_SW.setText(formattedWPM);
                if(speedTestWPM_BS != null) speedTestWPM_BS.setText(formattedWPM);

                handler.postDelayed(this, 1000); // Update setiap 1 detik
            }
        }
    };

    private boolean isEnterUpCLick = false;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN &&
                event.isCtrlPressed() &&
                event.getKeyCode() == KeyEvent.KEYCODE_SPACE) {
            showBottomSheet();
            return true;
        }

        // Mode Calculator
        if (event.getAction() == KeyEvent.ACTION_DOWN &&
                event.isCtrlPressed() &&
                event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_DOT ||
                event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.isCtrlPressed() &&
                        event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE ) {
            new Thread(() -> {

                try {
                    sendKeyEvent(KeyEvent.KEYCODE_A, true);
                    Thread.sleep(100);
                    sendKeyEvent(KeyEvent.KEYCODE_X, true);
                    Thread.sleep(160);
                    processCalculation();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            return true;
        }

        if (event.getAction() == KeyEvent.ACTION_DOWN &&
                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            super.dispatchKeyEvent(event);
            if (isTesting) {
                totalKata++;
            }
            return true;
        }

        /*
        if (isTesting && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
            // Tangani tombol ENTER dengan ACTION_UP
            totalKata++;
            if (getUrl1.contains("kolotibablo.com") || getUrl2.contains("kolotibablo.com")) {
                if (pageTitle1.contains(title_earning) || pageTitle2.contains(title_earning)) {
                    new Thread(() -> {

                        try {
							if (getUrl1.contains("kolotibablo.com") && pageTitle1.contains(title_earning)){
							    if (!webAppInterface.ImgBase64.isEmpty() && !webAppInterface.ImgLabel.isEmpty()) {
									saveBase64Image(webAppInterface.ImgBase64, webAppInterface.ImgLabel);
									Thread.sleep(80);
                                    runOnUiThread(() -> {super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));});
								} else {
                                    runOnUiThread(() -> {
                                        updateConsoleLog("Gambar tidak ditemukan");
                                    });
                                    isEnterUpCLick = true;
								}
							} else
								if (getUrl2.contains("kolotibablo.com") && pageTitle2.contains(title_earning)){
                                    isEnterUpCLick = true;
							}
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            updateConsoleLog("InterruptedException: " + e.getMessage());
                            isEnterUpCLick = true;
                        }
                    }).start();

                    if (isEnterUpCLick) {
                        super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                        isEnterUpCLick = false;
                        return true;
                    }
                    if (isEnterUpCLick && pageTitle1.contains(title_earning) && pageTitle2.contains(title_earning)) {
                        super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                        isEnterUpCLick = false;
                        new Handler().postDelayed(() -> performAutoClick(), 200);
                        return true;
                    }
                }
            }
            return true;
        }*/

        if (getUrl1.contains("kolotibablo.com") || getUrl2.contains("kolotibablo.com")){

            if (pageTitle1.equals(title_earning) && pageTitle2.equals(title_earning)) {

                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    // Tangani tombol ENTER dengan ACTION_UP
                    super.dispatchKeyEvent(event);
                    new Handler().postDelayed(() -> performAutoClick(), 200);
                    return true;
                }

                if (event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE && event.getAction() == KeyEvent.ACTION_UP) {
                    // Tangani tombol ESCAPE dengan ACTION_UP
                    super.dispatchKeyEvent(event);
                    new Handler().postDelayed(() -> performAutoClick(), 200);
                    return true;
                }

                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.isCtrlPressed() && event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_7) {
                    webViewTab1.requestFocus(); // Root view batas pointerLeft
                    simulateTouch(webViewTab1, lastX_left, lastY_left);
                    toggleClick = true;
                    return true;
                }

                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.isCtrlPressed() && event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_9) {
                    viewGecko.requestFocus(); // Root view batas pointerRight
                    simulateTouch(viewGecko, lastX_right, lastY_right);
                    toggleClick = false;
                    return true;
                }

                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.isAltPressed() && event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_7) {
                    simulateTouch(webViewTab1, lastX_pauseL, lastY_pauseL);
                    return true;
                }
                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.isAltPressed() && event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_9) {
                    simulateTouch(viewGecko, lastX_pauseR, lastY_pauseR);
                    return true;
                }

                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.isCtrlPressed() &&
                        event.isAltPressed() &&
                        event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_7) {
                    simulateTouch(webViewTab1, lastX_closeL, lastY_closeL);
                    return true;
                }
                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.isCtrlPressed() &&
                        event.isAltPressed() &&
                        event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_9) {
                    simulateTouch(viewGecko, lastX_closeR, lastY_closeR);
                    return true;
                }

            }  else if (pageTitle1.equals(title_earning)) {
                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.isCtrlPressed() && event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_7) {
                    webViewTab1.requestFocus(); // Root view batas pointerLeft
                    simulateTouch(webViewTab1, lastX_left, lastY_left);
                    toggleClick = true;
                    return true;
                }
            } else if (pageTitle2.equals(title_earning)) {
                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.isCtrlPressed() && event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_9) {
                    viewGecko.requestFocus(); // Root view batas pointerRight
                    simulateTouch(viewGecko, lastX_right, lastY_right);
                    toggleClick = false;
                    return true;
                }
            }

            // End page title kb

            if (pageTitle1.contains("Employees Area")){
                if ((event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.isCtrlPressed() && event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_7)){
                    if (getUrl1.startsWith("https://kolotibablo.com/workers/entrance/login")){
                        webViewTab1.evaluateJavascript(webAppInterface.autoInputLogin, null);
                        updateConsoleLog("Script Injected. [CTRL+NUM-7]");
                        return true;
                    }
                }
            } else if (pageTitle2.contains("Employees Area")) {

            }

        }// end kolotibablo.com

        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_SUBTRACT) {
            String suggestedLabel = matchCaptchaImage(webAppInterface.ImageCaptcha);
            if (suggestedLabel != null) {
                showCaptchaSuggestion(suggestedLabel);
            }
            super.dispatchKeyEvent(event);
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    private void handleCalculationModeToggle() {
        isCalculationMode = !isCalculationMode;
        // Set status awal

        if (!isCalculationMode) {
            //processCalculation();
        }
    }

    private void sendKeyEvent(int keyCode, boolean withCtrl) {
        runOnUiThread(() -> { // Bungkus dengan runOnUiThread
            long eventTime = System.currentTimeMillis();

            KeyEvent pressEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0,
                    withCtrl ? KeyEvent.META_CTRL_ON : 0);
            dispatchKeyEvent(pressEvent);

            KeyEvent releaseEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0,
                    withCtrl ? KeyEvent.META_CTRL_ON : 0);
            dispatchKeyEvent(releaseEvent);
        });
    }

    private void processCalculation() {
        if (getClipboardData().isEmpty()) {
            calculatorSetress.showError("Tidak ada perhitungan yang dimasukkan");
            return;
        }

        String input = getClipboardData();
        if (input != null) {
            try {
                String result = calculatorSetress.calculate(input);
                new Thread(() -> {
                    try {
                        Thread.sleep(100);
                        copyToClipboard(result);
                        Thread.sleep(100);
                        sendKeyEvent(KeyEvent.KEYCODE_V, true);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getClipboardData() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip() &&
                clipboard.getPrimaryClipDescription() != null &&
                clipboard.getPrimaryClipDescription().hasMimeType("text/plain")) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            return item.getText().toString(); // Ambil teks dari clipboard
        }
        return ""; // Jika clipboard kosong atau tidak ada data teks
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("copied", text);
        clipboard.setPrimaryClip(clip);
    }

    private void clearInputField() {
        runOnUiThread(() -> {
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_CLEAR));
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_CLEAR));
        });
    }

    private void pasteFromClipboard() {
        runOnUiThread(() -> {
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_PASTE));
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_PASTE));
        });
    }

    private void performAutoClick() {

        if (pageTitle1.equals(title_earning)&&pageTitle2.equals(title_earning)){
            if (toggleClick) {
                // AutoClick untuk pointerRight
                //webViewTab1.clearFocus();
                viewGecko.requestFocus(); // Root view batas pointerRight
                simulateTouch(viewGecko, lastX_right, lastY_right);
                toggleClick = false;
            } else {
                // AutoClick untuk pointerLeft
                //viewGecko.clearFocus();
                webViewTab1.requestFocus(); // Root view batas pointerLeft
                simulateDoubleClick(webViewTab1, lastX_left, lastY_left);
                toggleClick = true;
            }
        } else if (pageTitle1.equals(title_earning)){
            webViewTab1.requestFocus(); // Root view batas pointerLeft
            simulateDoubleClick(webViewTab1, lastX_left, lastY_left);
            toggleClick = true;
        } else if (pageTitle2.equals(title_earning)){
            viewGecko.requestFocus(); // Root view batas pointerRight
            simulateTouch(viewGecko, lastX_right, lastY_right);
            toggleClick = false;
        }
    }

    private void simulateTouch(View rootView, float x, float y) {
        // Konversi koordinat ke layar global (jika diperlukan)
        int[] location = new int[2];
        rootView.getLocationOnScreen(location);
        float screenX = location[0] + x;
        float screenY = location[1] + y;

        // Buat MotionEvent ACTION_DOWN
        long downTime = SystemClock.uptimeMillis();
        MotionEvent downEvent = MotionEvent.obtain(
                downTime,
                downTime,
                MotionEvent.ACTION_DOWN,
                screenX,
                screenY,
                0
        );

        // Buat MotionEvent ACTION_UP
        long upTime = SystemClock.uptimeMillis();
        MotionEvent upEvent = MotionEvent.obtain(
                downTime,
                upTime,
                MotionEvent.ACTION_UP,
                screenX,
                screenY,
                0
        );

        // Kirim event ke rootView
        rootView.dispatchTouchEvent(downEvent);
        new Handler().postDelayed(() -> rootView.dispatchTouchEvent(upEvent), 180);

        // Release event
        downEvent.recycle();
        new Handler().postDelayed(() -> upEvent.recycle(), 280);
    }

    private void simulateDoubleClick(View rootView, float x, float y) {
        // Konversi koordinat ke layar global (jika diperlukan)

        float screenX = x;
        float screenY = y;

        // Durasi antar klik untuk double click
        final int doubleClickInterval = 120; // 100ms antara klik pertama dan kedua

        // Simulasi klik pertama
        long downTime = SystemClock.uptimeMillis();
        MotionEvent downEvent1 = MotionEvent.obtain(
                downTime,
                downTime,
                MotionEvent.ACTION_DOWN,
                screenX,
                screenY,
                0
        );

        MotionEvent upEvent1 = MotionEvent.obtain(
                downTime,
                downTime + 50, // 50ms setelah ACTION_DOWN
                MotionEvent.ACTION_UP,
                screenX,
                screenY,
                0
        );

        // Simulasi klik kedua
        long secondClickTime = downTime + doubleClickInterval;
        MotionEvent downEvent2 = MotionEvent.obtain(
                secondClickTime,
                secondClickTime,
                MotionEvent.ACTION_DOWN,
                screenX,
                screenY,
                0
        );

        MotionEvent upEvent2 = MotionEvent.obtain(
                secondClickTime,
                secondClickTime + 50, // 50ms setelah ACTION_DOWN kedua
                MotionEvent.ACTION_UP,
                screenX,
                screenY,
                0
        );

        // Kirim event untuk klik pertama
        rootView.dispatchTouchEvent(downEvent1);
        rootView.dispatchTouchEvent(upEvent1);

        // Delay sebelum klik kedua
        new Handler().postDelayed(() -> {
            rootView.dispatchTouchEvent(downEvent2);
            rootView.dispatchTouchEvent(upEvent2);

            // Release semua event
            downEvent1.recycle();
            upEvent1.recycle();
            downEvent2.recycle();
            upEvent2.recycle();
        }, doubleClickInterval);
    }


    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onBackPressed() {
        // Cek apakah WebView terlihat (visible) dan mendapatkan fokus
        if (webViewTab1.hasFocus()) {
            if (webViewTab1.canGoBack()) {
                webViewTab1.goBack(); // Kembali ke URL sebelumnya jika ada riwayat navigasi
            } else {
                webViewTab1.clearCache(false);
                webViewTab1.clearFocus();
                viewGecko.clearFocus();
                // webViewTab1.setVisibility(View.GONE);
                //Menonaktifkan WebView
                // webViewCSP.setEnabled(false);

                showExitConfirmationDialog();
            }
        }

        if (viewGecko.hasFocus()) {
            if (canGoBack == true) {
                sessionGecko.goBack();
            } else {
                viewGecko.clearFocus();
                webViewTab1.clearFocus();
                showExitConfirmationDialog();
            }
        }

        /*
        if (!webViewTab1.hasFocus()) {
            if (!viewGecko.hasFocus()){
                showExitConfirmationDialog();
            } else {
                if (canGoBack == true) {
                    sessionGecko.goBack();
                } else {
                    showExitConfirmationDialog();
                }
            }
        } else if (!viewGecko.hasFocus()) {
            if (!webViewTab1.hasFocus()) {
                showExitConfirmationDialog();
            } else {
                if (webViewTab1.canGoBack()) {
                    webViewTab1.goBack();
                } else {
                    showExitConfirmationDialog();
                }
            }
        } else */ if (webViewTab1.getVisibility()==View.GONE && viewGecko.getVisibility()==View.GONE) {
            super.onBackPressed();
        }
        //super.onBackPressed();
    }

    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi Keluar");
        builder.setMessage("Apakah Anda yakin ingin keluar dari aplikasi?");
        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish(); // Tutup aplikasi jika pengguna menekan "Ya"
            }
        });
        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Tutup dialog jika pengguna menekan "Tidak"
            }
        });
        builder.show();
    }

    private void setupGeckoSession () {

        sessionGecko.setProgressDelegate(new GeckoSession.ProgressDelegate() {

            @Override
            public void onPageStart(@NonNull @NotNull GeckoSession session, @NonNull @NotNull String url) {
                pBarTab2.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageStop(@NonNull @NotNull GeckoSession session, boolean success) {
                pBarTab2.setVisibility(View.GONE);
                /*if (pageTitle2.contains("kolotibablo.com")) {
                    session.loadUri("javascript:"+injectInputKB);
                }*/
            }
            @Override
            public void onProgressChange(GeckoSession session, int progress) {
                //textViewProgress.setText(progress + "%"); // Tampilkan progress di TextView
                pBarTab2.setProgress(progress);
                if (progress == 100) {
                    pBarTab2.setVisibility(View.GONE);
                } else {
                    pBarTab2.setVisibility(View.VISIBLE);
                }
            }
        });

        sessionGecko.setContentDelegate(new GeckoSession.ContentDelegate() {
            @Override
            public void onCloseRequest(GeckoSession session) {
                System.out.println("Permintaan untuk menutup sesi.");
            }

            @Override
            public void onTitleChange(GeckoSession session, String title) {
                pageTitle2 = title;
                titleTab2.setText(title); // Perbarui title di TextView
            }


        });

        sessionGecko.setNavigationDelegate(new GeckoSession.NavigationDelegate() {
           /* @Override
            public void onLoadRequest(GeckoSession session, GeckoSession.NavigationDelegate.LoadRequest request) {
               // System.out.println("Navigasi ke URL: " + request.uri);
            }*/

            @Override
            public void onCanGoBack(GeckoSession session, boolean GcanGoBack) {
                canGoBack = GcanGoBack;
            }

            @Override
            public void onLocationChange(GeckoSession session, String url, List<GeckoSession.PermissionDelegate.ContentPermission> perms, Boolean hasUserGesture) {
                getUrl2 = url;
                etSearch2.setText(url); // Perbarui URL di EditText
            }


        });
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

                        updateConsoleLog("[ Izin penyimpanan diberikan ]");
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
                        updateConsoleLog("[ Aplikasi membutuhkan izin penyimpanan ]");
                    }
                }
            } catch (Exception e) {
                updateConsoleLog("[!] Error: " + e.getMessage());
            }
        }
    }

    // Method onResume atau saat aplikasi dibuka kembali
    @Override
    protected void onResume() {
        super.onResume();
        // Muat hash terakhir dari SharedPreferences
        //lastBase64Hash = preferences.getString(LAST_BASE64_HASH_KEY, lastBase64Hash);
        lastBase64HashLocal = preferences.getString(LAST_BASE64_HASH_KEY_LOCAL, lastBase64HashLocal);
        //updateConsoleLog("Last Base64 Hash Server: " + lastBase64Hash);
        updateConsoleLog("Last Base64 Hash Local: " + lastBase64HashLocal);

        checkLastHashFromServer();
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
                    updateConsoleLog("[ Izin MANAGE_EXTERNAL_STORAGE diberikan ]");
                    storagePermission = true;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(STORAGE_PERMISSION_KEY, true);
                    editor.apply();

                    // Buat direktori setelah izin diberikan
                    createDirectories();
                } else {
                    updateConsoleLog("[!] Izin MANAGE_EXTERNAL_STORAGE ditolak");
                    storagePermission = false;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(STORAGE_PERMISSION_KEY, false);
                    editor.apply();
                }
            }
        }

    }

    private void showBottomSheet() {

        /*
        LinearLayout rootBsSW = bottomSheetView.findViewById(R.id.rootBottomsheetSW);

        BottomSheetBehavior behaviorBsSW = BottomSheetBehavior.from(rootBsSW);

        behaviorBsSW.setState(BottomSheetBehavior.STATE_EXPANDED);
        */

        etSearch1.setText(getUrl1);
        etSearch2.setText(getUrl2);
        titleTab1.setText(pageTitle1);
        titleTab2.setText(pageTitle2);

        bottomSheetDialog.show();
    }


    private void switchViewSearch1(){
        pageSecure1.setVisibility(View.GONE);
        titleTab1.setVisibility(View.GONE);
        refreshTab1.setVisibility(View.GONE);

        etLayout1.setVisibility(View.VISIBLE);
        layoutAddressBar1.setBackground(null);

        etSearch1.selectAll();
        etSearch1.requestFocus();
        showKeyboard(etSearch1);
    }

    private void switchViewSearch2(){
        pageSecure2.setVisibility(View.GONE);
        titleTab2.setVisibility(View.GONE);
        refreshTab2.setVisibility(View.GONE);

        etLayout2.setVisibility(View.VISIBLE);
        layoutAddressBar2.setBackground(null);

        etSearch2.selectAll();
        etSearch2.requestFocus();
        showKeyboard(etSearch2);
    }



    private void setupWebViewClient() {


        webViewTab1.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // Untuk development/testing, bisa menggunakan handler.proceed()
                // Untuk production, sebaiknya handle dengan proper certificate validation
                handler.proceed(); // Gunakan dengan hati-hati di production
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(final WebView view, WebResourceRequest request) {
                final String url = request.getUrl().toString();


                return AdBlockerWebView.blockAds(view,url) ? AdBlocker.createEmptyResource() :
                        super.shouldInterceptRequest(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // Tampilkan ProgressBar saat halaman dimuat


            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (getUrl1.contains("kolotibablo.com")&&
                        pageTitle1.contains("Employees Area")) {
                    view.evaluateJavascript(webAppInterface.autoInputLogin, null);
                }

            }



            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Mendapatkan URL baru dari permintaan navigasi
                String newUrl = request.getUrl().toString();
                getUrl1 = newUrl;

					/*if (pageTitle.equals("Pop")){
						if (getLink.equals(newUrl)){
							wcYtPlayBtn();
							//CallbackToast("newUrl");
						}
					}*/




                // Memperbarui TextView dengan URL baru
                etSearch1.setText(getUrl1);

                if (AdBlocker.isAd(request.getUrl().toString())) {
                    // Block the ad by returning true
                    return true;
                } else {
                    // Allow regular URLs to be loaded
                    return false;
                }
            }
        });

        webViewTab1.addJavascriptInterface(webAppInterface, "AndroidInterface");

    }



    private void setupWebViewSettings() {
        WebSettings webSettings = webViewTab1.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        // Mengatur dukungan untuk zoom
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true); // Tampilkan kontrol zoom bawaan
        webSettings.setDisplayZoomControls(false); // Sembunyikan kontrol zoom built-in jika diperlukan

        // Mengatur pengaturan tampilan
        webSettings.setUseWideViewPort(true); // Mendukung tampilan halaman lebar
        webSettings.setLoadWithOverviewMode(true); // Muat halaman dengan mode tinjauan

        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);


        // Enable debugging untuk development
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webViewTab1.setWebContentsDebuggingEnabled(true);
        }

    }

    private void setupWebChromeClient() {
        webViewTab1.setWebChromeClient(new WebChromeClient() {
            private View mCustomView;
            private CustomViewCallback mCustomViewCallback;
            private View customView;
            private WebChromeClient.CustomViewCallback customViewCallback;
            private ViewGroup.LayoutParams originalLayoutParams;


            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (StartWorking.this.filePathCallback != null) {
                    StartWorking.this.filePathCallback.onReceiveValue(null);
                }
                StartWorking.this.filePathCallback = filePathCallback;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*"); // Mengatur tipe file yang bisa dipilih, "*" untuk semua jenis
                startActivityForResult(Intent.createChooser(intent, "Pilih File"), FILE_CHOOSER_REQUEST_CODE);
                return true;
            }


            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                // Mendapatkan URL saat ini dari WebView
                String currentUrl = view.getUrl();
                getUrl1 = currentUrl;

                pBarTab1.setProgress(newProgress);
                if (newProgress == 100) {
                    pBarTab1.setVisibility(View.GONE);
                } else {
                    pBarTab1.setVisibility(View.VISIBLE);
                }

                // Lakukan sesuatu dengan URL saat ini, misalnya memperbarui TextView
                etSearch1.setText(getUrl1);


            }
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                // Simpan judul dalam string
                pageTitle1 = title;
                titleTab1.setText(pageTitle1);




                if (pageTitle1.length() > 6) {
                    // Judul memiliki lebih dari 10 karakter, atur animasi marquee
                    titleTab1.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                    titleTab1.setMarqueeRepeatLimit(-1);
                    titleTab1.setSelected(true);
                } else {
                    // Judul memiliki 10 karakter atau kurang, nonaktifkan animasi marquee
                    titleTab1.setEllipsize(TextUtils.TruncateAt.END);
                    titleTab1.setSelected(false);
                }
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                // Dapatkan pesan dari console.log
                String message = consoleMessage.message();

                // Tambahkan pesan ke TextView
                updateConsoleLog(message);

                return super.onConsoleMessage(consoleMessage);
            }

        });
    }

    public void updateConsoleLog(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Tambahkan log baru ke TextView

                if (consoleEnabled==true) {
                    logView.log(message);
                } else {
                    logView.clear();
                }

            }
        });
    }

    private TextWatcher watchTitleEarning = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            String newTitle = s.toString().trim();
            title_earning = newTitle;
            saveEditTextTitleKB(title_earning);
        }
    };
    private void saveEditTextTitleKB (String titleKB) {

        try {
            SharedPreferences sharedPreferences = getSharedPreferences("titleKB", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("titleKB", titleKB);
            editor.apply();
            title_earning = titleKB;
            webAppInterface.updateTitleEarning(title_earning);
            // Debugging log
            Log.d("titleKB", "Saved " + " : " + titleKB);
            Toast.makeText(this, "Title saved successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("titleKB", "Error saving title: " + e.getMessage());
            Toast.makeText(this, "Error saving title:  "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void loadTitleKB(String titleKB) {
        SharedPreferences sharedPreferences = getSharedPreferences("titleKB", MODE_PRIVATE);
        title_earning = sharedPreferences.getString("titleKB", titleKB);
        editTextTitleKB.setText(title_earning);
        webAppInterface.updateTitleEarning(title_earning);
        // Debugging log
        Log.d("titleKB", "Loaded " + " : " + titleKB);

    }

    private void savePointerPosition(String keyX, String keyY, float x, float y) {
        SharedPreferences sharedPreferences = getSharedPreferences("PointerPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(keyX, x);
        editor.putFloat(keyY, y);
        editor.apply();

        // Debugging log
        Log.d("PointerPrefs", "Saved " + keyX + ": " + x + ", " + keyY + ": " + y);
    }

    private float[] loadPointerPosition(String keyX, String keyY) {
        SharedPreferences sharedPreferences = getSharedPreferences("PointerPrefs", MODE_PRIVATE);
        float x = sharedPreferences.getFloat(keyX, 0);
        float y = sharedPreferences.getFloat(keyY, 0);

        // Debugging log
        Log.d("PointerPrefs", "Loaded " + keyX + ": " + x + ", " + keyY + ": " + y);

        return new float[]{x, y};
    }


    private void setupPointerMovement() {

        pointerRight.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Ambil jarak antara titik sentuh dan posisi ImageView
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        textPointerR.setVisibility(View.VISIBLE);
                        textPointerR.setText("X: " + dX + " Y: " + dY);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // Hitung posisi baru berdasarkan titik sentuh
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        // Dapatkan batas root layout
                        int rootWidth = viewGecko.getWidth();
                        int rootHeight = viewGecko.getHeight();

                        // Pastikan pointer tetap dalam batas
                        if (newX < 0) newX = 0;
                        if (newX + v.getWidth() > rootWidth) newX = rootWidth - v.getWidth();
                        if (newY < 0) newY = 0;
                        if (newY + v.getHeight() > rootHeight) newY = rootHeight - v.getHeight();

                        // Pindahkan pointer
                        v.setX(newX);
                        v.setY(newY);
                        textPointerR.setText("X: " + newX + " Y: " + newY);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Simpan koordinat saat pointer dilepaskan
                        lastX_right = v.getX();
                        lastY_right = v.getY();
                        savePointerPosition(KEY_POINTER_RIGHT_X, KEY_POINTER_RIGHT_Y, lastX_right, lastY_right);
                        Log.d("PointerMovement", "Saved pointerRight: X=" + lastX_right + ", Y=" + lastY_right);
                        textPointerR.setText("X: " + lastX_right + " Y: " + lastY_right);
                        // Simpan atau gunakan koordinat untuk kebutuhan Anda
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                textPointerR.setVisibility(View.GONE);
                            }
                        }, 1000);
                        break;
                }
                return true;
            }
        });

        pointerLeft.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Ambil jarak antara titik sentuh dan posisi ImageView
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        textPointerL.setVisibility(View.VISIBLE);
                        textPointerL.setText("X: " + dX + " Y: " + dY);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // Hitung posisi baru berdasarkan titik sentuh
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        // Dapatkan batas root layout
                        int rootWidth = webViewTab1.getWidth();
                        int rootHeight = webViewTab1.getHeight();

                        // Pastikan pointer tetap dalam batas
                        if (newX < 0) newX = 0;
                        if (newX + v.getWidth() > rootWidth) newX = rootWidth - v.getWidth();
                        if (newY < 0) newY = 0;
                        if (newY + v.getHeight() > rootHeight) newY = rootHeight - v.getHeight();

                        // Pindahkan pointer
                        v.setX(newX);
                        v.setY(newY);
                        textPointerL.setText("X: " + newX + " Y: " + newY);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Simpan koordinat saat pointer dilepaskan
                        lastX_left = v.getX();
                        lastY_left = v.getY();
                        savePointerPosition(KEY_POINTER_LEFT_X, KEY_POINTER_LEFT_Y, lastX_left, lastY_left);
                        Log.d("PointerMovement", "Saved pointerLeft: X=" + lastX_left + ", Y=" + lastY_left);
                        textPointerL.setText("X: " + lastX_left + " Y: " + lastY_left);
                        // Simpan atau gunakan koordinat untuk kebutuhan Anda
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                textPointerL.setVisibility(View.GONE);
                            }
                        }, 1000);

                        break;
                }
                return true;
            }
        });

        pointerPauseR.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Ambil jarak antara titik sentuh dan posisi ImageView
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        textPointerR.setVisibility(View.VISIBLE);
                        textPointerR.setText("X: " + dX + " Y: " + dY);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // Hitung posisi baru berdasarkan titik sentuh
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        // Dapatkan batas root layout
                        int rootWidth = viewGecko.getWidth();
                        int rootHeight = viewGecko.getHeight();

                        // Pastikan pointer tetap dalam batas
                        if (newX < 0) newX = 0;
                        if (newX + v.getWidth() > rootWidth) newX = rootWidth - v.getWidth();
                        if (newY < 0) newY = 0;
                        if (newY + v.getHeight() > rootHeight) newY = rootHeight - v.getHeight();

                        // Pindahkan pointer
                        v.setX(newX);
                        v.setY(newY);
                        textPointerR.setText("X: " + newX + " Y: " + newY);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Simpan koordinat saat pointer dilepaskan
                        lastX_pauseR = v.getX();
                        lastY_pauseR = v.getY();
                        savePointerPosition(KEY_POINTER_PAUSE_R_X, KEY_POINTER_PAUSE_R_Y, lastX_pauseR, lastY_pauseR);
                        Log.d("PointerMovement", "Saved pointerPauseR: X=" + lastX_pauseR + ", Y=" + lastY_pauseR);
                        textPointerR.setText("X: " + lastX_pauseR + " Y: " + lastY_pauseR);
                        // Simpan atau gunakan koordinat untuk kebutuhan Anda
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                textPointerR.setVisibility(View.GONE);
                            }
                        }, 1000);
                        break;
                }
                return true;
            }
        });

        pointerPauseL.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Ambil jarak antara titik sentuh dan posisi ImageView
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        textPointerL.setVisibility(View.VISIBLE);
                        textPointerL.setText("X: " + dX + " Y: " + dY);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // Hitung posisi baru berdasarkan titik sentuh
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        // Dapatkan batas root layout
                        int rootWidth = webViewTab1.getWidth();
                        int rootHeight = webViewTab1.getHeight();

                        // Pastikan pointer tetap dalam batas
                        if (newX < 0) newX = 0;
                        if (newX + v.getWidth() > rootWidth) newX = rootWidth - v.getWidth();
                        if (newY < 0) newY = 0;
                        if (newY + v.getHeight() > rootHeight) newY = rootHeight - v.getHeight();

                        // Pindahkan pointer
                        v.setX(newX);
                        v.setY(newY);
                        textPointerL.setText("X: " + newX + " Y: " + newY);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Simpan koordinat saat pointer dilepaskan
                        lastX_pauseL = v.getX();
                        lastY_pauseL = v.getY();
                        savePointerPosition(KEY_POINTER_PAUSE_L_X, KEY_POINTER_PAUSE_L_Y, lastX_pauseL, lastY_pauseL);
                        Log.d("PointerMovement", "Saved pointerPauseL: X=" + lastX_pauseL + ", Y=" + lastY_pauseL);
                        textPointerL.setText("X: " + lastX_pauseL + " Y: " + lastY_pauseL);
                        // Simpan atau gunakan koordinat untuk kebutuhan Anda
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                textPointerL.setVisibility(View.GONE);
                            }
                        }, 1000);
                        break;
                }
                return true;
            }
        });

        pointerCloseR.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Ambil jarak antara titik sentuh dan posisi ImageView
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        textPointerR.setVisibility(View.VISIBLE);
                        textPointerR.setText("X: " + dX + " Y: " + dY);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // Hitung posisi baru berdasarkan titik sentuh
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        // Dapatkan batas root layout
                        int rootWidth = viewGecko.getWidth();
                        int rootHeight = viewGecko.getHeight();

                        // Pastikan pointer tetap dalam batas
                        if (newX < 0) newX = 0;
                        if (newX + v.getWidth() > rootWidth) newX = rootWidth - v.getWidth();
                        if (newY < 0) newY = 0;
                        if (newY + v.getHeight() > rootHeight) newY = rootHeight - v.getHeight();

                        // Pindahkan pointer
                        v.setX(newX);
                        v.setY(newY);
                        textPointerR.setText("X: " + newX + " Y: " + newY);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Simpan koordinat saat pointer dilepaskan
                        lastX_closeR = v.getX();
                        lastY_closeR = v.getY();
                        savePointerPosition(KEY_POINTER_CLOSE_R_X, KEY_POINTER_CLOSE_R_Y, lastX_closeR, lastY_closeR);
                        Log.d("PointerMovement", "Saved pointerCloseR: X=" + lastX_closeR + ", Y=" + lastY_closeR);
                        textPointerR.setText("X: " + lastX_closeR + " Y: " + lastY_closeR);
                        // Simpan atau gunakan koordinat untuk kebutuhan Anda
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                textPointerR.setVisibility(View.GONE);
                            }
                        }, 1000);
                        break;
                }
                return true;
            }
        });

        pointerCloseL.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Ambil jarak antara titik sentuh dan posisi ImageView
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        textPointerL.setVisibility(View.VISIBLE);
                        textPointerL.setText("X: " + dX + " Y: " + dY);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // Hitung posisi baru berdasarkan titik sentuh
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        // Dapatkan batas root layout
                        int rootWidth = webViewTab1.getWidth();
                        int rootHeight = webViewTab1.getHeight();

                        // Pastikan pointer tetap dalam batas
                        if (newX < 0) newX = 0;
                        if (newX + v.getWidth() > rootWidth) newX = rootWidth - v.getWidth();
                        if (newY < 0) newY = 0;
                        if (newY + v.getHeight() > rootHeight) newY = rootHeight - v.getHeight();

                        // Pindahkan pointer
                        v.setX(newX);
                        v.setY(newY);
                        textPointerL.setText("X: " + newX + " Y: " + newY);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Simpan koordinat saat pointer dilepaskan
                        lastX_closeL = v.getX();
                        lastY_closeL = v.getY();
                        savePointerPosition(KEY_POINTER_CLOSE_L_X, KEY_POINTER_CLOSE_L_Y, lastX_closeL, lastY_closeL);
                        Log.d("PointerMovement", "Saved pointerCloseL: X=" + lastX_closeL + ", Y=" + lastY_closeL);
                        textPointerL.setText("X: " + lastX_closeL + " Y: " + lastX_closeL);
                        // Simpan atau gunakan koordinat untuk kebutuhan Anda
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                textPointerL.setVisibility(View.GONE);
                            }
                        }, 1000);
                        break;
                }
                return true;
            }
        });


    }





}
