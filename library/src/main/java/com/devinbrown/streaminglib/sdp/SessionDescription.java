package com.devinbrown.streaminglib.sdp;

import android.media.MediaFormat;

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
    public String emailAddress;
    public String phoneNumber;
    public TimeZoneAdjustments timeZoneAdjustments;
    public List<TimeDescription> timeDescriptions;
    public List<MediaDescription> mediaDescriptions;

    public SessionDescription() {
        version = 0;
        bandwidths = new ArrayList<>();
        attributes = new ArrayList<>();
        timeDescriptions = new ArrayList<>();
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

        if (l.length() > 0) {
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
                    if (sd != null) sd.emailAddress = value;
                    break;
                case 'p':
                    if (sd != null) sd.phoneNumber = value;
                    break;
                case 't':
                    if (sd != null) {
                        TimeDescription currentTd = new TimeDescription(Timing.fromString(value));
                        sd.timeDescriptions.add(currentTd);
                    }
                    break;
                case 'r':
                    // TODO: No support for repeat times. This requires this parser to be stateful. Might require some refactoring of the parser.
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
        }

        return md;
    }

    public static SessionDescription fromMediaFormats(List<MediaFormat> m) {
        SessionDescription sd = new SessionDescription();

        // SessionDescription
        // TODO: Make a better way of setting this
        sd.sessionName = "Android RTSP Server";
        // TODO: Get IP address here
        sd.origin = new Origin("-", "0", "0", "IN", "IP4", "0.0.0.0");

        // MediaDescription(s)
        int dynamicPayloadType = 96;
        for (MediaFormat mf : m) {
            MediaDescription md = MediaDescription.fromMedia(mf, dynamicPayloadType++);
            sd.mediaDescriptions.add(md);
        }
        return sd;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("v=").append(version).append(CRLF);
        sb.append("o=").append(origin).append(CRLF);
        sb.append("s=").append(sessionName).append(CRLF);
        if (information != null) sb.append("i=").append(information).append(CRLF);
        if (uri != null) sb.append("u=").append(information).append(CRLF);
        if (emailAddress != null) sb.append("e=").append(emailAddress).append(CRLF);
        if (phoneNumber != null) sb.append("p=").append(phoneNumber).append(CRLF);
        if (connection != null) sb.append(connection).append(CRLF);
        for (Bandwidth b : bandwidths) sb.append(b).append(CRLF);
        for (TimeDescription d : timeDescriptions) sb.append(d).append(CRLF);
        if (timeZoneAdjustments != null) sb.append(timeZoneAdjustments).append(CRLF);
        if (key != null) sb.append(key).append(CRLF);
        for (Attribute a : attributes) sb.append(a).append(CRLF);
        for (MediaDescription m : mediaDescriptions) sb.append(m).append(CRLF);

        return sb.toString();
    }
}
