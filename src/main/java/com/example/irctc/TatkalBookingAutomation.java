package com.example.irctc;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Scanner;

public class TatkalBookingAutomation {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Taking inputs from the user
        System.out.print("Enter IRCTC Username: ");
        String username = scanner.nextLine();

        System.out.print("Enter IRCTC Password: ");
        String password = scanner.nextLine();

        System.out.print("Enter From Station Code (e.g., NDLS): ");
        String fromStation = scanner.nextLine();

        System.out.print("Enter To Station Code (e.g., HWH): ");
        String toStation = scanner.nextLine();

        System.out.print("Enter Date (DD/MM/YYYY): ");
        String journeyDate = scanner.nextLine().trim();

        System.out.print("Enter Train Number (Leave blank to pick the 1st available train): ");
        String trainNumber = scanner.nextLine().trim();

        System.out.print("Enter Class (e.g., 3A, 2A, SL) [Default is 3A]: ");
        String travelClass = scanner.nextLine().trim();
        if (travelClass.isEmpty()) {
            travelClass = "3A";
        }

        // Initialize WebDriver
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            driver.manage().window().maximize();
            driver.get("https://www.irctc.co.in/");

            // 1. Clear initial modal popup/advisory overlay using JS
            try {
                js.executeScript(
                        "var masks = document.querySelectorAll('.ui-dialog-mask, .disha-banner'); " +
                                "masks.forEach(m => m.remove());"
                );
            } catch (Exception ignored) {}

            // 2. Click the Login button safely
            WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(text(),'LOGIN')]")
            ));

            try {
                loginButton.click();
            } catch (Exception ex) {
                js.executeScript("arguments[0].click();", loginButton);
            }
            System.out.println("[INFO] Clicked Login button successfully.");

            // 3. Enter Username
            WebElement userInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@formcontrolname='userid' or @id='userId' or contains(@placeholder, 'User ID')]")
            ));
            userInput.clear();
            userInput.sendKeys(username);

            // 4. Enter Password
            WebElement passInput = driver.findElement(
                    By.xpath("//input[@formcontrolname='password' or @id='pwd' or contains(@placeholder, 'Password')]")
            );
            passInput.clear();
            passInput.sendKeys(password);

            System.out.println("\n[INFO] Username and Password entered.");
            System.out.println("[ACTION REQUIRED] Please manually solve the CAPTCHA and click Sign In...");

            // 5. Dynamic Login Detector
            System.out.println("[INFO] Waiting for login completion...");
            WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(120));
            longWait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//input[@formcontrolname='userid']")));
            System.out.println("[INFO] Login detected!");

            // 6. Explicitly wait for any lingering PrimeNG modal masks to disappear completely
            try {
                longWait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".ui-dialog-mask")));
            } catch (Exception e) {
                // Force-remove via JS if it's stuck
                js.executeScript("var masks = document.querySelectorAll('.ui-dialog-mask'); masks.forEach(m => m.remove());");
                System.out.println("[INFO] Force-cleared lingering modal overlay via JavaScript.");
            }

            // 7. Enter From Station
            WebElement fromInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//p-autocomplete[contains(@id, 'origin')]//input | //input[@aria-autocomplete='list' and contains(@class, 'ui-autocomplete-input')]")
            ));
            fromInput.click();
            fromInput.clear();
            fromInput.sendKeys(fromStation);
            Thread.sleep(800);
            fromInput.sendKeys(Keys.ENTER);

            // 8. Enter To Station
            WebElement toInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//p-autocomplete[contains(@id, 'destination')]//input | (//input[@aria-autocomplete='list' and contains(@class, 'ui-autocomplete-input')])[2]")
            ));
            toInput.click();
            toInput.clear();
            toInput.sendKeys(toStation);
            Thread.sleep(800);
            toInput.sendKeys(Keys.ENTER);

            // 9. Enter Journey Date (Formatted as DD/MM/YYYY)
            WebElement dateInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//p-calendar//input | //input[contains(@placeholder, 'Journey Date')]")
            ));
            dateInput.click();
            dateInput.sendKeys(Keys.COMMAND + "a");
            dateInput.sendKeys(Keys.BACK_SPACE);
            dateInput.sendKeys(journeyDate);
            dateInput.sendKeys(Keys.ESCAPE);

            // 10. Click Search Button
            WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(@class, 'search_btn') or contains(text(), 'Search')]")
            ));
            searchButton.click();
            System.out.println("[INFO] Search executed successfully. Loading trains...");

            // 11. Select Train and Class
            Thread.sleep(2500);
            WebElement targetClassElement;

            if (trainNumber.isEmpty()) {
                System.out.println("[INFO] No train number specified. Selecting the 1st available train.");
                targetClassElement = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("(//div[contains(@class, 'train-heading')])[1]/following::div[contains(@class, 'available-class') or contains(text(), '" + travelClass + "')][1]")
                ));
            } else {
                System.out.println("[INFO] Searching for specified train number: " + trainNumber);
                targetClassElement = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[contains(@class, 'train-heading') and contains(., '" + trainNumber + "')]/following::div[contains(text(), '" + travelClass + "')][1]")
                ));
            }

            targetClassElement.click();
            System.out.println("[INFO] Selected class: " + travelClass);

            // 12. Click Book Now
            WebElement bookNowButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(), 'Book Now') or contains(@class, 'train_Search')]")
            ));
            bookNowButton.click();
            System.out.println("[SUCCESS] Clicked Book Now! Proceeding to passenger details page.");

        } catch (Exception e) {
            System.err.println("[ERROR] An error occurred during execution: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
            System.out.println("[INFO] Script execution completed.");
        }
    }
}