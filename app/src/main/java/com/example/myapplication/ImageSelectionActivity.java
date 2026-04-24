package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageSelectionActivity extends AppCompatActivity {

    private static final String TAG = "ImageSelection";

    private GridView     gridView;
    private ProgressBar  progressBar;
    private TextView     emptyText;

    private final List<String> imageUrls  = new ArrayList<>();
    private final List<String> imageNames = new ArrayList<>();
    private ImageGridAdapter   adapter;

    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_selection);

        gridView    = findViewById(R.id.imageGrid);
        progressBar = findViewById(R.id.progressBar);
        emptyText   = findViewById(R.id.emptyText);

        findViewById(R.id.refreshButton).setOnClickListener(v -> fetchImagesFromSupabase());

        adapter = new ImageGridAdapter(this, imageUrls);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, ImageLabelingActivity.class);
            intent.putExtra("imageUrl",  imageUrls.get(position));
            intent.putExtra("imageName", imageNames.get(position));
            startActivity(intent);
        });

        fetchImagesFromSupabase();
    }

    private void fetchImagesFromSupabase() {
        showLoading(true);
        emptyText.setVisibility(View.GONE);

        String url  = SupabaseClient.SUPABASE_URL + "/storage/v1/object/list/images";
        String body = "{\"prefix\":\"\",\"limit\":100,\"offset\":0,"
                + "\"sortBy\":{\"column\":\"name\",\"order\":\"asc\"}}";

        Log.d(TAG, "LIST URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + SupabaseClient.SUPABASE_KEY)
                .addHeader("apikey", SupabaseClient.SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body,
                        MediaType.parse("application/json; charset=utf-8")))
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network failure: " + e.getMessage());
                runOnUiThread(() -> {
                    showLoading(false);
                    emptyText.setText("Network error:\n" + e.getMessage());
                    emptyText.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int    code = response.code();
                String rb   = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "HTTP " + code + " → " + rb);

                runOnUiThread(() -> {
                    showLoading(false);
                    if (code == 200) {
                        parseAndDisplay(rb);
                    } else {
                        // Show raw Supabase error so we can diagnose
                        emptyText.setText("Supabase error " + code + ":\n" + rb);
                        emptyText.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    private void parseAndDisplay(String json) {
        try {
            JSONArray array = new JSONArray(json);
            imageUrls.clear();
            imageNames.clear();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String name    = obj.getString("name");
                Log.d(TAG, "Object in bucket: " + name);

                if (name.toLowerCase().matches(".*\\.(jpg|jpeg|png|webp|gif)$")) {
                    imageNames.add(name);
                    String publicUrl = SupabaseClient.SUPABASE_URL
                            + "/storage/v1/object/public/images/" + name;
                    imageUrls.add(publicUrl);
                    Log.d(TAG, "Image URL: " + publicUrl);
                }
            }

            adapter.notifyDataSetChanged();

            if (imageUrls.isEmpty()) {
                // Show count so we know if objects were found but regex didn't match
                emptyText.setText("No image files matched.\nTotal objects in bucket: "
                        + array.length() + "\nCheck Logcat tag: " + TAG);
                emptyText.setVisibility(View.VISIBLE);
            } else {
                emptyText.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Log.e(TAG, "Parse error: " + e.getMessage() + " | raw: " + json);
            emptyText.setText("Parse error: " + e.getMessage()
                    + "\n\nRaw response:\n" + json);
            emptyText.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        gridView.setVisibility(show ? View.GONE   : View.VISIBLE);
    }

    // ── Adapter ───────────────────────────────────────────────────────────────

    static class ImageGridAdapter extends BaseAdapter {
        private final Context      context;
        private final List<String> urls;

        ImageGridAdapter(Context ctx, List<String> urls) {
            this.context = ctx;
            this.urls    = urls;
        }

        @Override public int    getCount()         { return urls.size(); }
        @Override public Object getItem(int pos)   { return urls.get(pos); }
        @Override public long   getItemId(int pos) { return pos; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView iv;
            if (convertView instanceof ImageView) {
                iv = (ImageView) convertView;
            } else {
                iv = new ImageView(context);
                iv.setLayoutParams(new GridView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 300));
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                iv.setPadding(8, 8, 8, 8);
            }

            Glide.with(context)
                    .load(urls.get(position))
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_close_clear_cancel)
                    .into(iv);

            return iv;
        }
    }
}