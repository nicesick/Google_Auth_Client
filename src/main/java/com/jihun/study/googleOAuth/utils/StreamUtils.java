package com.jihun.study.googleOAuth.utils;

import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class StreamUtils {
    public static final String METHOD_GET   = HttpMethod.GET.name();
    public static final String METHOD_POST  = HttpMethod.POST.name();

    public static String getStream(String url, final String method, @Nullable final Map<String, String> headers, @Nullable final Map<String, String> params) {
        StringBuffer result = new StringBuffer();

        try {
            String paramStr = getRequestGetParams(params);

            if (method.equals(METHOD_GET) && params != null) {
                if (url.indexOf('?') < 0) {
                    url += "?" + paramStr;
                } else {
                    url += "&" + paramStr;
                }
            }

            System.out.println("getStream : URL    = " + url);
            System.out.println("getStream : METHOD = " + method);

            URL requestUrl                  = new URL(url);
            HttpURLConnection connection    = (HttpURLConnection) requestUrl.openConnection();

            connection.setRequestMethod(method);

            if (headers != null) {
                for (String headerKey : headers.keySet()) {
                    connection.setRequestProperty(headerKey, headers.get(headerKey));
                }
            }

            if (method.equals(METHOD_POST) && params != null) {
                connection.setDoOutput(true);
                try (DataOutputStream output = new DataOutputStream(connection.getOutputStream())) {
                    output.writeBytes(paramStr);
                    output.flush();
                }
            }

            int responseCode = connection.getResponseCode();
            System.out.println("getStream : responseCode = " + responseCode);

            if (responseCode == 200) {
                try (BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;

                    while ((line = input.readLine()) != null) {
                        result.append(line);
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return result.toString();
        }
    }

    public static String getRequestGetParams(Map<String, String> params) {
        StringBuilder getParamsBuilder = new StringBuilder();

        for (String paramKey : params.keySet()) {
            if (getParamsBuilder.length() > 1) {
                getParamsBuilder.append("&");
            }

            getParamsBuilder.append(paramKey);
            getParamsBuilder.append("=");
            getParamsBuilder.append(params.get(paramKey));
        }

        return getParamsBuilder.toString();
    }
}
