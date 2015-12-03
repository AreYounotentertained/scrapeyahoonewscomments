package com.company;

import com.sun.deploy.security.MozillaJSSDSASignature;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {

        ArrayList<Person> persons = new ArrayList<>();

        try {
            Document document = Jsoup.connect("https://news.yahoo.com/-false-positive-mammograms-linked-to-increased-risk-of-breast-cancer-215934616.html").userAgent("Mozilla").get();

            Scanner scanner = new Scanner(document.toString());

            Pattern pattern = Pattern.compile("contentId: '(?:.*)',");
            String contentId = null;
            boolean foundContentId = false;

            String line;
            Matcher matcher;
            for (int i = 0; i < 400; i++){
                line = scanner.nextLine();
                matcher = pattern.matcher(line);
                if (matcher.find()){
                    contentId = line.replaceAll("contentId: '","");
                    contentId = contentId.replaceFirst("',","");
                    contentId = contentId.replaceAll("\\s+","");
                    foundContentId = true;
                    break;
                }
            }

            int commentCount = Integer.parseInt(document.select("span#total-comment-count").get(0).text());

            if (!foundContentId){
                throw new Exception("Could not find contentId");
            }
            
            Integer count = null;
            Integer counter = 1;
            Integer offset = 0;
            Elements names = null;
            Elements commentContents = null;

            while(commentCount > 1) {
                if (commentCount >= 100) {
                    count = 100;
                    commentCount = commentCount - 100;

                } else {
                    count = commentCount;
                    commentCount = commentCount - count;
                }
                document = Jsoup.connect("http://news.yahoo.com/_xhr/contentcomments/get_comments/?content_id= " + contentId + "&_device=full&count=" + count.toString() + "&isNext=true&offset=" + offset + "&_media.modules.content_comments.switches._enable_view_others=1&_media.modules.content_comments.switches._enable_mutecommenter=1&enable_collapsed_comment=1").ignoreContentType(true).userAgent("Mozilla").get();
                offset = count + offset;
                commentContents = document.select("p[class*=comment-content]");
                names = document.select("span[data-guid]");

                for(int i = 0; i < commentContents.size(); i++){
                    String nickname = new Scanner(names.get(i).toString()).nextLine();
                    nickname = nickname.substring(nickname.indexOf(">")-1,nickname.indexOf("&lt"));

                    String comment = commentContents.get(i).toString();
                    comment = comment.substring(comment.indexOf(">")+3, comment.indexOf("&lt")-3);

                    persons.add(new Person(nickname,comment));
                }

            }

            } catch (Exception e) {
            e.printStackTrace();
        }

        Integer counter = 0;
        for (Person person: persons){
            counter++;
            System.out.println(person + counter.toString());
        }


    }
}
