package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ImageLabelingActivity extends Activity implements TextToSpeech.OnInitListener {

    private TextToSpeech textToSpeech;
    private ImageView roomImageView;
    private FrameLayout imageContainer;
    private LabelOverlayView overlayView;
    private Button addLabelButton;
    private Button saveLabelButton;
    private Button clearAllButton;
    private Button backButton;
    private TextView instructionText;

    private List<ImageLabel> labels;
    private static final String PREFS_NAME = "ImageLabelsPrefs";
    private static final String LABELS_KEY = "saved_labels";

    // Label data class
    private static class ImageLabel {
        String name;
        float x, y; // Position in pixels
        int id;

        ImageLabel(int id, String name, float x, float y) {
            this.id = id;
            this.name = name;
            this.x = x;
            this.y = y;
        }

        JSONObject toJSON() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("name", name);
            json.put("x", x);
            json.put("y", y);
            return json;
        }

        static ImageLabel fromJSON(JSONObject json) throws JSONException {
            return new ImageLabel(
                    json.getInt("id"),
                    json.getString("name"),
                    (float) json.getDouble("x"),
                    (float) json.getDouble("y")
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_labeling);

        initializeViews();
        initializeTextToSpeech();
        loadLabels();
    }

    private void initializeViews() {
        roomImageView = findViewById(R.id.room_image);
        imageContainer = findViewById(R.id.image_container);
        addLabelButton = findViewById(R.id.add_label_button);
        saveLabelButton = findViewById(R.id.save_label_button);
        clearAllButton = findViewById(R.id.clear_all_button);
        backButton = findViewById(R.id.back_button);
        instructionText = findViewById(R.id.instruction_text);

        labels = new ArrayList<>();

        // Create overlay view for drawing labels
        overlayView = new LabelOverlayView(this);
        imageContainer.addView(overlayView);

        // Set up button listeners
        addLabelButton.setOnClickListener(v -> enableLabelingMode());
        saveLabelButton.setOnClickListener(v -> saveLabels());
        clearAllButton.setOnClickListener(v -> confirmClearAll());
        backButton.setOnClickListener(v -> {
            speakOut("Going back to main menu");
            finish();
        });

        updateInstructions();
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setLanguage(Locale.US);
            speakOut("Image labeling tool ready. Tap add label to start marking objects.");
        }
    }

    private void enableLabelingMode() {
        speakOut("Tap on the image where you want to place a label");
        Toast.makeText(this, "Tap on the image to place a label", Toast.LENGTH_SHORT).show();

        overlayView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                float y = event.getY();
                showLabelInputDialog(x, y);
                return true;
            }
            return false;
        });
    }

    private void showLabelInputDialog(float x, float y) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Object Label");

        final EditText input = new EditText(this);
        input.setHint("e.g., Sofa, Table, Chair");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String labelName = input.getText().toString().trim();
            if (!labelName.isEmpty()) {
                int newId = labels.size() + 1;
                ImageLabel newLabel = new ImageLabel(newId, labelName, x, y);
                labels.add(newLabel);
                overlayView.invalidate();
                speakOut(labelName + " labeled");
                Toast.makeText(this, "Label added: " + labelName, Toast.LENGTH_SHORT).show();
                updateInstructions();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveLabels() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            JSONArray jsonArray = new JSONArray();
            for (ImageLabel label : labels) {
                jsonArray.put(label.toJSON());
            }

            editor.putString(LABELS_KEY, jsonArray.toString());
            editor.apply();

            speakOut(labels.size() + " labels saved successfully");
            Toast.makeText(this, "Labels saved: " + labels.size() + " objects", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Toast.makeText(this, "Error saving labels", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadLabels() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String jsonString = prefs.getString(LABELS_KEY, "");

            if (!jsonString.isEmpty()) {
                JSONArray jsonArray = new JSONArray(jsonString);
                labels.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    labels.add(ImageLabel.fromJSON(jsonObject));
                }

                if (!labels.isEmpty()) {
                    Toast.makeText(this, "Loaded " + labels.size() + " saved labels", Toast.LENGTH_SHORT).show();
                    overlayView.invalidate();
                    updateInstructions();
                }
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Error loading labels", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmClearAll() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Labels")
                .setMessage("Are you sure you want to delete all labels?")
                .setPositiveButton("Yes", (dialog, which) -> clearAllLabels())
                .setNegativeButton("No", null)
                .show();
    }

    private void clearAllLabels() {
        labels.clear();
        overlayView.invalidate();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(LABELS_KEY).apply();

        speakOut("All labels cleared");
        Toast.makeText(this, "All labels cleared", Toast.LENGTH_SHORT).show();
        updateInstructions();
    }

    private void updateInstructions() {
        if (labels.isEmpty()) {
            instructionText.setText("No labels yet. Tap 'Add Label' to start marking objects in the room.");
        } else {
            instructionText.setText(labels.size() + " objects labeled. Tap 'Add Label' to add more.");
        }
    }

    // Custom overlay view for drawing labels
    private class LabelOverlayView extends View {
        private Paint labelPaint;
        private Paint textPaint;
        private Paint circlePaint;

        public LabelOverlayView(Activity context) {
            super(context);

            labelPaint = new Paint();
            labelPaint.setColor(Color.WHITE);
            labelPaint.setStyle(Paint.Style.FILL);
            labelPaint.setTextSize(32);
            labelPaint.setAntiAlias(true);
            labelPaint.setShadowLayer(4, 2, 2, Color.BLACK);

            textPaint = new Paint();
            textPaint.setColor(Color.BLACK);
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setTextSize(32);
            textPaint.setAntiAlias(true);

            circlePaint = new Paint();
            circlePaint.setColor(Color.YELLOW);
            circlePaint.setStyle(Paint.Style.FILL);
            circlePaint.setAntiAlias(true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // Draw all labels
            for (ImageLabel label : labels) {
                // Draw marker circle
                canvas.drawCircle(label.x, label.y, 20, circlePaint);

                // Draw border around circle
                Paint borderPaint = new Paint();
                borderPaint.setColor(Color.BLACK);
                borderPaint.setStyle(Paint.Style.STROKE);
                borderPaint.setStrokeWidth(3);
                borderPaint.setAntiAlias(true);
                canvas.drawCircle(label.x, label.y, 20, borderPaint);

                // Draw label text with background
                String text = label.name;
                float textWidth = textPaint.measureText(text);
                float textHeight = textPaint.getTextSize();

                // Draw white background for text
                Paint bgPaint = new Paint();
                bgPaint.setColor(Color.WHITE);
                bgPaint.setStyle(Paint.Style.FILL);
                bgPaint.setShadowLayer(4, 2, 2, Color.BLACK);
                canvas.drawRect(
                        label.x - textWidth/2 - 10,
                        label.y - textHeight - 30,
                        label.x + textWidth/2 + 10,
                        label.y - 25,
                        bgPaint
                );

                // Draw text
                canvas.drawText(
                        text,
                        label.x - textWidth/2,
                        label.y - 30,
                        textPaint
                );

                // Draw ID number on circle
                Paint idPaint = new Paint();
                idPaint.setColor(Color.BLACK);
                idPaint.setTextSize(24);
                idPaint.setAntiAlias(true);
                idPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(
                        String.valueOf(label.id),
                        label.x,
                        label.y + 8,
                        idPaint
                );
            }
        }
    }

    private void speakOut(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}