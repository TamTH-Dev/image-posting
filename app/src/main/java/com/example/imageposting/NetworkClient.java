package com.example.imageposting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkClient {
//    private static Retrofit retrofit;
    private static final String BASE_IMAGE_URL = "http://10.0.2.2:5000/";
    private static final String BASE_VIDEO_URL = "http://10.0.2.2:5001/";
//
//    public static Retrofit getImageRetrofit() {
//        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
//        Gson gson = new GsonBuilder()
//                .setLenient()
//                .create();
//
////        String url = type.equals("image") ? BASE_IMAGE_URL : BASE_VIDEO_URL;
//        Retrofit retrofit = null;
//        if (retrofit == null) {
//            retrofit = new Retrofit.Builder().baseUrl(BASE_IMAGE_URL)
//                    .addConverterFactory(GsonConverterFactory.create(gson))
//                    .client(okHttpClient).build();
//        }
//
//        return retrofit;
//    }

    public static Retrofit getRetrofit(String type) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        String url = type.equals("image") ? BASE_IMAGE_URL : BASE_VIDEO_URL;
        Retrofit retrofit = null;
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpClient).build();
        }

        return retrofit;
    }
}
