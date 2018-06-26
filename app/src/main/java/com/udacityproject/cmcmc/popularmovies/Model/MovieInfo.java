package com.udacityproject.cmcmc.popularmovies.Model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MovieInfo{
    private int vote_count;
    private int id;
    private boolean video;
    private double vote_average;
    private String title;
    private double popularity;
    private String poster_path;
    private String original_language;
    private String original_title;
    private List<Integer> genre_ids;
    private String backdrop_path;
    private boolean adult;
    private String overview;
    private String release_date;

    public MovieInfo(int id, double vote_average, String title, String poster_path, String overview, String release_date) {
        this.id = id;
        this.vote_average = vote_average;
        this.title = title;
        this.poster_path = poster_path;
        this.overview = overview;
        this.release_date = release_date;
    }
    public MovieInfo(JSONObject jsonMovieInfo){
        if(jsonMovieInfo != null) {
            try {
                this.id = jsonMovieInfo.getInt("id");
                this.vote_count = jsonMovieInfo.getInt("vote_count");
                this.video = jsonMovieInfo.getBoolean("video");
                this.vote_average = jsonMovieInfo.getDouble("vote_average");
                this.title = jsonMovieInfo.getString("title");
                this.popularity = jsonMovieInfo.getDouble("popularity");
                this.poster_path = jsonMovieInfo.getString("poster_path");
                this.original_language = jsonMovieInfo.getString("original_language");
                this.original_title = jsonMovieInfo.getString("original_title");
                List<Integer> genreIdsList = new ArrayList<Integer>();
                JSONArray tempArray = jsonMovieInfo.getJSONArray("genre_ids");
                for(int i = 0; i < tempArray.length(); i++) {
                    genreIdsList.add(tempArray.getInt(i));
                }
                this.setGenre_ids(genreIdsList);
                this.backdrop_path = jsonMovieInfo.getString("backdrop_path");
                this.adult = jsonMovieInfo.getBoolean("adult");
                this.overview = jsonMovieInfo.getString("overview");
                this.release_date = jsonMovieInfo.getString("release_date");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public int getVote_count() {
        return vote_count;
    }

    public void setVote_count(int vote_count) {
        this.vote_count = vote_count;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isVideo() {
        return video;
    }

    public void setVideo(boolean video) {
        this.video = video;
    }

    public double getVote_average() {
        return vote_average;
    }

    public void setVote_average(double vote_average) {
        this.vote_average = vote_average;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPopularity() {
        return popularity;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

    public String getOriginal_language() {
        return original_language;
    }

    public void setOriginal_language(String original_language) {
        this.original_language = original_language;
    }

    public String getOriginal_title() {
        return original_title;
    }

    public void setOriginal_title(String original_title) {
        this.original_title = original_title;
    }

    public List<Integer> getGenre_ids() {
        return genre_ids;
    }

    public void setGenre_ids(List<Integer> genre_ids) {
        this.genre_ids = genre_ids;
    }

    public String getBackdrop_path() {
        return backdrop_path;
    }

    public void setBackdrop_path(String backdrop_path) {
        this.backdrop_path = backdrop_path;
    }

    public boolean isAdult() {
        return adult;
    }

    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

}
