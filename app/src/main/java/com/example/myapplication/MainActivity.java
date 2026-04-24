package com.example.myapplication;

import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private boolean      ttsReady  = false;
    private AppSettings  settings;

    private ConstraintLayout rootLayout;   // top-level layout for high-contrast
    private CardView imageLabelingCard;
    private CardView settingsCard;
    private CardView headerCard;
    private CardView infoCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = AppSettings.get(this);
        tts      = new TextToSpeech(this, this);

        rootLayout        = findViewById(R.id.mainRootLayout);
        imageLabelingCard = findViewById(R.id.image_labeling_card);
        settingsCard      = findViewById(R.id.settings_card);
        headerCard        = findViewById(R.id.header_card);
        infoCard          = findViewById(R.id.info_card);

        animateCardsOnStartup();
        setupImageLabelingCard();
        setupSettingsCard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-read settings in case user changed them
        applyHighContrast(settings.isHighContrast());

        if (imageLabelingCard != null) {
            animateCardEntry(imageLabelingCard, 0);
            animateCardEntry(settingsCard, 50);
        }
    }

    // ── High contrast ─────────────────────────────────────────────────────────

    private void applyHighContrast(boolean enabled) {
        if (rootLayout != null) {
            rootLayout.setBackgroundColor(
                    enabled ? Color.BLACK : Color.parseColor("#F0F4F8"));
        }
    }

    // ── TTS ───────────────────────────────────────────────────────────────────

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.setSpeechRate(settings.getVoiceSpeed());
                tts.setPitch(settings.getVoicePitch());
                ttsReady = true;
                new Handler().postDelayed(() ->
                    speak("Welcome to Smart Assistive Glasses. " +
                          "Tap any feature to begin, or long press for details."), 800);
            }
        }
    }

    private void speak(String text) {
        if (ttsReady && settings.isVoiceFeedbackEnabled() && settings.isAutoSpeakEnabled()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }

    // ── Card setup ────────────────────────────────────────────────────────────

    private void setupImageLabelingCard() {
        imageLabelingCard.setOnClickListener(v -> {
            animateCardClick(v);
            speak("Opening image labeling tool");
            new Handler().postDelayed(() -> {
                startActivity(new Intent(this, ImageSelectionActivity.class));
            }, 200);
        });

        imageLabelingCard.setOnLongClickListener(v -> {
            animateCardPulse(v);
            speak("Image Labeling: Select an image from Supabase and draw labels on objects.");
            return true;
        });
    }

    private void setupSettingsCard() {
        settingsCard.setOnClickListener(v -> {
            animateCardClick(v);
            speak("Opening settings");
            new Handler().postDelayed(() -> {
                startActivity(new Intent(this, SettingsActivity.class));
            }, 200);
        });

        settingsCard.setOnLongClickListener(v -> {
            animateCardPulse(v);
            speak("Settings: Adjust voice, detection confidence, contrast, and more.");
            return true;
        });
    }

    // ── Animations ────────────────────────────────────────────────────────────

    private void animateCardsOnStartup() {
        animateCardEntry(headerCard, 0);
        animateCardEntry(imageLabelingCard, 100);
        animateCardEntry(settingsCard, 200);
        animateCardEntry(infoCard, 300);
    }

    private void animateCardEntry(View view, long delay) {
        view.setAlpha(0f);
        view.setTranslationY(50f);
        view.animate()
            .alpha(1f).translationY(0f)
            .setDuration(600).setStartDelay(delay)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .start();
    }

    private void animateCardClick(View view) {
        ScaleAnimation down = new ScaleAnimation(1f, 0.95f, 1f, 0.95f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        down.setDuration(100);

        ScaleAnimation up = new ScaleAnimation(0.95f, 1f, 0.95f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        up.setDuration(100);
        up.setStartOffset(100);

        view.startAnimation(down);
        view.startAnimation(up);
    }

    private void animateCardPulse(View view) {
        ObjectAnimator sx = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator sy = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.05f, 1f);
        AnimatorSet set   = new AnimatorSet();
        set.playTogether(sx, sy);
        set.setDuration(400);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();
    }
}
