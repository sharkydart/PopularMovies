package com.udacityproject.cmcmc.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.udacityproject.cmcmc.popularmovies.Model.MovieInfo;
import com.udacityproject.cmcmc.popularmovies.Model.MovieReview;
import com.udacityproject.cmcmc.popularmovies.Model.MovieTrailer;
import com.udacityproject.cmcmc.popularmovies.database.FavoritesContract;
import com.udacityproject.cmcmc.popularmovies.database.FavoritesContract.FavoritesEntry;
import com.udacityproject.cmcmc.popularmovies.database.FavoritesDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class MovieDetailsActivity extends AppCompatActivity {
    public static final String MOVIE_ID = "movie_id";
    public static final String MOVIE_VOTE_AVERAGE = "movie_vote_average";
    public static final String MOVIE_TITLE = "movie_title";
    public static final String MOVIE_POSTER_PATH = "movie_poster_path";
    public static final String MOVIE_OVERVIEW = "movie_overview";
    public static final String MOVIE_RELEASE_DATE = "movie_release_date";
    private ImageButton mFavStar;
    private boolean mFavorited;
    private SQLiteDatabase mFavoritesDb;

    private List<MovieTrailer> mTrailers;
    private ArrayAdapter<MovieTrailer> mTrailersAdapter;
    private List<MovieReview> mReviews;
    private ArrayAdapter<MovieReview> mReviewsAdapter;
    private int mMovieId;
    private String mMovieTitle;
    private double mMovieVoteAvg;
    private String mMovieReleaseDate;
    private String mMoviePosterPath;
    private String mMovieOverview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        // database setup
        FavoritesDbHelper dbHelper = new FavoritesDbHelper(this);
        mFavoritesDb = dbHelper.getWritableDatabase();

        mTrailers = new ArrayList<>();
        mReviews = new ArrayList<>();

        Intent intent = getIntent();
        if (intent == null) {
            closeOnError();
        }else {
            TextView tvReleaseDate = (TextView) findViewById(R.id.tvReleaseDate);
            TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
            TextView tvUserRating = (TextView) findViewById(R.id.tvUserRating);
            TextView tvPlotSynopsis = (TextView) findViewById(R.id.tvPlotSynopsis);
            ImageView ivMoviePoster = (ImageView) findViewById(R.id.ivMoviePoster);

            mMovieId = intent.getIntExtra(MOVIE_ID, -99);
            if (mMovieId == -99) {
                closeOnError();
            }
            mMovieVoteAvg = intent.getDoubleExtra(MOVIE_VOTE_AVERAGE, -99.0);
            if (mMovieVoteAvg == -99.0) {
                closeOnError();
            }
            mMovieTitle = intent.getStringExtra(MOVIE_TITLE);
            mMoviePosterPath = intent.getStringExtra(MOVIE_POSTER_PATH);
            mMovieOverview = intent.getStringExtra(MOVIE_OVERVIEW);
            mMovieReleaseDate = intent.getStringExtra(MOVIE_RELEASE_DATE);

            tvReleaseDate.setText(mMovieReleaseDate);
            tvTitle.setText(mMovieTitle);
            ivMoviePoster.setContentDescription(mMovieTitle + " Poster Image");
            tvUserRating.setText(String.format(Locale.US, "%1.1f", mMovieVoteAvg) + "  /  10");
            tvPlotSynopsis.setText(mMovieOverview);

            // do stuff for the movie poster image
            String imgApiBasePath = this.getResources().getString(R.string.base_url_images);
            String posterSize = this.getResources().getStringArray(R.array.poster_sizes)[4];
            String imgpath = imgApiBasePath + posterSize + mMoviePosterPath;
            Picasso.get()
                    .load(imgpath)
                    .into(ivMoviePoster);

            // do stuff for favorite button
            mFavStar = (ImageButton) findViewById(R.id.btnFavorited);
            //get initial status of star
            mFavorited = isMovieFavorited();
            if(mFavorited)
                mFavStar.setImageResource(android.R.drawable.star_on);
            else
                mFavStar.setImageResource(android.R.drawable.star_off);
            mFavStar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mFavorited) {
                        //delete from DB
                        Log.d("fart", "Make MovieID Un-Favorited: " + mMovieId);
                        if(unFavoriteMovieFromDb())
                            Log.d("fart", "Un-Favorited that movie!");
                        else
                            Log.d("fart", "nothing to unfavorite...");
                        mFavStar.setImageResource(android.R.drawable.star_off);
                    }
                    else {
                        //put into DB
                        Log.d("fart", "Make MovieID Favorited: " + mMovieId);
                        addFavoritedMovie();
                        mFavStar.setImageResource(android.R.drawable.star_on);
                    }
                    mFavorited = !mFavorited;
                }
            });

            mTrailersAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, mTrailers);
            ListView trailersList = findViewById(R.id.trailers_listview);
            trailersList.setAdapter(mTrailersAdapter);
            trailersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    clickedTrailer(MovieDetailsActivity.this, mTrailers.get(position).getKey());
                }
            });

            // startFetchingTrailers
            String trailerPath = getString(R.string.get_movie) + Integer.toString(mMovieId) + getString(R.string.get_movie_trailers);
            new FetchTrailersTask().execute(trailerPath);

            mReviewsAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, mReviews);
            ListView reviewsList = findViewById(R.id.reviews_listview);
            reviewsList.setAdapter(mReviewsAdapter);

            // startFetchingReviews
            String reviewPath = getString(R.string.get_movie) + Integer.toString(mMovieId) + getString(R.string.get_movie_reviews);
            new FetchReviewsTask().execute(reviewPath);
        }
    }
    private class FetchTrailersTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... params) {
            if (params.length == 0) {return null;}

            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

                if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                    String pathForFetch = params[0];
                    URL fetchRequestUrl = buildFetchAPIUrl(pathForFetch);

                    try {
                        String queryResponse = MovieDetailsActivity.getResponseFromUrl(fetchRequestUrl);
                        return queryResponse;
                    }catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }catch(NullPointerException e) {e.printStackTrace();}
            return null;
        }
        @Override
        protected void onPostExecute(String trailersData) {
            if (trailersData != null) {
                try {
                    mTrailers.clear();
                    JSONObject trailersJson = new JSONObject(trailersData);
                    int id = trailersJson.getInt("id");
                    JSONArray results = trailersJson.getJSONArray("results");
                    for(int i=0; i<results.length(); i++) {
                        JSONObject currentObj = results.getJSONObject(i);
                        MovieTrailer tempTrailer = new MovieTrailer(currentObj);
                        Log.d("fart", "trailer: " + tempTrailer.getName());
                        mTrailers.add(tempTrailer);
                    }
                    mTrailersAdapter.notifyDataSetChanged();
                }catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(MovieDetailsActivity.this, "Trailers Data Issue", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(MovieDetailsActivity.this, "Trailers Network Issue", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class FetchReviewsTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... params) {
            if (params.length == 0) {return null;}

            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

                if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                    String pathForFetch = params[0];
                    URL fetchRequestUrl = buildFetchAPIUrl(pathForFetch);

                    try {
                        String queryResponse = MovieDetailsActivity.getResponseFromUrl(fetchRequestUrl);
                        return queryResponse;
                    }catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }catch(NullPointerException e) {e.printStackTrace();}
            return null;
        }
        @Override
        protected void onPostExecute(String reviewsData) {
            if (reviewsData != null) {
                try {
                    mReviews.clear();
                    JSONObject reviewsJson = new JSONObject(reviewsData);
                    int id = reviewsJson.getInt("id");
                    int page = reviewsJson.getInt("page");
                    JSONArray results = reviewsJson.getJSONArray("results");
                    for(int i=0; i<results.length(); i++) {
                        JSONObject currentObj = results.getJSONObject(i);
                        MovieReview tempReview = new MovieReview(currentObj);
                        Log.d("fart", "review by: " + tempReview.getAuthor());
                        mReviews.add(tempReview);
                    }
                    int total_pages = reviewsJson.getInt("total_pages");
                    int total_results = reviewsJson.getInt("total_results");
                    mReviewsAdapter.notifyDataSetChanged();
                }catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(MovieDetailsActivity.this, "Reviews Data Issue", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(MovieDetailsActivity.this, "Reviews Network Issue", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private URL buildFetchAPIUrl(String movieInfoPath) {
        /*  Example API call
        https://api.themoviedb.org/3
            movieInfoPath = "/movie/movie_id/videos" || "/movie/movie_id/reviews"
            ?
            api_key=012987c6c4644746653878570fdf79dd
            &language=en-US
            &page=1
        */

        int pageToReturn = 1;
        Uri builtUri = Uri.parse(this.getString(R.string.base_url)).buildUpon()
                .appendEncodedPath(movieInfoPath)
                .appendQueryParameter(this.getString(R.string.param_api), this.getString(R.string.query_apikey))
                .appendQueryParameter(this.getString(R.string.param_language), this.getString(R.string.query_language))
                .appendQueryParameter(this.getString(R.string.param_page), Integer.toString(pageToReturn))
                .build();

        URL url = null;
        try {
            Log.d("fart", "url: " + builtUri.toString());
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

    private void closeOnError() {
        finish();
        Toast.makeText(this, "Check Connectivity", Toast.LENGTH_SHORT).show();
    }

    private boolean isMovieFavorited(){
        Log.d("fart", "pull from db where: " + mMovieId + " exists");
        String[] pullColumns = {FavoritesEntry.COLUMN_TITLE,
                FavoritesEntry.COLUMN_MOVIEID,
                FavoritesEntry.COLUMN_VOTEAVG,
                FavoritesEntry.COLUMN_RELEASEDATE,
                FavoritesEntry.COLUMN_POSTERPATH,
                FavoritesEntry.COLUMN_OVERVIEW
        };
        String theWhereClause = FavoritesEntry.COLUMN_MOVIEID + " = ?";
        String[] theWhereArgument = {Integer.toString(mMovieId)};
        Cursor tempCursor = mFavoritesDb.query(FavoritesContract.FavoritesEntry.TABLE_NAME,
                pullColumns,
                theWhereClause,
                theWhereArgument,
                null,
                null,
                FavoritesContract.FavoritesEntry.COLUMN_TIMESTAMP);
        tempCursor.moveToPosition(0);
        int theCount = tempCursor.getCount();
        tempCursor.close();
        return (theCount >= 1);
    }
    private long addFavoritedMovie(){
        ContentValues cvObj = new ContentValues();
        //movie_id
        cvObj.put(FavoritesEntry.COLUMN_MOVIEID, mMovieId);
        //movie_title
        cvObj.put(FavoritesEntry.COLUMN_TITLE, mMovieTitle);
        //movie_release_date
        cvObj.put(FavoritesEntry.COLUMN_RELEASEDATE, mMovieReleaseDate);
        //movie_vote_avg
        cvObj.put(FavoritesEntry.COLUMN_VOTEAVG, mMovieVoteAvg);
        //movie_poster_path
        cvObj.put(FavoritesEntry.COLUMN_POSTERPATH, mMoviePosterPath);
        //movie_overview
        cvObj.put(FavoritesEntry.COLUMN_OVERVIEW, mMovieOverview);
        //add to db
        return mFavoritesDb.insert(FavoritesEntry.TABLE_NAME, null, cvObj);
    }
    private boolean unFavoriteMovieFromDb(){
        return (mFavoritesDb.delete(FavoritesEntry.TABLE_NAME,
                FavoritesEntry.COLUMN_MOVIEID + " = " + mMovieId, null) > 0);
    }

    private void clickedTrailer(Context context, String id){
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            context.startActivity(webIntent);
        }
    }
}
