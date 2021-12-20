package com.example.newsgateway;

import android.net.Uri;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ArticleRunnable implements Runnable
{
    private String sourceId;
    private NewsService service;
    private String API_KEY ="2a924e6142c64603bd5a2f15359517fc";
    private String ARTICLE_1 ="https://newsapi.org/v2/top-headlines?sources=";
    private String ARTICLE_2 = "&language=en&apiKey="+API_KEY;
    private Uri.Builder buildURL = null;
    private StringBuilder sb1;

    public ArticleRunnable(NewsService service, String sourceId){
        this.sourceId = sourceId;
        this.service= service;
    }


    @Override
    public void run() {
        String query ="";

        query = ARTICLE_1+sourceId+ARTICLE_2;
        buildURL = Uri.parse(query).buildUpon();
        String urlToUse = buildURL.build().toString();
        sb1 = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.addRequestProperty("User-Agent","");
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line=null;
            while ((line = reader.readLine()) != null) {
                sb1.append(line).append('\n');
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        handleResults(sb1.toString());

    }

    private void handleResults(String s){
        final ArrayList<Article> articleArrayList = parseJSON1(s);
        service.Articles_Set(articleArrayList);
    }

    private ArrayList<Article> parseJSON1(String s)
    {
        ArrayList<Article> articleList = new ArrayList <Article>();
        try
        {

                JSONObject jObjMain = new JSONObject(s);
                JSONArray articles = jObjMain.getJSONArray("articles");
                for(int i=0;i<articles.length();i++){
                    JSONObject art = (JSONObject) articles.get(i);
                    Article artObj = new Article();
                    artObj.setaAuthor(art.getString("author"));
                    artObj.setaDescription(art.getString("description"));
                    artObj.setaPublishedAt(art.getString("publishedAt"));
                    artObj.setaTitle(art.getString("title"));
                    artObj.setaUrlToImage(art.getString("urlToImage"));
                    artObj.setArticleUrl(art.getString("url"));
                    articleList.add(artObj);
                }
            return articleList;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}

