package com.github.stakkato95.affectivatest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.TextView;

import com.affectiva.android.affdex.sdk.Frame;
import com.affectiva.android.affdex.sdk.detector.CameraDetector;
import com.affectiva.android.affdex.sdk.detector.Detector;
import com.affectiva.android.affdex.sdk.detector.Face;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    CameraDetector cameraDetector;
    boolean isPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SurfaceView surfaceView = findViewById(R.id.surface);
        cameraDetector = new CameraDetector(
                this,
                CameraDetector.CameraType.CAMERA_FRONT,
                surfaceView
        );

        cameraDetector.setImageListener(new Detector.ImageListener() {
            @Override
            public void onImageResults(List<Face> list, Frame frame, float v) {
                if ((list != null) && !list.isEmpty()) {
                    Face.Emotions emotions = list.get(0).emotions;


                    String text = "joy=" + emotions.getJoy() + " \nanger=" + emotions.getAnger() +
                            " \nsadness=" + emotions.getSadness() + " \nEngagement=" + emotions.getEngagement() +
                            " \nSurprise=" + emotions.getSurprise() + " \nFear=" + emotions.getFear();

                    MainActivity.this.<TextView>findViewById(R.id.text).setText(text);
                }
            }
        });

        cameraDetector.setOnCameraEventListener(new CameraDetector.CameraEventListener() {
            @Override
            public void onCameraSizeSelected(final int cameraHeight, final int cameraWidth, Frame.ROTATE rotation) {
                surfaceView.post(new Runnable() {
                    @Override
                    public void run() {
                        int layoutWidth = surfaceView.getWidth();
                        int layoutHeight = surfaceView.getHeight();

                        float layoutAspectRatio = (float) layoutWidth / layoutHeight;
                        float cameraAspectRatio = (float) cameraWidth / cameraHeight;

                        int newWidth;
                        int newHeight;

                        if (cameraAspectRatio > layoutAspectRatio) {
                            newWidth = (int) (layoutHeight * cameraAspectRatio);
                            newHeight = layoutHeight;
                        } else {
                            newWidth = layoutWidth;
                            newHeight = (int) (layoutWidth / cameraAspectRatio);
                        }

                        ViewGroup.LayoutParams params = surfaceView.getLayoutParams();
                        params.height = newHeight;
                        params.width = newWidth;
                        surfaceView.setLayoutParams(params);
                    }
                });
            }
        });

//        cameraDetector.setDetectAllExpressions(true);
        cameraDetector.setDetectAllEmotions(true);
//        cameraDetector.setDetectAllEmojis(true);
//        cameraDetector.setDetectAllAppearances(true);

        int permissionCheckResult = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
            cameraDetector.start();
            isPermissionGranted = true;
            return;
        }

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isPermissionGranted) {
            cameraDetector.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isPermissionGranted) {
            cameraDetector.stop();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = true;
            cameraDetector.start();
        }
    }
}
