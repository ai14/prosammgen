package com.github.ai14.prosammgen;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikipediaArticles implements TextSource {
    private String[] searchTerms = new String[]{"philosophy", "science", "art"}; //TODO Make into input arguments.

    @Override
    public File[] getTexts() {
        List<File> articles = new ArrayList<>();
        try {
            for (String searchTerm : searchTerms) {

                // Use cached file instead, if fresh enough.
                File f = new File(searchTerm + ".txt");
                if (f.exists() && System.currentTimeMillis() - f.lastModified() < 2592000000l) {
                    articles.add(f);
                    continue;
                } else {
                    f.delete();
                }

                // Fetch fresh Wikipedia articles.
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(searchTerm + ".txt")));
                int gaplimit = 10; // “Because excerpts generation can be slow the limit is capped at one whole-page extract.” Solution: do several requests. Sorry Wikipedia!
                for (int i = 0; i < gaplimit; i++) {
                    String url = "http://en.wikipedia.org/w/api.php";
                    String query = "?action=query&generator=allpages&gaplimit=" + gaplimit + "&gapfrom=" + searchTerm + "&prop=extracts&exsectionformat=plain&explaintext&excontinue=" + i;

                    // Read url into a huge string.
                    Scanner s = new Scanner(new URL(url + query).openStream());
                    s.useDelimiter("\\Z");
                    String response = s.next();

                    // Extract article content from the response.
                    //String response = new String(Files.readAllBytes(Paths.get("file")), StandardCharsets.UTF_8); //TODO Use nio instead.
                    Pattern pattern = Pattern.compile("<extract xml:space=\"preserve\">(.*)</extract>");
                    Matcher matcher = pattern.matcher(response);
                    matcher.find();
                    String content = matcher.group(1);

                    // Write article content to file.
                    out.print(content);
                }
                articles.add(new File(searchTerm + ".txt"));
            }
        } catch (IOException e) {
            System.err.println("Wikipedia could not be retrieved.");
        }

        return (File[]) articles.toArray();
    }
}
