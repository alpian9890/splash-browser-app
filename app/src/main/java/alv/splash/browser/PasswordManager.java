package alv.splash.browser;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class PasswordManager {
    private static PasswordManager instance;
    private SQLiteDatabase database;
    private BrowserDatabaseHelper dbHelper;

    public static final String TABLE_PASSWORDS = "passwords";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_HOST = "host";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private String[] allColumns = {
            COLUMN_ID,
            COLUMN_HOST,
            COLUMN_USERNAME,
            COLUMN_PASSWORD,
            COLUMN_TIMESTAMP
    };

    private PasswordManager(Context context) {
        dbHelper = new BrowserDatabaseHelper(context.getApplicationContext());
    }

    public static synchronized PasswordManager getInstance(Context context) {
        if (instance == null) {
            instance = new PasswordManager(context);
        }
        return instance;
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void saveCredential(String host, String username, String password) {
        if (host == null || username == null || password == null) {
            return;
        }

        open();

        // Check if credential already exists
        String selection = COLUMN_HOST + " = ? AND " + COLUMN_USERNAME + " = ?";
        String[] selectionArgs = { host, username };
        Cursor cursor = database.query(TABLE_PASSWORDS, allColumns, selection, selectionArgs, null, null, null);

        ContentValues values = new ContentValues();
        values.put(COLUMN_HOST, host);
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());

        if (cursor.moveToFirst()) {
            // Update existing credential
            long id = cursor.getLong(0);
            database.update(TABLE_PASSWORDS, values, COLUMN_ID + " = ?", new String[] { String.valueOf(id) });
        } else {
            // Create new credential
            database.insert(TABLE_PASSWORDS, null, values);
        }

        cursor.close();
        close();
    }

    public LoginCredential getCredentialForSite(String host) {
        open();

        String selection = COLUMN_HOST + " = ?";
        String[] selectionArgs = { host };
        Cursor cursor = database.query(TABLE_PASSWORDS, allColumns, selection, selectionArgs, null, null, COLUMN_TIMESTAMP + " DESC");

        LoginCredential credential = null;
        if (cursor.moveToFirst()) {
            long id = cursor.getLong(0);
            String foundHost = cursor.getString(1);
            String username = cursor.getString(2);
            String password = cursor.getString(3);
            long timestamp = cursor.getLong(4);

            credential = new LoginCredential(id, foundHost, username, password, timestamp);
        }

        cursor.close();
        close();

        return credential;
    }

    public List<LoginCredential> getAllCredentials() {
        open();
        List<LoginCredential> credentials = new ArrayList<>();

        Cursor cursor = database.query(TABLE_PASSWORDS, allColumns, null, null, null, null, COLUMN_HOST + " ASC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            long id = cursor.getLong(0);
            String host = cursor.getString(1);
            String username = cursor.getString(2);
            String password = cursor.getString(3);
            long timestamp = cursor.getLong(4);

            LoginCredential credential = new LoginCredential(id, host, username, password, timestamp);
            credentials.add(credential);
            cursor.moveToNext();
        }

        cursor.close();
        close();

        return credentials;
    }

    public void deleteCredential(long id) {
        open();
        database.delete(TABLE_PASSWORDS, COLUMN_ID + " = ?", new String[] { String.valueOf(id) });
        close();
    }
}