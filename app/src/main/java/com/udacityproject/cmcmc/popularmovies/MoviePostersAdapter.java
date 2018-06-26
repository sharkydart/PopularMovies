package com.udacityproject.cmcmc.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.udacityproject.cmcmc.popularmovies.Model.MovieInfo;
import com.udacityproject.cmcmc.popularmovies.database.FavoritesContract;
import com.udacityproject.cmcmc.popularmovies.database.FavoritesContract.FavoritesEntry;

import java.util.List;

public class MoviePostersAdapter extends BaseAdapter {
    private final Context mContext;
    private List<MovieInfo> mPosters;
    private boolean mFromDb;
    private Cursor mDBCursor;

    public MoviePostersAdapter(Context theContext, List<MovieInfo> thePosters, Cursor theDbCursor, boolean fromDb){
        this.mContext = theContext;
        this.mPosters = thePosters;
        this.mDBCursor = theDbCursor;
        this.mFromDb = fromDb;
    }
    public MoviePostersAdapter(Context theContext, List<MovieInfo> thePosters){
        this.mContext = theContext;
        this.mPosters = thePosters;
    }

    public void useFavoritesDb(boolean fromDb){
        mFromDb = fromDb;
    }
    public void swapCursor(Cursor newCursor){
        if(newCursor != null) {
            if(mDBCursor != null)
                mDBCursor.close();
            mDBCursor = newCursor;
        }
    }

    @Override
    public int getCount() {
        if(mFromDb)
            return mDBCursor.getCount();
        return mPosters.size();
    }

    @Override
    public Object getItem(int position) {
        if(mFromDb){
            if(mDBCursor.moveToPosition(position)){
                MovieInfo tempMovie = new MovieInfo(
                        mDBCursor.getInt(mDBCursor.getColumnIndex(FavoritesEntry.COLUMN_MOVIEID)),
                        mDBCursor.getDouble(mDBCursor.getColumnIndex(FavoritesEntry.COLUMN_VOTEAVG)),
                        mDBCursor.getString(mDBCursor.getColumnIndex(FavoritesEntry.COLUMN_TITLE)),
                        mDBCursor.getString(mDBCursor.getColumnIndex(FavoritesEntry.COLUMN_POSTERPATH)),
                        mDBCursor.getString(mDBCursor.getColumnIndex(FavoritesEntry.COLUMN_OVERVIEW)),
                        mDBCursor.getString(mDBCursor.getColumnIndex(FavoritesEntry.COLUMN_RELEASEDATE))
                );
                return tempMovie;
            }
            else{
                Log.d("fart", "Couldn't move to position");
                return null;
            }
        }
        return mPosters.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView samplePoster;
        if(convertView == null){
            samplePoster = new ImageView(mContext);
            samplePoster.setScaleType(ImageView.ScaleType.FIT_START);
            samplePoster.setAdjustViewBounds(true);
        } else{
            samplePoster = (ImageView) convertView;
        }
        String imgApiBasePath = mContext.getResources().getString(R.string.base_url_images);
        String posterSize = mContext.getResources().getStringArray(R.array.poster_sizes)[4];
        String imgpath = imgApiBasePath + posterSize + ((MovieInfo)getItem(position)).getPoster_path();

        Picasso.get()
                .load(imgpath)
                .into(samplePoster);
        return samplePoster;
    }
}
