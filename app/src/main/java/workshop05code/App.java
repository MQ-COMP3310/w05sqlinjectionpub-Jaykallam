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
            System.err.println("Couldn’t load logging config.");
        }
    }

    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        SQLiteConnectionManager db = new SQLiteConnectionManager("words.db");

        db.createNewDatabase("/Users/jay/Documents/w05sqlinjectionpub-Jaykallam/sqlite/words.db");

        if (!db.checkIfConnectionDefined()) {
            System.out.println("Couldn't connect to the database.");
            return;
        }

        if (!db.createWordleTables()) {
            System.out.println("Error setting up game tables.");
            return;
        }

        // Load and add valid words from the file
        try (BufferedReader reader = new BufferedReader(new FileReader("/Users/jay/Documents/w05sqlinjectionpub-Jaykallam/resources/data.txt"))) {
            String word;
            int wordId = 1;

            while ((word = reader.readLine()) != null) {
                word = word.trim();

                if (word.matches("^[a-z]{4}$")) {
                    logger.info("Added valid word: " + word);
                    db.addValidWord(wordId, word);
                    wordId++;
                } else {
                    logger.severe("Bad word in data.txt: " + word);
                }
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to read from data file.", ex);
            System.out.println("Something went wrong while loading words.");
            return;
        }

        // Guessing loop
        try (Scanner input = new Scanner(System.in)) {
            while (true) {
                System.out.print("Guess a 4-letter word (or 'q' to quit): ");
                String guess = input.nextLine();

                if (guess.equals("q")) {
                    System.out.println("Bye!");
                    break;
                }

                if (!guess.matches("^[a-z]{4}$")) {
                    logger.warning("Invalid guess from user: " + guess);
                    System.out.println("Try again with exactly 4 lowercase letters.");
                    continue;
                }

                System.out.println("You guessed: " + guess);

                if (db.isValidWord(guess)) {
                    System.out.println("Nice! That’s a valid word.\n");
                } else {
                    System.out.println("Nope, not in the word list.\n");
                }
            }
        } catch (NoSuchElementException | IllegalStateException ex) {
            logger.log(Level.WARNING, "Problem while reading user input.", ex);
            System.out.println("Something went wrong while reading input.");
        }
    }
}
