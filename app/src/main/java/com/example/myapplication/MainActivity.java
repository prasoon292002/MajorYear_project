package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextToSpeech textToSpeech;
    private Button imageLabelingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textToSpeech = new TextToSpeech(this, this);

        imageLabelingButton = findViewById(R.id.image_labeling_button);
        imageLabelingButton.setOnClickListener(v -> {
            speakOut("Opening image labeling tool");
            Intent intent = new Intent(MainActivity.this, ImageLabelingActivity.class);
            startActivity(intent);
        });

        imageLabelingButton.setOnLongClickListener(v -> {
            speakOut("This tool allows you to label objects in a room for recognition");
            return true;
        });
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setLanguage(Locale.US);
            speakOut("Welcome to Smart Assistive Glasses.");
        }
    }

    private void speakOut(String text) {
        if (textToSpeech != null)
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
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
