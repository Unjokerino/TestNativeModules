package com.testnativemodules;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
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

  public static class ResponseData {
    public String type;
    public String data;
    public int statusCode;
  }
  public class RequestHandler {
    private RequestParams params;

    public RequestHandler(RequestParams params) {
      this.params = params;
    }

    public ResponseData execute() throws Exception {
      if (params.url == null || params.url.isEmpty()) {
        throw new IllegalArgumentException("URL is required");
      }
      if (params.type == null || (!params.type.equals("GET") && !params.type.equals("POST"))) {
        throw new IllegalArgumentException("Invalid or null request method type");
      }

      HttpURLConnection urlConnection = null;
      ResponseData responseData = new ResponseData();

      try {
        URL url = new URL(params.url);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod(params.type);

        if (params.headers != null) {
          for (Map.Entry<String, Object> header : params.headers.entrySet()) {
            urlConnection.setRequestProperty(header.getKey(), (String) header.getValue());
          }
        }

        if ("POST".equals(params.type)) {
          urlConnection.setDoOutput(true);
          try (OutputStream os = urlConnection.getOutputStream()) {
            byte[] input = params.body.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
          }
        }

        int responseCode = urlConnection.getResponseCode();
        responseData.statusCode = responseCode;

        InputStream inputStream = (responseCode == HttpURLConnection.HTTP_OK) ?
                urlConnection.getInputStream() : urlConnection.getErrorStream();

        StringBuilder content = new StringBuilder();

        try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
          String inputLine;
          while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
          }
        }

        // Contains error message
        if (responseCode == HttpURLConnection.HTTP_OK) {
          responseData.type = "success";
        } else {
          responseData.type = "error";
        }
        responseData.data = content.toString();

        return responseData;

      } finally {
        if (urlConnection != null) {
          urlConnection.disconnect();
        }
      }
    }
  }

  @ReactMethod
  public void makeRequest(
    String urlString,
    ReadableMap paramsMap,
    Promise promise
  ) {
    RequestParams params = new RequestParams();
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

    if (paramsMap == null || !paramsMap.hasKey("type")) {
      WritableMap result = new WritableNativeMap();
      result.putString("type", "error");
      result.putString("error", "Request 'type' is required");
      promise.resolve(result);
      return;
    }

    RequestHandler handler = new RequestHandler(params);

    try {
      ResponseData responseData = handler.execute();
      WritableMap result = new WritableNativeMap();
      result.putString("type", responseData.type);
      if(responseData.type == "success"){
        result.putString("data", responseData.data);
      } else {
        result.putString("error", responseData.data);
      }
      result.putInt("statusCode", responseData.statusCode);
      promise.resolve(result);
    } catch (Exception e) {
      WritableMap result = new WritableNativeMap();
      result.putString("type", "error");
      result.putString("error", e.toString());
      promise.resolve(result);
    }
  }
}
