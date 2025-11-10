package com.jinjinjara.pola.vision.util;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class AIUtil {

    private AIUtil() {}

    /** presigned S3 URL 등 외부 리소스를 직접 GET으로 내려받아 바이트 반환 */
    public static byte[] directDownloadBytes(String urlStr, long maxBytes) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            if (conn instanceof HttpsURLConnection https) {
                https.setInstanceFollowRedirects(true);
            }
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("User-Agent", "pola-http/1.0");

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 400) ? conn.getInputStream() : conn.getErrorStream();
            if (is == null) throw new RuntimeException("No response stream, status=" + code);

            long declared = conn.getContentLengthLong(); // -1 가능
            if (declared > 0 && declared > maxBytes) {
                throw new RuntimeException("Object too large (Content-Length): " + declared);
            }

            byte[] data = is.readAllBytes(); // JDK 11+
            if (data.length > maxBytes) {
                throw new RuntimeException("Object too large (actual): " + data.length);
            }
            if (code < 200 || code >= 300) {
                String snippet = new String(data, 0, Math.min(256, data.length), StandardCharsets.UTF_8);
                throw new RuntimeException("HTTP " + code + " bodySnippet=" + snippet);
            }
            return data;
        }
        catch (Exception e) {
            throw new Exception("directDownloadBytes failed: " + urlStr, e);
        }
        finally {
            if (conn != null) conn.disconnect();
        }
    }

    /** 간단 매직바이트 MIME 추정 */
    public static String sniffMime(byte[] bytes) {
        if (bytes.length >= 3 && bytes[0] == (byte)0xFF && bytes[1] == (byte)0xD8) return "image/jpeg";
        if (bytes.length >= 8 &&
                bytes[0] == (byte)0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) return "image/png";
        if (bytes.length >= 12 &&
                bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F' &&
                bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') return "image/webp";
        if (bytes.length >= 6 &&
                bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == '8' &&
                (bytes[4] == '7' || bytes[4] == '9') && bytes[5] == 'a') return "image/gif";
        return "text/plain";
    }
}
