package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import android.graphics.RectF;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Labels are saved to SharedPreferences as JSON under the key "labels_<imageName>".
 * They are loaded automatically every time this screen opens.
 *
 * No Firebase needed.
 */
public class ImageLabelingActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ImageLabels";

    private TouchImageView imageView;
    private Button addLabelButton, saveLabelButton, deleteButton, backButton;

    private String imageName = "unknown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_labeling);

        imageView       = findViewById(R.id.imageView);
        addLabelButton  = findViewById(R.id.addLabelButton);
        saveLabelButton = findViewById(R.id.saveLabelButton);
        deleteButton    = findViewById(R.id.clearLabelsButton); // existing button id
        backButton      = findViewById(R.id.backButton);

        // ── Load image from Supabase URL ──────────────────────────────────────
        String imageUrl = getIntent().getStringExtra("imageUrl");
        imageName       = getIntent().getStringExtra("imageName");
        if (imageName == null) imageName = "unknown";

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(imageView);
        }

        // ── Load previously saved labels for this image ───────────────────────
        loadLabels();

        // ── Button listeners ──────────────────────────────────────────────────

        // Add Label — prompts for a name, then lets user draw a rectangle
        addLabelButton.setOnClickListener(v -> showAddLabelDialog());

        // Save Labels — persists current labels to SharedPreferences
        saveLabelButton.setOnClickListener(v -> {
            saveLabels();
            Toast.makeText(this, "Labels saved!", Toast.LENGTH_SHORT).show();
        });

        // Delete Label — shows list of existing labels to pick one to remove
        deleteButton.setOnClickListener(v -> showDeleteDialog());

        backButton.setOnClickListener(v -> finish());
    }

    // ── Add label dialog ──────────────────────────────────────────────────────

    private void showAddLabelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Label Name");

        final EditText input = new EditText(this);
        input.setHint("e.g. Bedroom, Kitchen…");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String label = input.getText().toString().trim();
            if (label.isEmpty()) {
                Toast.makeText(this, "Label name cannot be empty",
                        Toast.LENGTH_SHORT).show();
            } else {
                imageView.setCurrentLabel(label);
                Toast.makeText(this, "Draw rectangle for: " + label,
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // ── Delete label dialog ───────────────────────────────────────────────────

    private void showDeleteDialog() {
        List<TouchImageView.LabeledRect> rects = imageView.getLabeledRects();

        if (rects.isEmpty()) {
            Toast.makeText(this, "No labels to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build list of label names to show in dialog
        String[] labelNames = new String[rects.size() + 1];
        for (int i = 0; i < rects.size(); i++) {
            labelNames[i] = (i + 1) + ". " + rects.get(i).label;
        }
        labelNames[rects.size()] = "🗑 Clear ALL labels";

        new AlertDialog.Builder(this)
                .setTitle("Select label to delete")
                .setItems(labelNames, (dialog, which) -> {
                    if (which == rects.size()) {
                        // Clear all
                        imageView.clearRectangles();
                        Toast.makeText(this, "All labels cleared",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Delete the selected one by index
                        String deleted = rects.get(which).label;
                        rects.remove(which);
                        imageView.setLabeledRects(rects);
                        Toast.makeText(this, "Deleted: " + deleted,
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── SharedPreferences: save ───────────────────────────────────────────────

    private void saveLabels() {
        List<TouchImageView.LabeledRect> labels = imageView.getLabeledRects();
        try {
            JSONArray array = new JSONArray();
            for (TouchImageView.LabeledRect lr : labels) {
                JSONObject obj = new JSONObject();
                obj.put("label",  lr.label);
                obj.put("left",   lr.rect.left);
                obj.put("top",    lr.rect.top);
                obj.put("right",  lr.rect.right);
                obj.put("bottom", lr.rect.bottom);
                array.put(obj);
            }

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit()
                    .putString(keyFor(imageName), array.toString())
                    .apply();

        } catch (Exception e) {
            Toast.makeText(this, "Save error: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    // ── SharedPreferences: load ───────────────────────────────────────────────

    private void loadLabels() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(keyFor(imageName), null);

        if (json == null) return; // no saved labels yet

        try {
            JSONArray array = new JSONArray(json);
            List<TouchImageView.LabeledRect> rects = new ArrayList<>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String label   = obj.getString("label");
                RectF  rect    = new RectF(
                        (float) obj.getDouble("left"),
                        (float) obj.getDouble("top"),
                        (float) obj.getDouble("right"),
                        (float) obj.getDouble("bottom"));
                rects.add(new TouchImageView.LabeledRect(rect, label));
            }

            imageView.setLabeledRects(rects);

            Toast.makeText(this,
                    rects.size() + " label(s) loaded", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Load error: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    // ── Helper: unique SharedPreferences key per image ────────────────────────

    private String keyFor(String name) {
        // Sanitise the filename so it's a valid prefs key
        return "labels_" + name.replaceAll("[^a-zA-Z0-9_]", "_");
    }
}