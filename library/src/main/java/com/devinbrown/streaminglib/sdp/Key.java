package com.devinbrown.streaminglib.sdp;

/**
 * https://tools.ietf.org/html/rfc4566#section-5.12
 * Format: k=<mode>
 * Format: k=<mode>:<encryption key>
 */

public class Key {

    enum Method {CLEAR, BASE64, URI, PROMPT}

    public Method method;
    public String key;

    private Key(Method m, String k) {
        method = m;
        key = k;
    }

    public static Key fromString(String s) {
        Method method;
        String key = null;

        String[] keyArray = s.split(":", 2);

        method = Method.valueOf(keyArray[0].toUpperCase());

        if (keyArray.length == 2 && method != Method.PROMPT) {
            key = keyArray[1].trim();
        }

        return new Key(method, key);
    }

    @Override
    public String toString() {
        return "k=" + method.name() + ":" + key;
    }
}
