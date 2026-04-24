package com.example.myapplication;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import java.util.Locale;

/**
 * Settings screen — every control is now fully functional.
 *
 * Settings are persisted via AppSettings (SharedPreferences) and can be
 * read from any other Activity using AppSettings.get(context).<getter>().
 *
 * What each setting does
 * ─────────────────────────────────────────────────────────────────────
 * Voice Speed / Pitch   → applied to the TTS engine in real time
 * Voice Feedback        → gates all TTS output throughout the app
 * Confidence Threshold  → stored; ImageLabelingActivity reads it to
 *                         decide whether to auto-announce a detection
 * Auto Speak            → when OFF the app never speaks proactively,
 *                         even if Voice Feedback is ON
 * Show Confidence       → stored; label overlay shows/hides the score
 * High Contrast         → toggles a dark background on this screen as
 *                         a live preview; other screens read the flag
 *                         from AppSettings and apply it in onResume()
 */
public class SettingsActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private AppSettings  settings;
    private boolean      ttsReady = false;

    // Voice
    private SeekBar      voiceSpeedBar, voicePitchBar;
    private TextView     voiceSpeedValue, voicePitchValue;
    private SwitchCompat voiceFeedbackSwitch;

    // Detection
    private SeekBar      confidenceBar;
    private TextView     confidenceValue;
    private SwitchCompat autoSpeakSwitch;

    // Display
    private SwitchCompat showConfidenceSwitch;
    private SwitchCompat highContrastSwitch;

    // UI
    private ImageView backButton;
    private CardView  testVoiceCard;
    private View      rootLayout;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settings = AppSettings.get(this);
        tts      = new TextToSpeech(this, this);

        bindViews();
        loadSettingsIntoUI();
        setupListeners();
        applyHighContrast(settings.isHighContrast());
    }

    @Override
    protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        speak("Returning to main screen");
        super.onBackPressed();
    }

    // ── TTS init ──────────────────────────────────────────────────────────────

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.setSpeechRate(settings.getVoiceSpeed());
                tts.setPitch(settings.getVoicePitch());
                ttsReady = true;
                speak("Settings screen opened");
            }
        }
    }

    // ── View binding ─────────────────────────────────────────────────────────

    private void bindViews() {
        rootLayout         = findViewById(R.id.settingsRootLayout);

        voiceSpeedBar      = findViewById(R.id.voice_speed_seekbar);
        voicePitchBar      = findViewById(R.id.voice_pitch_seekbar);
        voiceSpeedValue    = findViewById(R.id.voice_speed_value);
        voicePitchValue    = findViewById(R.id.voice_pitch_value);
        voiceFeedbackSwitch= findViewById(R.id.voice_feedback_switch);

        confidenceBar      = findViewById(R.id.confidence_seekbar);
        confidenceValue    = findViewById(R.id.confidence_value);
        autoSpeakSwitch    = findViewById(R.id.auto_speak_switch);

        showConfidenceSwitch = findViewById(R.id.show_confidence_switch);
        highContrastSwitch   = findViewById(R.id.high_contrast_switch);

        backButton         = findViewById(R.id.back_button);
        testVoiceCard      = findViewById(R.id.test_voice_card);
    }

    // ── Load current values → UI ──────────────────────────────────────────────

    private void loadSettingsIntoUI() {
        float speed = settings.getVoiceSpeed();
        float pitch = settings.getVoicePitch();
        int   conf  = settings.getConfidenceThreshold();

        // SeekBars: speed/pitch mapped 0.5–1.5 → 0–100
        voiceSpeedBar.setProgress((int) ((speed - 0.5f) * 100));
        voicePitchBar.setProgress((int) ((pitch - 0.5f) * 100));
        // Confidence 0–100
        confidenceBar.setProgress(conf);

        voiceFeedbackSwitch.setChecked(settings.isVoiceFeedbackEnabled());
        autoSpeakSwitch.setChecked(settings.isAutoSpeakEnabled());
        showConfidenceSwitch.setChecked(settings.isShowConfidence());
        highContrastSwitch.setChecked(settings.isHighContrast());

        updateSpeedLabel(speed);
        updatePitchLabel(pitch);
        updateConfidenceLabel(conf);
    }

    // ── Listeners ─────────────────────────────────────────────────────────────

    private void setupListeners() {

        // Back
        backButton.setOnClickListener(v -> {
            speak("Returning to main screen");
            finish();
        });

        // Test voice
        testVoiceCard.setOnClickListener(v ->
            speak("This is a test of your current voice settings. " +
                  "The quick brown fox jumps over the lazy dog."));

        // ── Voice Speed ────────────────────────────────────────────────────
        voiceSpeedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                float speed = 0.5f + (p / 100.0f);
                settings.setVoiceSpeed(speed);
                updateSpeedLabel(speed);
                if (ttsReady) tts.setSpeechRate(speed);  // live preview
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {
                speak("Voice speed set to " + String.format("%.1f", settings.getVoiceSpeed()));
            }
        });

        // ── Voice Pitch ────────────────────────────────────────────────────
        voicePitchBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                float pitch = 0.5f + (p / 100.0f);
                settings.setVoicePitch(pitch);
                updatePitchLabel(pitch);
                if (ttsReady) tts.setPitch(pitch);       // live preview
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {
                speak("Pitch set to " + String.format("%.1f", settings.getVoicePitch()));
            }
        });

        // ── Voice Feedback switch ──────────────────────────────────────────
        voiceFeedbackSwitch.setOnCheckedChangeListener((btn, checked) -> {
            settings.setVoiceFeedbackEnabled(checked);
            if (checked) speak("Voice feedback enabled");
            // If turned off: silence falls — no speak() call needed
        });

        // ── Confidence threshold ───────────────────────────────────────────
        confidenceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                settings.setConfidenceThreshold(p);
                updateConfidenceLabel(p);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {
                speak("Detection confidence set to " + settings.getConfidenceThreshold() + " percent");
            }
        });

        // ── Auto Speak ─────────────────────────────────────────────────────
        autoSpeakSwitch.setOnCheckedChangeListener((btn, checked) -> {
            settings.setAutoSpeakEnabled(checked);
            speak(checked ? "Auto speak enabled" : "Auto speak disabled");
        });

        // ── Show Confidence scores ─────────────────────────────────────────
        showConfidenceSwitch.setOnCheckedChangeListener((btn, checked) -> {
            settings.setShowConfidence(checked);
            speak(checked ? "Confidence scores will be shown"
                          : "Confidence scores will be hidden");
        });

        // ── High Contrast mode ─────────────────────────────────────────────
        highContrastSwitch.setOnCheckedChangeListener((btn, checked) -> {
            settings.setHighContrast(checked);
            applyHighContrast(checked);          // live preview on this screen
            speak(checked ? "High contrast mode enabled"
                          : "High contrast mode disabled");
        });
    }

    // ── High contrast: toggle root background as a live demo ─────────────────

    private void applyHighContrast(boolean enabled) {
        if (rootLayout == null) return;
        rootLayout.setBackgroundColor(enabled ? Color.BLACK : Color.parseColor("#F5F5F5"));
    }

    // ── Label helpers ─────────────────────────────────────────────────────────

    private void updateSpeedLabel(float speed) {
        String label = speed < 0.8f ? "Slow"
                     : speed > 1.2f ? "Fast"
                     : "Normal";
        voiceSpeedValue.setText(String.format("%s (%.1fx)", label, speed));
    }

    private void updatePitchLabel(float pitch) {
        String label = pitch < 0.8f ? "Low"
                     : pitch > 1.2f ? "High"
                     : "Normal";
        voicePitchValue.setText(String.format("%s (%.1fx)", label, pitch));
    }

    private void updateConfidenceLabel(int val) {
        String label = val < 40 ? "Low"
                     : val > 70 ? "High"
                     : "Medium";
        confidenceValue.setText(String.format("%s (%d%%)", label, val));
    }

    // ── TTS helper (respects voice-feedback toggle) ────────────────────────────

    private void speak(String text) {
        if (ttsReady && settings.isVoiceFeedbackEnabled()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
}
