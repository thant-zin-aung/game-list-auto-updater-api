package org.panda.domains.facebook;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.File;
import java.io.IOException;

public class FacebookHandler {
    private static final String BASE_URL = "https://graph.facebook.com/v21.0";
    static String appScopeUserId = "2295734984145341";
    static String appId = "587519854112328";
    static String appSecret = "9201df7ce8f1799dab84f6712b88a550";
    static String shortLiveAccessToken = "EAAIWWKAz7kgBOZCpgdtiZApCdcWNZCxmbNAlWQYdukt0wtrwYCOoKEHUJTmlY0urTatONHcZBc4ZBoAzA3cdBMwkXaFxdjTFPhOWlB1l15qgFZChZBoKMQyJzFn866brvCZBMcl7SDeJ5MxNTEoN8OhZCBdCMRmqukxx24Yw4aCzJ2NmEACWYrexgijTpur91SpXs9GvGF54UVlEmEQvW8BNqeHoZD";
    static String longLivePageAccessToken = null;
    public static void post(String pageAccessToken) {
        String endPointUrl = BASE_URL+"/208701392320007/photos";
        String imagePath = "C:\\Users\\black\\Pictures\\My Dream Setups\\ss34.png";
        String message = "Dream Setup 10";

        // Define the OkHttpClient
        OkHttpClient client = new OkHttpClient();

        // File to be uploaded
        File imageFile = new File(imagePath);

        // Create the request body for the image file
        RequestBody imageBody = RequestBody.create(imageFile, MediaType.parse("image/jpeg"));

        // Create the text part of the request
        RequestBody textBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("access_token", pageAccessToken)
                .addFormDataPart("message", message)
                .addFormDataPart("source", imageFile.getName(), imageBody)
                .build();

        // Define the POST request
        Request request = new Request.Builder()
                .url(endPointUrl) // Replace with your endpoint
                .post(textBody)
                .build();

        // Execute the request
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Response: " + response.body().string());
            } else {
                System.out.println("Response: "+ response.body().string());
                System.out.println("Request failed with code: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getLongLivedUserToken(String appId, String appSecret, String shortLivedToken) throws IOException {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = HttpUrl.parse(BASE_URL + "/oauth/access_token").newBuilder()
                .addQueryParameter("grant_type", "fb_exchange_token")
                .addQueryParameter("client_id", appId)
                .addQueryParameter("client_secret", appSecret)
                .addQueryParameter("fb_exchange_token", shortLivedToken)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            JsonObject jsonObject = JsonParser.parseString(response.body().string()).getAsJsonObject();
            System.out.println("long live user token: "+jsonObject.get("access_token").getAsString());
            return jsonObject.get("access_token").getAsString();
        }
    }

    private static String getPageAccessToken(String longLivedUserToken) throws IOException {
        OkHttpClient client = new OkHttpClient();

        HttpUrl url = HttpUrl.parse(BASE_URL + "/"+appScopeUserId+"/accounts").newBuilder()
                .addQueryParameter("access_token", longLivedUserToken)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            return response.body().string(); // JSON response with the page access token
        }
    }

    public static void extendPageAccessToken() {

        try {
            // Step 1: Get Long-Lived User Token
            String longLivedUserToken = getLongLivedUserToken(appId, appSecret, shortLiveAccessToken);
            // Step 2: Get Page Access Token
            String pageAccessTokenJson = getPageAccessToken(longLivedUserToken);
            System.out.println("Page Access Token Response: " + pageAccessTokenJson);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
