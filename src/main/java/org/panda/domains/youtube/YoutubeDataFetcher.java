package org.panda.domains.youtube;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchResultSnippet;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class YoutubeDataFetcher {
    private static final String APPLICATION_NAME = "Game List Auto Updater";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final Set<String> SCOPES = Collections.singleton(YouTubeScopes.YOUTUBE_READONLY);
    private static final String TOKENS_DIRECTORY_PATH = "C:\\ProgramData\\game-list-auto-updater\\tokens";

    private static void createRefreshTokenDirectory() {
        if(!new File(TOKENS_DIRECTORY_PATH).exists()) new File(TOKENS_DIRECTORY_PATH).mkdir();
    }

    private static Credential authorize(final NetHttpTransport httpTransport) throws Exception {
        createRefreshTokenDirectory();
        InputStream in = YoutubeDataFetcher.class.getResourceAsStream("/credentials.json");
        if (in == null) {
            throw new FileNotFoundException("Resource not found: credentials.json");
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")  // Important to get refresh token
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void fetch() throws Exception {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = authorize(httpTransport);

        YouTube youtubeService = new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        YouTube.Search.List request = youtubeService.search().list("snippet");

        request.setQ("The Witcher 3 PC Game Trailer");
        request.setType("video");
        request.setMaxResults(10L);

        SearchListResponse response = request.execute();

        List<SearchResult> searchResults = response.getItems();

        if (searchResults != null) {
            for (SearchResult result : searchResults) {
                SearchResultSnippet snippet = result.getSnippet();
                System.out.println("Title: " + snippet.getTitle());
                System.out.println("Description: " + snippet.getDescription());
                System.out.println("Video ID: " + result.getId().getVideoId());
                System.out.println("URL: https://www.youtube.com/watch?v=" + result.getId().getVideoId());
                System.out.println("=========================================");
            }
        } else {
            System.out.println("No results found.");
        }
    }
}
