package user;

import crawler.Crawler;

import java.util.Collections;

public class Main {
    public static void main(String[] args) {
        if(args.length!=3){
            System.out.println("Incorrect arguments");
            return;
        }
        try {
            String startUrl=args[0];
            int depth= Integer.parseInt(args[1]);
            String domain=args[2];
            Crawler crawler=new Crawler(depth, Collections.singleton(domain), startUrl);
            crawler.crawl();
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
