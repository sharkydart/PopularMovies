package com.udacityproject.cmcmc.popularmovies.Model;

import org.json.JSONException;
import org.json.JSONObject;

public class MovieTrailer {
    private String id;
    private String iso_639_1;
    private String iso_3166_1;
    private String key;
    private String name;
    private String site;
    private int size;
    private String type;

    public MovieTrailer(String id){
        this.id = id;
    }
    public MovieTrailer(String id, String key, String name, String site, int size, String type) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.site = site;
        this.size = size;
        this.type = type;
    }
    public MovieTrailer(JSONObject jsonTrailerInfo){
        if(jsonTrailerInfo != null){
            try{
                this.id = jsonTrailerInfo.getString("id");
                this.iso_639_1 = jsonTrailerInfo.getString("iso_639_1");
                this.iso_3166_1 = jsonTrailerInfo.getString("iso_3166_1");
                this.key = jsonTrailerInfo.getString("key");
                this.name = jsonTrailerInfo.getString("name");
                this.site = jsonTrailerInfo.getString("site");
                this.size = jsonTrailerInfo.getInt("size");
                this.type = jsonTrailerInfo.getString("type");
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    public String toString(){
        return this.site + " trailer: " + this.name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIso_639_1() {
        return iso_639_1;
    }

    public void setIso_639_1(String iso_639_1) {
        this.iso_639_1 = iso_639_1;
    }

    public String getIso_3166_1() {
        return iso_3166_1;
    }

    public void setIso_3166_1(String iso_3166_1) {
        this.iso_3166_1 = iso_3166_1;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
