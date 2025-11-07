package alv.splash.browser;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CaptchaDataManager {
    private static final String TAG = "CaptchaDataManager";
    private CaptchaDbHelper dbHelper;
    private Context context;
    private String databasePath;
    private String imagesPath;

    public CaptchaDataManager(Context context) {
        this.context = context;

        // Buat struktur direktori
        File baseDir = context.getExternalFilesDir("Datasets");
        if (baseDir != null) {
            File dbDir = new File(baseDir, "database");
            if (!dbDir.exists()) {
                dbDir.mkdirs();
            }

            // Buat direktori images
            File imagesDir = new File(dbDir, "Images");
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }

            this.databasePath = dbDir.getAbsolutePath();
            this.imagesPath = imagesDir.getAbsolutePath();
            dbHelper = new CaptchaDbHelper(context, databasePath);
            Log.d(TAG, "Database path: " + databasePath);
            Log.d(TAG, "Images path: " + imagesPath);
        } else {
            Log.e(TAG, "Gagal membuat direktori database");
        }
    }

    /**
     * Ekstrak ekstensi dari string Base64
     */
    private String extractExtension(String base64Data) {
        try {
            // Parse dari format "data:image/png;base64,..."
            if (base64Data.contains("data:image/")) {
                String mimeTypePart = base64Data.substring(base64Data.indexOf("data:image/") + 11);
                return mimeTypePart.substring(0, mimeTypePart.indexOf(";"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error ekstrak ekstensi: " + e.getMessage());
        }
        return "png"; // Default ke PNG jika tidak dapat menentukan tipe
    }

    /**
     * Decode Base64 ke file dan return path file
     */
    private String saveBase64ToImageFile(String base64Data, String hash) {
        try {
            // Ekstrak ekstensi dan konten base64
            String extension = extractExtension(base64Data);
            String actualBase64 = base64Data.substring(base64Data.indexOf(",") + 1);

            // Buat path file
            String fileName = hash + "." + extension;
            File outputFile = new File(imagesPath, fileName);

            // Decode dan simpan
            byte[] decodedBytes = android.util.Base64.decode(actualBase64, android.util.Base64.DEFAULT);
            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.write(decodedBytes);
            fos.close();

            return "Images/" + fileName;
        } catch (Exception e) {
            Log.e(TAG, "Error menyimpan base64 ke file: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Simpan data CAPTCHA ke SQLite
     */
    public boolean saveCaptchaData(String base64Data, String label) {
        Log.d(TAG, "saveCaptchaData() dipanggil");

        if (base64Data == null || base64Data.isEmpty()) {
            Log.e(TAG, "Data Base64 kosong");
            return false;
        }

        try {
            // Hitung hash
            String imageHash = calculateSHA256(base64Data);

            // Cek apakah sudah ada
            String extension = extractExtension(base64Data);
            String imagePath = "Images/" + imageHash + "." + extension;
            if (getLabelByImagePath(imagePath) != null) {
                Log.d(TAG, "Gambar sudah ada di database");
                return false;
            }

            // Simpan gambar ke file
            String savedImagePath = saveBase64ToImageFile(base64Data, imageHash);
            if (savedImagePath == null) {
                Log.e(TAG, "Gagal menyimpan file gambar");
                return false;
            }

            // Simpan record ke database
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(CaptchaDbHelper.COLUMN_IMAGE_PATH, savedImagePath);
            values.put(CaptchaDbHelper.COLUMN_LABEL, label);

            long newRowId = db.insert(CaptchaDbHelper.TABLE_CAPTCHAS, null, values);
            return newRowId != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error menyimpan data captcha: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Cari label berdasarkan path gambar
     */
    public String getLabelByImagePath(String imagePath) {
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String[] projection = {CaptchaDbHelper.COLUMN_LABEL};
            String selection = CaptchaDbHelper.COLUMN_IMAGE_PATH + " = ?";
            String[] selectionArgs = {imagePath};

            Cursor cursor = db.query(
                    CaptchaDbHelper.TABLE_CAPTCHAS,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            String label = null;
            if (cursor.moveToFirst()) {
                label = cursor.getString(cursor.getColumnIndexOrThrow(CaptchaDbHelper.COLUMN_LABEL));
            }
            cursor.close();
            return label;
        } catch (Exception e) {
            Log.e(TAG, "Error mendapatkan label berdasarkan path: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Cari label untuk gambar
     */
    public String getLabelForImage(String base64Data) {
        if (base64Data == null || base64Data.isEmpty()) {
            return null;
        }
        String imageHash = calculateSHA256(base64Data);
        String extension = extractExtension(base64Data);
        String imagePath = "Images/" + imageHash + "." + extension;
        return getLabelByImagePath(imagePath);
    }

    /**
     * Hitung SHA-256 hash
     */
    private String calculateSHA256(String data) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            return bytesToHex(hash);
        } catch (Exception e) {
            Log.e(TAG, "Error menghitung hash: " + e.getMessage(), e);
            return "";
        }
    }

    /**
     * Konversi bytes ke hex
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Dapatkan semua entri dari database
     */
    public List<CaptchaEntry> getAllEntries() {
        List<CaptchaEntry> entries = new ArrayList<>();

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
                    CaptchaDbHelper.COLUMN_ID + " DESC"
            );

            while (cursor.moveToNext()) {
                CaptchaEntry entry = new CaptchaEntry(
                        cursor.getLong(cursor.getColumnIndexOrThrow(CaptchaDbHelper.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(CaptchaDbHelper.COLUMN_IMAGE_PATH)),
                        cursor.getString(cursor.getColumnIndexOrThrow(CaptchaDbHelper.COLUMN_LABEL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(CaptchaDbHelper.COLUMN_TIMESTAMP))
                );
                entries.add(entry);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error mendapatkan semua entri: " + e.getMessage(), e);
        }

        return entries;
    }

    /**
     * Get total number of entries in the database
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
            Log.e(TAG, "Error getting entry count: " + e.getMessage(), e);
        }
        return count;
    }

    /**
     * Get entries with pagination support
     */
    public List<CaptchaEntry> getEntriesWithPagination(int offset, int limit) {
        List<CaptchaEntry> entries = new ArrayList<>();

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
                CaptchaEntry entry = new CaptchaEntry(
                        cursor.getLong(cursor.getColumnIndexOrThrow(CaptchaDbHelper.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(CaptchaDbHelper.COLUMN_IMAGE_PATH)),
                        cursor.getString(cursor.getColumnIndexOrThrow(CaptchaDbHelper.COLUMN_LABEL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(CaptchaDbHelper.COLUMN_TIMESTAMP))
                );
                entries.add(entry);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting paginated entries: " + e.getMessage(), e);
        }

        return entries;
    }

    /**
     * Update entri di database
     */
    public boolean updateEntry(long id, String label) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(CaptchaDbHelper.COLUMN_LABEL, label);

            int rowsAffected = db.update(
                    CaptchaDbHelper.TABLE_CAPTCHAS,
                    values,
                    CaptchaDbHelper.COLUMN_ID + " = ?",
                    new String[] { String.valueOf(id) }
            );

            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error mengupdate entri: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Hapus entri dari database
     */
    public boolean deleteEntry(long id, String imagePath) {
        try {
            // Hapus file gambar dulu
            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(databasePath, imagePath);
                if (imageFile.exists()) {
                    imageFile.delete();
                }
            }

            // Hapus record database
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int rowsAffected = db.delete(
                    CaptchaDbHelper.TABLE_CAPTCHAS,
                    CaptchaDbHelper.COLUMN_ID + " = ?",
                    new String[] { String.valueOf(id) }
            );

            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error menghapus entri: " + e.getMessage(), e);
            return false;
        }
    }

    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    /**
     * Class model untuk entri database
     */
    public static class CaptchaEntry {
        private long id;
        private String imagePath;
        private String label;
        private String timestamp;

        public CaptchaEntry(long id, String imagePath, String label, String timestamp) {
            this.id = id;
            this.imagePath = imagePath;
            this.label = label;
            this.timestamp = timestamp;
        }

        public long getId() { return id; }
        public String getImagePath() { return imagePath; }
        public String getLabel() { return label; }
        public String getTimestamp() { return timestamp; }
    }
}