package alv.splash.browser;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CaptchaViewHolder extends RecyclerView.ViewHolder {
    private Context context;
    private CaptchaViewerFragment fragment;
    private TextView textId;
    private TextView textPath;
    private TextView textLabel;
    private TextView textTimestamp;
    private TextView textImageDimension, textImageExtension;
    private ImageView captchaImageView;
    private MaterialButton buttonEditData;

    public CaptchaViewHolder(CaptchaViewerFragment fragment, @NonNull View itemView) {
        super(itemView);
        this.fragment = fragment;
        this.context = fragment.requireContext();
        textId = itemView.findViewById(R.id.textId);
        textPath = itemView.findViewById(R.id.textPath);
        textLabel = itemView.findViewById(R.id.textLabel);
        textTimestamp = itemView.findViewById(R.id.textTimestamp);
        textImageDimension = itemView.findViewById(R.id.textImageDimension);
        textImageExtension = itemView.findViewById(R.id.textImageExtension);
        captchaImageView = itemView.findViewById(R.id.captchaImageView);
        buttonEditData = itemView.findViewById(R.id.buttonEditData);
    }

    public void bind(CaptchaDataManager.CaptchaEntry entry) {
        textId.setText(" " + entry.getId());
        textPath.setText( entry.getImagePath());
        textLabel.setText( entry.getLabel());
        textTimestamp.setText( entry.getTimestamp());

        // Load image and get image info
        File imageFile = new File(context.getExternalFilesDir("Datasets") + "/database/" + entry.getImagePath());
        if (imageFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            captchaImageView.setImageBitmap(bitmap);

            // Get image dimensions
            String dimensions = "";
            if (bitmap != null) {
                dimensions = bitmap.getWidth() + "x" + bitmap.getHeight();
            }

            // Get file extension
            String extension = getFileExtension(entry.getImagePath());

            // Set image info
            textImageDimension.setText(dimensions);
            textImageExtension.setText(extension.toUpperCase());
            setExtensionBackground(textImageExtension, extension);
        } else {
            captchaImageView.setImageDrawable(null);
            textImageDimension.setText("File not found");
            textImageExtension.setText("");
            textImageExtension.setBackground(null);
        }

        // Set up edit button
        buttonEditData.setOnClickListener(v -> showEditDialog(entry));
    }

    // Method untuk mendapatkan warna background berdasarkan ekstensi file
    private void setExtensionBackground(TextView textView, String extension) {
        // Standarisasi ekstensi (lowercase tanpa titik)
        String ext = extension.toLowerCase().replace(".", "");

        // Map untuk menyimpan warna background dan warna text untuk setiap ekstensi
        Map<String, Pair<Integer, Integer>> extensionColors = new HashMap<>();

        // Menambahkan pasangan ekstensi dan warna (background, text)
        extensionColors.put("jpg", new Pair<>(Color.parseColor("#4CAF50"), Color.WHITE)); // Hijau
        extensionColors.put("jpeg", new Pair<>(Color.parseColor("#4CAF50"), Color.WHITE)); // Hijau
        extensionColors.put("png", new Pair<>(Color.parseColor("#2196F3"), Color.WHITE)); // Biru
        extensionColors.put("gif", new Pair<>(Color.parseColor("#FF9800"), Color.BLACK)); // Oranye
        extensionColors.put("webp", new Pair<>(Color.parseColor("#9C27B0"), Color.WHITE)); // Ungu
        extensionColors.put("bmp", new Pair<>(Color.parseColor("#F44336"), Color.WHITE)); // Merah
        extensionColors.put("heic", new Pair<>(Color.parseColor("#795548"), Color.WHITE)); // Coklat
        extensionColors.put("tiff", new Pair<>(Color.parseColor("#607D8B"), Color.WHITE)); // Blue Grey
        extensionColors.put("tif", new Pair<>(Color.parseColor("#607D8B"), Color.WHITE)); // Blue Grey
        extensionColors.put("svg", new Pair<>(Color.parseColor("#009688"), Color.WHITE)); // Teal
        extensionColors.put("raw", new Pair<>(Color.parseColor("#FF5722"), Color.WHITE)); // Deep Orange
        extensionColors.put("ico", new Pair<>(Color.parseColor("#3F51B5"), Color.WHITE)); // Indigo
        extensionColors.put("psd", new Pair<>(Color.parseColor("#673AB7"), Color.WHITE)); // Deep Purple

        // Pengaturan default jika ekstensi tidak ditemukan
        Pair<Integer, Integer> colors = null; // Grey
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            colors = extensionColors.getOrDefault(ext,
                    new Pair<>(Color.parseColor("#9E9E9E"), Color.BLACK));
        }

        // Membuat background drawable dengan sudut melengkung
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(colors.first);
        //drawable.setCornerRadius(8); // Sudut melengkung 8dp

        // Menerapkan background dan warna text
        textView.setBackground(drawable);
        textView.setTextColor(colors.second);
        /*
        // Menambahkan padding agar teks tidak terlalu dekat dengan tepi
        int paddingDp = 6;
        int paddingPx = (int) (paddingDp * textView.getResources().getDisplayMetrics().density);
        textView.setPadding(paddingPx, paddingPx / 2, paddingPx, paddingPx / 2);
        */
        // Membuat teks menjadi tebal (bold)
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
    }

    /**
     * Extract file extension from path
     */
    private String getFileExtension(String path) {
        if (path == null || path.isEmpty()) {
            return "unknown";
        }

        int lastDot = path.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < path.length() - 1) {
            return path.substring(lastDot + 1).toLowerCase();
        }

        return "unknown";
    }

    private void showEditDialog(CaptchaDataManager.CaptchaEntry entry) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Entry");

        // Set up the input
        final EditText input = new EditText(context);
        input.setText(entry.getLabel());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newLabel = input.getText().toString();
            new Thread(() -> {
                // Gunakan dataManager dari fragment
                CaptchaDataManager dataManager = fragment.getCaptchaDataManager();
                boolean success = dataManager.updateEntry(entry.getId(), newLabel);

                // Safety check untuk lifecycle fragment
                if (fragment.isAdded()) {
                    fragment.requireActivity().runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(context,
                                    "Updated successfully", Toast.LENGTH_SHORT).show();
                            // Panggil reloadData di fragment, bukan loadData
                            fragment.reloadData();
                        } else {
                            Toast.makeText(context,
                                    "Update failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).start();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.setNeutralButton("Delete", (dialog, which) -> {
            // Kode confirmation dialog
            new AlertDialog.Builder(context)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this entry?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        new Thread(() -> {
                            // Dapatkan dataManager dari fragment
                            CaptchaDataManager dataManager = fragment.getCaptchaDataManager();
                            boolean success = dataManager.deleteEntry(entry.getId(), entry.getImagePath());

                            // Safety check untuk lifecycle fragment
                            if (fragment.isAdded()) {
                                fragment.requireActivity().runOnUiThread(() -> {
                                    if (success) {
                                        Toast.makeText(context,
                                                "Deleted successfully", Toast.LENGTH_SHORT).show();
                                        fragment.reloadData();
                                    } else {
                                        Toast.makeText(context,
                                                "Delete failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).start();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        builder.show();
    }

}