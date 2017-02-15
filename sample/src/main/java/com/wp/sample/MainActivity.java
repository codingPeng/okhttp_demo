package com.wp.sample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView mTVtext;
    private String mBaseUrl = "http://192.168.3.101:8082/my_okhttp2/";
    private ImageView mIvImg;
    OkHttpClient okHttpClient;

    OkHttpClient.Builder builder = new OkHttpClient.Builder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        okHttpClient = builder.connectTimeout(60, TimeUnit.SECONDS).cookieJar(new CookiesManager()).build();
        mTVtext = (TextView) findViewById(R.id.textView);
        mIvImg = (ImageView) findViewById(R.id.ImageView);
    }

    public void doDownloadImg(View view) {
        // 1. 拿到okhttpClient对象

        //2. 构建request
        Request.Builder builder = new Request.Builder();
        Request request = builder.url(mBaseUrl + "files/test.jpg").build();

        Call call = okHttpClient.newCall(request);

        //4. 执行call
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                L.e(e.getMessage());
                e.printStackTrace();

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                // 此时并不在UI线程
                InputStream is = response.body().byteStream();
                final Bitmap bitmap = BitmapFactory.decodeStream(is);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mIvImg.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }

    public void doDownload(View view) {
        // 1. 拿到okhttpClient对象

        //2. 构建request
        Request.Builder builder = new Request.Builder();
        Request request = builder.url(mBaseUrl + "files/test.jpg").build();
        Call call = okHttpClient.newCall(request);

        //4. 执行call
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                L.e(e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                // 此时并不在UI线程
                InputStream is = response.body().byteStream();
                final long total = response.body().contentLength();
                File file = new File(Environment.getExternalStorageDirectory(), "wp12306.jpg");
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buf = new byte[1028];
                int len = 0;
                long sum = 0L;

                while ((len = is.read(buf)) != -1) {

                    sum += len;
                    fos.write(buf, 0, len);

                    L.e(sum + " / " + total);
                    final long finalSum = sum;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //设置下载progress
                            mTVtext.setText(finalSum + " / " + total);
                        }
                    });
                }

                fos.flush();
                fos.close();
                is.close();
            }
        });
    }


    public void uploadFile(View view) {
        // 1. 拿到okhttpClient对象

        //2. 构建request
        Request.Builder builder = new Request.Builder();

        File file = new File(Environment.getExternalStorageDirectory(), "1.jpg");
        if (!file.exists()) {
            L.e("file is not exists");
            return;
        }
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder();
        RequestBody requestBody = multipartBuilder
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", "wp") //
                .addFormDataPart("password", "1231")//
                .addFormDataPart("mPhoto", "1.jpg", RequestBody.create(MediaType.parse
                        ("application/octet-stream"), file))
                .build();
        CountingResquestBody countingResquestBody = new
                CountingResquestBody(requestBody, new CountingResquestBody.listener() {
            @Override
            public void onRequestProgress(long contingLenth, long contentLength) {

                L.e(contingLenth + " / " + contentLength);
            }
        });
        Request request = builder.url(mBaseUrl + "uploadFile").post(countingResquestBody).build();
        //3. 将request封装为call
        excuteRequest(request);
    }

    public void doPostString(View view) {
        // 1. 拿到okhttpClient对象

        //2. 构建request
        Request.Builder builder = new Request.Builder();

        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), "{username:wp,password:123}");
        Request request = builder
                .url(mBaseUrl + "postString")
                .post(requestBody)
                .build();
        //3. 将request封装为call
        excuteRequest(request);
    }

    public void doPostFile(View view) {
        // 1. 拿到okhttpClient对象

        //2. 构建request
        Request.Builder builder = new Request.Builder();

        File file = new File(Environment.getExternalStorageDirectory(), "1.jpg");
        if (file.exists()) {
            L.e("file is not exists");
            return;
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse
                ("application/octet-stream"), file);
        Request request = builder.url(mBaseUrl + "postFile").post(requestBody).build();

        //3. 将request封装为call
        excuteRequest(request);
    }

    public void doPost(View view) {
        // 1. 拿到okhttpClient对象

        //2. 构建request
        Request.Builder builder = new Request.Builder();

        FormBody.Builder requestbuilder = new FormBody.Builder();
        FormBody body = requestbuilder.add("username", "wp").add("password", "1234").build();
        Request request = builder.url(mBaseUrl + "login").post(body).build();
        //3. 将request封装为call
        excuteRequest(request);
    }


    public void doGet(View view) {

        // 1. 拿到okhttpClient对象

        //2. 构建 request
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .get()//
                .url(mBaseUrl + "login?username=wp&password=123")//
                .build();

        L.e(mBaseUrl + "login?username=wp&password=123");
        //3. 将request封装为call
        excuteRequest(request);
    }

    private void excuteRequest(Request request) {
        Call call = okHttpClient.newCall(request);

        //4. 执行call
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                L.e(e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                // 此时并不在UI线程
                L.e("onRespose: ");
                final String res = response.body().string();
                L.e(res);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTVtext.setText(res);
                    }
                });

            }
        });
    }

    /**
     * 自动管理Cookies
     */
    private class CookiesManager implements CookieJar {
        private final PersistentCookieStore cookieStore = new PersistentCookieStore(getApplicationContext());

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            if (cookies != null && cookies.size() > 0) {
                for (Cookie item : cookies) {
                    cookieStore.add(url, item);
                }
            }
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> cookies = cookieStore.get(url);
            return cookies;
        }
    }


}
