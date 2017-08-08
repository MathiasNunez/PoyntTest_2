package mnunez.com.poynttest.network;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by mnunez on 5/26/17.
 */
public class RestApihHandler {

    private OkHttpClient mHttpClient;

    protected RestApihHandler() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.connectTimeout(60, TimeUnit.SECONDS);
        clientBuilder.readTimeout(60, TimeUnit.SECONDS);
        clientBuilder.writeTimeout(60, TimeUnit.SECONDS);
        clientBuilder.retryOnConnectionFailure(true);
        mHttpClient = clientBuilder.build();
    }

    protected void execute(HttpMethodEnum method, String url, HashMap<String, String> params,
                           RestApiCallback<Response> callback, HashMap<String, String> headers) {
        switch (method) {
            case POST:
                doPost(url, params, callback, headers);
                break;
            case GET:
                doGet(url, callback, headers);
                break;
            default:
                break;
        }
    }

    private void doPost(String url, HashMap<String, String> params,
                        final RestApiCallback<Response> callback, HashMap<String, String> headers) {
        try {
            MediaType mMediaType = MediaType.parse("application/json");
            Gson gson = new Gson();
            String body = gson.toJson(params);
            RequestBody requestBody = RequestBody.create(mMediaType, body);
            Request.Builder requestBuilder = new Request.Builder();
            requestBuilder.url(url).post(requestBody);
            this.addHeaders(requestBuilder, headers);
            Request request = requestBuilder.build();
            mHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    callback.onSuccess(response);
                }
            });
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    private void doGet(String url, final RestApiCallback<Response> callback, HashMap<String, String> headers) {
        try {
            Request.Builder requestBuilder = new Request.Builder();
            requestBuilder.url(url);
            this.addHeaders(requestBuilder, headers);
            Request request = requestBuilder.build();
            mHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    callback.onSuccess(response);
                }
            });
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    private void addHeaders(Request.Builder requestBuilder, HashMap<String, String> headers){
        for(String k: headers.keySet()){
            requestBuilder.addHeader(k, headers.get(k));
        }
    }

}
