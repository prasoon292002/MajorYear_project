package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class ImageSelectionActivity extends AppCompatActivity {

    private ImageView image1, image2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_selection);

        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);

        // Click listeners
        image1.setOnClickListener(v -> openLabeling("image1"));
        image2.setOnClickListener(v -> openLabeling("image2"));
    }

    private void openLabeling(String imageName) {
        Intent intent = new Intent(this, ImageLabelingActivity.class);
        intent.putExtra("imageName", imageName);
        startActivity(intent);
    }
}