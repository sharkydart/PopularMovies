package com.udacityproject.cmcmc.popularmovies.Model;

import org.json.JSONException;
import org.json.JSONObject;

public class MovieReview {
    private String id;
    private String author;
    private String content;
    private String url;

    public MovieReview(String id, String author, String content, String url) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.url = url;
    }
    public MovieReview(JSONObject jsonReviewInfo){
        if(jsonReviewInfo != null){
            try{
                this.id = jsonReviewInfo.getString("id");
                this.author = jsonReviewInfo.getString("author");
                this.content = jsonReviewInfo.getString("content");
                this.url = jsonReviewInfo.getString("url");
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    public String toString(){
        return this.getAuthor() + " says, \"" + this.getContent() + "\"";
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
