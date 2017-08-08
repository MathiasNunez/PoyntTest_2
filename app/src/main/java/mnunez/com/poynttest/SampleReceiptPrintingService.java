package mnunez.com.poynttest;


import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.poynt.api.model.BalanceInquiry;
import co.poynt.api.model.Transaction;
import co.poynt.os.model.Intents;
import co.poynt.os.model.PrintedReceipt;
import co.poynt.os.model.PrintedReceiptLine;
import co.poynt.os.services.v1.IPoyntReceiptPrintingService;
import co.poynt.os.services.v1.IPoyntReceiptPrintingServiceListener;
import co.poynt.os.services.v1.IPoyntReceiptSendListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Dennis Natochy
 * @date 3/6/2017
 * <p>
 * This sample app shows how the default Poynt receipt service (IPoyntReceiptPrintingService)
 * can be overridden such that when the customer taps the receipt button on the second screen
 * *printReceipt* function gets called and a custom receipt can be created
 * <p>
 * Overriding IPoyntReceiptPrintingService has an implication for all the other apps running on
 * the same terminal. So it is your responsibility to be a "good citizen" and implement all of the
 * methods of IPoyntReceiptPrintingService or forward them to the default implementation of the service.
 * <p>
 * Since you are overriding the default receipt it is also your responsibility to make sure that all
 * required fields (e.g. AID, auth code,etc.) are printed. Consult your acquiring partner to make
 * sure your app meets their receipt specification
 * <p>
 * As of 6/6/2016 changing the default receipt printing service requires Poynt to update business
 * settings for the terminal. Contact Poynt and provide the package name of your receipt service.
 */

public class SampleReceiptPrintingService extends Service {

    private static final String TAG = SampleReceiptPrintingService.class.getSimpleName();
    private IPoyntReceiptPrintingService poyntReceiptPrintingService;


    private ServiceConnection receiptPrintingConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "connected to Poynt receipt printing service");
            poyntReceiptPrintingService = IPoyntReceiptPrintingService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            poyntReceiptPrintingService = null;
            Log.d(TAG, "onServiceDisconnected: receiptPrintingConnection");
            bindPoyntReceiptPrintingService();
        }
    };


    private final IPoyntReceiptPrintingService.Stub mBinder = new IPoyntReceiptPrintingService.Stub() {
        public void printTransaction(String jobId, Transaction transaction, long
                tipAmount, boolean signatureCollected, IPoyntReceiptPrintingServiceListener callback)
                throws RemoteException {
            Log.d(TAG, "printTransaction");
            // call default
            if (poyntReceiptPrintingService != null) {
                poyntReceiptPrintingService.printTransaction(jobId, transaction, tipAmount,
                        signatureCollected, callback);
            }
        }

        public void printTransactionReceipt(String jobId, String transactionId, long
                tipAmount, IPoyntReceiptPrintingServiceListener callback) throws RemoteException {
            Log.d(TAG, "printTransactionReceipt");
            // call default
            if (poyntReceiptPrintingService != null) {
                poyntReceiptPrintingService.printTransactionReceipt(jobId, transactionId, tipAmount,
                        callback);
            }
        }

        public void printOrderReceipt(String jobId, String orderId,
                                      IPoyntReceiptPrintingServiceListener callback) throws RemoteException {
            Log.d(TAG, "printOrderReceipt ");
            // call default
            if (poyntReceiptPrintingService != null) {
                poyntReceiptPrintingService.printOrderReceipt(jobId, orderId, callback);
            }
        }

        // This method will be called from the Payment Fragment
        public void printReceipt(final String jobId, final PrintedReceipt receipt,
                                 final IPoyntReceiptPrintingServiceListener callback) throws RemoteException {
            //Adding custom footer
            Log.d(TAG, "printReceipt with PrintedReceipt");

            Handler h = new Handler(Looper.getMainLooper());
            Runnable r = new Runnable() {
                @Override
                public void run() {

                    OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
                    clientBuilder.connectTimeout(60, TimeUnit.SECONDS);
                    clientBuilder.readTimeout(60, TimeUnit.SECONDS);
                    clientBuilder.writeTimeout(60, TimeUnit.SECONDS);
                    clientBuilder.retryOnConnectionFailure(true);
                    OkHttpClient mHttpClient = clientBuilder.build();
                    Request.Builder requestBuilder = new Request.Builder();
                    requestBuilder.url("http://192.168.203.39:8080/poyntTest");
                    Request request = requestBuilder.build();
                    mHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Gson gson = new Gson();
                            JsonElement jsonE = gson.fromJson(response.body().charStream(), JsonElement.class);
                            String token = jsonE.getAsJsonObject().get("token").getAsString();

                            List<PrintedReceiptLine> footerLines = receipt.getFooter();
                            PrintedReceiptLine line = new PrintedReceiptLine();
                            line.setText("Probando WebService");
                            footerLines.add(0, line);
                            PrintedReceiptLine line1 = new PrintedReceiptLine();
                            line1.setText(token);
                            footerLines.add(1, line1);

                            try {
                                poyntReceiptPrintingService.printReceipt(jobId, receipt, callback);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    });


                }
            };
            if (poyntReceiptPrintingService != null) {
                Log.d(TAG, "printReceiptPrintingService was not null");
                h.post(r);
            } else {
                Log.d(TAG, "printReceiptPrintingService was null: ");
                h.postDelayed(r, 2000l);
            }
        }

        public void printBitmap(String jobId, Bitmap bitmap,
                                IPoyntReceiptPrintingServiceListener callback) throws RemoteException {
            Log.d(TAG, "printBitmap ");
            // call default
            if (poyntReceiptPrintingService != null) {
                poyntReceiptPrintingService.printBitmap(jobId, bitmap, callback);
            }
        }

        @Override
        public void printStayReceipt(String s, String s1,
                                     IPoyntReceiptPrintingServiceListener callback) throws RemoteException {
            Log.d(TAG, "printStayReceipt: ");
            // call default
            if (poyntReceiptPrintingService != null) {
                poyntReceiptPrintingService.printStayReceipt(s, s1, callback);
            }
        }

        @Override
        public void printBalanceInquiry(String s, BalanceInquiry balanceInquiry,
                                        IPoyntReceiptPrintingServiceListener callback) throws RemoteException {
            Log.d(TAG, "printBalanceInquiry: ");
            // call default
            if (poyntReceiptPrintingService != null) {
                poyntReceiptPrintingService.printBalanceInquiry(s, balanceInquiry, callback);
            }
        }

        @Override
        public void sendReceipt(String orderId, String transactionId, String email, String phoneNumber,
                                IPoyntReceiptSendListener callback) throws RemoteException {
            Log.d(TAG, "sendReceipt: ");
            if (poyntReceiptPrintingService != null) {
                poyntReceiptPrintingService.sendReceipt(orderId, transactionId, email, phoneNumber, callback);
            } else {
                bindPoyntReceiptPrintingService();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        bindPoyntReceiptPrintingService();
    }

    private void bindPoyntReceiptPrintingService() {
        if (poyntReceiptPrintingService == null) {
            bindService(Intents.getComponentIntent(Intents.COMPONENT_POYNT_RECEIPT_PRINTING_SERVICE),
                    receiptPrintingConnection, BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(receiptPrintingConnection);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: called");
        return mBinder;
    }


}



