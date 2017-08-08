package mnunez.com.poynttest.network;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import okhttp3.Response;

/**
 * Created by mnunez on 6/30/17.
 */

public class ServicesHandler extends RestApihHandler {

    public ServicesHandler() {
    }

    public void doGetBankApps(final RestApiCallback<String> callback) {
        execute(HttpMethodEnum.GET, "http://banredsdk.azurewebsites.net/api/sdk/Init", null, new RestApiCallback<Response>() {
            @Override
            public void onSuccess(Response response) {
                try {
                    Gson gson = new Gson();
                    JsonObject jsonObject = gson.fromJson(response.body().charStream(), JsonObject.class);
                    JsonArray jsonArray = jsonObject.getAsJsonArray("appList");
                    callback.onSuccess(jsonArray.getAsString());
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }

        }, this.setHeaders());
    }

    private HashMap<String, String> setHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("AppKey", "85822C18-2A01-4EE8-8CEC-06FFF38B0104");
        headers.put("PlatformId", "1");
        return headers;
    }

}
