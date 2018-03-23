package com.railscon.englishnotebook.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.railscon.englishnotebook.utilities.DbOpenHelper;

/**
 * Created by maxazure on 2018/3/2.
 */

public class ConversationsProvider extends ContentProvider {

    private static final String AUTHORITY = "com.railscon.englishnotebook.conversationsprovider";
    private static final String BASE_PATH = "conversations";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH );

    // Constant to identify the requested operation
    private static final int CONVERSATIONS = 1;
    private static final int CONVERSATIONS_ID = 2;

    private static final UriMatcher uriMatcher =
            new UriMatcher((UriMatcher.NO_MATCH));

    public static final String CONTENT_ITEM_TYPE = "Conversation";

    //****
    //  Need to understand.
    //****
    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, CONVERSATIONS);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", CONVERSATIONS_ID);
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
    public Cursor query(@NonNull Uri uri, @Nullable String[] selectionArgs, String selection, String[] having,  String orderBy) {

        if(uriMatcher.match(uri) == CONVERSATIONS_ID)
            selection = DbOpenHelper.C_ID + "=" + uri.getLastPathSegment();

        return database.query(DbOpenHelper.TABLE_CONVERSATIONS, DbOpenHelper.ALL_COLUMNS_IN_CONVERSATIONS,
                selection, null, null,null, DbOpenHelper.C_CREATED + " DESC");
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        long id = database.insert(DbOpenHelper.TABLE_CONVERSATIONS, null, contentValues);
        return Uri.parse(BASE_PATH+ "/" +id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String whereClause, @Nullable String[] whereArgs) {
        return database.delete(DbOpenHelper.TABLE_CONVERSATIONS, whereClause, whereArgs);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String whereClause, @Nullable String[] whereArgs) {
        return database.update(DbOpenHelper.TABLE_CONVERSATIONS,
                contentValues, whereClause, whereArgs);
    }


}
