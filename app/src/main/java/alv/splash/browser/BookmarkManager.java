package alv.splash.browser;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class BookmarkManager {
    private static BookmarkManager instance;
    private SQLiteDatabase database;
    private BrowserDatabaseHelper dbHelper;

    public static final String TABLE_BOOKMARKS = "bookmarks";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private String[] allColumns = {
            COLUMN_ID,
            COLUMN_URL,
            COLUMN_TITLE,
            COLUMN_TIMESTAMP
    };

    private BookmarkManager(Context context) {
        dbHelper = new BrowserDatabaseHelper(context.getApplicationContext());
    }

    public static synchronized BookmarkManager getInstance(Context context) {
        if (instance == null) {
            instance = new BookmarkManager(context);
        }
        return instance;
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public BookmarkItem addBookmark(String url, String title) {
        open();

        // Check if bookmark already exists
        String selection = COLUMN_URL + " = ?";
        String[] selectionArgs = { url };
        Cursor cursor = database.query(TABLE_BOOKMARKS, allColumns, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            // Bookmark already exists
            BookmarkItem existingItem = cursorToBookmarkItem(cursor);
            cursor.close();
            close();
            return existingItem;
        }

        // Add new bookmark
        ContentValues values = new ContentValues();
        values.put(COLUMN_URL, url);
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());

        long insertId = database.insert(TABLE_BOOKMARKS, null, values);
        cursor = database.query(TABLE_BOOKMARKS, allColumns, COLUMN_ID + " = " + insertId, null, null, null, null);
        cursor.moveToFirst();
        BookmarkItem newItem = cursorToBookmarkItem(cursor);
        cursor.close();
        close();

        return newItem;
    }

    public void deleteBookmark(long id) {
        open();
        database.delete(TABLE_BOOKMARKS, COLUMN_ID + " = " + id, null);
        close();
    }

    public boolean isBookmarked(String url) {
        open();
        String selection = COLUMN_URL + " = ?";
        String[] selectionArgs = { url };
        Cursor cursor = database.query(TABLE_BOOKMARKS, allColumns, selection, selectionArgs, null, null, null);
        boolean isBookmarked = cursor.getCount() > 0;
        cursor.close();
        close();
        return isBookmarked;
    }

    public List<BookmarkItem> getAllBookmarks() {
        open();
        List<BookmarkItem> bookmarks = new ArrayList<>();
        Cursor cursor = database.query(TABLE_BOOKMARKS, allColumns, null, null, null, null, COLUMN_TITLE + " ASC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            BookmarkItem item = cursorToBookmarkItem(cursor);
            bookmarks.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        close();
        return bookmarks;
    }

    public List<BookmarkItem> searchBookmarks(String query) {
        open();
        List<BookmarkItem> bookmarks = new ArrayList<>();

        String selection = COLUMN_TITLE + " LIKE ? OR " + COLUMN_URL + " LIKE ?";
        String[] selectionArgs = { "%" + query + "%", "%" + query + "%"};

        Cursor cursor = database.query(TABLE_BOOKMARKS, allColumns, selection, selectionArgs, null, null, COLUMN_TITLE + " ASC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            BookmarkItem item = cursorToBookmarkItem(cursor);
            bookmarks.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        close();
        return bookmarks;
    }

    private BookmarkItem cursorToBookmarkItem(Cursor cursor) {
        long id = cursor.getLong(0);
        String url = cursor.getString(1);
        String title = cursor.getString(2);
        long timestamp = cursor.getLong(3);
        return new BookmarkItem(id, url, title, timestamp);
    }
}