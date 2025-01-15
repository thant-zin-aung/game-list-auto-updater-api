package org.panda;

import org.panda.domains.facebook.FacebookHandler;
import org.panda.domains.scraping.IggGameWebScraper;
import org.panda.domains.youtube.YoutubeDataFetcher;
import org.panda.exceptions.ChromeRelatedException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, ChromeRelatedException {
//        System.out.println(Initializer.initialize() ? "Ready to use..." : "Failed to use");
//        IggGameWebScraper iggGameWebScraper = new IggGameWebScraper(2);
//        iggGameWebScraper.start();

//        FacebookHandler.extendPageAccessToken();

        String shortLiveAccessToken = "EAAIWWKAz7kgBOzajvUsvfnNcTLdoIulVNQWZADbkpeZAR5wnZAr17c3mdCY2ahA4MFZCxJoZCCnNRwC0LtcfYFZB5e0lUdS84XjjwetWADKLF6hgZBvpiYFcHGW9IvJsuGaTKFOTZCrZCbXESkUqa3eBcGeXUuLVrsD1PMhzDabwsxgBm8DWtRbQQb03fXFdVVGDEE8Rhkdzc9RVGNLAZCxvbjYVMKFgZDZD";
//        FacebookHandler.post("EAAIWWKAz7kgBOZCp8mnp51hYI7FkRtCe3g4GovPe9irNFhP1TTPa39TfyLEz2Va7QzJKMXoR59XN4YIOQm8yLYZCHQhTgrzxm6SxZAuiRxHGlkNVpltbbttzTZC6ZCljEG9g4j529EJGbdyqZBqdmlyZC3SLeZCfZBrgMsKBuUZBwXQlizA0FUHGcaZAzTZAN5P3sJ0ZD");
//        FacebookHandler.extendPageAccessToken(null, null, null, shortLiveAccessToken);
        System.out.println("Is token valid: "+FacebookHandler.tokenExpiryChecker());

    }
}