package com.example.android.sportsnews;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pierrick on 08/04/2015.
 */
public class RssParser {

    private final String ns = null;

    //Read the rss feed
    public List<RssItem> parse(InputStream inputStream) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            inputStream.close();
        }
    }

    private List<RssItem> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "rss");
        String title = null;
        String link = null;
        String date = null;
        String copyright;
        String description = null;
        String image = null;
        List<RssItem> items = new ArrayList<RssItem>();
        //recover data from the rss feed
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            copyright = null;
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("title")) {
                title = readTitle(parser);
            } else if (name.equals("link")) {
                link = readLink(parser);
            } else if (name.equals("pubDate")) {
                date = readDate(parser);
            } else if (name.equals("copyright")) {
                copyright = readCopyright(parser);
            } else if (name.equals("description")) {
                description = readDescription(parser);
            } else if (name.equals("enclosure")) {
                image = readImage(parser);
            }
            //If the data is not null then populate the item
            if (title != null && link != null && date != null && copyright == null & description != null && image != null) {
                RssItem item = new RssItem(title, link, date, description, image);
                items.add(item);
                title = null;
                link = null;
                date = null;
                description = null;
                image = null;
            }
        }
        return items;
    }

    //recovery Link
    private String readLink(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "link");
        String link = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "link");
        return link;
    }

    //recovery Title
    private String readTitle(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "title");
        return title;
    }

    //recovery Time
    private String readDate(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "pubDate");
        String date = readText(parser);
        date = (date.replace(" ","")).substring(13,18);
        parser.require(XmlPullParser.END_TAG, ns, "pubDate");
        return date;
    }

    //recovery copyright
    private String readCopyright(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "copyright");
        String copyright = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "copyright");
        return copyright;
    }

    //recovery description
    private String readDescription(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "description");
        String description = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "description");
        return description;
    }

    //recovery url
    private String readImage(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "enclosure");
        String image = readText(parser);
        String url = parser.getAttributeValue(ns, "url");
        parser.require(XmlPullParser.END_TAG, ns, "enclosure");
        return url;
    }

    // For the tags title and link, extract their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }
}
