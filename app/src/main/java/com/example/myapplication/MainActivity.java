package com.example.myapplication;

import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.os.Handler;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextToSpeech textToSpeech;
    private CardView imageLabelingCard;
    private CardView settingsCard;
    private CardView headerCard;
    private CardView infoCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textToSpeech = new TextToSpeech(this, this);

        // Initialize views
        imageLabelingCard = findViewById(R.id.image_labeling_card);
        settingsCard = findViewById(R.id.settings_card);
        headerCard = findViewById(R.id.header_card);
        infoCard = findViewById(R.id.info_card);

        // Animate cards on startup
        animateCardsOnStartup();

        // Setup click listeners with animations
        setupImageLabelingCard();
        setupSettingsCard();
    }

    private void animateCardsOnStartup() {
        // Animate header card
        animateCardEntry(headerCard, 0);
        
        // Animate feature cards with staggered delay
        animateCardEntry(imageLabelingCard, 100);
        animateCardEntry(settingsCard, 200);
        animateCardEntry(infoCard, 300);
    }

    private void animateCardEntry(View view, long delay) {
        view.setAlpha(0f);
        view.setTranslationY(50f);
        
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(delay)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .start();
    }

    private void setupImageLabelingCard() {
        imageLabelingCard.setOnClickListener(v -> {
            animateCardClick(v);
            speakOut("Opening image labeling tool");
            
            // Delay navigation to show animation
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, ImageLabelingActivity.class);
                startActivity(intent);
            }, 200);
        });

        imageLabelingCard.setOnLongClickListener(v -> {
            animateCardPulse(v);
            speakOut("Image Labeling: This tool allows you to label objects in a room for recognition. Point your camera at objects and the app will identify them for you.");
            return true;
        });
    }

    private void setupSettingsCard() {
        settingsCard.setOnClickListener(v -> {
            animateCardClick(v);
            speakOut("Opening settings");
            
            // Delay navigation to show animation
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }, 200);
        });

        settingsCard.setOnLongClickListener(v -> {
            animateCardPulse(v);
            speakOut("Settings: Customize your experience, adjust voice settings, manage preferences, and configure accessibility options.");
            return true;
        });
    }

    private void animateCardClick(View view) {
        // Scale down and back up animation
        ScaleAnimation scaleDown = new ScaleAnimation(
            1.0f, 0.95f,
            1.0f, 0.95f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleDown.setDuration(100);
        scaleDown.setFillAfter(false);

        ScaleAnimation scaleUp = new ScaleAnimation(
            0.95f, 1.0f,
            0.95f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleUp.setDuration(100);
        scaleUp.setStartOffset(100);

        view.startAnimation(scaleDown);
        view.startAnimation(scaleUp);
    }

    private void animateCardPulse(View view) {
        // Pulse animation for long press
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.05f, 1f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(400);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            
            if (result == TextToSpeech.LANG_MISSING_DATA || 
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Handle language not supported
            } else {
                // Welcome message with slight delay for better UX
                new Handler().postDelayed(() -> {
                    speakOut("Welcome to Smart Assistive Glasses. Tap any feature to begin, or long press for details.");
                }, 800);
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

    @Override
    protected void onResume() {
        super.onResume();
        // Re-animate cards when returning to this activity
        if (imageLabelingCard != null) {
            animateCardEntry(imageLabelingCard, 0);
            animateCardEntry(settingsCard, 50);
        }
    }
}
