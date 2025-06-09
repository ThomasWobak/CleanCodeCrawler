package user;

import crawler.Crawler;
import parser.InputParser;
import dto.ParsedInputArguments;

public class Main {
    //Example input arguments:
    //https://gilead-verein.at/ 2 gilead-verein.at hivegames.at
    public static void main(String[] args) {
        try {
            InputParser inputParser = new InputParser(args);
            ParsedInputArguments input = inputParser.parseInputArguments();
            Crawler crawler = new Crawler(input);
            crawler.startCrawl();
        } catch (IllegalArgumentException e) {
            System.err.println("Usage error: " + e.getMessage());
            System.exit(2);
        } catch (RuntimeException e) {
            System.err.println("Crawler failed: " + e.getMessage());
            System.exit(1);
        }
    }
}