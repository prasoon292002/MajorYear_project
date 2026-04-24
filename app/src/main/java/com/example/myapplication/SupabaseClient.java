package com.example.myapplication;

/**
 * Supabase configuration helper.
 *
 * Replace the two constants below with your own project values:
 *   SUPABASE_URL  → Project URL from Supabase Dashboard → Settings → API
 *   SUPABASE_KEY  → "anon / public" key from the same page
 *
 * Storage bucket: create a PUBLIC bucket called "images" in
 *   Supabase Dashboard → Storage → New bucket → name "images", toggle Public ON.
 *
 * Upload images there and the app will list + load them automatically.
 */
public class SupabaseClient {

    // ─────────────────────────────────────────────────────────────────────────
    // 🔑  REPLACE THESE WITH YOUR PROJECT VALUES
    public static final String SUPABASE_URL = "https://owvbrvelyswadsrhkzwa.supabase.co";
    public static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im93dmJydmVseXN3YWRzcmhrendhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzcwMDY0MDcsImV4cCI6MjA5MjU4MjQwN30.BFaj3QHqnyyJ-1GXTsh1F-p9Gi3ajw9LIXzEnIeZFc0";
    // ─────────────────────────────────────────────────────────────────────────

    private static final String BUCKET_NAME = "images";

    /** Base URL for Storage REST calls (list / download). */
    public static String storageBaseUrl() {
        return SUPABASE_URL + "/storage/v1";
    }

    /** URL to list all objects inside the images bucket. */
    public static String listImagesUrl() {
        return storageBaseUrl() + "/object/list/" + BUCKET_NAME;
    }

    /** Public URL to download a specific file from the images bucket. */
    public static String publicImageUrl(String fileName) {
        return storageBaseUrl() + "/object/public/" + BUCKET_NAME + "/" + fileName;
    }

    /** Authorization header value required for every Supabase API call. */
    public static String authHeader() {
        return "Bearer " + SUPABASE_KEY;
    }
}
