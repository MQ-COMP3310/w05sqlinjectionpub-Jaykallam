package workshop05code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class App {
    static {
        try {
            LogManager.getLogManager().readConfiguration(
                new FileInputStream("/Users/jay/Documents/w05sqlinjectionpub-Jaykallam/resources/logging.properties")
            );
        } catch (SecurityException | IOException e) {
            System.err.println("Logging configuration failed.");
        }
    }

    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        SQLiteConnectionManager wordleDatabaseConnection = new SQLiteConnectionManager("words.db");

        wordleDatabaseConnection.createNewDatabase("/Users/jay/Documents/w05sqlinjectionpub-Jaykallam/sqlite/words.db");
        if (!wordleDatabaseConnection.checkIfConnectionDefined()) {
            System.out.println("Not able to connect. Sorry!");
            return;
        }

        if (!wordleDatabaseConnection.createWordleTables()) {
            System.out.println("Not able to launch. Sorry!");
            return;
        }

        // Load words from file into database with validation
        try (BufferedReader br = new BufferedReader(new FileReader("/Users/jay/Documents/w05sqlinjectionpub-Jaykallam/resources/data.txt"))) {
            String line;
            int i = 1;
            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.matches("^[a-z]{4}$")) {
                    logger.info("Valid word added from file: " + line);
                    wordleDatabaseConnection.addValidWord(i, line);
                    i++;
                } else {
                    logger.severe("Invalid word in data.txt: " + line);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading data.txt.", e);
            System.out.println("Something went wrong while loading data. Please try again.");
            return;
        }

        // Start the game loop
        try (Scanner scanner = new Scanner(System.in)) {
            String guess;
            while (true) {
                System.out.print("Enter a 4-letter word for a guess or 'q' to quit: ");
                guess = scanner.nextLine();

                if (guess.equals("q")) {
                    System.out.println("Thanks for playing!");
                    break;
                }

                if (!guess.matches("^[a-z]{4}$")) {
                    logger.warning("Invalid user guess: " + guess);
                    System.out.println("Invalid input! Please enter exactly 4 lowercase letters (a-z only).\n");
                    continue;
                }

                System.out.println("You've guessed '" + guess + "'.");

                if (wordleDatabaseConnection.isValidWord(guess)) {
                    System.out.println("Success! It is in the list.\n");
                } else {
                    System.out.println("Sorry. This word is NOT in the list.\n");
                }
            }
        } catch (NoSuchElementException | IllegalStateException e) {
            logger.log(Level.WARNING, "Input error during game loop.", e);
            System.out.println("An error occurred while processing your input.");
        }
    }
}
