package com.devinbrown.streaminglib.rtsp;

import com.devinbrown.streaminglib.Utils;

public class RtspAuth {
    private static final String TAG = "RtspAuth";

    public static class AuthParams {
        String username;
        String password;

        public AuthParams(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static class DigestAuth {
        public String username;
        public String realm;
        public String nonce;
        public String uri;
        public String response;

        private DigestAuth(String username, String realm, String nonce, String uri, String response) {
            this.username = username;
            this.realm = realm;
            this.nonce = nonce;
            this.uri = uri;
            this.response = response;
        }

        public static DigestAuth fromString(String s) {
            DigestAuth digestAuth = null;
            String username = null;
            String realm = null;
            String nonce = null;
            String uri = null;
            String response = null;

            String[] sArray = s.split(" ", 2);
            if (sArray.length == 2) {
                sArray = sArray[1].split(",");
                for (String param : sArray) {
                    String[] pair = param.trim().split("=");
                    if (pair.length == 2) {
                        String key = pair[0];
                        String value = Utils.trim(pair[1], "\"");
                        switch (key.trim()) {
                            case "username":
                                username = value;
                                break;
                            case "realm":
                                realm = value;
                                break;
                            case "nonce":
                                nonce = value;
                                break;
                            case "uri":
                                uri = value;
                                break;
                            case "response":
                                response = value;
                                break;
                        }
                    }
                }
                if (username != null && realm != null && nonce != null && uri != null && response != null) {
                    digestAuth = new DigestAuth(username, realm, nonce, uri, response);
                }
            }

            return digestAuth;
        }
    }

    public static class BasicAuth {
        public String response;

        private BasicAuth(String response) {
            this.response = response;
        }

        public static BasicAuth fromString(String s) {
            BasicAuth basicAuth = null;
            String[] sArray = s.split(" ", 2);
            if (sArray.length == 2) {
                basicAuth = new BasicAuth(sArray[1].trim());
            }
            return basicAuth;
        }
    }

    /**
     * Formula:
     *      HA1=MD5(username:realm:password)
     *      HA2=MD5(method:digestURI)
     *      hash=MD5(HA1:nonce:HA2)
     */
    public static String generateDigestAuth(String username,
                                            String password,
                                            Rtsp.Method method,
                                            String uri,
                                            String realm,
                                            String nonce) {
        String ha1 = Utils.encodeMD5Hash(username + ":" + realm + ":" + password);
        String ha2 = Utils.encodeMD5Hash(method.name() + ":" + uri);
        return Utils.encodeMD5Hash(ha1 + ":" + nonce + ":" + ha2);
    }

    /**
     * Formula: hash=MD5(username:password)
     */
    public static String generateBasicAuth(String username, String password) {
        return Utils.encodeMD5Hash(username + ":" + password);
    }

    public static boolean authenticateRequest(RtspRequest r, AuthParams auth, String realm,  String nonce) {
        if (RtspAuth.methodRequiredAuthentication(r.getMethod()) && auth != null) {
            String providedAuth;
            String calculatedAuth;

            String providedDigestAuth = r.getAuthorizationDigest();
            String providedBasicAuth = r.getAuthorizationBasic();

            if (providedDigestAuth == null && providedBasicAuth == null) {
                // There was not an attempt to authenticate
                return false;
            }

            // Try to find Digest auth first
            if (providedDigestAuth != null) {
                // Use Digest auth
                RtspAuth.DigestAuth digestAuth = RtspAuth.DigestAuth.fromString(providedDigestAuth);
                providedAuth = digestAuth.response;
                calculatedAuth = RtspAuth.generateDigestAuth(auth.username, auth.password, r.getMethod(), digestAuth.uri, realm, nonce);
            } else {
                // Use Basic auth
                RtspAuth.BasicAuth basicAuth = RtspAuth.BasicAuth.fromString(providedBasicAuth);
                providedAuth = basicAuth.response;
                calculatedAuth = RtspAuth.generateBasicAuth(auth.username, auth.password);
            }

            if (providedAuth == null || !providedAuth.equals(calculatedAuth)) {
                return false;
            }
        }
        return true;
    }

    public static String generateNonce() {
        long startTime = System.nanoTime();
        String n = Utils.encodeMD5Hash(Utils.getNewSsrc()).toLowerCase();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000;
        return n;
    }

    private static boolean methodRequiredAuthentication(Rtsp.Method method) {
        switch (method) {
            case OPTIONS:
                return false;
            default:
                return true;
        }
    }
}
