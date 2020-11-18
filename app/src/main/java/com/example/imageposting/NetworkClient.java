package com.example.imageposting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkClient {
    //    private static final String BASE_IMAGE_URL = "http://10.0.2.2:5000/";
    private static final String BASE_IMAGE_URL = "http://192.168.43.154:5000/";

    //    private static final String BASE_IMAGE_URL = "https://orders-detection.herokuapp.com/";
    private static final String BASE_VIDEO_URL = "http://192.168.43.154:5001/";

    public static Retrofit getRetrofit(String type) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        String url = type.equals("image") ? BASE_IMAGE_URL : BASE_VIDEO_URL;
        return new Retrofit.Builder().baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient).build();
    }
}
