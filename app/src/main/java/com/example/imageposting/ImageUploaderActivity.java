package com.example.imageposting;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ImageUploaderActivity extends AppCompatActivity implements UploadCallback {
    private final int IMAGE_PICK_CODE = 100;
    private ImageView imageView;
    private Uri imageUri;
    private Bitmap bitmap;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_uploader);

        progressBar = findViewById(R.id.progress_bar);

        imageView = findViewById(R.id.image_view);
        imageView.setTag("Initial");

        imageView.setOnClickListener(v -> {
            Intent gallery = new Intent();
            gallery.setType("image/*");
            gallery.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(gallery, "Select Picture"), IMAGE_PICK_CODE);
        });

        Button uploadBtn = findViewById(R.id.upload_btn);
        uploadBtn.setOnClickListener(v -> {
            if (imageView.getTag().equals("Picked")) {
                uploadImage(imageUri, bitmap);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK) {
            imageUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setBackground(null);
                imageView.setImageDrawable(null);
                imageView.setImageBitmap(bitmap);
                imageView.setTag("Picked");
                progressBar.setProgress(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getImageName(Uri uri) {
        String imageName = uri.getPath();
        int lastDivider = imageName.lastIndexOf('/');

        if (lastDivider != -1) {
            imageName = imageName.substring(lastDivider + 1);
        }

        return imageName;
    }

    private File convertBitmapToImage(String imageName, Bitmap bitmap) {
        File file = new File(getCacheDir(), imageName);
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

    private void uploadImage(Uri imageUri, Bitmap bitmap) {
        Retrofit retrofit = NetworkClient.getRetrofit("image");

        String imageName = getImageName(imageUri);
        File image = convertBitmapToImage(imageName, bitmap);

        UploadRequestBody uploadRequestBody = new UploadRequestBody(image, "image", this);
        MultipartBody.Part part = MultipartBody.Part
                .createFormData("image", imageName, uploadRequestBody);

        progressBar.setProgress(0);

        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);
        uploadAPIs.uploadImage(part).enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@Nullable Call<ServerResponse> call, @Nullable Response<ServerResponse> response) {
                assert response != null;
                assert response.body() != null;

                String res = response.body().getMessage();
                Toast.makeText(ImageUploaderActivity.this, res, Toast.LENGTH_SHORT).show();
                progressBar.setProgress(100);

                Drawable backgroundPicture = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_picture, null);
                imageView.setBackground(backgroundPicture);
                imageView.setImageDrawable(backgroundPicture);
                imageView.setTag("Initial");
            }

            @Override
            public void onFailure(@Nullable Call<ServerResponse> call, @Nullable Throwable t) {
                assert t != null;
                t.printStackTrace();

                progressBar.setProgress(0);
            }
        });
    }

    @Override
    public void onProgressUpdate(int percentage) {
        progressBar.setProgress(percentage);
    }
}