package user;

import crawler.Crawler;
import parser.InputParser;
import parser.ParsedInputArguments;

import java.io.IOException;

public class Main {
    //Example input arguments:
    //https://gilead-verein.at/ 2 gilead-verein.at hivegames.at
        public static void main(String[] args) throws IOException, InterruptedException {
            InputParser parser = new InputParser(args);
            ParsedInputArguments input=parser.parseInputArguments();
            Crawler crawler = new Crawler(input);
            crawler.startCrawl();
        }
}

