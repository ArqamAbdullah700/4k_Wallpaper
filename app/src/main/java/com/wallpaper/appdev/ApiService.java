package com.wallpaper.appdev;

import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @Multipart
    @POST("addWallpaper.php") // Change this to the endpoint of your PHP API
    Call<JsonObject> uploadImage(@Part MultipartBody.Part image);
}
