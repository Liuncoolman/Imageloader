package com.liun.example;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import com.liun.example.adapter.NewsAdapter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Description :
 * Author : Liun
 * Date   : 2017/9/29 14:22
 * Email  : liun_coolman@foxmail.com
 */
public class ImageLoader {
    private final ListView mListView;
    private ImageView mImageView;
    private String mUrl;
    private LruCache<String, Bitmap> mCache;
    private Set<LoadImageAsyncTask> mTasks;// 所有异步加载图片的任务

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mImageView.getTag().equals(mUrl)) {
                // 当url与imageview上的url一致时，设置imageview  防止应listview重绘导致图片加载多次
                mImageView.setImageBitmap((Bitmap) msg.obj);
            }
        }
    };

    public ImageLoader(ListView listView) {
        this.mListView = listView;

        mTasks = new HashSet<>();

        // 获取系统最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        // 设置LruCache总内存
        int cacheSize = maxMemory / 4;
        // 初始化LruCache
        mCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();// 返回bitmap的大小
            }
        };


    }

    /**
     * 把bitmap存入LruCache中
     *
     * @param url
     * @param bitmap
     */
    private void addBitmapToLruCache(String url, Bitmap bitmap) {
        if (getBitmapFromLruCache(url) == null) {
            mCache.put(url, bitmap);
        }
    }

    /**
     * 从LruCache获取已缓存的bitmap
     *
     * @param url
     * @return
     */
    private Bitmap getBitmapFromLruCache(String url) {
        return mCache.get(url);
    }


    /**
     * 以多线程方式获取url对应的BitMap，并设置到Imageview
     *
     * @param imageView
     * @param url
     */
    public void showImageByThread(ImageView imageView, final String url) {
        this.mImageView = imageView;
        this.mUrl = url;

        new Thread() {
            @Override
            public void run() {
                super.run();
                Bitmap bitmap = getBitmapFromUrl(url);

                // 通过handler到主线程设置图片
                Message msg = Message.obtain();
                msg.obj = bitmap;
                mHandler.sendMessage(msg);
            }
        }.start();
    }

    /**
     * 通过url获取bitmap
     *
     * @param urlString
     * @return
     */
    private Bitmap getBitmapFromUrl(String urlString) {
        Bitmap bitmap;
        BufferedInputStream is = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            is = new BufferedInputStream(connection.getInputStream());
            bitmap = BitmapFactory.decodeStream(is);

            connection.disconnect();

            return bitmap;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 通过异步任务方式 加载图片
     *
     * @param imageView
     * @param url
     */
    public void showImageByAsyncTask(ImageView imageView, String url) {
        // 如果LruCache中没有bitmap，去网络下载
        if (getBitmapFromLruCache(url) == null) {
            imageView.setImageResource(R.mipmap.ic_launcher);
        } else {
            imageView.setImageBitmap(getBitmapFromLruCache(url));
        }

    }


    /**
     * 加载可见区域内item上imageview的图片
     *
     * @param start
     * @param end
     */
    public void loadImages(int start, int end) {
        for (int i = start; i < end; i++) {
            String url = NewsAdapter.URLS[i];// adapter可见item图片url
            ImageView imageView = (ImageView) mListView.findViewWithTag(url);
            // 如果LruCache中没有bitmap，去网络下载
            if (getBitmapFromLruCache(url) == null) {
                LoadImageAsyncTask task = new LoadImageAsyncTask(url);
                task.execute(url);
                mTasks.add(task);
            } else {
                imageView.setImageBitmap(getBitmapFromLruCache(url));
            }
        }
    }

    /**
     * 取消所有异步任务
     */
    public void cancelAllTasks() {
        if (mTasks != null) {
            for (AsyncTask task : mTasks) {
                task.cancel(false);
            }
        }
    }

    private class LoadImageAsyncTask extends AsyncTask<String, Void, Bitmap> {
        //        private final ImageView mImageView;
        private final String mUrl;

        public LoadImageAsyncTask(String url) {
            this.mUrl = url;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = getBitmapFromUrl(params[0]);
            // 保存缓存中没有的图片
            if (bitmap != null) {
                addBitmapToLruCache(params[0], bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView imageView = (ImageView) mListView.findViewWithTag(mUrl);
            // 当url与imageview上的url一致时，设置imageview  防止应listview重绘导致图片加载多次
//            if (imageView.getTag().equals(mUrl)) {
//
//                imageView.setImageBitmap(bitmap);
//            }

            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }

            mTasks.remove(this);
        }
    }
}
