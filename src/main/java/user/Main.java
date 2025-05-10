package user;

import crawler.Crawler;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        /*
        if(args.length!=3){
            System.out.println("Incorrect arguments");
            return;
        }
        try {
            String startUrl=args[0];
            int depth= Integer.parseInt(args[1]);
            String domain=args[2];
            Crawler crawler=new Crawler(depth, Collections.singleton(domain), startUrl);
            crawler.startCrawl();
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
        */
        Set<String> allowedDomains = Set.of("gilead-verein.at", "hivegames.at");
        List<String> startUrls = List.of("https://gilead-verein.at/", "https://hivegames.at/");
        Crawler crawler = new Crawler(2, allowedDomains);
        crawler.startCrawl(startUrls);
    }
}
