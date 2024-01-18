package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
       Elements elements = document.select(".faded-content__container");
       return elements.text();
    }

    public static void main(String[] args) throws IOException {
        int pageNumber = 1;
        while (pageNumber < 6) {
            Connection connection = Jsoup.connect(PAGE_LINK);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                Element dateElement = row.select(".vacancy-card__date").first().child(0);
                String date = dateElement.attr("datetime");
                System.out.printf("%s %s %s%n", vacancyName, link, date);
                try {
                    System.out.println(retrieveDescription(link));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            pageNumber++;
        }
    }
}
