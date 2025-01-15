package org.panda.domains.facebook;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.panda.exceptions.ChromeRelatedException;
import org.panda.util.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class FacebookHandler {
    private static final String ENV_FB_USER_TOKEN_KEY = "fb_user_access_token";
    private static final String ENV_FB_PAGE_TOKEN_KEY = "fb_long_live_page_access_token";
    private static final String ENV_FB_APP_ID_KEY = "fb_app_id";
    private static final String ENV_FB_APP_SECRET_KEY = "fb_app_secret";
    private static final String BASE_URL = "https://graph.facebook.com/v21.0";
    static String appScopeUserId = "2295734984145341";
    static String appId = "587519854112328";
    static String appSecret = "9201df7ce8f1799dab84f6712b88a550";

    public static void post() {
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
                .addFormDataPart("access_token", System.getenv(ENV_FB_PAGE_TOKEN_KEY))
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

    public static boolean tokenExpiryChecker() throws IOException {
        String pageToken = System.getenv(ENV_FB_PAGE_TOKEN_KEY);
        if(pageToken==null) {
            return false;
        }
        OkHttpClient client = new OkHttpClient();

        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE_URL + "/me")).newBuilder()
                .addQueryParameter("access_token", pageToken)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        }
    }

    private static String getLongLivedUserToken(String shortLivedToken) throws IOException {
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

    private static void addFacebookEnvironmentValue(String longLivePageAccessToken) {
        try {
            CommandLine.i().getResultOfExecution("setx "+ENV_FB_PAGE_TOKEN_KEY+" "+longLivePageAccessToken);
        } catch (Exception e) {
            System.out.println("Failed to add fb long live page access key to environment...");
            System.out.println("Error: "+e.getMessage());
        }
    }

    public static void extendPageAccessToken(String appId, String appSecret, String shortLiveUserAccessToken, String shortLivePageAccessToken) {

        try {
            // Step 1: Get Long-Lived User Token
            String longLivedUserToken = getLongLivedUserToken(shortLivePageAccessToken);
            // Step 2: Get Page Access Token
            String pageAccessTokenJson = getPageAccessToken(longLivedUserToken);
            System.out.println("Page Access Token Response: " + pageAccessTokenJson);

            JsonObject jsonObject = JsonParser.parseString(pageAccessTokenJson).getAsJsonObject();
            String longLivePageAccessToken = jsonObject.get("data").getAsJsonArray().get(0).getAsJsonObject().get("access_token").getAsString();
            System.out.println("Extracted page long live access token: "+ jsonObject.get("data").getAsJsonArray().get(0).getAsJsonObject().get("access_token").getAsString());

            addFacebookEnvironmentValue(longLivePageAccessToken);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
