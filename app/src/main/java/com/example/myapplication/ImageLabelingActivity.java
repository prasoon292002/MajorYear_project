package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.*;

import android.graphics.RectF;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads the image from a Supabase public URL (passed via Intent "imageUrl")
 * and lets the user draw labelled rectangles on it.
 *
 * Labels are still saved to Firebase Realtime Database under "imageLabels/<imageName>/".
 * (If you want to move labels to Supabase too, swap the Firebase block for a
 *  Supabase REST call to the "labels" table — see SupabaseClient for the base URL.)
 */
public class ImageLabelingActivity extends AppCompatActivity {

    private TouchImageView imageView;
    private Button addLabelButton, saveLabelButton, clearLabelsButton, backButton;
    private String currentLabelName = null;
    private String imageName = "unknown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_labeling);

        imageView        = findViewById(R.id.imageView);
        addLabelButton   = findViewById(R.id.addLabelButton);
        saveLabelButton  = findViewById(R.id.saveLabelButton);
        clearLabelsButton = findViewById(R.id.clearLabelsButton);
        backButton       = findViewById(R.id.backButton);

        // ── Load image from Supabase URL ──────────────────────────────────────
        String imageUrl = getIntent().getStringExtra("imageUrl");
        imageName       = getIntent().getStringExtra("imageName");
        if (imageName == null) imageName = "unknown";

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_close_clear_cancel)
                    .into(imageView);
        } else {
            Toast.makeText(this, "No image URL provided", Toast.LENGTH_SHORT).show();
        }

        // ── Button listeners ──────────────────────────────────────────────────
        addLabelButton.setOnClickListener(v -> showLabelDialog());
        saveLabelButton.setOnClickListener(v -> saveLabelsToFirebase());
        clearLabelsButton.setOnClickListener(v -> {
            imageView.clearRectangles();
            Toast.makeText(this, "All labels cleared", Toast.LENGTH_SHORT).show();
        });
        backButton.setOnClickListener(v -> finish());
    }

    // ── Label dialog ──────────────────────────────────────────────────────────

    private void showLabelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Label Name");

        final EditText input = new EditText(this);
        input.setHint("e.g. Bedroom, Kitchen…");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            currentLabelName = input.getText().toString().trim();
            if (currentLabelName.isEmpty()) {
                Toast.makeText(this, "Label name cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                imageView.setCurrentLabel(currentLabelName);
                Toast.makeText(this, "Draw rectangle for: " + currentLabelName,
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // ── Save to Firebase ──────────────────────────────────────────────────────

    private void saveLabelsToFirebase() {
        List<TouchImageView.LabeledRect> labels = imageView.getLabeledRects();
        if (labels.isEmpty()) {
            Toast.makeText(this, "No labels to save!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Store under imageLabels/<imageName>/  so each image has its own node
        DatabaseReference labelsRef = FirebaseDatabase.getInstance()
                .getReference("imageLabels")
                .child(imageName.replaceAll("[.#$\\[\\]]", "_")); // sanitise key

        Map<String, Object> data = new HashMap<>();
        int i = 1;
        for (TouchImageView.LabeledRect lr : labels) {
            Map<String, Object> labelData = new HashMap<>();
            labelData.put("label",  lr.label);
            labelData.put("left",   lr.rect.left);
            labelData.put("top",    lr.rect.top);
            labelData.put("right",  lr.rect.right);
            labelData.put("bottom", lr.rect.bottom);
            data.put("Label_" + i, labelData);
            i++;
        }

        labelsRef.setValue(data)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Labels saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}
