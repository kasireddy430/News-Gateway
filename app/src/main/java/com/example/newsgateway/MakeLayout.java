package com.example.newsgateway;

import java.io.Serializable;
import java.util.ArrayList;

public class MakeLayout implements Serializable {
    private ArrayList<Source> sourceList = new ArrayList<Source>();
    private ArrayList<Article> articleList = new ArrayList <Article>();
    private ArrayList<String> categories = new ArrayList <String>();

    public ArrayList <Source> getSourceList() {
        return sourceList;
    }

    public void setSourceList(ArrayList <Source> sourceList) {
        this.sourceList = sourceList;
    }

    public ArrayList <Article> getArticleList() {
        return articleList;
    }

    public void setArticleList(ArrayList <Article> articleList) {
        this.articleList = articleList;
    }

    public ArrayList <String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList <String> categories) {
        this.categories = categories;
    }

    public void setCurrentSource(int currentSource) {
    }

    public void setCurrentArticle(int currentArticle) {
    }
}

