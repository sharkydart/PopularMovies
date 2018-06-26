package com.udacityproject.cmcmc.popularmovies.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FavoritesDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "favorites.db";
    private static final int DATABASE_VERSION = 1;

    public FavoritesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_TABLE = "CREATE TABLE " +
                FavoritesContract.FavoritesEntry.TABLE_NAME + " (" +
                FavoritesContract.FavoritesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FavoritesContract.FavoritesEntry.COLUMN_TITLE + " TEXT NOT NULL," +
                FavoritesContract.FavoritesEntry.COLUMN_MOVIEID + " INTEGER NOT NULL," +
                FavoritesContract.FavoritesEntry.COLUMN_RELEASEDATE + " TEXT NOT NULL," +
                FavoritesContract.FavoritesEntry.COLUMN_VOTEAVG + " REAL NOT NULL," +
                FavoritesContract.FavoritesEntry.COLUMN_POSTERPATH + " TEXT NOT NULL," +
                FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL," +
                FavoritesContract.FavoritesEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavoritesContract.FavoritesEntry.TABLE_NAME);
        onCreate(db);
    }
}
