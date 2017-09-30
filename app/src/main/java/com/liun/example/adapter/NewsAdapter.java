package com.liun.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.liun.example.ImageLoader;
import com.liun.example.R;
import com.liun.example.bean.NewsBean;

import java.util.List;

/**
 * Description :
 * Author : Liun
 * Date   : 2017/9/29 11:54
 * Email  : liun_coolman@foxmail.com
 */
public class NewsAdapter extends BaseAdapter implements AbsListView.OnScrollListener {
    private final List<NewsBean> list;
    private final LayoutInflater mInflater;
    private final ImageLoader mImageLoader;
    private final ListView mListView;
    private int mStart, mEnd;
    public static String[] URLS;// 储存所有图片url
    private boolean mFirstIn = true;

    public NewsAdapter(Context context, List<NewsBean> list, ListView listView) {
        this.list = list;
        this.mListView = listView;
        mInflater = LayoutInflater.from(context);
        mImageLoader = new ImageLoader(listView);

        // 初始化并赋值
        URLS = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            URLS[i] = list.get(i).newsIcon;
        }

        listView.setOnScrollListener(this);

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item, null);

            holder.newsTitle = (TextView) convertView.findViewById(R.id.tv_news_title);
            holder.newsAuthorName = (TextView) convertView.findViewById(R.id.tv_news_author);
            holder.newsDate = (TextView) convertView.findViewById(R.id.tv_news_date);
            holder.newsIcon = (ImageView) convertView.findViewById(R.id.iv_news_icon);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        NewsBean newsBean = list.get(position);
        holder.newsTitle.setText(newsBean.newsTitle);
        holder.newsDate.setText(newsBean.date);
        holder.newsAuthorName.setText(newsBean.authorName);
        holder.newsIcon.setTag(newsBean.newsIcon);
//        new ImageLoader().showImageByThread(holder.newsIcon,newsBean.newsIcon);
        mImageLoader.showImageByAsyncTask(holder.newsIcon, newsBean.newsIcon);
        return convertView;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            // 开始加载
            mImageLoader.loadImages(mStart, mEnd);
        } else {
            // 停止加载
            mImageLoader.cancelAllTasks();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int
            totalItemCount) {
        mStart = firstVisibleItem;
        mEnd = firstVisibleItem + visibleItemCount;

        // 首次显示加载图片
        if (mFirstIn && visibleItemCount > 0) {
            mImageLoader.loadImages(firstVisibleItem, visibleItemCount);
            mFirstIn = false;
        }
    }

    class ViewHolder {
        private TextView newsTitle, newsDate, newsAuthorName;
        private ImageView newsIcon;
    }
}
