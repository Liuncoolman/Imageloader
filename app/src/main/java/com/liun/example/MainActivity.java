package com.liun.example;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.liun.example.adapter.NewsAdapter;
import com.liun.example.bean.NewsBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView mListView;
    private String URL = "http://v.juhe" +
            ".cn/toutiao/index?type=&key=31d85b4313cc053cd5917527389299f8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.mListView);

        new NewsAsyncTask().execute(URL);
    }

    private class NewsAsyncTask extends AsyncTask<String, Void, List<NewsBean>> {
        @Override
        protected List<NewsBean> doInBackground(String... params) {
            List<NewsBean> newsBeanList = new ArrayList<>();
            String url = params[0];
            try {
                String jsonString = readStream(new URL(url).openStream());
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONObject result = jsonObject.optJSONObject("result");
                JSONArray data = result.optJSONArray("data");
                if (data != null && data.length() > 0) {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject object = data.getJSONObject(i);
                        String title = object.optString("title");
                        String author_name = object.optString("author_name");
                        String date = object.optString("date");
                        String thumbnail_pic_s = object.optString("thumbnail_pic_s");

                        NewsBean newsBean = new NewsBean();
                        newsBean.newsTitle = title;
                        newsBean.newsIcon = thumbnail_pic_s;
                        newsBean.authorName = author_name;
                        newsBean.date = date;

                        newsBeanList.add(newsBean);
                    }

                    return newsBeanList;
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<NewsBean> newsBeen) {
            super.onPostExecute(newsBeen);
            NewsAdapter mAdapter = new NewsAdapter(MainActivity.this, newsBeen, mListView);
            mListView.setAdapter(mAdapter);
        }
    }

    private String readStream(InputStream in) {
        String result = "";
        try {
            String line = "";
            // 字节流转换成字符流
            InputStreamReader isr = new InputStreamReader(in, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                result += line;
            }
            return result;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}
