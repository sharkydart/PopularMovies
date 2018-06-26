package com.udacityproject.cmcmc.popularmovies.database;

import android.provider.BaseColumns;

public class FavoritesContract {
    public static final class FavoritesEntry implements BaseColumns {
        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_MOVIEID = "movie_id";
        public static final String COLUMN_RELEASEDATE = "movie_release_date";
        public static final String COLUMN_VOTEAVG = "movie_vote_avg";
        public static final String COLUMN_POSTERPATH = "movie_poster_path";
        public static final String COLUMN_OVERVIEW = "movie_overview";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }
}