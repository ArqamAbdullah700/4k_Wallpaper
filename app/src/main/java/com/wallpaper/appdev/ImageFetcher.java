package com.wallpaper.appdev;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ImageFetcher extends AsyncTask<String, Void, ArrayList<String>> {

    private static final String TAG = "ImageFetcher";
    ArrayList<String> imageUrls;

    // Interface to communicate the result back to the calling code
    public interface ImageFetchListener {
        void onImageFetchComplete(ArrayList<String> imageUrls);

        void onImageFetchError(String errorMessage);
    }

    private ImageFetchListener listener;

    public ImageFetcher(ImageFetchListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        imageUrls = new ArrayList<>();
    }

    @Override
    protected ArrayList<String> doInBackground(String... params) {
        if (params.length == 0) {
            return null;
        }

        String apiUrl = params[0];
        for (int j = 1; j <= 5; j++) {
            apiUrl = "https://api.unsplash.com/search/photos/?client_id=d3wrZOMtXmwkEv_oH1wF92WCxJf5ED3DY0fvPAAkd0U&page=" + j + "&query=office&per_page=30";


            try {
                URL url = new URL(apiUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    InputStream inputStream = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject jsonObjectNew = new JSONObject(response.toString());
                    JSONArray jsonArrayNew = jsonObjectNew.getJSONArray("results");

                    for (int i = 0; i < jsonArrayNew.length(); i++) {
                        JSONObject jsonObject = jsonArrayNew.getJSONObject(i);
                        JSONObject jsonUrlObject = jsonObject.getJSONObject("urls");
                        String imageUrl = jsonUrlObject.getString("thumb");
                        if (!imageUrl.isEmpty()) {
                            imageUrls.add(imageUrl);
                        }
                        Log.d("Arqam", "Index: " + i);
                    }

                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error fetching images", e);
                return null;
            }

        }
        return imageUrls;
    }

    @Override
    protected void onPostExecute(ArrayList<String> imageUrls) {
        if (imageUrls != null) {
            listener.onImageFetchComplete(imageUrls);
        } else {
            listener.onImageFetchError("Error fetching images");
        }
    }
}
