package org.panda.domains.scraping;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.panda.domains.automations.IggGameAutomateBrowser;
import org.panda.domains.youtube.YoutubeDataFetcher;
import org.panda.exceptions.ChromeRelatedException;

import java.io.IOException;
import java.util.*;

public class IggGameWebScraper {
    private final int MAX_SERVER_ACCEPT_IMAGE_COUNT = 5;
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
//                for (int articleCount = articleMapList.size()-1 ; articleCount >= 0 ; articleCount-- ) {
                //testing
                for (int articleCount = 0 ; articleCount <= articleMapList.size()-1 ; articleCount++ ) {
                    System.out.println("Article Title: "+articleMapList.get(articleCount).get("articleTitle"));
                    System.out.println("Article id: "+articleMapList.get(articleCount).get("articleId"));
                    System.out.println("Article Link: "+articleMapList.get(articleCount).get("articleLink"));
                    if(!iggGameAutomateBrowser.checkGameAlreadyExist(articleMapList.get(articleCount).get("articleTitle"))) {
                        uploadGameToServer(articleMapList.get(articleCount));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error while connecting to "+GAME_WEB_URL);
            System.out.println("Error message: "+e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            iggGameAutomateBrowser.closeBrowser();
        }
    }

    private void uploadGameToServer(Map<String, String> articleMap) throws Exception {
        getSpecificGamePageInfo(articleMap);
    }

    // Using Jsoup.connect one time because I just want to call target website once to avoid blocking ip from target website.
    // Requesting target website several times may lead to ip blocking (403, Unauthorized).
    // If so, you need to wait specific amount of time to request that website again.
    // Btw, you can use vpn to request again immediately once you got ip blocking
    public void getSpecificGamePageInfo(Map<String, String> articleMap) throws Exception {
        Document document = Jsoup.connect(articleMap.get("articleLink")).get();
        getGenreList(document);
        getSpecificationList(document);
        getDownloadLinks(document);
        getGamePlayImages(document, articleMap);
        getYoutubeTrailerLink(articleMap);
        System.out.println("-".repeat(20));
    }

    public void getGenreList(Document document) {
        List<String> genreList = new LinkedList<>();
        Elements genres = document.getElementsByTag("p").first().getElementsByTag("a");
        genres.forEach(genre -> genreList.add(genre.text()));
        System.out.println("Genre list: "+genreList);
    }

    public List<Map<String, String>> getSpecificationList(Document document) {
        List<Map<String, String>> specList = new LinkedList<>();
        Elements totalSpec = document.select(".uk-heading-bullet strong");
        for (int specCount = 5 ; specCount <= (totalSpec.size() == 2 ? 6 : 5) ; specCount++ ) {
            Map<String, String> specMap = new LinkedHashMap<>();
            Elements specInfos = document.getElementsByTag("ul").get(specCount).getElementsByTag("li");
            specInfos.forEach(spec->{
                if(spec.text().toLowerCase().contains("os:") || spec.text().toLowerCase().contains("processor:") || spec.text().toLowerCase().contains("memory:") ||
                        spec.text().toLowerCase().contains("graphics:") || spec.text().toLowerCase().contains("storage:")) {
                    String[] pair = spec.text().split(": ");
                    specMap.put(pair[0].toLowerCase(), pair.length==2 ? pair[1] : "-");
                }
            });
            specList.add(specMap);
        }
        // preventing game specification from being not exist
        if(specList.size() == 0) {
            specList.add(new LinkedHashMap<>());
            specList.add(new LinkedHashMap<>());
            specList.forEach(specMap -> {
                List<String> specFormatList = new LinkedList<>(List.of("os","processor","memory","graphics","storage"));
                specFormatList.forEach(format -> specMap.putIfAbsent(format, "-"));
            });
        }
        System.out.println("spec list size: "+specList.size());
        if(specList.size()==1 || (specList.size()==2 && specList.get(1).size()==0)) {
            if(specList.size()==2) specList.remove(1);
            specList.add(specList.get(0));
        }
        specList.forEach(specMap -> {
            List<String> specFormatList = new LinkedList<>(List.of("os","processor","memory","graphics","storage"));
            specFormatList.forEach(format -> specMap.putIfAbsent(format, "-"));
        });
        System.out.println("Specification List: "+specList);
        // preventing game specification from being not exist
        return specList;
    }

    private List<String> getDownloadLinks(Document document) throws ChromeRelatedException {
        List<String> redirectLinks = new LinkedList<>();
        Elements wrappedMegaUpLinks = document.select(".uk-margin-medium-top > p:has(.uk-heading-bullet)")
                .stream()
                .filter(wrappedLink -> wrappedLink.getElementsByClass("uk-heading-bullet").get(0).text().toLowerCase().contains("megaup"))
                .findFirst().orElseThrow(()->new ChromeRelatedException("Cannot find MegaUp.net links")).getElementsByTag("a");
        wrappedMegaUpLinks.forEach(wrappedLink -> redirectLinks.add(wrappedLink.attr("href")));
        System.out.println(wrappedMegaUpLinks.size());
//        redirectLinks.forEach(System.out::println);
        iggGameAutomateBrowser.getActualGameLinks(redirectLinks,4).forEach(System.out::println);
        return redirectLinks;
    }

    public List<String> getGamePlayImages(Document document, Map<String, String> articleMap) throws IOException {
        List<String> imageList = new LinkedList<>();
        Elements imageTags = document.getElementsByClass("igg-image-content");
        imageTags.forEach(imageTag -> imageList.add(imageTag.attr("src")));
        if(imageTags.size() < MAX_SERVER_ACCEPT_IMAGE_COUNT ) {
            int remainingImageCount = MAX_SERVER_ACCEPT_IMAGE_COUNT - imageTags.size();
            String searchGameTitle = articleMap.get("articleTitle").toLowerCase();
            Document mrPcGamerDocument = Jsoup.connect("https://mrpcgamer.net/?s=".concat(searchGameTitle)).get();
            Elements aTagGameInfoList = mrPcGamerDocument.select("article .post-title a");
            boolean fetchingMrPcGamerImagesOK = false;
            for(Element aTag : aTagGameInfoList ) {
                String fetchedGameTitle = aTag.text();
                if(containsIgnoreCharacters(fetchedGameTitle, searchGameTitle)) {
                    Document gameDetailPageDocument = Jsoup.connect(aTag.attr("href")).get();
                    Elements imgTags = gameDetailPageDocument.select(".slideshow-container .mySlides img");
                    // fill remain array slot with last fetched images from MrPcGamer website
                    for(int i=0 ; i < remainingImageCount ; i++ ) {
                        imageList.add(imgTags.get(i).attr("src"));
                    }
                    fetchingMrPcGamerImagesOK=true;
                    break;
                }
            }

            // fill remain array slot with last updated image if fetching from MrPcGamer website failed!
            if(!fetchingMrPcGamerImagesOK) {
                for(int i=imageTags.size() ; i < MAX_SERVER_ACCEPT_IMAGE_COUNT ; i++ ) {
                    imageList.add(imageList.get(imageList.size()-1));
                }
            }

        }
        imageList.forEach(System.out::println);
        return imageList;
    }

    private static String getYoutubeTrailerLink(Map<String, String> articleMap) {
        try {
            return YoutubeDataFetcher.fetch(articleMap.get("articleTitle"));
        } catch (Exception e) {
            YoutubeDataFetcher.deleteRefreshTokenDirectory();
            try {
                return YoutubeDataFetcher.fetch(articleMap.get("articleTitle"));
            } catch (Exception ee) {
                System.out.println(ee.getMessage());
                ee.printStackTrace();
            }
        }
        return null;
    }

    private static boolean containsIgnoreCharacters(String target, String search) {
        String filteredTarget = target.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        String filteredSearch = search.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        return filteredTarget.contains(filteredSearch);
    }
}
