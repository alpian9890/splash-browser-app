package alv.splash.browser;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Locale;

public class LogView extends LinearLayout {
    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }

    private TextView logTextView;
    private ScrollView scrollView;
    private final LinkedList<LogEntry> logEntries = new LinkedList<>();
    private int MAX_LOG_ENTRIES = 100;
    private boolean autoScroll = true;
    private boolean isEnabled = true;
    private LogLevel minimumLogLevel = LogLevel.DEBUG;
    private EnumMap<LogLevel, Integer> logLevelColors = new EnumMap<>(LogLevel.class);
    private TextView copyButton, saveButton;
    private boolean isCopyCooldown = false;
    private boolean isSaveCooldown = false;
    private Handler handler = new Handler();

    // Constructor
    public LogView(Context context) {
        super(context);
        init(context);
    }

    public LogView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LogView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        initializeColors();

        // Create log display
        scrollView = new ScrollView(context);
        scrollView.setFocusable(false);
        scrollView.setFocusableInTouchMode(false);

        logTextView = new TextView(context);
        logTextView.setFocusable(false);
        logTextView.setFocusableInTouchMode(false);
        
        logTextView.setTypeface(Typeface.MONOSPACE);
        logTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        logTextView.setTextColor(Color.WHITE);
        logTextView.setPadding(8, 8, 8, 8);

        scrollView.addView(logTextView);
        addView(scrollView, new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT, 1.0f));

        /*// Create control buttons
        LinearLayout controlsLayout = createControlsLayout(context);
        addView(controlsLayout, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        */
    }

    private void initializeColors() {
        logLevelColors.put(LogLevel.DEBUG, Color.GRAY);
        logLevelColors.put(LogLevel.INFO, Color.WHITE);
        logLevelColors.put(LogLevel.WARN, Color.YELLOW);
        logLevelColors.put(LogLevel.ERROR, Color.RED);
    }

    private LinearLayout createControlsLayout(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(HORIZONTAL);

        TextView clearButton = createButton(context, "Clear", v -> clear());
        copyButton = createButton(context, "Copy", v -> handleCopyClick());
        saveButton = createButton(context, "Save", v -> handleSaveClick());
        TextView scrollToggle = createButton(context, "Auto-scroll: ON",
                v -> toggleAutoScroll((TextView) v));

        layout.addView(clearButton);
        layout.addView(copyButton);
        layout.addView(saveButton);
        layout.addView(scrollToggle);
        return layout;
    }

    private TextView createButton(Context context, String text, OnClickListener listener) {
        TextView button = new TextView(context);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setBackgroundResource(android.R.color.darker_gray);
        button.setPadding(16, 8, 16, 8);
        button.setTypeface(Typeface.MONOSPACE);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        button.setOnClickListener(listener);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(8, 8, 8, 8);
        button.setLayoutParams(params);
       // button.setVisibility(View.GONE);// GONE
        return button;
    }

    private void toggleAutoScroll(TextView button) {
        autoScroll = !autoScroll;
        button.setText(autoScroll ? "Auto-scroll: ON" : "Auto-scroll: OFF");
    }

    // Public methods
    public void enableLogging() { isEnabled = true; }
    public void disableLogging() { isEnabled = false; }

    public void setLogLevelColor(LogLevel level, int color) {
        logLevelColors.put(level, color);
        updateDisplay();
    }

    public void setMinimumLogLevel(LogLevel level) {
        minimumLogLevel = level;
        updateDisplay();
    }

    public void log(String message) {
        log(LogLevel.INFO, message);
    }

    public void log(LogLevel level, String message) {
        if (!isEnabled || level.ordinal() < minimumLogLevel.ordinal()) return;

        post(() -> {
            synchronized (logEntries) {
                logEntries.add(new LogEntry(level, message));
                while (logEntries.size() > MAX_LOG_ENTRIES) {
                    logEntries.removeFirst();
                }
                updateDisplay();
            }
        });
    }

    public void clear() {
        post(() -> {
            synchronized (logEntries) {
                logEntries.clear();
                updateDisplay();
            }
        });
    }

    public String getLogContent() {
        synchronized (logEntries) {
            StringBuilder content = new StringBuilder();
            for (LogEntry entry : logEntries) {
                content.append(formatLogEntry(entry)).append("\n");
            }
            return content.toString();
        }
    }

    private void updateDisplay() {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        synchronized (logEntries) {
            for (LogEntry entry : logEntries) {
                if (ssb.length() > 0) ssb.append("\n");
                addStyledEntry(ssb, entry);
            }
        }
        logTextView.setText(ssb);
        scrollToBottom();
    }

    private void addStyledEntry(SpannableStringBuilder ssb, LogEntry entry) {
        int start = ssb.length();
        String logLine = formatLogEntry(entry);
        ssb.append(logLine);
        ssb.setSpan(new ForegroundColorSpan(logLevelColors.get(entry.level)),
                start, ssb.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private String formatLogEntry(LogEntry entry) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return String.format("[%s %s] %s",
                entry.level.name(), sdf.format(entry.timestamp), entry.message);
    }

    private void scrollToBottom() {
        if (autoScroll) {
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        }
    }

    private void handleCopyClick() {
        if (isCopyCooldown) return;
        isCopyCooldown = true;
        copyButton.setText("Copied");
        copyToClipboard();
        handler.postDelayed(() -> {
            copyButton.setText("Copy");
            isCopyCooldown = false;
        }, 1000);
    }

    private void handleSaveClick() {
        if (isSaveCooldown) return;
        isSaveCooldown = true;
        saveButton.setText("Saved");
        saveToFile();
        handler.postDelayed(() -> {
            saveButton.setText("Save");
            isSaveCooldown = false;
        }, 1000);
    }

    private void copyToClipboard() {
        String content = getLogContent();
        if (!content.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager)
                    getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("Log Content", content));
            showToast("Log copied to clipboard");
        }
    }

    public void saveToFile() {
        try {
            String content = getLogContent();
            if (content.isEmpty()) {
                showToast("No content to save");
                return;
            }

            File logDir = new File(getContext().getExternalFilesDir(null), "logs");
            if (!logDir.exists() && !logDir.mkdirs()) {
                throw new Exception("Failed to create log directory");
            }

            String filename = "log_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date()) + ".txt";
            File logFile = new File(logDir, filename);

            try (FileOutputStream fos = new FileOutputStream(logFile)) {
                fos.write(content.getBytes());
            }

            log(LogLevel.INFO, "[Log saved: " + logFile.getAbsolutePath() + "]");
            showToast("Log saved to: " + logFile.getAbsolutePath());
        } catch (Exception e) {
            log(LogLevel.ERROR, "[!] Error saving log: " + e.getMessage());
            showToast("Error saving log: " + e.getMessage());
        }
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private static class LogEntry {
        LogLevel level;
        String message;
        long timestamp;

        LogEntry(LogLevel level, String message) {
            this.level = level;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
    }
}