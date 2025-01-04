package org.panda.domains.automations;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class AutomateBrowser implements AutoCloseable {
    private final String WEB_ROOT_URL = "https://blackskypcgamestore.infinityfreeapp.com";
    private final WebDriver driver;
    public AutomateBrowser() {
        System.setProperty("webdriver.chrome.driver", System.getenv("CHROME_DRIVER"));
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Run in headless mode (no UI)
        options.addArguments("--disable-gpu"); // Disable GPU hardware acceleration
        options.addArguments("--window-size=1920x1080"); // Set window size
        // Initialize WebDriver with ChromeOptions
        this.driver = new ChromeDriver(options);
    }

    public boolean checkGameAlreadyExist(String gameTitle) {
        long startTime = System.nanoTime();
        // Navigate to a website
        driver.get(WEB_ROOT_URL+"/admin_panel/exist-game-checker.php?game_title="+gameTitle);
        String pageTitle = driver.getTitle();
        System.out.println(pageTitle);

        // Find the element by its tag name, id, or class name (here we use class name)
        List<WebElement> preTags = driver.findElements(By.tagName("pre"));
        if (preTags.isEmpty()) {
            WebElement proceedLink = driver.findElement(By.id("proceed-link"));
            WebElement advanceButton = driver.findElement(By.id("details-button"));
            advanceButton.click();
            proceedLink.click();
            // Wait for the new page to load (e.g., wait for a specific element)
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(600));
            WebElement newPagePreElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("pre")));
            System.out.println("Page title: " + pageTitle);
            System.out.println("Tag value: " + newPagePreElement.getText());

        } else {
            // Get the text inside the <div> (or other elements)
            String tagValue = preTags.get(0).getText();
            System.out.println("Page title: " + pageTitle);
            System.out.println("Tag value: " + tagValue);
        }

        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;
        double elapsedTimeInMilliseconds = elapsedTime / 1_000_000_000.0;
        System.out.println("Processing time: ".concat(String.format("%.2f",elapsedTimeInMilliseconds)).concat("s"));
        System.out.println("-".repeat(20));
        return false;
    }

    public void closeBrowser() {
        // Close the browser
        driver.quit();
    }

    @Override
    public void close() {
        // Close the browser
        driver.quit();
    }
}
