package org.panda;

import org.panda.exceptions.ChromeRelatedException;
import org.panda.util.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

// Beware of that, this application does only support starting from Chrome version 120.0.6098.0
public class Initializer {
    private static final String CHROME_PATH = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
    private static final String CHROME_DRIVER_DEST_PATH = "C:\\Users\\"+System.getProperty("user.name")+"\\Documents\\chromedriver";
    public static boolean initialize() {
        if(new File(CHROME_PATH).exists()) {
            try {
                if(System.getenv("CHROME_DRIVER")!=null && isSystemEnvironmentCorrect()) return true;
                downloadChromeDriver(getChromeDriverDownloadLink());
                extractZipFile();
                copyChromeDriverToDestinationFolder();
                cleanTempFiles();
                addEnvironmentValue();
                return true;
            } catch (ChromeRelatedException e) {
                System.out.println(e.getMessage());
                return false;
            }
        } else {
            System.out.println("Chrome is not installed! File not found error: "+ CHROME_PATH +" not found!!!");
            return false;
        }
    }

    // Beware of that, this application does only support starting from Chrome version 120.0.6098.0
    private static boolean isSystemEnvironmentCorrect() throws ChromeRelatedException {
        try {
            return System.getenv("CHROME_DRIVER").toLowerCase().contains("chromedriver.exe");
        } catch (Exception e) {
            throw new ChromeRelatedException("CHROME_DRIVER environment variable is not correct.\n" +
                    "Format should be -> C:\\.....\\chromedriver.exe");
        }
    }
    private static String getChromeDriverDownloadLink() throws ChromeRelatedException {
        try {
            String chromeVersion = CommandLine.i().getResultOfExecution("powershell -command (Get-Item '"+ CHROME_PATH +"').VersionInfo | findstr \"chrome.exe\"").split("\\s+")[0];
            int currentChromeMajorVersion = Integer.parseInt(chromeVersion.split("\\.")[0]);
            return (currentChromeMajorVersion < 120 ) ? null
                    : "https://storage.googleapis.com/chrome-for-testing-public/"+chromeVersion+"/win64/chromedriver-win64.zip";
        } catch (Exception e) {
            throw new ChromeRelatedException("Error while getting chrome driver download link...\n" +
                    "Error: "+e.getMessage());
        }
    }

    private static void downloadChromeDriver(String downloadURL) throws ChromeRelatedException {
        File file = new File(CHROME_DRIVER_DEST_PATH);
        if(!file.exists()) file.mkdir();
        try {
            Path targetPath = Paths.get(CHROME_DRIVER_DEST_PATH+"\\chromedriver-win64.zip");
            URL url = new URL(downloadURL);
            InputStream inputStream = url.openStream();
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            inputStream.close();
        } catch (IOException e) {
            throw new ChromeRelatedException("Failed to download chrome driver\n" +
                    "Error: "+e.getMessage());
        }
    }

    private static void extractZipFile() throws ChromeRelatedException {
        try {
            CommandLine.i().getResultOfExecution("powershell -c Expand-Archive -Path '"+CHROME_DRIVER_DEST_PATH+"\\chromedriver-win64.zip' " +
                    "-DestinationPath '"+CHROME_DRIVER_DEST_PATH+"'");
        } catch (Exception e) {
            throw new ChromeRelatedException("Unable to extract zip file.\n" +
                    "Error: "+e.getMessage());
        }
    }

    private static void copyChromeDriverToDestinationFolder() throws ChromeRelatedException {
        Path source = Paths.get(CHROME_DRIVER_DEST_PATH+"\\chromedriver-win64\\chromedriver.exe");
        Path destination = Paths.get(CHROME_DRIVER_DEST_PATH+"\\chromedriver.exe");
        try {
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ChromeRelatedException("Failed to copy chromedriver to destination folder...\n" +
                    "Error: "+e.getLocalizedMessage());
        }
    }

    private static void cleanTempFiles() throws ChromeRelatedException {
        try {
            // Deleting zip file
            CommandLine.i().getResultOfExecution("powershell -c Remove-Item '"+CHROME_DRIVER_DEST_PATH+"\\chromedriver-win64.zip'");
            // Deleting chrome driver folder
            CommandLine.i().getResultOfExecution("powershell -c Remove-Item '"+CHROME_DRIVER_DEST_PATH+"\\chromedriver-win64' -Recurse");
        } catch (Exception e) {
            throw new ChromeRelatedException("[Warning]: Unable to clean temp files...");
        }
    }

    private static void addEnvironmentValue() throws ChromeRelatedException {
        try {
            CommandLine.i().getResultOfExecution("setx CHROME_DRIVER \""+CHROME_DRIVER_DEST_PATH+"\\chromedriver.exe\"");
        } catch (Exception e) {
            throw new ChromeRelatedException("Failed to add CHROME_DRIVER variable to system environment");
        }
    }
}
