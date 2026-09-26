package com.example.irctc;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Scanner;

public class TatkalHelper {

    private static final String IRCTC_URL =
            "https://www.irctc.co.in/nget/train-search";

    private final WebDriver driver;
    private final WebDriverWait wait;

    public TatkalHelper() {
        driver = new ChromeDriver();

        driver.manage().window().maximize();

        wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(20)
        );
    }

    public void openIRCTC() {
        driver.get(IRCTC_URL);

        System.out.println("IRCTC opened.");
        System.out.println("Please log in manually.");
        System.out.println("Complete CAPTCHA/OTP if requested.");
    }

    public void waitForLogin() {
        Scanner scanner = new Scanner(System.in);

        System.out.println();
        System.out.println("After you have successfully logged in,");
        System.out.println("press ENTER here.");

        scanner.nextLine();
    }

    public void searchTrain(
            String from,
            String to,
            String date
    ) {

        System.out.println(
                "Searching: " + from +
                        " -> " + to +
                        " on " + date
        );

        /*
         * IRCTC changes its Angular/DOM structure periodically.
         * Keep selectors in one place so they are easy to update.
         */

        WebElement fromField = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector(
                                "input[placeholder*='From']"
                        )
                )
        );

        fromField.click();
        fromField.sendKeys(from);

        selectSuggestion(from);

        WebElement toField = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector(
                                "input[placeholder*='To']"
                        )
                )
        );

        toField.click();
        toField.sendKeys(to);

        selectSuggestion(to);

        /*
         * Date handling is intentionally left as a separate method.
         * IRCTC's date picker can change between releases.
         */

        setJourneyDate(date);

        selectTatkalQuota();

        clickSearch();
    }

    private void selectSuggestion(String station) {

        try {
            WebElement suggestion = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath(
                                    "//*[contains(normalize-space(),'"
                                            + station +
                                            "')]"
                            )
                    )
            );

            suggestion.click();

        } catch (TimeoutException e) {
            System.out.println(
                    "Could not automatically select station: "
                            + station
            );
        }
    }

    private void setJourneyDate(String date) {

        System.out.println(
                "Journey date: " + date
        );

        /*
         * Example expected format:
         * DD/MM/YYYY
         *
         * Implement the date-picker selector for the
         * current IRCTC UI here.
         */
    }

    private void selectTatkalQuota() {

        System.out.println("Selecting Tatkal quota...");

        /*
         * Locate the quota dropdown and select:
         *
         * TATKAL
         *
         * Selector may need updating if IRCTC changes
         * its Angular components.
         */
    }

    private void clickSearch() {

        try {

            WebElement searchButton = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath(
                                    "//button[contains(.,'Search')]"
                            )
                    )
            );

            searchButton.click();

            System.out.println(
                    "Search submitted."
            );

        } catch (TimeoutException e) {

            System.out.println(
                    "Search button was not found."
            );
        }
    }

    public void showAvailableTrains() {

        System.out.println();
        System.out.println("Checking train results...");

        List<WebElement> trains =
                driver.findElements(
                        By.cssSelector(
                                ".train-heading"
                        )
                );

        if (trains.isEmpty()) {

            System.out.println(
                    "No train elements found. "
                            + "The IRCTC page structure may have changed."
            );

            return;
        }

        for (WebElement train : trains) {

            System.out.println(
                    "Train: " + train.getText()
            );
        }
    }

    public void close() {

        if (driver != null) {
            driver.quit();
        }
    }

    public static void main(String[] args) {

        TatkalHelper helper = new TatkalHelper();

        try {

            helper.openIRCTC();

            helper.waitForLogin();

            /*
             * Example:
             *
             * Pune -> New Delhi
             *
             * Replace these with your journey.
             */

            helper.searchTrain(
                    "PUNE",
                    "NDLS",
                    "30/09/2026"
            );

            helper.showAvailableTrains();

            System.out.println();
            System.out.println(
                    "The browser will remain open."
            );

            System.out.println(
                    "Select the train and continue manually."
            );

            new Scanner(System.in).nextLine();

        } finally {

            helper.close();
        }
    }
}
