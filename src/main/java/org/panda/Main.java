package org.panda;

import org.panda.domains.automations.IggGameAutomateBrowser;
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

        IggGameAutomateBrowser iggGameAutomateBrowser = new IggGameAutomateBrowser();
        iggGameAutomateBrowser.checkGameAlreadyExist("nightmaer");
//        FacebookHandler.extendPageAccessToken();


    }
}