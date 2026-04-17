package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextToSpeech textToSpeech;
    private SharedPreferences sharedPreferences;
    
    // Voice Settings
    private SeekBar voiceSpeedSeekBar;
    private SeekBar voicePitchSeekBar;
    private TextView voiceSpeedValue;
    private TextView voicePitchValue;
    private SwitchCompat voiceFeedbackSwitch;
    
    // Detection Settings
    private SeekBar confidenceSeekBar;
    private TextView confidenceValue;
    private SwitchCompat autoSpeakSwitch;
    
    // Display Settings
    private SwitchCompat showConfidenceSwitch;
    private SwitchCompat highContrastSwitch;
    
    // UI Elements
    private ImageView backButton;
    private CardView testVoiceCard;
    
    // Settings values
    private float voiceSpeed = 1.0f;
    private float voicePitch = 1.0f;
    private int confidenceThreshold = 60;
    private boolean voiceFeedbackEnabled = true;
    private boolean autoSpeakEnabled = true;
    private boolean showConfidenceEnabled = true;
    private boolean highContrastEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, this);
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SmartGlassesSettings", MODE_PRIVATE);
        
        // Initialize views
        initializeViews();
        
        // Load saved settings
        loadSettings();
        
        // Setup listeners
        setupListeners();
    }

    private void initializeViews() {
        // Voice Settings
        voiceSpeedSeekBar = findViewById(R.id.voice_speed_seekbar);
        voicePitchSeekBar = findViewById(R.id.voice_pitch_seekbar);
        voiceSpeedValue = findViewById(R.id.voice_speed_value);
        voicePitchValue = findViewById(R.id.voice_pitch_value);
        voiceFeedbackSwitch = findViewById(R.id.voice_feedback_switch);
        
        // Detection Settings
        confidenceSeekBar = findViewById(R.id.confidence_seekbar);
        confidenceValue = findViewById(R.id.confidence_value);
        autoSpeakSwitch = findViewById(R.id.auto_speak_switch);
        
        // Display Settings
        showConfidenceSwitch = findViewById(R.id.show_confidence_switch);
        highContrastSwitch = findViewById(R.id.high_contrast_switch);
        
        // UI Elements
        backButton = findViewById(R.id.back_button);
        testVoiceCard = findViewById(R.id.test_voice_card);
    }

    private void loadSettings() {
        // Load Voice Settings
        voiceSpeed = sharedPreferences.getFloat("voice_speed", 1.0f);
        voicePitch = sharedPreferences.getFloat("voice_pitch", 1.0f);
        voiceFeedbackEnabled = sharedPreferences.getBoolean("voice_feedback", true);
        
        // Load Detection Settings
        confidenceThreshold = sharedPreferences.getInt("confidence_threshold", 60);
        autoSpeakEnabled = sharedPreferences.getBoolean("auto_speak", true);
        
        // Load Display Settings
        showConfidenceEnabled = sharedPreferences.getBoolean("show_confidence", true);
        highContrastEnabled = sharedPreferences.getBoolean("high_contrast", false);
        
        // Update UI
        voiceSpeedSeekBar.setProgress((int)((voiceSpeed - 0.5f) * 100));
        voicePitchSeekBar.setProgress((int)((voicePitch - 0.5f) * 100));
        voiceFeedbackSwitch.setChecked(voiceFeedbackEnabled);
        
        confidenceSeekBar.setProgress(confidenceThreshold);
        autoSpeakSwitch.setChecked(autoSpeakEnabled);
        
        showConfidenceSwitch.setChecked(showConfidenceEnabled);
        highContrastSwitch.setChecked(highContrastEnabled);
        
        updateVoiceSpeedText();
        updateVoicePitchText();
        updateConfidenceText();
    }

    private void setupListeners() {
        // Back button
        backButton.setOnClickListener(v -> {
            speakOut("Returning to main screen");
            finish();
        });
        
        // Voice Speed SeekBar
        voiceSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                voiceSpeed = 0.5f + (progress / 100.0f);
                updateVoiceSpeedText();
                if (textToSpeech != null) {
                    textToSpeech.setSpeechRate(voiceSpeed);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                saveSettings();
                if (voiceFeedbackEnabled) {
                    speakOut("Voice speed set to " + String.format("%.1f", voiceSpeed));
                }
            }
        });
        
        // Voice Pitch SeekBar
        voicePitchSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                voicePitch = 0.5f + (progress / 100.0f);
                updateVoicePitchText();
                if (textToSpeech != null) {
                    textToSpeech.setPitch(voicePitch);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                saveSettings();
                if (voiceFeedbackEnabled) {
                    speakOut("Voice pitch set to " + String.format("%.1f", voicePitch));
                }
            }
        });
        
        // Voice Feedback Switch
        voiceFeedbackSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            voiceFeedbackEnabled = isChecked;
            saveSettings();
            if (isChecked) {
                speakOut("Voice feedback enabled");
            }
        });
        
        // Confidence SeekBar
        confidenceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                confidenceThreshold = progress;
                updateConfidenceText();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                saveSettings();
                if (voiceFeedbackEnabled) {
                    speakOut("Detection confidence set to " + confidenceThreshold + " percent");
                }
            }
        });
        
        // Auto Speak Switch
        autoSpeakSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            autoSpeakEnabled = isChecked;
            saveSettings();
            if (voiceFeedbackEnabled) {
                speakOut(isChecked ? "Auto speak enabled" : "Auto speak disabled");
            }
        });
        
        // Show Confidence Switch
        showConfidenceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showConfidenceEnabled = isChecked;
            saveSettings();
            if (voiceFeedbackEnabled) {
                speakOut(isChecked ? "Confidence scores will be shown" : "Confidence scores will be hidden");
            }
        });
        
        // High Contrast Switch
        highContrastSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            highContrastEnabled = isChecked;
            saveSettings();
            if (voiceFeedbackEnabled) {
                speakOut(isChecked ? "High contrast mode enabled" : "High contrast mode disabled");
            }
        });
        
        // Test Voice Button
        testVoiceCard.setOnClickListener(v -> {
            speakOut("This is a test of your current voice settings. The quick brown fox jumps over the lazy dog.");
        });
    }

    private void updateVoiceSpeedText() {
        String speedText;
        if (voiceSpeed < 0.8f) {
            speedText = String.format("Slow (%.1fx)", voiceSpeed);
        } else if (voiceSpeed > 1.2f) {
            speedText = String.format("Fast (%.1fx)", voiceSpeed);
        } else {
            speedText = String.format("Normal (%.1fx)", voiceSpeed);
        }
        voiceSpeedValue.setText(speedText);
    }

    private void updateVoicePitchText() {
        String pitchText;
        if (voicePitch < 0.8f) {
            pitchText = String.format("Low (%.1fx)", voicePitch);
        } else if (voicePitch > 1.2f) {
            pitchText = String.format("High (%.1fx)", voicePitch);
        } else {
            pitchText = String.format("Normal (%.1fx)", voicePitch);
        }
        voicePitchValue.setText(pitchText);
    }

    private void updateConfidenceText() {
        String confidenceText;
        if (confidenceThreshold < 40) {
            confidenceText = String.format("Low (%d%%)", confidenceThreshold);
        } else if (confidenceThreshold > 70) {
            confidenceText = String.format("High (%d%%)", confidenceThreshold);
        } else {
            confidenceText = String.format("Medium (%d%%)", confidenceThreshold);
        }
        confidenceValue.setText(confidenceText);
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        
        // Save Voice Settings
        editor.putFloat("voice_speed", voiceSpeed);
        editor.putFloat("voice_pitch", voicePitch);
        editor.putBoolean("voice_feedback", voiceFeedbackEnabled);
        
        // Save Detection Settings
        editor.putInt("confidence_threshold", confidenceThreshold);
        editor.putBoolean("auto_speak", autoSpeakEnabled);
        
        // Save Display Settings
        editor.putBoolean("show_confidence", showConfidenceEnabled);
        editor.putBoolean("high_contrast", highContrastEnabled);
        
        editor.apply();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            
            if (result == TextToSpeech.LANG_MISSING_DATA || 
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Handle language not supported
            } else {
                // Apply saved settings
                textToSpeech.setSpeechRate(voiceSpeed);
                textToSpeech.setPitch(voicePitch);
                
                if (voiceFeedbackEnabled) {
                    speakOut("Settings screen opened");
                }
            }
        }
    }

    private void speakOut(String text) {
        if (textToSpeech != null && voiceFeedbackEnabled) {
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
    public void onBackPressed() {
        speakOut("Returning to main screen");
        super.onBackPressed();
    }
}
