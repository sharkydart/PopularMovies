package com.udacityproject.cmcmc.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Movie;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.udacityproject.cmcmc.popularmovies.Model.MovieInfo;
import com.udacityproject.cmcmc.popularmovies.database.FavoritesContract;
import com.udacityproject.cmcmc.popularmovies.database.FavoritesContract.FavoritesEntry;
import com.udacityproject.cmcmc.popularmovies.database.FavoritesDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainDiscovery extends AppCompatActivity {

    private GridView mMoviePosters;
    private List<MovieInfo> mMovies;
    private MoviePostersAdapter mMoviePostersAdapter;
    private SQLiteDatabase mFavoritesDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_discovery);

        // database setup
        FavoritesDbHelper dbHelper = new FavoritesDbHelper(this);
        mFavoritesDb = dbHelper.getReadableDatabase();

        mMovies = new ArrayList<MovieInfo>();
        mMoviePostersAdapter = new MoviePostersAdapter(this, mMovies, getFavoritesFromDb(), false);

        mMoviePosters = (GridView)findViewById(R.id.gv_moviePosters);
        mMoviePosters.setAdapter(mMoviePostersAdapter);
        mMoviePosters.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                launchMovieDetailsActivity(position);
                //load detail view from here, sending/setting movie info selected
            }
        });

        //Start by loading based on most popular
        String sortMethod = this.getString(R.string.get_most_popular);
        loadMovieData(sortMethod);
    }

    private void loadMovieData(String sortMethod) {
        new FetchMoviesTask().execute(sortMethod);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }
            if(params[0].equals(MainDiscovery.this.getString(R.string.load_db_favorites))){
                Log.d("fart", "Load from database!");
                return params[0];
            }else{
                mMoviePostersAdapter.useFavoritesDb(false);
            }

            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

                if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                    String sortMethod = params[0];
                    URL moviesRequestUrl = buildSortAPIUrl(sortMethod);

                    try {
                        String moviesResponse = getResponseFromUrl(moviesRequestUrl);

                        return moviesResponse;

                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }catch(NullPointerException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String movieData) {
            if (movieData != null) {
                if(movieData.equals(MainDiscovery.this.getString(R.string.load_db_favorites))){
                    Log.d("fart", "Movie Posters Adapter should load based off of DB list of movie_ids");
                    mMoviePostersAdapter.useFavoritesDb(true);
                    mMoviePostersAdapter.swapCursor(getFavoritesFromDb());
                    mMoviePostersAdapter.notifyDataSetChanged();
                }else {
                    try {
                        mMovies.clear();
                        JSONObject movieJson = new JSONObject(movieData);
                        int page = movieJson.getInt("page");
                        int total_results = movieJson.getInt("total_results");
                        int total_pages = movieJson.getInt("total_pages");
                        JSONArray results = movieJson.getJSONArray("results");
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject currentObj = results.getJSONObject(i);
                            MovieInfo tempMovie = new MovieInfo(currentObj);
                            mMovies.add(tempMovie);
                        }
                        mMoviePostersAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainDiscovery.this, "Data Issue", Toast.LENGTH_LONG).show();
                    }
                }
            }
            else{
                Toast.makeText(MainDiscovery.this, "Network Issue", Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sortselection, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_most_popular) {
            String sortMethod = this.getString(R.string.get_most_popular);
            loadMovieData(sortMethod);
            return true;
        }
        else if(id == R.id.action_highest_rated){
            String sortMethod = this.getString(R.string.get_top_rated);
            loadMovieData(sortMethod);
            return true;
        }
        else if(id == R.id.action_show_favorited){
            String sortMethod = this.getString(R.string.load_db_favorites);
            loadMovieData(sortMethod);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public URL buildSortAPIUrl(String sortAPI) {
        /*  Example API call
        https://api.themoviedb.org/3
            /movie/popular
            ?
            api_key=012987c6c4644746653878570fdf79dd
            &language=en-US
            &page=1
        */

        int pageToReturn = 1;
        Uri builtUri = Uri.parse(this.getString(R.string.base_url)).buildUpon()
                .appendEncodedPath(sortAPI)
                .appendQueryParameter(this.getString(R.string.param_api), this.getString(R.string.query_apikey))
                .appendQueryParameter(this.getString(R.string.param_language), this.getString(R.string.query_language))
                .appendQueryParameter(this.getString(R.string.param_page), Integer.toString(pageToReturn))
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static String getResponseFromUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    private void launchMovieDetailsActivity(int position) {
        Intent intent = new Intent(this, MovieDetailsActivity.class);
        intent.putExtra(MovieDetailsActivity.MOVIE_ID, ((MovieInfo)mMoviePostersAdapter.getItem(position)).getId());
        intent.putExtra(MovieDetailsActivity.MOVIE_VOTE_AVERAGE, ((MovieInfo)mMoviePostersAdapter.getItem(position)).getVote_average());
        intent.putExtra(MovieDetailsActivity.MOVIE_TITLE, ((MovieInfo)mMoviePostersAdapter.getItem(position)).getTitle());
        intent.putExtra(MovieDetailsActivity.MOVIE_POSTER_PATH, ((MovieInfo)mMoviePostersAdapter.getItem(position)).getPoster_path());
        intent.putExtra(MovieDetailsActivity.MOVIE_OVERVIEW, ((MovieInfo)mMoviePostersAdapter.getItem(position)).getOverview());
        intent.putExtra(MovieDetailsActivity.MOVIE_RELEASE_DATE, ((MovieInfo)mMoviePostersAdapter.getItem(position)).getRelease_date());
        startActivity(intent);
    }

    private Cursor getFavoritesFromDb(){
        String columns[] = {
                FavoritesEntry.COLUMN_TITLE,
                FavoritesEntry.COLUMN_MOVIEID,
                FavoritesEntry.COLUMN_VOTEAVG,
                FavoritesEntry.COLUMN_RELEASEDATE,
                FavoritesEntry.COLUMN_POSTERPATH,
                FavoritesEntry.COLUMN_OVERVIEW
        };
        return mFavoritesDb.query(
                FavoritesEntry.TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                FavoritesEntry.COLUMN_TITLE);
    }
}
