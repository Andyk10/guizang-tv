package io.github.op7418.guizangtv;

import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/** Adds the missing public CA only to the two weather hosts on the API 22 build. */
final class WeatherTls {
    private WeatherTls() { }

    static boolean needsRoot(boolean legacy, int sdk, URL url) {
        return legacy && sdk == 22 && "https".equals(url.getProtocol())
                && (url.getPort() == -1 || url.getPort() == 443)
                && ("api.open-meteo.com".equals(url.getHost())
                    || "geocoding-api.open-meteo.com".equals(url.getHost()));
    }

    static SSLSocketFactory factory(InputStream source) throws Exception {
        X509Certificate root = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(source);
        root.checkValidity();
        root.verify(root.getPublicKey());
        if (root.getBasicConstraints() < 0) throw new java.security.cert.CertificateException("Not a CA");
        KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
        store.load(null, null);
        store.setCertificateEntry("isrg-root-x1", root);
        TrustManagerFactory trust = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trust.init(store);
        SSLContext tls = SSLContext.getInstance("TLS");
        tls.init(null, trust.getTrustManagers(), null);
        // HttpsURLConnection retains its normal hostname verification.
        return tls.getSocketFactory();
    }
}
