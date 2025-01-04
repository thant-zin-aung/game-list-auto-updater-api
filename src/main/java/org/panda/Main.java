package org.panda;

import org.panda.domains.scraping.IggGameWebScraper;

public class Main {
    public static void main(String[] args) {
//        System.out.println(Initializer.initialize() ? "Ready to use..." : "Failed to use");

//        AutomateBrowser automateBrowser = new AutomateBrowser();
//        List<String> gameTitleList = new ArrayList<>(List.of("Little Nightmare", "Ghost Warrior", "Red Dead Re", "Blend", "SimSons"));
//        gameTitleList.forEach(title -> {
//            System.out.println(title);
//            automateBrowser.checkGameAlreadyExist(title);
//        });
//        automateBrowser.closeBrowser();

        IggGameWebScraper iggGameWebScraper = new IggGameWebScraper(2);
        iggGameWebScraper.start();

    }
}