package com.example.myapplication;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public abstract class Map3DActivity extends Activity implements TextToSpeech.OnInitListener {

//    private GLSurfaceView glSurfaceView;
//    private TextToSpeech textToSpeech;
//    private TextView infoText;
//    private Button backButton;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_3d_map);
//
//        initializeViews();
//        initializeOpenGL();
//        initializeTextToSpeech();
//    }
//
//    private void initializeViews() {
//        glSurfaceView = findViewById(R.id.gl_surface_view);
//        infoText = findViewById(R.id.info_text);
//        backButton = findViewById(R.id.back_button);
//
//        infoText.setText("3D Room Visualization Demo\\nShowing: Living Room Layout");
//
//        backButton.setOnClickListener(v -> {
//            speakOut("Going back to main menu");
//            finish();
//        });
//    }
//
//    private void initializeOpenGL() {
//        glSurfaceView.setEGLContextClientVersion(2);
//        glSurfaceView.setRenderer(new RoomRenderer());
//        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
//    }
//
//    private void initializeTextToSpeech() {
//        textToSpeech = new TextToSpeech(this, this);
//    }
//
//    @Override
//    public void onInit(int status) {
//        if (status == TextToSpeech.SUCCESS) {
//            textToSpeech.setLanguage(Locale.US);
//            speakOut("3D room visualization loaded. This shows a sample living room with furniture.");
//        }
//    }
//
//    private void speakOut(String text) {
//        if (textToSpeech != null) {
//            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (glSurfaceView != null) {
//            glSurfaceView.onPause();
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (glSurfaceView != null) {
//            glSurfaceView.onResume();
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        if (textToSpeech != null) {
//            textToSpeech.stop();
//            textToSpeech.shutdown();
//        }
//        super.onDestroy();
//    }
//
//    // Simple 3D renderer for demo room
//    private class RoomRenderer implements GLSurfaceView.Renderer {
//
//        private float rotationY = 0.0f;
//
//        @Override
//        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//            gl.glClearColor(0.05f, 0.05f, 0.15f, 1.0f);
//            gl.glEnable(GL10.GL_DEPTH_TEST);
//            gl.glEnable(GL10.GL_LINE_SMOOTH);
//        }
//
//        @Override
//        public void onSurfaceChanged(GL10 gl, int width, int height) {
//            gl.glViewport(0, 0, width, height);
//            gl.glMatrixMode(GL10.GL_PROJECTION);
//            gl.glLoadIdentity();
//
//            float ratio = (float) width / height;
//            gl.glFrustumf(-ratio, ratio, -1, 1, 2, 20);
//        }
//
//        @Override
//        public void onDrawFrame(GL10 gl) {
//            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
//            gl.glMatrixMode(GL10.GL_MODELVIEW);
//            gl.glLoadIdentity();
//
//            // Position camera
//            gl.glTranslatef(0.0f, -1.0f, -8.0f);
//            gl.glRotatef(20.0f, 1.0f, 0.0f, 0.0f);
//            gl.glRotatef(rotationY, 0.0f, 1.0f, 0.0f);
//
//            // Draw room
//            drawRoom(gl);
//
//            // Draw furniture
//            drawSofa(gl);
//            drawTable(gl);
//            drawChair(gl);
//
//            // Slow rotation for demo
//            rotationY += 0.5f;
//            if (rotationY >= 360.0f) {
//                rotationY = 0.0f;
//            }
//        }
//
//        private void drawRoom(GL10 gl) {
//            gl.glColor4f(0.7f, 0.7f, 0.8f, 1.0f);
//            gl.glLineWidth(2.0f);
//
//            // Floor outline
//            gl.glBegin(GL10.GL_LINE_LOOP);
//            gl.glVertex3f(-4.0f, 0.0f, -3.0f);
//            gl.glVertex3f(4.0f, 0.0f, -3.0f);
//            gl.glVertex3f(4.0f, 0.0f, 3.0f);
//            gl.glVertex3f(-4.0f, 0.0f, 3.0f);
//            gl.glEnd();
//
//            // Vertical room edges
//            gl.glBegin(GL10.GL_LINES);
//            // Four corner verticals
//            gl.glVertex3f(-4.0f, 0.0f, -3.0f); gl.glVertex3f(-4.0f, 3.0f, -3.0f);
//            gl.glVertex3f(4.0f, 0.0f, -3.0f); gl.glVertex3f(4.0f, 3.0f, -3.0f);
//            gl.glVertex3f(4.0f, 0.0f, 3.0f); gl.glVertex3f(4.0f, 3.0f, 3.0f);
//            gl.glVertex3f(-4.0f, 0.0f, 3.0f); gl.glVertex3f(-4.0f, 3.0f, 3.0f);
//            gl.glEnd();
//        }
//
//        private void drawSofa(GL10 gl) {
//            gl.glPushMatrix();
//            gl.glTranslatef(-2.5f, 0.0f, 1.5f);
//            gl.glColor4f(0.4f, 0.6f, 0.9f, 1.0f); // Blue sofa
//            drawBox(gl, 2.0f, 0.8f, 0.8f);
//            gl.glPopMatrix();
//        }
//
//        private void drawTable(GL10 gl) {
//            gl.glPushMatrix();
//            gl.glTranslatef(0.0f, 0.0f, 0.0f);
//            gl.glColor4f(0.6f, 0.4f, 0.2f, 1.0f); // Brown table
//            drawBox(gl, 1.5f, 0.6f, 1.0f);
//            gl.glPopMatrix();
//        }
//
//        private void drawChair(GL10 gl) {
//            gl.glPushMatrix();
//            gl.glTranslatef(2.0f, 0.0f, -1.0f);
//            gl.glColor4f(0.8f, 0.6f, 0.4f, 1.0f); // Light brown chair
//            drawBox(gl, 0.8f, 0.7f, 0.8f);
//            gl.glPopMatrix();
//        }
//
//        private void drawBox(GL10 gl, float width, float height, float depth) {
//            gl.glLineWidth(2.0f);
//            float w = width/2, h = height/2, d = depth/2;
//
//            gl.glBegin(GL10.GL_LINES);
//            // Bottom face
//            gl.glVertex3f(-w, 0, -d); gl.glVertex3f(w, 0, -d);
//            gl.glVertex3f(w, 0, -d); gl.glVertex3f(w, 0, d);
//            gl.glVertex3f(w, 0, d); gl.glVertex3f(-w, 0, d);
//            gl.glVertex3f(-w, 0, d); gl.glVertex3f(-w, 0, -d);
//
//            // Top face
//            gl.glVertex3f(-w, h, -d); gl.glVertex3f(w, h, -d);
//            gl.glVertex3f(w, h, -d); gl.glVertex3f(w, h, d);
//            gl.glVertex3f(w, h, d); gl.glVertex3f(-w, h, d);
//            gl.glVertex3f(-w, h, d); gl.glVertex3f(-w, h, -d);
//
//            // Vertical edges
//            gl.glVertex3f(-w, 0, -d); gl.glVertex3f(-w, h, -d);
//            gl.glVertex3f(w, 0, -d); gl.glVertex3f(w, h, -d);
//            gl.glVertex3f(w, 0, d); gl.glVertex3f(w, h, d);
//            gl.glVertex3f(-w, 0, d); gl.glVertex3f(-w, h, d);
//            gl.glEnd();
//        }
//    }
}