package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView titleText;
    private TextView descriptionText;
    private Button map3DButton;
    private Button navigationButton;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeTextToSpeech();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        titleText = findViewById(R.id.title_text);
        descriptionText = findViewById(R.id.description_text);
        map3DButton = findViewById(R.id.map_3d_button);
        navigationButton = findViewById(R.id.navigation_button);

        // Set up UI text
        titleText.setText("Smart Assistive Glasses");
        descriptionText.setText("Indoor Navigation for Visually Impaired");

        // Button click listeners
        map3DButton.setOnClickListener(v -> {
            speakOut("Opening 3D room visualization");
            Intent intent = new Intent(MainActivity.this, com.example.myapplication.Map3DActivity.class);
            startActivity(intent);
        });

        navigationButton.setOnClickListener(v -> {
            speakOut("Opening navigation demo");
            Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
            startActivity(intent);
        });

        // Long press for voice description
        map3DButton.setOnLongClickListener(v -> {
            speakOut("3D Room Mapping - Visualize your indoor environment with obstacles and pathways");
            return true;
        });

        navigationButton.setOnLongClickListener(v -> {
            speakOut("Voice Navigation - Get step by step audio directions to reach your destination");
            return true;
        });
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setLanguage(Locale.US);
            speakOut("Welcome to Smart Assistive Glasses companion app");
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