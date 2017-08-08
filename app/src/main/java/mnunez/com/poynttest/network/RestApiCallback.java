package mnunez.com.poynttest.network;

/**
 * Created by mnunez on 5/26/17.
 */
public interface RestApiCallback<T> {

    void onSuccess(T object);

    void onError(String message);

}