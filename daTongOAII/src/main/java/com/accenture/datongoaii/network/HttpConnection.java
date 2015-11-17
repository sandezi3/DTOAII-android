package com.accenture.datongoaii.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.accenture.datongoaii.Config;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class HttpConnection implements Runnable {
    private static final String TAG = "HttpConnect";
    public static final int DID_START = 0;
    public static final int DID_ERROR = 1;
    public static final int DID_SUCCEED = 2;

    private static final int GET = 0;
    private static final int POST = 1;
    private static final int PUT = 2;
    private static final int DELETE = 3;
    //    private static final int BITMAP = 4;
    private static final int POST_FILE = 4;
    private static final int PUT_IMAGE = 5;
    public static final String ERROR_INTERFACE = "SERVER INTERFACE ERROR";

    private String url;
    private int method;
    private JSONObject data;
    private CallbackListener listener;
    private String path;

    public void create(int method, String url, JSONObject data, String path, CallbackListener listener) {
        this.method = method;
        this.url = url;
        this.data = data;
        this.listener = listener;
        this.path = path;
        ConnectionManager.getInstance().push(this);
    }

    public void get(String url, CallbackListener listener) {
        create(GET, url, null, null, listener);
    }

    public void post(String url, JSONObject data, CallbackListener listener) {
        create(POST, url, data, null, listener);
    }

    public void put(String url, JSONObject data, CallbackListener listener) {
        create(PUT, url, data, null, listener);
    }

    public void put(String url, CallbackListener listener) {
        create(PUT, url, null, null, listener);
    }

    public void delete(String url, CallbackListener listener) {
        create(DELETE, url, null, null, listener);
    }

    //    public void bitmap(String url) {
//        create(BITMAP, url, null, null, listener);
//    }
//
    public void uploadFile(String url, String path, CallbackListener listener) {
        create(POST_FILE, url, null, path, listener);
    }

    public void uploadImage(String url, String path, CallbackListener listener) {
        create(PUT_IMAGE, url, null, path, listener);
    }

    public interface CallbackListener {
        void callBack(String result);
    }

    private static final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case HttpConnection.DID_START: {
                    break;
                }
                case HttpConnection.DID_SUCCEED: {
                    CallbackListener listener = (CallbackListener) message.obj;
                    Object data = message.getData();
                    if (listener != null) {
                        if (data != null) {
                            Bundle bundle = (Bundle) data;
                            String result = bundle.getString("callbackkey");
                            listener.callBack(result);
                        }
                    }
                    break;
                }
                case HttpConnection.DID_ERROR: {
                    break;
                }
            }
        }
    };

    private void setHeader(HttpRequestBase access, boolean isContentJson) {
        access.setHeader("Accept", "application/json");
        if (isContentJson) {
            access.setHeader("Content-type", "application/json");
        }
        access.setHeader("Accept-Encoding", "gzip"); // only
        String token = Account.getInstance().getToken();
        if (token != null && token.length() > 0) {
            Logger.i(TAG, "Init Header Token = " + token);
            access.setHeader("token", Account.getInstance().getToken());
        }
        access.setHeader("access_from", "client_android");
    }

    public void run() {
        // handler.sendMessage(Message.obtain(handler,
        // HttpConnection.DID_START));
        HttpClient httpClient = getHttpClient();
        try {
            HttpResponse httpResponse;
            switch (method) {
                case GET: {
                    HttpGet httpGetRequest = new HttpGet(url);

                    setHeader(httpGetRequest, true);

                    long t = System.currentTimeMillis();
                    httpResponse = httpClient.execute(httpGetRequest);
                    Logger.i(TAG, "HTTPResponse received in [" + (System.currentTimeMillis() - t) + "ms]");

                    if (isHttpSuccessExecuted(httpResponse)) {
                        // Get hold of the response entity (-> the data):
                        HttpEntity entity = httpResponse.getEntity();

                        if (entity != null) {
                            // Read the content stream
                            InputStream instream = entity.getContent();
                            Header contentEncoding = httpResponse.getFirstHeader("Content-Encoding");
                            if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                                instream = new GZIPInputStream(instream);
                            }

                            // convert content stream to a String
                            String resultString = convertStreamToString(instream);
                            Logger.e("JSON", resultString);
                            instream.close();
                            this.sendMessage(resultString);
                        } else {
                            this.sendMessage("fail");
                        }
                    } else {
                        Logger.e("GET.Err", "Please contact interface provider!");
                        this.sendMessage(ERROR_INTERFACE);
                    }
                }
                break;
                case POST: {
                    HttpPost httpPostRequest = new HttpPost(url);
                    StringEntity se = null;
                    Logger.e("POST url = ", url);
                    if (this.data != null) {
                        se = new StringEntity(data.toString(), "UTF-8");
                        Logger.e("POST body = ", data.toString());
                    }
                    // Set HTTP parameters
                    httpPostRequest.setEntity(se);
                    setHeader(httpPostRequest,true);

                    // set
                    // this
                    // parameter
                    // if you
                    // would
                    // like to
                    // use
                    // gzip
                    // compression

                    long t = System.currentTimeMillis();
                    httpResponse = httpClient.execute(httpPostRequest);
                    Logger.i(TAG, "HTTPResponse received in [" + (System.currentTimeMillis() - t) + "ms]");

                    if (isHttpSuccessExecuted(httpResponse)) {
                        // Get hold of the response entity (-> the data):
                        HttpEntity entity = httpResponse.getEntity();

                        if (entity != null) {
                            // Read the content stream
                            InputStream instream = entity.getContent();
                            Header contentEncoding = httpResponse.getFirstHeader("Content-Encoding");
                            if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                                instream = new GZIPInputStream(instream);
                            }

                            // convert content stream to a String
                            String resultString = convertStreamToString(instream);
                            Logger.e("JSON", resultString);
                            instream.close();
                            this.sendMessage(resultString);
                        } else {
                            this.sendMessage("fail");
                        }
                    } else {
                        // if (Account.govDebug) {
                        // this.sendMessage("");
                        // }
                        Logger.e("POST.Err", "Please contact interface provider!");
                        this.sendMessage(ERROR_INTERFACE);
                    }
                    break;
                }
                case POST_FILE: {
                    HttpPost httpPostRequest = new HttpPost(url);
                    String tempFilePath = Environment.getExternalStorageDirectory().getPath() + "/TEMP";
                    String fileName = tempFilePath + "/temp" + System.currentTimeMillis() + ".jpg";
                    try {
                        File f = new File(tempFilePath);
                        f.mkdirs();
                        f = new File(fileName);
                        if (f.exists()) {
                            f.delete();
                        }
                        if (f.createNewFile()) {
                            FileOutputStream fos = new FileOutputStream(f);
                            int degree = Utils.readPictureDegree(path);
                            BitmapFactory.Options opts=new BitmapFactory.Options();
                            opts.inSampleSize=2;
                            Bitmap bitmap = BitmapFactory.decodeFile(path, opts);
                            bitmap = Utils.rotateImageView(degree, bitmap);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, fos);
                            fos.close();
                        } else {
                            Logger.i("CreateFile", "CreateFile failed!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    File f = new File(fileName);
                    FileInputStream fis = new FileInputStream(f);
                    InputStreamEntity ise = new InputStreamEntity(fis, f.length());

                    // Set HTTP parameters
                    setHeader(httpPostRequest,false);
                    httpPostRequest.setHeader("Content-Type", "image/jpeg");
                    httpPostRequest.setEntity(ise);
                    ise.setContentType("binary/octet-stream");

                    long t = System.currentTimeMillis();
                    httpResponse = httpClient.execute(httpPostRequest);
                    Logger.i(TAG, "HTTPResponse received in [" + (System.currentTimeMillis() - t) + "ms]");

                    if (isHttpSuccessExecuted(httpResponse)) {
                        // Get hold of the response entity (-> the data):
                        HttpEntity entity = httpResponse.getEntity();

                        if (entity != null) {
                            // Read the content stream
                            InputStream instream = entity.getContent();
                            Header contentEncoding = httpResponse.getFirstHeader("Content-Encoding");
                            if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                                instream = new GZIPInputStream(instream);
                            }

                            // convert content stream to a String
                            String resultString = convertStreamToString(instream);
                            Logger.e("JSON Upload Image", resultString);
                            instream.close();
                            this.sendMessage(resultString);
                        } else {
                            this.sendMessage("fail");
                        }
                    } else {
                        Logger.e("PUT_IMAGE.Err", "Please contact interface provider!");
                        this.sendMessage(ERROR_INTERFACE);
                    }
                    break;
                }
                case PUT: {
                    HttpPut httpPutRquest = new HttpPut(url);
                    StringEntity se = null;
                    if (this.data != null) {
                        Logger.e("PUT.Data", this.data.toString());
                        se = new StringEntity(data.toString(), "UTF-8");
                    }
                    // Set HTTP parameters
                    httpPutRquest.setEntity(se);
                    setHeader(httpPutRquest, true);

                    // set
                    // this
                    // parameter
                    // if you
                    // would
                    // like to
                    // use
                    // gzip
                    // compression

                    long t = System.currentTimeMillis();
                    httpResponse = httpClient.execute(httpPutRquest);
                    Logger.i(TAG, "HTTPResponse received in [" + (System.currentTimeMillis() - t) + "ms]");

                    if (isHttpSuccessExecuted(httpResponse)) {
                        // Get hold of the response entity (-> the data):
                        HttpEntity entity = httpResponse.getEntity();

                        if (entity != null) {
                            // Read the content stream
                            InputStream instream = entity.getContent();
                            Header contentEncoding = httpResponse.getFirstHeader("Content-Encoding");
                            if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                                instream = new GZIPInputStream(instream);
                            }

                            // convert content stream to a String
                            String resultString = convertStreamToString(instream);
                            Logger.e("JSON", resultString);
                            instream.close();
                            this.sendMessage(resultString);
                        } else {
                            this.sendMessage("fail");
                        }
                    } else {
                        // if (Account.govDebug) {
                        // this.sendMessage("");
                        // }
                        this.sendMessage(ERROR_INTERFACE);
                        Logger.e("PUT.Err", "Please contact interface provider!");
                    }
                    break;
                }
                case PUT_IMAGE: {
                    HttpPut httpPutRequest = new HttpPut(url);
                    String tempFilePath = Environment.getExternalStorageDirectory().getPath() + "/TEMP";
                    String fileName = tempFilePath + "/temp" + System.currentTimeMillis() + ".jpg";
                    try {
                        File f = new File(tempFilePath);
                        f.mkdirs();
                        f = new File(fileName);
                        if (f.exists()) {
                            f.delete();
                        }
                        if (f.createNewFile()) {
                            FileOutputStream fos = new FileOutputStream(f);
                            int degree = Utils.readPictureDegree(path);
                            BitmapFactory.Options opts=new BitmapFactory.Options();
                            opts.inSampleSize=2;
                            Bitmap bitmap = BitmapFactory.decodeFile(path, opts);
                            bitmap = Utils.rotateImageView(degree, bitmap);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, fos);
                            fos.close();
                        } else {
                            Logger.i("CreateFile", "CreateFile failed!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    File f = new File(fileName);
                    FileInputStream fis = new FileInputStream(f);
                    InputStreamEntity ise = new InputStreamEntity(fis, f.length());

                    // Set HTTP parameters
                    setHeader(httpPutRequest,false);
                    httpPutRequest.setHeader("Content-Type", "image/jpeg");
                    httpPutRequest.setEntity(ise);
//                    ise.setContentType("binary/octet-stream");

                    long t = System.currentTimeMillis();
                    httpResponse = httpClient.execute(httpPutRequest);
                    Logger.i(TAG, "HTTPResponse received in [" + (System.currentTimeMillis() - t) + "ms]");

                    if (isHttpSuccessExecuted(httpResponse)) {
                        // Get hold of the response entity (-> the data):
                        HttpEntity entity = httpResponse.getEntity();

                        if (entity != null) {
                            // Read the content stream
                            InputStream instream = entity.getContent();
                            Header contentEncoding = httpResponse.getFirstHeader("Content-Encoding");
                            if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                                instream = new GZIPInputStream(instream);
                            }

                            // convert content stream to a String
                            String resultString = convertStreamToString(instream);
                            Logger.e("JSON Upload Image", resultString);
                            instream.close();
                            this.sendMessage(resultString);
                        } else {
                            this.sendMessage("fail");
                        }
                    } else {
                        Logger.e("PUT_IMAGE.Err", "Please contact interface provider!");
                        this.sendMessage(ERROR_INTERFACE);
                    }
                    break;
                }
                case DELETE: {
                    HttpDelete httpDeleteRequest = new HttpDelete(url);
                    setHeader(httpDeleteRequest, true);

                    long t = System.currentTimeMillis();
                    httpResponse = httpClient.execute(httpDeleteRequest);
                    Logger.i(TAG, "HTTPResponse received in [" + (System.currentTimeMillis() - t) + "ms]");

                    if (isHttpSuccessExecuted(httpResponse)) {
                        // Get hold of the response entity (-> the data):
                        HttpEntity entity = httpResponse.getEntity();

                        if (entity != null) {
                            // Read the content stream
                            InputStream instream = entity.getContent();
                            Header contentEncoding = httpResponse.getFirstHeader("Content-Encoding");
                            if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                                instream = new GZIPInputStream(instream);
                            }

                            // convert content stream to a String
                            String resultString = convertStreamToString(instream);
                            Logger.e("JSON", resultString);
                            instream.close();
                            this.sendMessage(resultString);
                        } else {
                            this.sendMessage("fail");
                        }
                    } else {
                        Logger.e("DELETE.Err", "Please contact interface provider!");
                        this.sendMessage(ERROR_INTERFACE);
                    }
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.sendMessage("fail");
        }
        ConnectionManager.getInstance().didComplete(this);
    }

    // private void processBitmapEntity(HttpEntity entity) throws IOException {
    // BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
    // Bitmap bm = BitmapFactory.decodeStream(bufHttpEntity.getContent());
    // handler.sendMessage(Message.obtain(handler, DID_SUCCEED, bm));
    // }

    private void sendMessage(String result) {
        Message message = Message.obtain(handler, DID_SUCCEED, listener);
        Bundle data = new Bundle();
        data.putString("callbackkey", result);
        message.setData(data);
        handler.sendMessage(message);
    }

    public static DefaultHttpClient getHttpClient() {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, Config.HTTP_CONNECTION_EXPIRE_SECONDS * 1000);
        HttpConnectionParams.setSoTimeout(httpParams, Config.HTTP_CONNECTION_EXPIRE_SECONDS * 1000);
        httpParams.setParameter("charset", "UTF-8");
        // HttpConnectionParams.setSocketBufferSize(httpParams, 8192);

        return new DefaultHttpClient(httpParams);
    }

    public static boolean isHttpSuccessExecuted(HttpResponse response) {
        int statusCode = response.getStatusLine().getStatusCode();
        return (statusCode > 199) && (statusCode < 400);
    }

    private static String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 * 
		 * (c) public domain:
		 * http://senior.ceng.metu.edu.tr/2009/praeda/2009/01/
		 * 11/a-simple-restful-client-at-android/
		 */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static Map<String, String> getHeaderMap() {
        Map<String, String> header = new HashMap<String, String>();
        header.put("token", Account.getInstance().getToken());
        header.put("access_from", "web_mobile");
        return header;
    }
}
