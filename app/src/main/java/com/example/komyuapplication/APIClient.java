package com.example.komyuapplication;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {
    private static Retrofit retrofit;

    public static Retrofit getInstance() {
        if (retrofit == null) {
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(log)
                    .connectTimeout(30, TimeUnit.SECONDS)  // wait up to 30s to connect
                    .readTimeout(60, TimeUnit.SECONDS)     // wait up to 60s for server to reply
                    .writeTimeout(30, TimeUnit.SECONDS)    // wait up to 30s to send request body
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("https://komyu.swuitapp.com/API/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
