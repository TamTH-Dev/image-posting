package com.example.imageposting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ScreenRecorder implements UploadCallback {
    private final MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private MediaProjectionCallback mediaProjectionCallback;
    private MediaRecorder mediaRecorder;
    private VirtualDisplay virtualDisplay;

    private final Activity activity;
    private final Context context;
    private final int REQUEST_RECORD_CODE;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int DISPLAY_WIDTH = 720;
    private static final int DISPLAY_HEIGHT = 1280;
    private static final String TAG = "MainActivity";
    private final int mScreenDensity;
    private String videoUri;
    private String recordedTime;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    public ScreenRecorder(Activity activity, Context context, int REQUEST_RECORD_CODE) {
        this.activity = activity;
        this.context = context;
        this.REQUEST_RECORD_CODE = REQUEST_RECORD_CODE;

        this.projectionManager = (MediaProjectionManager) context.getSystemService
                (Context.MEDIA_PROJECTION_SERVICE);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mScreenDensity = metrics.densityDpi;
    }

    public void onActivityResult(int resultCode, Intent data) {
        mediaProjectionCallback = new MediaProjectionCallback();
        mediaProjection = projectionManager.getMediaProjection(resultCode, data);
        mediaProjection.registerCallback(mediaProjectionCallback, null);
        virtualDisplay = createVirtualDisplay();
        mediaRecorder.start();
    }

    public void startRecord() {
        if (mediaRecorder == null) {
            this.mediaRecorder = new MediaRecorder();
            initRecorder();
            shareScreen();
        }
    }

    public void stopRecord() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            Log.v(TAG, "Stopping Recording");
            stopScreenSharing();
        }
    }

    private void shareScreen() {
        if (mediaProjection == null) {
            activity.startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_RECORD_CODE);
            return;
        }
        virtualDisplay = createVirtualDisplay();
        mediaRecorder.start();
    }

    private VirtualDisplay createVirtualDisplay() {
        return mediaProjection.createVirtualDisplay("MainActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.getSurface(), null, null);
    }


    private void initRecorder() {
        try {
            recordedTime = new SimpleDateFormat("ddMMyyyyhhmmss", Locale.getDefault())
                    .format(new Date());
            videoUri = getVideoUri();

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(videoUri);
            mediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setVideoEncodingBitRate(512 * 1000);
            mediaRecorder.setVideoFrameRate(30);
            int rotation = context.getResources().getConfiguration().orientation;
            int orientation = ORIENTATIONS.get(rotation + 90);
            mediaRecorder.setOrientationHint(orientation);
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getVideoUri() {
        return Environment
                .getExternalStoragePublicDirectory(Environment
                        .DIRECTORY_PICTURES) + File.separator + getVideoName();
    }

    private String getVideoName() {
        return "record_" + recordedTime + ".mp4";
    }

    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            mediaRecorder.stop();
            mediaRecorder.reset();
            Log.v(TAG, "Recording Stopped");
            mediaProjection = null;
            stopScreenSharing();
        }
    }

    private void stopScreenSharing() {
        if (virtualDisplay == null) {
            return;
        }
        virtualDisplay.release();
        destroyMediaProjection();
    }


    public void destroyMediaProjection() {
        if (mediaProjection != null) {
            mediaProjection.unregisterCallback(mediaProjectionCallback);
            mediaProjection.stop();
            mediaProjection = null;
            mediaRecorder = null;
            uploadVideo();
        }
        Log.i(TAG, "MediaProjection Stopped");
    }


    private void uploadVideo() {
        Retrofit retrofit = NetworkClient.getRetrofit("video");

        File video = new File(videoUri);

        UploadRequestBody uploadRequestBody = new UploadRequestBody(video, "video", this);
        MultipartBody.Part part = MultipartBody.Part
                .createFormData("video", getVideoName(), uploadRequestBody);

        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);
        uploadAPIs.uploadVideo(part).enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@Nullable Call<ServerResponse> call, @Nullable Response<ServerResponse> response) {
                assert response != null;
                assert response.body() != null;

                String res = response.body().getMessage();
                Toast.makeText(activity, res, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@Nullable Call<ServerResponse> call, @Nullable Throwable t) {
                assert t != null;
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onProgressUpdate(int percentage) {
    }

    public boolean getIsRecording() {
        return this.mediaRecorder != null;
    }
}
