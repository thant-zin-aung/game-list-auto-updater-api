package org.panda.domains.scraping;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class WebScraper {
    public static void scrape() {
        try {
            Document document = Jsoup.connect("https://igg-games.com/").timeout(200000).get();
            Elements elements = document.getElementsByTag("article");
            elements.forEach(element -> {
                System.out.println(element.getElementsByClass("contener-out").first().getElementsByTag("a").text());
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
