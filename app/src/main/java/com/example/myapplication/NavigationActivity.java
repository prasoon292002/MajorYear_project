package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;

import java.util.Locale;

public class NavigationActivity extends Activity implements TextToSpeech.OnInitListener {

    private TextToSpeech textToSpeech;
    private Button toggleButton;
    private Button backButton;
    private LinearLayout outsideViewLayout;
    private LinearLayout insideViewLayout;
    private TextView statusText;

    private boolean isOutsideView = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        initializeViews();
        initializeTextToSpeech();
        updateViewState();
    }

    private void initializeViews() {
        toggleButton = findViewById(R.id.toggle_button);
        backButton = findViewById(R.id.back_button);
        outsideViewLayout = findViewById(R.id.outside_view_layout);
        insideViewLayout = findViewById(R.id.inside_view_layout);
        statusText = findViewById(R.id.status_text);

        toggleButton.setOnClickListener(v -> toggleView());
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
            speakOut(isOutsideView ? "Outside view activated" : "Inside view activated");
        }
    }

    private void toggleView() {
        isOutsideView = !isOutsideView;
        updateViewState();

        String announcement = isOutsideView ? "Switched to outside view" : "Switched to inside view";
        speakOut(announcement);
    }

    private void updateViewState() {
        if (isOutsideView) {
            // Show outside view
            outsideViewLayout.setVisibility(View.VISIBLE);
            insideViewLayout.setVisibility(View.GONE);
            toggleButton.setText("Switch to Inside View");
            statusText.setText("Outside View Active");
        } else {
            // Show inside view
            outsideViewLayout.setVisibility(View.GONE);
            insideViewLayout.setVisibility(View.VISIBLE);
            toggleButton.setText("Switch to Outside View");
            statusText.setText("Inside View Active");
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
