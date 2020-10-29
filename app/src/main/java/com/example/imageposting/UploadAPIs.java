package com.example.imageposting;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploadAPIs {
    @Multipart
    @POST("upload-image")
    Call<ServerResponse> uploadImage(@Part MultipartBody.Part image);

    @Multipart
    @POST("upload-video")
    Call<ServerResponse> uploadVideo(@Part MultipartBody.Part video);
}
