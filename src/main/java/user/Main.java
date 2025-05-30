package user;

import crawler.Crawler;
import parser.InputParser;
import parser.JsoupParser;
import parser.ParsedInputArguments;

import java.io.IOException;

public class Main {
    //Example input arguments:
    //https://gilead-verein.at/ 2 gilead-verein.at hivegames.at
    public static void main(String[] args) throws IOException, InterruptedException {
        InputParser inputParser = new InputParser(args);
        ParsedInputArguments input = inputParser.parseInputArguments();
        Crawler crawler = new Crawler(input, new JsoupParser());
        crawler.startCrawl();
    }
}