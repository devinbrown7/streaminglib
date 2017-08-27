package com.devinbrown.streaminglib.sdp;

/**
 * https://tools.ietf.org/html/rfc4566#section-5.13
 * Format: a=<attribute>
 * Format: a=<attribute>:<value>
 */

public class Attribute {

    String attribute;
    String value;

    private Attribute(String a, String v) {
        attribute = a;
        value = v;
    }

    public static Attribute fromString(String s) {
        String attribute;
        String value = null;

        String[] attributeArray = s.split(":", 2);

        attribute = attributeArray[0];

        if (attributeArray.length == 2) {
            value = attributeArray[1].trim();
        }

        return new Attribute(attribute, value);
    }

    @Override
    public String toString() {
        return "a=" + attribute + ":" + value;
    }
}
