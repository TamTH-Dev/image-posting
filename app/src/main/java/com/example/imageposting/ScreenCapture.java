package com.example.imageposting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import androidx.exifinterface.media.ExifInterface;

import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ScreenCapture implements UploadCallback {
    private final Activity activity;
    private final Context context;
    private final int REQUEST_IMAGE_CAPTURE_CODE;
    private File imageFile;

    public ScreenCapture(Activity activity, Context context, int REQUEST_IMAGE_CAPTURE_CODE) {
        this.activity = activity;
        this.context = context;
        this.REQUEST_IMAGE_CAPTURE_CODE = REQUEST_IMAGE_CAPTURE_CODE;
    }

    public void executeCapturedImage() {
        String capturedTime = new SimpleDateFormat("ddMMyyyyhhmmss", Locale.getDefault())
                .format(new Date());
        String imageName = "capture_" + capturedTime + ".png";

        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        Bitmap processedBitmap = getProcessedBitmap(context, bitmap, Uri.fromFile(imageFile));

        uploadImage(processedBitmap, imageName);
    }

    private Bitmap getProcessedBitmap(Context context, Bitmap bitmap, Uri selectedImage) {
        int rotate = 90;
        try {
            context.getContentResolver().notifyChange(selectedImage, null);
            File imageFile = new File(selectedImage.getPath());

            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotateImage(bitmap, rotate);
    }

    public Bitmap rotateImage(Bitmap source, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public void takeImage() {
        Intent takeImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            File filePath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            imageFile = File.createTempFile("tmp", ".png", filePath);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        Uri fileProvider = FileProvider.getUriForFile(context, "com.example.imageposting.fileprovider", imageFile);
        takeImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        if (takeImageIntent.resolveActivity(context.getPackageManager()) != null) {
            activity.startActivityForResult(takeImageIntent, REQUEST_IMAGE_CAPTURE_CODE);
        }
    }

    private void uploadImage(Bitmap bitmap, String imageName) {
        Retrofit retrofit = NetworkClient.getRetrofit("image");

        File image = convertBitmapToImage(imageName, bitmap);

        UploadRequestBody uploadRequestBody = new UploadRequestBody(image, "image", this);
        MultipartBody.Part part = MultipartBody.Part
                .createFormData("image", imageName, uploadRequestBody);

        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);
        uploadAPIs.uploadImage(part).enqueue(new Callback<ServerResponse>() {
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

    private File convertBitmapToImage(String imageName, Bitmap bitmap) {
        File file = new File(context.getCacheDir(), imageName);
        try {
            boolean isSuccess = file.createNewFile();
            if (isSuccess) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
                byte[] bitMapData = bos.toByteArray();

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    assert fos != null;
                    fos.write(bitMapData);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    @Override
    public void onProgressUpdate(int percentage) {
    }
}
