package com.seedit.feature.news.external;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class RssParser{
    public List<RssArticle> parse(byte[] feedXml, String press){
        List<RssArticle> articles = new ArrayList<>();
        try {
            String enc = new XmlReader(new ByteArrayInputStream(feedXml)).getEncoding();
            String xml = new String(feedXml, enc)
                    .replaceAll("([+-]\\d{2}):(\\d{2})(\\s*</pubDate>)","$1$2$3");
            SyndFeed feed = new SyndFeedInput().build(new XmlReader(new ByteArrayInputStream(xml.getBytes(enc))));
            for(SyndEntry e : feed.getEntries()){
                LocalDateTime pub = (e.getPublishedDate() == null) ? null
                        : e.getPublishedDate().toInstant()
                        .atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime();
                articles.add(new RssArticle(e.getTitle(),e.getLink(),pub,press));
            }
        } catch (Exception e){
            throw new RuntimeException("RSS 파싱 실패: " + press, e);
        }
        return articles;
    }
}