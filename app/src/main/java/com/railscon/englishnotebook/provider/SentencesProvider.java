package com.railscon.englishnotebook.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.railscon.englishnotebook.utilities.DbOpenHelper;

/**
 * Created by maxazure on 2018/3/11.
 */

public class SentencesProvider extends ContentProvider {

    private static final String AUTHORITY = "com.railscon.englishnotebook.sentencesprovider";
    private static final String BASE_PATH = "Sentences";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH );

    // Constant to identify the requested operation
    private static final int SENTENCES_ID = 1;

    private static final UriMatcher uriMatcher =
            new UriMatcher((UriMatcher.NO_MATCH));

    public static final String CONTENT_ITEM_TYPE = "Conversation";

    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", SENTENCES_ID);
    }
    private SQLiteDatabase database;
    @Override
    public boolean onCreate() {

        DbOpenHelper helper = new DbOpenHelper(getContext());
        database = helper.getWritableDatabase();
        return true;

    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
     //   if(uriMatcher.match(uri) == SENTENCES_ID)
     //       selection = DbOpenHelper.S_LESSON_ID + "=" + uri.getLastPathSegment();

        Log.d("Maxazure",selection);
        return database.query(DbOpenHelper.TABLE_SENTENCES, DbOpenHelper.ALL_COLUMNS_IN_SENTENCES,
                selection, null, null,null, DbOpenHelper.S_SEQUENCE);

    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        long id = database.insert(DbOpenHelper.TABLE_SENTENCES, null, values);
        return Uri.parse(BASE_PATH+ "/" +id);

    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return database.delete(DbOpenHelper.TABLE_SENTENCES, selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return database.update(DbOpenHelper.TABLE_SENTENCES,
                values, selection, selectionArgs);
    }
}
