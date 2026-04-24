package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Singleton that centralises reading/writing of all app settings.
 *
 * Any Activity can call AppSettings.get(context).isHighContrast() etc.
 * without duplicating SharedPreferences keys everywhere.
 */
public class AppSettings {

    private static final String PREFS_NAME = "SmartGlassesSettings";

    // Keys
    private static final String KEY_VOICE_SPEED         = "voice_speed";
    private static final String KEY_VOICE_PITCH         = "voice_pitch";
    private static final String KEY_VOICE_FEEDBACK      = "voice_feedback";
    private static final String KEY_CONFIDENCE          = "confidence_threshold";
    private static final String KEY_AUTO_SPEAK          = "auto_speak";
    private static final String KEY_SHOW_CONFIDENCE     = "show_confidence";
    private static final String KEY_HIGH_CONTRAST       = "high_contrast";

    // Defaults
    public static final float   DEFAULT_VOICE_SPEED     = 1.0f;
    public static final float   DEFAULT_VOICE_PITCH     = 1.0f;
    public static final boolean DEFAULT_VOICE_FEEDBACK  = true;
    public static final int     DEFAULT_CONFIDENCE      = 60;
    public static final boolean DEFAULT_AUTO_SPEAK      = true;
    public static final boolean DEFAULT_SHOW_CONFIDENCE = true;
    public static final boolean DEFAULT_HIGH_CONTRAST   = false;

    private static AppSettings instance;

    private final SharedPreferences prefs;

    private AppSettings(Context context) {
        prefs = context.getApplicationContext()
                       .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized AppSettings get(Context context) {
        if (instance == null) instance = new AppSettings(context);
        return instance;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public float   getVoiceSpeed()         { return prefs.getFloat  (KEY_VOICE_SPEED,     DEFAULT_VOICE_SPEED);     }
    public float   getVoicePitch()         { return prefs.getFloat  (KEY_VOICE_PITCH,     DEFAULT_VOICE_PITCH);     }
    public boolean isVoiceFeedbackEnabled(){ return prefs.getBoolean(KEY_VOICE_FEEDBACK,  DEFAULT_VOICE_FEEDBACK);  }
    public int     getConfidenceThreshold(){ return prefs.getInt    (KEY_CONFIDENCE,      DEFAULT_CONFIDENCE);      }
    public boolean isAutoSpeakEnabled()    { return prefs.getBoolean(KEY_AUTO_SPEAK,      DEFAULT_AUTO_SPEAK);      }
    public boolean isShowConfidence()      { return prefs.getBoolean(KEY_SHOW_CONFIDENCE, DEFAULT_SHOW_CONFIDENCE); }
    public boolean isHighContrast()        { return prefs.getBoolean(KEY_HIGH_CONTRAST,   DEFAULT_HIGH_CONTRAST);   }

    // ── Setters (each saves immediately) ─────────────────────────────────────

    public void setVoiceSpeed(float v)              { prefs.edit().putFloat  (KEY_VOICE_SPEED,     v).apply(); }
    public void setVoicePitch(float v)              { prefs.edit().putFloat  (KEY_VOICE_PITCH,     v).apply(); }
    public void setVoiceFeedbackEnabled(boolean v)  { prefs.edit().putBoolean(KEY_VOICE_FEEDBACK,  v).apply(); }
    public void setConfidenceThreshold(int v)       { prefs.edit().putInt    (KEY_CONFIDENCE,      v).apply(); }
    public void setAutoSpeakEnabled(boolean v)      { prefs.edit().putBoolean(KEY_AUTO_SPEAK,      v).apply(); }
    public void setShowConfidence(boolean v)        { prefs.edit().putBoolean(KEY_SHOW_CONFIDENCE, v).apply(); }
    public void setHighContrast(boolean v)          { prefs.edit().putBoolean(KEY_HIGH_CONTRAST,   v).apply(); }
}
