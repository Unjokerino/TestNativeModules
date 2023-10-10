package com.testnativemodules;

import android.util.Log;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class RequestModule extends ReactContextBaseJavaModule {

  public RequestModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public String getName() {
    return "RequestModule";
  }

  public static class RequestParams {
    public String type;
    public String url;
    public HashMap<String, Object> body;
    public HashMap<String, Object> headers;
  }

  @ReactMethod
  public void makeRequest(
    String urlString,
    ReadableMap paramsMap,
    Promise promise
  ) {
    RequestParams params = new RequestParams();
    WritableMap result = new WritableNativeMap();
    HttpURLConnection urlConnection = null;

    params.url = urlString;

    if (paramsMap != null && paramsMap.hasKey("type")) {
      params.type = paramsMap.getString("type");
    }
    if (paramsMap != null && paramsMap.hasKey("body")) {
      params.body = paramsMap.getMap("body").toHashMap();
    }
    if (paramsMap != null && paramsMap.hasKey("headers")) {
      params.headers = paramsMap.getMap("headers").toHashMap();
    }

    try {
      if (urlString == null || urlString.isEmpty()) {
        throw new IllegalArgumentException("URL is required");
      }
      if (paramsMap == null || !paramsMap.hasKey("type")) {
        throw new IllegalArgumentException("Request 'type' is required");
      }
      if (params.type == null || (!params.type.equals("GET") && !params.type.equals("POST"))) {
        throw new IllegalArgumentException("Invalid or null request method type");
      }
      URL url = new URL(urlString);
      urlConnection = (HttpURLConnection) url.openConnection();
      urlConnection.setRequestMethod(params.type);
      
      if (params.headers != null) {
        for (Map.Entry<String, Object> header : params.headers.entrySet()) {
          urlConnection.setRequestProperty(header.getKey(), (String) header.getValue());
        }
      }

      if (params.type.equals("POST")) {
        urlConnection.setDoOutput(true);
        try (OutputStream os = urlConnection.getOutputStream()) {
          byte[] input = params.body.toString().getBytes("utf-8");
          os.write(input, 0, input.length);
        }
      }
      InputStream inputStream;

      int responseCode = urlConnection.getResponseCode();
      result.putInt("statusCode", responseCode);

      if (responseCode == HttpURLConnection.HTTP_OK) {
        inputStream = urlConnection.getInputStream();
      } else {
        inputStream = urlConnection.getErrorStream();  // Get error details
      }


      try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
        String inputLine;
        StringBuffer content = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
          content.append(inputLine);
        }

        if (responseCode == HttpURLConnection.HTTP_OK) {
          result.putString("type", "success");
          result.putString("data", content.toString());
        } else {
          result.putString("type", "error");
          result.putString("error", content.toString());
        }
      } catch (IOException e) {
        throw new IOException("Server returned non-OK status: " + responseCode);
      }
    } catch (Exception e) {
      result.putString("type", "error");
      result.putString("error", e.toString());
    } finally {
      promise.resolve(result);
      if (urlConnection != null) {
        urlConnection.disconnect();
      }
    }
  }
}
