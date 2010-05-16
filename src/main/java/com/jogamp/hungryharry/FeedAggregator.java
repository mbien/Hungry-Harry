/*
 * Created on Saturday, May 14 2010 17:08
 */
package com.jogamp.hungryharry;

import com.jogamp.hungryharry.Config.Planet;
import com.sun.syndication.io.SyndFeedOutput;
import java.io.PrintWriter;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.fetcher.FetcherException;
import com.sun.syndication.io.FeedException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Comparator;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import static java.util.Collections.*;
import static java.util.logging.Level.*;
import static java.io.File.*;

/**
 * Always hungry, always.
 *
 * @author Michael Bien
 *
 */
public class FeedAggregator {
    
    private static final Logger LOG = Logger.getLogger(FeedAggregator.class.getName());


    private void aggregate() throws MalformedURLException {

        Config config = null;
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(Config.class).createUnmarshaller();
            Object obj = unmarshaller.unmarshal(getClass().getResourceAsStream("config.xml"));
            config = (Config) obj;
        } catch (JAXBException ex) {
            throw new RuntimeException("can not read configuration", ex);
        }


        List<Config.Feed> feeds = config.feed;

        List<URL> urls = new ArrayList<URL>();
        for (Config.Feed feed : feeds) {
            urls.add(new URL(feed.url));
        }


        List<SyndEntry> entries = new ArrayList<SyndEntry>();

        FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
        FeedFetcher feedFetcher = new HttpURLFeedFetcher(feedInfoCache);

        for (int i = 0; i < urls.size(); i++) {
            try {
                SyndFeed inFeed = feedFetcher.retrieveFeed(urls.get(i));
                entries.addAll(inFeed.getEntries());
            } catch (IOException ex) {
                LOG.log(WARNING, "skipping feed", ex);
            } catch (FetcherException ex) {
                LOG.log(WARNING, "skipping feed", ex);
            } catch (FeedException ex) {
                LOG.log(WARNING, "skipping feed", ex);
            }
        }

        sort(entries, new Comparator<SyndEntry>() {
            @Override
            public int compare(SyndEntry o1, SyndEntry o2) {
                return o2.getPublishedDate().compareTo(o1.getPublishedDate());
            }
        });

        Planet planet = config.planet;
        String path = cutoffTail(planet.templatePath, separatorChar);

        for (String feedType : planet.feeds) {

            try {
                SyndFeed feed = new SyndFeedImpl();
                feed.setFeedType(feedType);

                feed.setTitle(planet.title);
                feed.setDescription(planet.description);
                feed.setAuthor(planet.author);
                feed.setLink(planet.link);
                feed.setEntries(entries);

                SyndFeedOutput output = new SyndFeedOutput();

                output.output(feed, new File(path+separatorChar+feedType+".xml"));

            } catch (IOException ex) {
                LOG.log(SEVERE, null, ex);
            } catch (FeedException ex) {
                LOG.log(SEVERE, null, ex);
            }
        }

        StringBuilder content = new StringBuilder();
        int max = 20;
        int n = 0;
        for (SyndEntry entry : entries) {
            if(n++>max) {
                break;
            }
            String link = entry.getLink();
            for (Config.Template template : config.template) {
                if(link.contains(template.keyword)) {
                    Pattern pattern = Pattern.compile(template.idpattern);
                    Matcher matcher = pattern.matcher(link);
                    matcher.find();
                    content.append(template.text.replaceAll("#id#", matcher.group(1)));
                    break;
                }
            }

        }

        try {
            StringBuilder template = readFileAsString(planet.templatePath);

            replace(template, "@atom@", planet.link);
            replace(template, "@rss@", planet.link);
            replace(template, "@content@", content.toString());

            FileOutputStream fos = new FileOutputStream(new File(path+separator+"planet.html"));
            fos.write(template.toString().getBytes());

        } catch (IOException ex) {
            LOG.log(SEVERE, null, ex);
        }
    }

    private String cutoffTail(String text, char cut) {
        return text.substring(0, text.lastIndexOf(cut));
    }

    private StringBuilder replace(StringBuilder sb, String token, String replacement) {
        int start = sb.indexOf(token);
        sb.replace(start, start+token.length(), replacement);
        return sb;
    }

    private static StringBuilder readFileAsString(String filePath) throws java.io.IOException{
        StringBuilder fileData = new StringBuilder(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData;
    }



    public static void main(String[] args) throws MalformedURLException {
        new FeedAggregator().aggregate();
    }

}
