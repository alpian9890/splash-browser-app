package alv.splash.browser;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BrowserDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "browser.db";
    private static final int DATABASE_VERSION = 2;

    // History table
    public static final String TABLE_HISTORY = "history";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    // Bookmarks table
    public static final String TABLE_BOOKMARKS = "bookmarks";

    // Passwords table
    public static final String TABLE_PASSWORDS = "passwords";
    public static final String COLUMN_HOST = "host";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    // History table creation
    private static final String CREATE_HISTORY_TABLE =
            "CREATE TABLE " + TABLE_HISTORY + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_URL + " TEXT NOT NULL, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_TIMESTAMP + " INTEGER NOT NULL);";

    // Bookmarks table creation
    private static final String CREATE_BOOKMARKS_TABLE =
            "CREATE TABLE " + TABLE_BOOKMARKS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_URL + " TEXT NOT NULL, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_TIMESTAMP + " INTEGER NOT NULL);";

    // Passwords table creation
    private static final String CREATE_PASSWORDS_TABLE =
            "CREATE TABLE " + TABLE_PASSWORDS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_HOST + " TEXT NOT NULL, " +
                    COLUMN_USERNAME + " TEXT NOT NULL, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_TIMESTAMP + " INTEGER NOT NULL);";

    public BrowserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_HISTORY_TABLE);
        db.execSQL(CREATE_BOOKMARKS_TABLE);
        db.execSQL(CREATE_PASSWORDS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add new tables for version 2
            db.execSQL(CREATE_BOOKMARKS_TABLE);
            db.execSQL(CREATE_PASSWORDS_TABLE);
        }
    }
}