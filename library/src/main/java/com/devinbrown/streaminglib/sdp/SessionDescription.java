package com.devinbrown.streaminglib.sdp;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.devinbrown.streaminglib.Constants.CRLF;

/**
 * SDP Version 0
 * <p>
 * Specification: https://tools.ietf.org/html/rfc2327
 */

public class SessionDescription extends Description {
    private static final String TAG = "SessionDescription";

    public Integer version;
    public Origin origin;
    public URI uri;
    public String sessionName;
    public List<String> emails;
    public List<String> phones;
    public List<Timing> timings;
    public List<RepeatTimes> repeatTimes;
    public List<MediaDescription> mediaDescriptions;
    public TimeZoneAdjustments timeZoneAdjustments;

    public SessionDescription() {
        version = 0;
        emails = new ArrayList<>();
        phones = new ArrayList<>();
        bandwidths = new ArrayList<>();
        attributes = new ArrayList<>();
        timings = new ArrayList<>();
        mediaDescriptions = new ArrayList<>();
    }

    /**
     * Parses a string representation of a Session Description into a Session Description object
     *
     * @param s string representation of a Session Description
     * @return Session Description object
     */
    public static SessionDescription fromString(String s) {
        SessionDescription sd = new SessionDescription();

        // Split the Session description on media descriptions
        String m = "m=";
        String[] sections = s.split("(?=" + m + ")");

        // Parse each section. The first section is the Session Description and zero or more
        // following sections are Media Descriptions
        for (int i = 0; i < sections.length; i++) {
            if (i == 0) {
                parseSessionDescription(sd, sections[0]);
            } else {
                MediaDescription md = parseMediaDescriptionString(sections[i]);
                sd.mediaDescriptions.add(md);
            }
        }

        return sd;
    }

    private static void parseSessionDescription(SessionDescription sd, String s) {
        String[] sessionLines = s.split(CRLF);
        for (String line : sessionLines) {
            parseLine(sd, line);
        }
    }

    private static MediaDescription parseMediaDescriptionString(String m) {
        String[] mediaLines = m.split(CRLF);
        MediaDescription md = null;
        for (String line : mediaLines) {
            md = parseLine(md, line);
        }
        return md;
    }

    /**
     * Parse a Session Description line
     *
     * @param d generic Description object
     * @param l line of text in the Session Description
     * @return a MediaDescription if the line describes a media description
     */
    private static MediaDescription parseLine(Description d, String l) {
        // Use a more specific cast for the entries that are specific to a particular Description
        SessionDescription sd = null;
        MediaDescription md = null;
        if (d instanceof SessionDescription) {
            sd = (SessionDescription) d;
        } else if (d instanceof MediaDescription) {
            md = (MediaDescription) d;
        }

        char firstChar = l.charAt(0);
        String value = l.substring(2, l.length());

        switch (firstChar) {
            // Session only
            case 'v':
                if (sd != null) sd.version = Integer.parseInt(value);
                break;
            case 'o':
                if (sd != null) sd.origin = Origin.fromString(value);
                break;
            case 's':
                if (sd != null) sd.sessionName = value;
                break;
            case 'u':
                if (sd != null) sd.uri = URI.create(value);
                break;
            case 'e':
                if (sd != null) sd.emails.add(value);
                break;
            case 'p':
                if (sd != null) sd.emails.add(value);
                break;
            case 't':
                if (sd != null) sd.timings.add(Timing.fromString(value));
                break;
            case 'r':
                if (sd != null) sd.repeatTimes.add(RepeatTimes.fromString(value));
                break;
            case 'z':
                if (sd != null) sd.timeZoneAdjustments = TimeZoneAdjustments.fromString(value);
                break;

            // Media only
            case 'm':
                md = MediaDescription.fromString(value);
                break;

            // Either Session or Media Description
            case 'i':
                d.information = value;
                break;
            case 'c':
                d.connection = Connection.fromString(value);
                break;
            case 'b':
                d.bandwidths.add(Bandwidth.fromString(value));
                break;
            case 'k':
                d.key = Key.fromString(value);
                break;
            case 'a':
                d.attributes.add(Attribute.fromString(value));
                break;
        }

        return md;
    }
}
