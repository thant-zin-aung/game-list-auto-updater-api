package org.panda.domains.facebook;

import okhttp3.*;

import java.io.File;
import java.io.IOException;

public class FacebookHandler {
    static String endPoint = "https://graph.facebook.com/v21.0/208701392320007/photos?access_token=EAAIWWKAz7kgBO1EHcG8ZBBTV0VoxmSMTWBhBqvsKBriLBXfqedXpDUqyxCZAc5qe410jOjx43ppC4KihZB3AibCcFwfB7p2YZAYg29GG3sOKE3p6m3vmxQRfRiwnsRDaDDYFF8ZCZCbQqTwrJHDC0DT8ZBCpKZBpZCDfAgS1Xq5H7DUAihxXWq9QEL8edGYx1xfr5fsZAbTpfrpSo2Xb8ku0QE1lwZD&message=Beautiful Wallpaper 3";
    public static void post() {
        String endPointUrl = "https://graph.facebook.com/v21.0/208701392320007/photos";
        String imagePath = "C:\\Users\\black\\Pictures\\My Dream Setups\\ss33.png";
        String accessToken = "EAAIWWKAz7kgBO0JRMWxPPbdlZCmzgPJkcwtYDmztlNLsas1m5Q7wy8j9FuEvIZCoSW4uxwbZBKRxzzZCACuzNZCVLygdAUonTEoeZCsrZCagt7NBXXxw0lhZAxRZCjpSEz0FXe31ai3jZBiS6NXBIYXkCUcQKGa93UmU8LZAYzSPyQ9sGKj0ptUZCGvucuUCACgZAk7a8b1dZAGLkTMXJUDI1BevkZBSKIB";
        String message = "Dream Setup 1";
        System.out.println(endPoint);

        // Define the OkHttpClient
        OkHttpClient client = new OkHttpClient();

        // File to be uploaded
        File imageFile = new File(imagePath);

        // Create the request body for the image file
        RequestBody imageBody = RequestBody.create(imageFile, MediaType.parse("image/jpeg"));

        // Create the text part of the request
        RequestBody textBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("access_token", accessToken)
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
}
