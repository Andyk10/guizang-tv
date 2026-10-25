package io.github.op7418.guizangtv;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import org.junit.Test;
import static org.junit.Assert.*;

public class WeatherTlsTest {
    @Test public void extraRootIsLimitedToLegacyWeatherHosts() throws Exception {
        for (String host : new String[]{"api.open-meteo.com", "geocoding-api.open-meteo.com"}) {
            URL url = new URL("https://" + host + "/v1/search");
            assertTrue(WeatherTls.needsRoot(true, 22, url));
            assertFalse(WeatherTls.needsRoot(false, 22, url));
            assertFalse(WeatherTls.needsRoot(true, 26, url));
        }
        for (String url : new String[]{"http://api.open-meteo.com", "https://api.open-meteo.com:444",
                "https://api.open-meteo.com.example.org", "https://ipwho.is", "https://example.org"})
            assertFalse(WeatherTls.needsRoot(true, 22, new URL(url)));
    }

    @Test public void bundledCertificateIsThePublishedIsrgRootX1() throws Exception {
        try (FileInputStream source = root()) {
            X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(source);
            StringBuilder hash = new StringBuilder();
            for (byte b : MessageDigest.getInstance("SHA-256").digest(cert.getEncoded())) hash.append(String.format("%02x", b & 255));
            assertEquals("96bcec06264976f37460779acf28c5a7cfe8a3c0aae11a8ffcee05c0bddf08c6", hash.toString());
            assertTrue(cert.getBasicConstraints() >= 0);
            cert.verify(cert.getPublicKey());
        }
        try (FileInputStream source = root()) { assertNotNull(WeatherTls.factory(source)); }
    }

    @Test(expected = java.security.cert.CertificateException.class)
    public void invalidCertificateNeverBecomesABypass() throws Exception {
        WeatherTls.factory(new ByteArrayInputStream(new byte[0]));
    }

    private FileInputStream root() throws Exception { return new FileInputStream("src/main/res/raw/isrg_root_x1.crt"); }
}
