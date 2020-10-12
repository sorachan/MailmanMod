package me.dillbox.mailmanmod;

import android.util.Log;

import org.apache.james.mime4j.Charsets;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.james.mime4j.codec.DecoderUtil;
import org.jsoup.select.Elements;

public class MailmanParser {
    static class MailmanParserException extends Exception {
        MailmanParserException(String msg) {
            super(msg);
        }
    }

    public static HeldMail heldMailFromHtml(String html, int msgId, String list) throws MailmanParserException {
        Document doc = Jsoup.parse(html);
        Element headerTextArea = doc.select("textarea[name=\"headers-"+msgId+"\"]").first();
        if (headerTextArea == null) {
            throw new MailmanParserException("header field 'headers-'"+msgId+" not found");
        }
        Element mailTextArea = doc.select("textarea[name=\"fulltext-"+msgId+"\"]").first();
        if (mailTextArea == null) {
            throw new MailmanParserException("message field 'fulltext-'"+msgId+" not found");
        }
        String[] headerStrings = headerTextArea.html().split("\\n(?=[A-Za-z-]*: )");
        HashMap<String, String> headerMap = new HashMap<>();
        for (String headerString : headerStrings) {
            String[] headerArray = headerString.split(": ", 2);
            if (headerArray.length > 1) {
                String headerKey = headerArray[0];
                String headerValue = headerArray[1];
                if (!headerMap.containsKey(headerKey)) {
                    headerMap.put(headerKey, headerValue);
                }
            }
        }

        String sender = "";
        String email = "";
        String fromRaw = headerMap.get("From").replaceAll("\\n\\t", " ");
        if (fromRaw != null) {
            String from = org.jsoup.parser.Parser.unescapeEntities(DecoderUtil.decodeEncodedWords(fromRaw, Charsets.DEFAULT_CHARSET), true);
            // this regex matches different mail address formats:
            // John Doe <jdoe@example.org> -> groups 5 and 6
            // "John \"J.\" Doe" <jdoe@example.org> -> groups 4 and 6
            // <jdoe@example.org> -> group 6
            // jdoe@example.org -> group 7
            Matcher matcher = Pattern.compile("(((\"(.*?)\"|(.*?)) )?<(.*?)>|(.*))").matcher(from);
            if (matcher.find()) {
                if (matcher.group(4) != null) { // i.e. 4 = John \"J.\" Doe
                    sender = matcher.group(4).replaceAll("\\\\\"", "\"");
                } else if (matcher.group(5) != null) { // i.e. 5 = John Doe
                    sender = matcher.group(5);
                }

                if (matcher.group(6) != null) { // i.e. 6 = <jdoe@example.org>
                    email = matcher.group(6);
                } else { // i.e. 7 = jdoe@example.org
                    email = matcher.group(7);
                }
            }
        }

        String subject = "";
        String subjectRaw = headerMap.get("Subject");
        if (subjectRaw != null) {
            subject = org.jsoup.parser.Parser.unescapeEntities(DecoderUtil.decodeEncodedWords(subjectRaw, Charsets.DEFAULT_CHARSET), true);
        }

        String timestamp = "";
        String receivedRaw = headerMap.get("Received").replaceAll("\\n\\t", " ");
        Log.i("rcvd", "raw: "+receivedRaw);
        if (receivedRaw != null) {
            String received = receivedRaw;
            Matcher matcher = Pattern.compile("; (\\w{3}, [ ]?\\d{1,2} \\w{3} \\d{4} \\d{2}:\\d{2}:\\d{2} [+-]\\d{4})( \\(\\w{3,4}\\))?").matcher(received);
            if (matcher.find() && matcher.group(1) != null) {
                timestamp = matcher.group(1);
            }
        }
        Log.i("rcvd", "stamp: "+timestamp);

        String headers = org.jsoup.parser.Parser.unescapeEntities(headerTextArea.html(), true);
        String message = org.jsoup.parser.Parser.unescapeEntities(mailTextArea.html(), true);

        return new HeldMail(msgId, list, sender, email, subject, timestamp, headers, message);
    }

    public static List<Integer> mailQueueFromHtml(String html) {
        Document doc = Jsoup.parse(html);
        Elements msgId_links = doc.select("a[href*=\"?msgid=\"]");
        List<Integer> msgId_list = new ArrayList<>();
        Pattern msgId_pat = Pattern.compile("(\\?|&)msgid=(\\d*)");
        for (Element msgId_link : msgId_links) {
            String href= msgId_link.attr("href");
            Matcher matcher = msgId_pat.matcher(href);
            if (matcher.find() && matcher.group(2) != null) {
                msgId_list.add(Integer.parseInt(matcher.group(2)));
            }
        }

        return msgId_list;
    }
}
