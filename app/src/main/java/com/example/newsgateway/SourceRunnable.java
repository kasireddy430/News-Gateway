package com.example.newsgateway;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SourceRunnable implements Runnable {

    private static final String TAG = "DownloadeSource";
    private StringBuilder sb1;
    private MainActivity mainActivity;
    private String category;
    private Uri.Builder buildURL = null;

    private ArrayList<String> categoryList = new ArrayList <String>();
    private String API_KEY ="2a924e6142c64603bd5a2f15359517fc";
    private String NewsAPI;

    public SourceRunnable(MainActivity ma, String category){
        mainActivity = ma;
        if(category.equalsIgnoreCase("all") || category.equalsIgnoreCase("")) {
            this.category = "";
            NewsAPI ="https://newsapi.org/v2/sources?language=en&country=us&apiKey="+API_KEY;
        }
        else
        {
            String api1= "https://newsapi.org/v2/sources?language=en&country=us&category=";
            String api2 ="&apiKey="+API_KEY;
            NewsAPI = api1+category+api2;
            this.category = category;
        }

    }

    @Override
    public void run() {
        buildURL = Uri.parse(NewsAPI).buildUpon();
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
            Log.d(TAG, "Exception doInBackground: " + e.getMessage());
        }
        handleResults(sb1.toString());
    }


    private void handleResults(String s){
        final ArrayList<Source> sourceList = parseJSON1(s);

        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            if(sourceList != null) {
                for (int j = 0; j < sourceList.size(); j++) {
                    String temp = sourceList.get(j).getsCategory();
                    if (!categoryList.contains(temp))
                        categoryList.add(temp);
                }
                mainActivity.initialiseSource(sourceList, categoryList);
            }
            }
        });
    }

    private ArrayList<Source> parseJSON1(String s) {
        ArrayList<Source> sourceList = new ArrayList<Source>();
        try{

            JSONObject jObjMain = new JSONObject(s);
            JSONArray sources = jObjMain.getJSONArray("sources");
            for(int i=0;i<sources.length();i++){
                JSONObject src = (JSONObject) sources.get(i);
                Source srcObj = new Source();
                srcObj.setsId(src.getString("id"));
                srcObj.setsCategory(src.getString("category"));
                srcObj.setsName(src.getString("name"));
                srcObj.setsUrl(src.getString("url"));
                sourceList.add(srcObj);
            }

            return sourceList;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
