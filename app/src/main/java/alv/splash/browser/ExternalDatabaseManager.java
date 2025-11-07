package alv.splash.browser;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class ExternalDatabaseManager {
    private static final String TAG = "ExternalDatabaseManager";
    private Context context;
    private SQLiteDatabase externalDb;
    CaptchaDbHelper dbHelper;
    private String dbPath;
    private CaptchaDataManager internalManager;

    public ExternalDatabaseManager(Context context, String dbPath) {
        this.context = context;
        this.dbPath = dbPath;
        this.internalManager = new CaptchaDataManager(context);

        try {
            externalDb = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
            ensureTableExists();
        } catch (Exception e) {
            Log.e(TAG, "Error opening external database: " + e.getMessage());
            throw new RuntimeException("Failed to open external database", e);
        }
    }

    private void ensureTableExists() {
        Cursor cursor = externalDb.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{CaptchaDbHelper.TABLE_CAPTCHAS}
        );

        boolean tableExists = cursor.getCount() > 0;
        cursor.close();

        if (!tableExists) {
            externalDb.execSQL("CREATE TABLE " + CaptchaDbHelper.TABLE_CAPTCHAS + " (" +
                    CaptchaDbHelper.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CaptchaDbHelper.COLUMN_IMAGE_PATH + " TEXT NOT NULL UNIQUE, " +
                    CaptchaDbHelper.COLUMN_LABEL + " TEXT NOT NULL, " +
                    CaptchaDbHelper.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        }
    }

    public List<CaptchaDataManager.CaptchaEntry> getAllEntries() {
        List<CaptchaDataManager.CaptchaEntry> entries = new ArrayList<>();

        try {
            String[] projection = {
                    CaptchaDbHelper.COLUMN_ID,
                    CaptchaDbHelper.COLUMN_IMAGE_PATH,
                    CaptchaDbHelper.COLUMN_LABEL,
                    CaptchaDbHelper.COLUMN_TIMESTAMP
            };

            Cursor cursor = externalDb.query(
                    CaptchaDbHelper.TABLE_CAPTCHAS,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    CaptchaDbHelper.COLUMN_ID + " DESC"
            );

            while (cursor.moveToNext()) {
                CaptchaDataManager.CaptchaEntry entry = new CaptchaDataManager.CaptchaEntry(
                        cursor.getLong(cursor.getColumnIndexOrThrow(CaptchaDbHelper.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(CaptchaDbHelper.COLUMN_IMAGE_PATH)),
                        cursor.getString(cursor.getColumnIndexOrThrow(CaptchaDbHelper.COLUMN_LABEL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(CaptchaDbHelper.COLUMN_TIMESTAMP))
                );
                entries.add(entry);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting entries: " + e.getMessage());
        }

        return entries;
    }

    /**
     * Get total number of entries in the external database
     */
    public int getEntryCount() {
        int count = 0;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + CaptchaDbHelper.TABLE_CAPTCHAS, null);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting entry count from external DB: " + e.getMessage(), e);
        }
        return count;
    }

    /**
     * Get entries with pagination support from external database
     */
    public List<CaptchaDataManager.CaptchaEntry> getEntriesWithPagination(int offset, int limit) {
        List<CaptchaDataManager.CaptchaEntry> entries = new ArrayList<>();

        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String[] projection = {
                    CaptchaDbHelper.COLUMN_ID,
                    CaptchaDbHelper.COLUMN_IMAGE_PATH,
                    CaptchaDbHelper.COLUMN_LABEL,
                    CaptchaDbHelper.COLUMN_TIMESTAMP
            };

            Cursor cursor = db.query(
                    CaptchaDbHelper.TABLE_CAPTCHAS,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    CaptchaDbHelper.COLUMN_ID + " DESC",
                    offset + "," + limit
            );

            while (cursor.moveToNext()) {
                CaptchaDataManager.CaptchaEntry entry = new CaptchaDataManager.CaptchaEntry(
                        cursor.getLong(cursor.getColumnIndexOrThrow(CaptchaDbHelper.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(CaptchaDbHelper.COLUMN_IMAGE_PATH)),
                        cursor.getString(cursor.getColumnIndexOrThrow(CaptchaDbHelper.COLUMN_LABEL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(CaptchaDbHelper.COLUMN_TIMESTAMP))
                );
                entries.add(entry);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting paginated entries from external DB: " + e.getMessage(), e);
        }

        return entries;
    }

    public boolean updateEntry(long id, String label) {
        try {
            externalDb.execSQL("UPDATE " + CaptchaDbHelper.TABLE_CAPTCHAS +
                            " SET " + CaptchaDbHelper.COLUMN_LABEL + " = ? WHERE " +
                            CaptchaDbHelper.COLUMN_ID + " = ?",
                    new Object[]{label, id});
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error updating entry: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteEntry(long id, String imagePath) {
        try {
            externalDb.execSQL("DELETE FROM " + CaptchaDbHelper.TABLE_CAPTCHAS +
                            " WHERE " + CaptchaDbHelper.COLUMN_ID + " = ?",
                    new Object[]{id});
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting entry: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        if (externalDb != null && externalDb.isOpen()) {
            externalDb.close();
        }
        if (internalManager != null) {
            internalManager.close();
        }
    }
}