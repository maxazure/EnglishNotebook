package com.railscon.englishnotebook.utilities;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by maxazure on 2018/3/1.
 */

public class DbOpenHelper extends SQLiteOpenHelper {

    // constants for db name and version
    private static final String DATABASE_NAME = "english.db";
    private static final int DATABASE_VERSION = 1;

    // Constants for identifying table and columns
    public static final String TABLE_CONVERSATIONS = "conversations";
    public static final String TABLE_SENTENCES = "sentences";


    public static final String C_ID = "_id";
    public static final String C_TITLE = "cTitle";
    public static final String C_BODY = "cBody";
    public static final String C_VOICE = "cVoice";
    public static final String C_CREATED = "cCreated";
    public static final String C_SORTID = "cSort";
    public static final String C_RANK = "cRank";
    public static final String C_TAGS = "cTags";
    public static final String C_LESSON_ID = "cLessonId";


    public static final String S_ID = "_id";
    public static final String S_SEQUENCE = "sequence";
    public static final String S_EN_SENTENCE = "en_sentence";
    public static final String S_CN_SENTENCE = "cn_sentence";
    public static final String S_START_TIME = "start_time";
    public static final String S_END_TIME = "end_time";
    public static final String S_DURATION = "duration";
    public static final String S_LESSON_ID = "lesson_id";


    public static final String[] ALL_COLUMNS_IN_CONVERSATIONS =
            {C_ID, C_TITLE, C_BODY, C_VOICE, C_CREATED, C_SORTID, C_RANK, C_TAGS,C_LESSON_ID};
    public static final String[] ALL_COLUMNS_IN_SENTENCES =
            {S_ID,S_SEQUENCE, S_EN_SENTENCE, S_CN_SENTENCE, S_START_TIME, S_END_TIME, S_DURATION, S_LESSON_ID};


    //SQL to create conversation table
    public static final String TABLE_CONVERSATIONS_CREATE =
            "CREATE TABLE " + TABLE_CONVERSATIONS + " (" +
                    C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    C_TITLE + " TEXT, " +
                    C_BODY + " TEXT, " +
                    C_VOICE + " TEXT, " +
                    C_CREATED + " TEXT default CURRENT_TIMESTAMP, " +
                    C_SORTID + " INTEGER default 0, " +
                    C_RANK + " INTEGER default 0, " +
                    C_TAGS + " TEXT, " +
                    C_LESSON_ID + " TEXT" +
                    ")";
    //SQL to create sentences table
    public static final String TABLE_SENTENCES_CREATE =
            "CREATE TABLE " + TABLE_SENTENCES + " (" +
                    S_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    S_SEQUENCE + " INTEGER," +
                    S_EN_SENTENCE + " TEXT, " +
                    S_CN_SENTENCE+ " TEXT, " +
                    S_START_TIME + " INTEGER, " +
                    S_END_TIME + " INTEGER default 0, " +
                    S_DURATION+ " INTEGER default 0, " +
                    S_LESSON_ID + " INTEGER" +
                    ")";

    public DbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CONVERSATIONS_CREATE);
        db.execSQL(TABLE_SENTENCES_CREATE);
        db.execSQL("insert ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONVERSATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENTENCES);
        onCreate(db);
    }


}
