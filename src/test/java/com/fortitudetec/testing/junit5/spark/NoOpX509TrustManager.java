package com.fortitudetec.testing.junit5.spark;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

class NoOpX509TrustManager implements X509TrustManager {
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    public void checkClientTrusted(X509Certificate[] certs, String authType) {
    }

    public void checkServerTrusted(X509Certificate[] certs, String authType) {
    }
}
