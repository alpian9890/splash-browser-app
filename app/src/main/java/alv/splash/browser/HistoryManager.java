package alv.splash.browser;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class HistoryManager {
    private static HistoryManager instance;
    private SQLiteDatabase database;
    private BrowserDatabaseHelper dbHelper;
    private String[] allColumns = {
            BrowserDatabaseHelper.COLUMN_ID,
            BrowserDatabaseHelper.COLUMN_URL,
            BrowserDatabaseHelper.COLUMN_TITLE,
            BrowserDatabaseHelper.COLUMN_TIMESTAMP
    };

    private HistoryManager(Context context) {
        dbHelper = new BrowserDatabaseHelper(context.getApplicationContext());
    }

    public static synchronized HistoryManager getInstance(Context context) {
        if (instance == null) {
            instance = new HistoryManager(context);
        }
        return instance;
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void updateHistoryTitle(String url, String newTitle) {
        // Hanya update jika judul baru tidak kosong
        if (newTitle == null || newTitle.isEmpty()) return;

        open();

        // Cari entri dengan URL yang sama
        Cursor cursor = database.query(
                BrowserDatabaseHelper.TABLE_HISTORY,
                allColumns,
                BrowserDatabaseHelper.COLUMN_URL + " = ?",
                new String[] { url },
                null, null,
                BrowserDatabaseHelper.COLUMN_TIMESTAMP + " DESC",
                "1" // Ambil hanya entri terbaru dengan URL ini
        );

        if (cursor.moveToFirst()) {
            long id = cursor.getLong(0);
            String currentTitle = cursor.getString(2);

            // Update judul jika berbeda
            if (!newTitle.equals(currentTitle)) {
                ContentValues values = new ContentValues();
                values.put(BrowserDatabaseHelper.COLUMN_TITLE, newTitle);
                database.update(
                        BrowserDatabaseHelper.TABLE_HISTORY,
                        values,
                        BrowserDatabaseHelper.COLUMN_ID + " = ?",
                        new String[] { String.valueOf(id) }
                );
            }
        }

        cursor.close();
        close();
    }

    public HistoryItem addHistoryItem(HistoryItem item) {
        open();

        String selection = BrowserDatabaseHelper.COLUMN_URL + " = ?";
        String[] selectionArgs = new String[]{item.getUrl()};
        Cursor cursor = database.query(BrowserDatabaseHelper.TABLE_HISTORY,
                allColumns, selection, selectionArgs, null, null, null);

        long id;
        if (cursor.moveToFirst()) {
            int titleColumnIndex = cursor.getColumnIndex(BrowserDatabaseHelper.COLUMN_TITLE);
            int idColumnIndex = cursor.getColumnIndex(BrowserDatabaseHelper.COLUMN_ID);

            // Pengecekan apakah kolom ditemukan
            if (titleColumnIndex != -1 && idColumnIndex != -1) {
                String existingTitle = cursor.getString(titleColumnIndex);
                id = cursor.getLong(idColumnIndex);

                if (!existingTitle.equals(item.getTitle())) {
                    ContentValues values = new ContentValues();
                    values.put(BrowserDatabaseHelper.COLUMN_URL, item.getUrl());
                    values.put(BrowserDatabaseHelper.COLUMN_TITLE, item.getTitle());
                    values.put(BrowserDatabaseHelper.COLUMN_TIMESTAMP, item.getTimestamp());
                    id = database.insert(BrowserDatabaseHelper.TABLE_HISTORY, null, values);
                    item.setId(id);
                } else {
                    ContentValues values = new ContentValues();
                    values.put(BrowserDatabaseHelper.COLUMN_TIMESTAMP, item.getTimestamp());
                    database.update(BrowserDatabaseHelper.TABLE_HISTORY, values,
                            BrowserDatabaseHelper.COLUMN_ID + " = ?",
                            new String[]{String.valueOf(id)});
                    item.setId(id);
                }
            } else {
                // Kolom tidak ditemukan, tangani kesalahan sesuai kebutuhan
                // Contoh: log error atau buat entri baru
                Log.e("DatabaseError", "Kolom tidak ditemukan dalam hasil kueri.");
                ContentValues values = new ContentValues();
                values.put(BrowserDatabaseHelper.COLUMN_URL, item.getUrl());
                values.put(BrowserDatabaseHelper.COLUMN_TITLE, item.getTitle());
                values.put(BrowserDatabaseHelper.COLUMN_TIMESTAMP, item.getTimestamp());
                id = database.insert(BrowserDatabaseHelper.TABLE_HISTORY, null, values);
                item.setId(id);
            }
        } else {
            ContentValues values = new ContentValues();
            values.put(BrowserDatabaseHelper.COLUMN_URL, item.getUrl());
            values.put(BrowserDatabaseHelper.COLUMN_TITLE, item.getTitle());
            values.put(BrowserDatabaseHelper.COLUMN_TIMESTAMP, item.getTimestamp());
            id = database.insert(BrowserDatabaseHelper.TABLE_HISTORY, null, values);
            item.setId(id);
        }

        cursor.close();
        close();
        return item;
    }


    public void deleteHistoryItem(long id) {
        open();
        database.delete(BrowserDatabaseHelper.TABLE_HISTORY,
                BrowserDatabaseHelper.COLUMN_ID + " = " + id, null);
        close();
    }

    public void clearHistory() {
        open();
        database.delete(BrowserDatabaseHelper.TABLE_HISTORY, null, null);
        close();
    }

    public List<HistoryItem> getAllHistoryItems() {
        open();
        List<HistoryItem> items = new ArrayList<>();

        Cursor cursor = database.query(BrowserDatabaseHelper.TABLE_HISTORY,
                allColumns, null, null, null, null,
                BrowserDatabaseHelper.COLUMN_TIMESTAMP + " DESC");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            HistoryItem item = cursorToHistoryItem(cursor);
            items.add(item);
            cursor.moveToNext();
        }

        cursor.close();
        close();
        return items;
    }

    private HistoryItem cursorToHistoryItem(Cursor cursor) {
        long id = cursor.getLong(0);
        String url = cursor.getString(1);
        String title = cursor.getString(2);
        long timestamp = cursor.getLong(3);

        return new HistoryItem(id, url, title, timestamp);
    }

    public List<HistoryItem> searchHistory(String query) {
        open();
        List<HistoryItem> items = new ArrayList<>();

        String selection = BrowserDatabaseHelper.COLUMN_TITLE + " LIKE ? OR " +
                BrowserDatabaseHelper.COLUMN_URL + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%", "%" + query + "%"};

        Cursor cursor = database.query(BrowserDatabaseHelper.TABLE_HISTORY,
                allColumns, selection, selectionArgs, null, null,
                BrowserDatabaseHelper.COLUMN_TIMESTAMP + " DESC");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            HistoryItem item = cursorToHistoryItem(cursor);
            items.add(item);
            cursor.moveToNext();
        }

        cursor.close();
        close();
        return items;
    }
}