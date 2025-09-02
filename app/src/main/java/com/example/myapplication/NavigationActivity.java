package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

public class NavigationActivity extends Activity implements TextToSpeech.OnInitListener {

    private TextToSpeech textToSpeech;
    private TextView instructionText;
    private TextView stepCounter;
    private ImageView directionArrow;
    private Button nextButton;
    private Button backButton;

    // Simple demo steps (no backend logic)
    private String[] instructions = {
            "Welcome to navigation demo",
            "Walk forward 3 steps",
            "Turn right at the sofa",
            "Continue straight 5 steps",
            "Turn left toward the door",
            "Destination reached!"
    };

    private int currentStep = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        initializeViews();
        initializeTextToSpeech();
        showCurrentStep();
    }

    private void initializeViews() {
        instructionText = findViewById(R.id.instruction_text);
        stepCounter = findViewById(R.id.step_counter);
        directionArrow = findViewById(R.id.direction_arrow);
        nextButton = findViewById(R.id.next_button);
        backButton = findViewById(R.id.back_button);

        nextButton.setOnClickListener(v -> nextStep());
        backButton.setOnClickListener(v -> {
            speakOut("Going back to main menu");
            finish();
        });
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setLanguage(Locale.US);
            speakOut(instructions[currentStep]);
        }
    }

    private void nextStep() {
        currentStep++;
        if (currentStep >= instructions.length) {
            currentStep = 0; // Restart demo
        }
        showCurrentStep();
        speakOut(instructions[currentStep]);
    }

    private void showCurrentStep() {
        instructionText.setText(instructions[currentStep]);
        stepCounter.setText("Step " + (currentStep + 1) + " of " + instructions.length);

        // Update arrow based on instruction
        updateArrow(instructions[currentStep]);

        // Update button text
        if (currentStep == instructions.length - 1) {
            nextButton.setText("Restart Demo");
        } else {
            nextButton.setText("Next Step");
        }
    }

    private void updateArrow(String instruction) {
        int rotation = 0;

        if (instruction.toLowerCase().contains("forward") ||
                instruction.toLowerCase().contains("straight")) {
            rotation = 0; // Up
        } else if (instruction.toLowerCase().contains("right")) {
            rotation = 90; // Right
        } else if (instruction.toLowerCase().contains("left")) {
            rotation = 270; // Left
        } else {
            rotation = 0; // Default up
        }

        directionArrow.setRotation(rotation);
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