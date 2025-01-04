package org.panda.domains.scraping;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.panda.domains.automations.IggGameAutomateBrowser;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class IggGameWebScraper {
    private final String GAME_WEB_URL = "https://igg-games.com";
    private final IggGameAutomateBrowser iggGameAutomateBrowser;
    private int startPageNumber;

    public IggGameWebScraper(int startPageNumber) {
        this.iggGameAutomateBrowser = new IggGameAutomateBrowser();
        this.startPageNumber = startPageNumber;
    }
    public void setStartPageNumber(int startPageNumber) {
        this.startPageNumber = startPageNumber;
    }

    public void start() {
        try {
            for (int pageCount = startPageNumber ; pageCount >= 1 ; pageCount-- ) {
                Document document = Jsoup.connect(GAME_WEB_URL+"/page/"+pageCount).timeout(200000).get();
                Elements articles = document.getElementsByTag("article");
                List<Map<String, String>> articleMapList = new LinkedList<>();
                articles.forEach(article -> {
                    Map<String, String> articleMap = new LinkedHashMap<>();
                    Element aTag = article.getElementsByClass("uk-link-reset").first();
                    String articleId = article.id();
                    String articleLink = aTag.attr("href");
                    String articleTitle = aTag.text().replace(" Free Download","").trim();
                    articleMap.put("articleId", articleId);
                    articleMap.put("articleLink", articleLink);
                    articleMap.put("articleTitle", articleTitle);
                    articleMapList.add(articleMap);
                });
                for (int articleCount = articleMapList.size()-1 ; articleCount >= 0 ; articleCount-- ) {
                    System.out.println("Article Title: "+articleMapList.get(articleCount).get("articleTitle"));
                    System.out.println("Article id: "+articleMapList.get(articleCount).get("articleId"));
                    System.out.println("Article Link: "+articleMapList.get(articleCount).get("articleLink"));
                    iggGameAutomateBrowser.checkGameAlreadyExist(articleMapList.get(articleCount).get("articleTitle"));
                }
            }
        } catch (IOException e) {
            System.out.println("Error while connecting to "+GAME_WEB_URL);
            System.out.println("Error message: "+e.getMessage());
        } finally {
            iggGameAutomateBrowser.closeBrowser();
        }
    }

    private void uploadGameToServer() {

    }
}
