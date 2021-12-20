package com.example.newsgateway;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;


import java.util.ArrayList;

public class NewsService extends Service
{


    private boolean isRunning = true;
    private final ArrayList<Article> articleList = new ArrayList <Article>();

    public NewsService()
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        ServiceReceiver serviceReceiver = new ServiceReceiver();
        IntentFilter filter1 = new IntentFilter(MainActivity.ACTION_MSG_TO_SERVICE);
        registerReceiver(serviceReceiver, filter1);

        new Thread(new Runnable() {
            @Override
            public void run() {

                while (isRunning)
                {
                    while(articleList.isEmpty())
                    {
                        try
                        {
                            Thread.sleep(250);
                        } catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    Intent i;
                    i = new Intent();
                    i.setAction(MainActivity.ACTION_NEWS_STORY);
                    i.putExtra(MainActivity.ARTICLE_LIST, articleList);
                    sendBroadcast(i);
                    articleList.clear();
                }

            }
        }).start();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        isRunning = false;
    }

    public void Articles_Set(ArrayList<Article> list)
    {
        articleList.clear();
        articleList.addAll(list);

    }

    class ServiceReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {

            if (MainActivity.ACTION_MSG_TO_SERVICE.equals(intent.getAction())) {
                String sourceId;
                String temp = "";
                if (intent.hasExtra(MainActivity.SOURCE_ID)) {
                    sourceId = intent.getStringExtra(MainActivity.SOURCE_ID);
                    temp = sourceId.replaceAll(" ", "-");
                }

                ArticleRunnable d = new ArticleRunnable(NewsService.this, temp);
                new Thread(d).start();
            }

        }
    }
}


