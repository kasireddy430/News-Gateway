package com.example.newsgateway;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
{

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean serviceRunning = false;
    static final String ACTION_MSG_TO_SERVICE = "ACTION_MSG_TO_SERVICE";
    static final String ACTION_NEWS_STORY = "ACTION_NEWS_STORY";
    static final String ARTICLE_LIST = "ARTICLE_LIST";
    static final String SOURCE_ID = "SOURCE_ID";
    private ArrayList<String> srcList = new ArrayList <String>();
    private ArrayList<String> catList = new ArrayList <String>();
    private ArrayList<Source> sourceArrayList = new ArrayList <Source>();
    private ArrayList<Article> articleArrayList = new ArrayList <Article>();
    private HashMap<String, Source> sourceDataMap = new HashMap<>();
    private Menu main_menu;
    private NewsReceiver newsReceiver;
    private String currentNewsSource;
    private ColorAdapter adapter;
    private MyPageAdapter pageAdapter;
    private List <Fragment> fragments;
    private ViewPager pager;
    private boolean stateFlag;
    private int currentSourcePointer;
    ArrayList<ContentDrawer> contentDrawers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if(!doNetCheck()){
            errorDialog();
        }

        if(!serviceRunning)
        {
            if(savedInstanceState == null) {
                Intent intent;
                intent = new Intent(MainActivity.this, NewsService.class);
                startService(intent);
                serviceRunning = true;
            }
        }

        newsReceiver = new NewsReceiver();
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_NEWS_STORY);
        registerReceiver(newsReceiver, filter);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);


        mDrawerList.setOnItemClickListener(
                new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        pager.setBackgroundResource(0);
                        currentSourcePointer = position;
                        selectItem(position);
                    }
                }
        );

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        );

        adapter = new ColorAdapter(this,contentDrawers);
        mDrawerList.setAdapter(adapter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        fragments = new ArrayList<>();

        pageAdapter = new MyPageAdapter(getSupportFragmentManager());
        pager = findViewById(R.id.pageViewer);
        pager.setAdapter(pageAdapter);

        if (sourceDataMap.isEmpty()) {
            if(savedInstanceState == null) {
                SourceRunnable sourceLoader = new SourceRunnable(MainActivity.this, "");
                new Thread(sourceLoader).start();
            }
        }
    }

    private boolean doNetCheck() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            Toast.makeText(this, "Cannot access the Connectivity Manager", Toast.LENGTH_SHORT).show();
            return false;
        }

        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if(netInfo != null && netInfo.isConnected()){
            return true;
        }
        else{return false;}
    }

    private void errorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet Connection");
        builder.setMessage("Please turn on your mobile data to retrieve data.");

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {

        if (mDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }

        SourceRunnable download = new SourceRunnable(this,item.getTitle().toString());
        new Thread(download).start();
        colorMenuOptions(item);
        mDrawerLayout.openDrawer(mDrawerList);
        return super.onOptionsItemSelected(item);
    }

    private void colorMenuOptions(MenuItem item) {

        String items = item.getTitle().toString();
        if(items.equals("business")){
            setColor(item,Color.BLUE);
        }else if(items.equals("entertainment")){
                setColor(item,Color.rgb(255,192,203));
                }else if(items.equals("sports")){
                        setColor(item,Color.GREEN);
                        }else if(items.equals("science")){
                                setColor(item,Color.MAGENTA);
                                }else if(items.equals("technology")){
                                    setColor(item,Color.YELLOW);
                                    }else if(items.equals("general")){
                                            setColor(item,Color.RED);
                                            }else if(items.equals("health")){
                                                    setColor(item,Color.CYAN);
                                                    }

    }


    private void selectItem(int position) {
        currentNewsSource = srcList.get(position);
        Intent intent;
        intent = new Intent(MainActivity.ACTION_MSG_TO_SERVICE);
        intent.putExtra(SOURCE_ID, currentNewsSource);
        sendBroadcast(intent);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.action_menu, menu);
        main_menu=menu;
        if(stateFlag){
            main_menu.add("All");
            for (String s : catList)
                main_menu.add(s);
        }
        return true;
    }

    public void initialiseSource(ArrayList<Source> sourceList, ArrayList<String> categoryList)
    {
        sourceDataMap.clear();
        contentDrawers.clear();
        srcList.clear();
        sourceArrayList.clear();
        sourceArrayList.addAll(sourceList);

        for(int i=0;i<sourceList.size();i++){
            srcList.add(sourceList.get(i).getsName());
            sourceDataMap.put(sourceList.get(i).getsName(), (Source)sourceList.get(i));
        }

        if(!main_menu.hasVisibleItems()) {
            catList.clear();
            catList =categoryList;
            main_menu.add("All");
            Collections.sort(categoryList);
            for (String s : categoryList)
                main_menu.add(s);
        }
        for( Source s : sourceList){
            ContentDrawer drawerContent = new ContentDrawer();
            switch (s.getsCategory()){
                case "business":
                    drawerContent.setColor(Color.BLUE);
                    drawerContent.setName(s.getsName());
                    contentDrawers.add(drawerContent);
                    break;
                case "entertainment":
                    drawerContent.setColor(Color.rgb(255,192,203));
                    drawerContent.setName(s.getsName());
                    contentDrawers.add(drawerContent);
                    break;
                case "sports":
                    drawerContent.setColor(Color.GREEN);
                    drawerContent.setName(s.getsName());
                    contentDrawers.add(drawerContent);
                    break;
                case "science":
                    drawerContent.setColor(Color.MAGENTA);
                    drawerContent.setName(s.getsName());
                    contentDrawers.add(drawerContent);
                    break;
                case "technology":
                    drawerContent.setColor(Color.YELLOW);
                    drawerContent.setName(s.getsName());
                    contentDrawers.add(drawerContent);
                    break;
                case "general":
                    drawerContent.setColor(Color.RED);
                    drawerContent.setName(s.getsName());
                    contentDrawers.add(drawerContent);
                    break;
                case "health":
                    drawerContent.setColor(Color.CYAN);
                    drawerContent.setName(s.getsName());
                    contentDrawers.add(drawerContent);
            }
        }
        adapter.notifyDataSetChanged();

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(newsReceiver);
        Intent intent;
        intent = new Intent(MainActivity.this, NewsReceiver.class);
        stopService(intent);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        MakeLayout layoutRestore;
        layoutRestore = new MakeLayout();
        layoutRestore.setCategories(catList);
        layoutRestore.setSourceList(sourceArrayList);
        layoutRestore.setCurrentArticle(pager.getCurrentItem());
        layoutRestore.setCurrentSource(currentSourcePointer);
        layoutRestore.setArticleList(articleArrayList);
        outState.putSerializable("state", layoutRestore);
        super.onSaveInstanceState(outState);
    }

    private void setColor(MenuItem item, int color) {
        SpannableString spannableString;
        spannableString = new SpannableString(item.getTitle());
        spannableString.setSpan(new ForegroundColorSpan(color), 0, spannableString.length(), 0);
        item.setTitle(spannableString);
    }


    private void reDoFragments(ArrayList<Article> articles) {

        setTitle(currentNewsSource);
        for (int i = 0; i < pageAdapter.getCount(); i++)
            pageAdapter.notifyChangeInPosition(i);

        fragments.clear();

        for (int i = 0; i < articles.size(); i++) {
            fragments.add(Fragments.newFragment(articles.get(i), i, articles.size()));
        }

        pageAdapter.notifyDataSetChanged();
        pager.setCurrentItem(0);
        articleArrayList = articles;
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        MakeLayout layoutRestore;
        layoutRestore = (MakeLayout) savedInstanceState.getSerializable("state");
        stateFlag = true;
        articleArrayList = layoutRestore.getArticleList();
        catList = layoutRestore.getCategories();
        sourceArrayList = layoutRestore.getSourceList();
        for(int i=0;i<sourceArrayList.size();i++){
            srcList.add(sourceArrayList.get(i).getsName());
            sourceDataMap.put(sourceArrayList.get(i).getsName(), (Source)sourceArrayList.get(i));
        }
        mDrawerList.clearChoices();
        adapter.notifyDataSetChanged();
        mDrawerList.setOnItemClickListener(

                new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        pager.setBackgroundResource(0);
                        currentSourcePointer = position;
                        selectItem(position);

                    }
                }
        );
        setTitle("News Gateway Application");

    }


    private class MyPageAdapter extends FragmentPagerAdapter {
        private long baseId = 0;


        public MyPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public long getItemId(int position) {

            return baseId + position;
        }

        public void notifyChangeInPosition(int n) {
            baseId += getCount() + n;
        }

    }

    class NewsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_NEWS_STORY.equals(intent.getAction())) {
                if (intent.hasExtra(ARTICLE_LIST)) {
                    ArrayList<Article> artList;
                    artList = (ArrayList<Article>) intent.getSerializableExtra(ARTICLE_LIST);
                    reDoFragments(artList);
                }
            }
        }
    }
}


