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

public class ImageLabelingActivity extends AppCompatActivity {

    private TouchImageView imageView;
    private Button addLabelButton, saveLabelButton, clearLabelsButton, backButton;
    private String currentLabelName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_labeling);

        imageView = findViewById(R.id.imageView);
        addLabelButton = findViewById(R.id.addLabelButton);
        saveLabelButton = findViewById(R.id.saveLabelButton);
        clearLabelsButton = findViewById(R.id.clearLabelsButton);
        backButton = findViewById(R.id.backButton);

        // 🔹 Fetch image from Firebase
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("demoImageUrl");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String imageUrl = snapshot.getValue(String.class);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(ImageLabelingActivity.this)
                            .load(imageUrl)
                            .into(imageView);
                } else {
                    Toast.makeText(ImageLabelingActivity.this, "No image URL found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ImageLabelingActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // 🔹 Add Label button
        addLabelButton.setOnClickListener(v -> showLabelDialog());

        // 🔹 Save Labels button
        saveLabelButton.setOnClickListener(v -> saveLabelsToFirebase());

        // 🔹 Clear all labels
        clearLabelsButton.setOnClickListener(v -> {
            imageView.clearRectangles();
            Toast.makeText(this, "All labels cleared", Toast.LENGTH_SHORT).show();
        });

        // 🔹 Back button
        backButton.setOnClickListener(v -> finish());
    }

    private void showLabelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Label Name");

        final EditText input = new EditText(this);
        input.setHint("e.g. Bedroom, Kitchen...");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            currentLabelName = input.getText().toString().trim();
            if (currentLabelName.isEmpty()) {
                Toast.makeText(this, "Label name cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                imageView.setCurrentLabel(currentLabelName);
                Toast.makeText(this, "Now draw rectangle for: " + currentLabelName, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveLabelsToFirebase() {
        List<TouchImageView.LabeledRect> labels = imageView.getLabeledRects();
        if (labels.isEmpty()) {
            Toast.makeText(this, "No labels to save!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference labelsRef = FirebaseDatabase.getInstance().getReference("imageLabels");
        Map<String, Object> data = new HashMap<>();

        int i = 1;
        for (TouchImageView.LabeledRect lr : labels) {
            Map<String, Object> labelData = new HashMap<>();
            labelData.put("label", lr.label);
            labelData.put("left", lr.rect.left);
            labelData.put("top", lr.rect.top);
            labelData.put("right", lr.rect.right);
            labelData.put("bottom", lr.rect.bottom);
            data.put("Label_" + i, labelData);
            i++;
        }

        labelsRef.setValue(data).addOnSuccessListener(unused ->
                Toast.makeText(this, "Labels saved to Firebase", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->
                Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }
}
