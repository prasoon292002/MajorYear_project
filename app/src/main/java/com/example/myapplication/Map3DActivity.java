package com.example.myapplication;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Map3DActivity extends Activity implements TextToSpeech.OnInitListener {

    private TextToSpeech textToSpeech;
    private TextView statusText;
    private Button backButton;
    private Button analyzeButton;
    private FrameLayout cameraLayout;
    private DemoOverlayView overlayView;
    private View simulatedCameraView;

    private boolean isAnalyzing = false;
    private Handler analysisHandler;
    private List<DetectedObject> detectedObjects;
    private int analysisStep = 0;

    // Simulated object detection data
    private class DetectedObject {
        String name;
        float distance;
        int x, y, width, height;
        boolean isNew;

        DetectedObject(String name, float distance, int x, int y, int width, int height) {
            this.name = name;
            this.distance = distance;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.isNew = true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3d_map);

        initializeViews();
        initializeTextToSpeech();
        initializeAnalysis();
    }

    private void initializeViews() {
        cameraLayout = findViewById(R.id.camera_layout);
        statusText = findViewById(R.id.info_text);
        backButton = findViewById(R.id.back_button);
        analyzeButton = findViewById(R.id.describe_button);

        statusText.setText("Smart Glasses Demo - Room Analysis\nSimulated camera view with AI detection");
        analyzeButton.setText("Start Analysis");

        backButton.setOnClickListener(v -> {
            speakOut("Going back to main menu");
            finish();
        });

        analyzeButton.setOnClickListener(v -> toggleAnalysis());

        // Create simulated camera background
        simulatedCameraView = findViewById(R.id.simulated_camera);

        // Create overlay view for drawing detection boxes
        overlayView = new DemoOverlayView(this);
        cameraLayout.addView(overlayView);
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, this);
    }

    private void initializeAnalysis() {
        analysisHandler = new Handler();
        detectedObjects = new ArrayList<>();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setLanguage(Locale.US);
            speakOut("Smart glasses demo ready. Simulated room analysis available.");
        }
    }

    private void toggleAnalysis() {
        if (!isAnalyzing) {
            startAnalysis();
        } else {
            stopAnalysis();
        }
    }

    private void startAnalysis() {
        isAnalyzing = true;
        analysisStep = 0;
        detectedObjects.clear();
        analyzeButton.setText("Stop Analysis");
        statusText.setText("🔍 Analyzing Room - AI Detection Active");
        speakOut("Starting room analysis. Scanning for furniture and obstacles.");

        // Start simulated real-time analysis
        runAnalysisLoop();
    }

    private void stopAnalysis() {
        isAnalyzing = false;
        analyzeButton.setText("Start Analysis");
        statusText.setText("Analysis Complete - " + detectedObjects.size() + " objects mapped");
        describeDetectedObjects();
    }

    private void runAnalysisLoop() {
        if (!isAnalyzing) return;

        // Simulate progressive object detection
        simulateObjectDetection();
        overlayView.invalidate(); // Redraw overlay

        analysisStep++;

        // Continue analysis every 2 seconds for 6 steps
        if (analysisStep < 6) {
            analysisHandler.postDelayed(this::runAnalysisLoop, 2000);
        } else {
            // Auto-complete after detecting all objects
            analysisHandler.postDelayed(() -> {
                if (isAnalyzing) stopAnalysis();
            }, 1000);
        }
    }

    private void simulateObjectDetection() {
        String[] objectTypes = {"Sofa", "Coffee Table", "Chair", "TV Stand", "Plant", "Bookshelf"};
        float[] distances = {2.3f, 1.8f, 3.1f, 4.2f, 1.5f, 2.8f};
        int[][] positions = {
                {120, 200, 180, 100}, // Sofa
                {350, 320, 120, 80},  // Coffee Table
                {580, 180, 100, 120}, // Chair
                {200, 400, 160, 90},  // TV Stand
                {500, 450, 60, 80},   // Plant
                {80, 350, 140, 140}   // Bookshelf
        };

        if (analysisStep < objectTypes.length) {
            String objectName = objectTypes[analysisStep];
            float distance = distances[analysisStep];
            int[] pos = positions[analysisStep];

            DetectedObject obj = new DetectedObject(objectName, distance, pos[0], pos[1], pos[2], pos[3]);
            detectedObjects.add(obj);

            speakOut(objectName + " detected at " + String.format("%.1f", distance) + " meters");

            // Update status
            statusText.setText("🔍 Detecting... Found: " + (analysisStep + 1) + "/6 objects");
        }
    }

    private void describeDetectedObjects() {
        if (detectedObjects.isEmpty()) {
            speakOut("No objects detected in current view");
            return;
        }

        StringBuilder description = new StringBuilder("Room mapping complete. Found " + detectedObjects.size() + " objects. ");

        // Find closest object
        DetectedObject closest = detectedObjects.get(0);
        for (DetectedObject obj : detectedObjects) {
            if (obj.distance < closest.distance) {
                closest = obj;
            }
        }

        description.append("Closest object is ").append(closest.name)
                .append(" at ").append(String.format("%.1f", closest.distance)).append(" meters. ");

        description.append("Other objects detected: ");
        for (DetectedObject obj : detectedObjects) {
            if (obj != closest) {
                description.append(obj.name).append(" at ")
                        .append(String.format("%.1f", obj.distance)).append(" meters. ");
            }
        }

        speakOut(description.toString());
    }

    // Custom overlay view for drawing detection boxes
    private class DemoOverlayView extends View {
        private Paint boxPaint;
        private Paint textPaint;
        private Paint scanLinePaint;
        private float scanLineY = 0;

        public DemoOverlayView(Activity context) {
            super(context);

            boxPaint = new Paint();
            boxPaint.setColor(Color.GREEN);
            boxPaint.setStyle(Paint.Style.STROKE);
            boxPaint.setStrokeWidth(3);

            textPaint = new Paint();
            textPaint.setColor(Color.CYAN);
            textPaint.setTextSize(24);
            textPaint.setAntiAlias(true);
            textPaint.setShadowLayer(3, 2, 2, Color.BLACK);

            scanLinePaint = new Paint();
            scanLinePaint.setColor(Color.RED);
            scanLinePaint.setStrokeWidth(2);
            scanLinePaint.setAlpha(150);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // Draw scanning effect when analyzing
            if (isAnalyzing) {
                // Moving scan line
                canvas.drawLine(0, scanLineY, getWidth(), scanLineY, scanLinePaint);
                scanLineY += 5;
                if (scanLineY > getHeight()) scanLineY = 0;

                // Corner brackets to simulate viewfinder
                drawViewfinderCorners(canvas);

                // Redraw for animation
                postInvalidateDelayed(50);
            }

            // Draw detection boxes and labels
            for (DetectedObject obj : detectedObjects) {
                // Draw bounding box
                boxPaint.setColor(obj.isNew ? Color.YELLOW : Color.GREEN);
                Rect rect = new Rect(obj.x, obj.y, obj.x + obj.width, obj.y + obj.height);
                canvas.drawRect(rect, boxPaint);

                // Draw object label
                canvas.drawText(obj.name, obj.x + 5, obj.y - 10, textPaint);
                canvas.drawText(String.format("%.1fm", obj.distance), obj.x + 5, obj.y + obj.height + 25, textPaint);

                // Draw distance line to user position
                int userX = getWidth() / 2;
                int userY = getHeight() - 60;
                int objCenterX = obj.x + obj.width / 2;
                int objCenterY = obj.y + obj.height / 2;

                Paint linePaint = new Paint();
                linePaint.setColor(Color.YELLOW);
                linePaint.setStrokeWidth(2);
                linePaint.setAlpha(120);
                canvas.drawLine(objCenterX, objCenterY, userX, userY, linePaint);

                obj.isNew = false; // Mark as seen
            }

            // Draw user position indicator
            if (!detectedObjects.isEmpty() || isAnalyzing) {
                Paint userPaint = new Paint();
                userPaint.setColor(Color.BLUE);
                userPaint.setStyle(Paint.Style.FILL);
                int centerX = getWidth() / 2;
                int bottomY = getHeight() - 40;
                canvas.drawCircle(centerX, bottomY, 15, userPaint);

                textPaint.setColor(Color.WHITE);
                canvas.drawText("YOU", centerX - 25, bottomY + 35, textPaint);
                textPaint.setColor(Color.CYAN);
            }

            // Draw analysis info
            if (isAnalyzing) {
                textPaint.setColor(Color.RED);
                canvas.drawText("AI SCANNING...", 20, getHeight() - 100, textPaint);
                textPaint.setColor(Color.CYAN);
            }
        }

        private void drawViewfinderCorners(Canvas canvas) {
            Paint cornerPaint = new Paint();
            cornerPaint.setColor(Color.WHITE);
            cornerPaint.setStrokeWidth(3);
            cornerPaint.setStyle(Paint.Style.STROKE);

            int cornerSize = 30;
            int margin = 20;

            // Top left
            canvas.drawLine(margin, margin, margin + cornerSize, margin, cornerPaint);
            canvas.drawLine(margin, margin, margin, margin + cornerSize, cornerPaint);

            // Top right
            canvas.drawLine(getWidth() - margin - cornerSize, margin, getWidth() - margin, margin, cornerPaint);
            canvas.drawLine(getWidth() - margin, margin, getWidth() - margin, margin + cornerSize, cornerPaint);

            // Bottom left
            canvas.drawLine(margin, getHeight() - margin, margin + cornerSize, getHeight() - margin, cornerPaint);
            canvas.drawLine(margin, getHeight() - margin - cornerSize, margin, getHeight() - margin, cornerPaint);

            // Bottom right
            canvas.drawLine(getWidth() - margin - cornerSize, getHeight() - margin, getWidth() - margin, getHeight() - margin, cornerPaint);
            canvas.drawLine(getWidth() - margin, getHeight() - margin - cornerSize, getWidth() - margin, getHeight() - margin, cornerPaint);
        }
    }

    private void speakOut(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        isAnalyzing = false;
        if (analysisHandler != null) {
            analysisHandler.removeCallbacksAndMessages(null);
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
