package alv.splash.browser;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CaptchaDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "captcha_database.db";
    private static final int DATABASE_VERSION = 1;

    // Table and columns
    public static final String TABLE_CAPTCHAS = "captchas";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_IMAGE_PATH = "image_path"; // Berubah menjadi path
    public static final String COLUMN_LABEL = "label";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String SQL_CREATE_CAPTCHAS_TABLE =
            "CREATE TABLE " + TABLE_CAPTCHAS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_IMAGE_PATH + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_LABEL + " TEXT NOT NULL, " +
                    COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

    // Custom database path
    public CaptchaDbHelper(Context context, String databasePath) {
        super(context, databasePath + "/" + DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CAPTCHAS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CAPTCHAS);
        onCreate(db);
    }
}