package com.chaoxing.demo.audioplayer.util;

import android.webkit.CookieManager;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.zip.GZIPInputStream;

/**
 * Created by HuWei on 2017/6/27.
 */

public class Utils {

    public static String loadString(String url) throws Exception {
        DefaultHttpClient httpClient = getHttpClient();
        HttpGet httpRequest = new HttpGet(url);
        httpRequest.addHeader("Accept-Encoding", "gzip");
        try {
            CookieManager cookieManager = CookieManager.getInstance();
            String cookie = cookieManager.getCookie(url);
            if (cookie != null && cookie.trim().length() > 0)
                httpRequest.addHeader("Cookie", cookie);
            final String[] redirectUrls = new String[1];
            redirectUrls[0] = url;
            httpClient.setRedirectHandler(new DefaultRedirectHandler() {
                @Override
                public URI getLocationURI(HttpResponse response, HttpContext context)
                        throws ProtocolException {
                    URI uri = super.getLocationURI(response, context);
                    redirectUrls[0] = uri.toString();
                    return uri;
                }
            });
            String result = null;
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            int status = httpResponse.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                boolean gzip = false;
                HttpEntity entity = httpResponse.getEntity();
                if (entity != null) {
                    Header ceheader = entity.getContentEncoding();
                    if (ceheader != null) {
                        HeaderElement[] codecs = ceheader.getElements();
                        for (int i = 0; i < codecs.length; i++) {
                            if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                                gzip = true;
                                break;
                            }
                        }
                    }
                }

                if (gzip) {
                    GZIPInputStream gis = null;
                    ByteArrayOutputStream baos = null;
                    try {
                        gis = new GZIPInputStream(entity.getContent());
                        baos = new ByteArrayOutputStream();
                        int count;
                        byte[] buffer = new byte[4096];
                        while ((count = gis.read(buffer)) != -1) {
                            baos.write(buffer, 0, count);
                        }
                        result = new String(baos.toByteArray(), "UTF-8");

                    } finally {
                        if (baos != null) {
                            try {
                                baos.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (gis != null) {
                            try {
                                gis.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    result = EntityUtils.toString(httpResponse.getEntity());
                }
//                WebCacheUtil.saveCacheResult(url,result);
            }
            if (httpResponse != null && httpResponse.getEntity() != null) {
                httpResponse.getEntity().consumeContent();
            }
            return result;
        } finally {
            if (!httpRequest.isAborted()) {
                httpRequest.abort();
            }
            ClientConnectionManager manager = httpClient.getConnectionManager();
            if (manager != null) {
                manager.shutdown();
            }
        }
    }

    private static DefaultHttpClient getHttpClient() throws Exception {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        HttpConnectionParams.setConnectionTimeout(params, 30 * 1000);
        HttpConnectionParams.setSoTimeout(params, 30 * 1000);
        return new DefaultHttpClient(params);
    }
}
