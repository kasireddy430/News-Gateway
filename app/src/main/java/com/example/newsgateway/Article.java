package com.example.newsgateway;

import java.io.Serializable;

public class Article implements Serializable {
    String author;
    String Title;
    String description;
    String urlToImage;
    String publishedAt;
    String articleUrl;

    public String getaAuthor()
    {
        return author;
    }

    public void setaAuthor(String author) {
        this.author = author;
    }

    public String getaTitle() {
        return Title;
    }

    public void setaTitle(String title) {
        this.Title = title;
    }

    public String getaDescription() {
        return description;
    }

    public void setaDescription(String description) {
        this.description = description;
    }

    public String getaUrlToImage() {
        return urlToImage;
    }

    public void setaUrlToImage(String urlToImage) {
        this.urlToImage = urlToImage;
    }

    public String getaPublishedAt() {
        return publishedAt;
    }

    public void setaPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getArticleUrl() {
        return articleUrl;
    }

    public void setArticleUrl(String articleUrl) {
        this.articleUrl = articleUrl;
    }




}

