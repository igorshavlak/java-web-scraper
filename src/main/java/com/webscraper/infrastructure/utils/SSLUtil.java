package com.webscraper.infrastructure.utils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Utility class for SSL configuration.
 * This class provides a method to disable SSL certificate verification for HTTPS connections.
 */
public class SSLUtil {

    /**
     * Disables SSL certificate and hostname verification.
     * This method configures the SSL context to trust all certificates and sets a hostname verifier
     * that accepts all hostnames. Use this method only for testing or specific scenarios (e.g., web scraping),
     * not in production environments.
     */
    public static void disableSslVerification() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        /**
                         * Returns an empty array of accepted issuers.
                         *
                         * @return an empty array of X509Certificates
                         */
                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }

                        /**
                         * Does not perform any checks on client certificates.
                         *
                         * @param certs    the client's certificate chain
                         * @param authType the authentication type based on the client certificate
                         */
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                            // Do nothing
                        }

                        /**
                         * Does not perform any checks on server certificates.
                         *
                         * @param certs    the server's certificate chain
                         * @param authType the key exchange algorithm used
                         */
                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                            // Do nothing
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            HostnameVerifier allHostsValid = new HostnameVerifier() {
                /**
                 * Always returns true, meaning any hostname is accepted.
                 *
                 * @param hostname the hostname
                 * @param session  the current SSL session
                 * @return true always
                 */
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}