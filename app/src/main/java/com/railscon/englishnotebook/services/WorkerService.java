package com.railscon.englishnotebook.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

import com.railscon.englishnotebook.R;
import com.railscon.englishnotebook.model.DataItem;
import com.railscon.englishnotebook.retrofit.RetrofitClient;
import com.railscon.englishnotebook.retrofit.model.Lesson;
import com.railscon.englishnotebook.retrofit.model.Sentence;
import com.railscon.englishnotebook.retrofit.model.Version;
import com.railscon.englishnotebook.retrofit.services.SoService;
import com.railscon.englishnotebook.utilities.DbOpenHelper;
import com.railscon.englishnotebook.utilities.PrefUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;




public class WorkerService extends IntentService {


    public static final String TAG = "WorkerService";
    public static final String MY_SERVICE_MESSAGE = "myServiceMessage";
    public static final String MY_SERVICE_PAYLOAD = "myServicePayload";


    public WorkerService() {
        super("WorkerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {


        // TODO: make request to get Lessons
        getVersionNum();



    }

    private void sendMsgToBroadcast(String msgName, String msgValue) {
        //        Return the data to MainActivity
        Intent messageIntent = new Intent(MY_SERVICE_MESSAGE);
        messageIntent.putExtra(msgName, msgValue);
        LocalBroadcastManager manager =
                LocalBroadcastManager.getInstance(getApplicationContext());
        manager.sendBroadcast(messageIntent);
    }

    private void getVersionNum() {

        final Retrofit retrofit = RetrofitClient.getClient();
        SoService service = retrofit.create(SoService.class);
        Call<Version> call = service.getVersion();
        call.enqueue(new Callback<Version>() {
                         @Override
                         public void onResponse(Call<Version> call, Response<Version> response) {

                             String res = null;
                             if (response.body() != null) {
                                 int oldversion = Integer.parseInt(PrefUtils.getFromPrefs(getApplicationContext(),"version","0"));
                                 int newVersion = response.body().getVersion();
                                 if(oldversion<newVersion)
                                 { sendMsgToBroadcast(MY_SERVICE_PAYLOAD, "Found new version, downloading...");
                                     getLesson();
                                     PrefUtils.saveToPrefs(getApplicationContext(),"version",Integer.toString(newVersion));
                                 }
                             }
                         }

                         @Override
                         public void onFailure(Call<Version> call, Throwable t) {
                             sendMsgToBroadcast(MY_SERVICE_PAYLOAD, "Nothing");
                         }
                     }
        );

    }

    private void getLesson() {

        Retrofit retrofit = RetrofitClient.getClient();

        SoService service = retrofit.create(SoService.class);

        Call<Lesson[]> call = service.getLessons();

        call.enqueue(new Callback<Lesson[]>() {
                         @Override
                         public void onResponse(Call<Lesson[]> call, Response<Lesson[]> response) {

                             StringBuilder res = new StringBuilder();
                             if (response.body() != null) {

                                 updateLessonsToloacl(response.body());
                                 res.append("update records number: ").append(response.body().length);
                                 sendMsgToBroadcast(MY_SERVICE_PAYLOAD, res.toString() );

                             } else {
                                 sendMsgToBroadcast(MY_SERVICE_PAYLOAD, "nothing");

                             }


                         }

                         @Override
                         public void onFailure(Call<Lesson[]> call, Throwable t) {
                             sendMsgToBroadcast(MY_SERVICE_PAYLOAD, "nothing");

                         }
                     }
        );


    }


    /**
     * 通过读取文件，插入初始化数据
     * @throws IOException
     */
    private void updateLessonsToloacl(Lesson[] Lessons) {


        //通过事务，进行批量插入
        SQLiteDatabase database;
        DbOpenHelper helper = new DbOpenHelper(this.getApplicationContext());
        database = helper.getWritableDatabase();
        database.beginTransaction();

        database.execSQL("DROP TABLE IF EXISTS `conversations`;");
        database.execSQL("DROP TABLE IF EXISTS `sentences`;");
        database.execSQL(DbOpenHelper.TABLE_CONVERSATIONS_CREATE);
        database.execSQL(DbOpenHelper.TABLE_SENTENCES_CREATE);

        for (Lesson l : Lessons) {
            String sql = "INSERT INTO `conversations` VALUES (null,'"+ l.getCTitle()+"','"+""+
                    "','"+l.getCVoice()+"','"+l.getCCreated()+"',"+l.getCSort()+
                    ","+l.getCRank()+",'"+l.getCTags()+"'"+",'"+l.getCLessonId()+"'"
                    +");";

            List<Sentence> sentences = new ArrayList<Sentence>();
            sentences = l.getSentences();
            StringBuilder subSql = new StringBuilder("INSERT INTO `sentences` VALUES ");
            for (Sentence s : sentences) {
                subSql.append("(null,").append(s.getSequence()).append(",'").append(s.getEnSentence()).append("','").append(s.getCnSentence()).append("',").append(s.getStartTime()).append(",").append(s.getEndTime()).append(",").append(s.getDuration()).append(",").append(s.getLessonId()).append("),");
            }
            subSql = new StringBuilder(subSql.toString().substring(0, subSql.length() - 1));
            subSql.append(";");

            //TODO insert Sentences to database

            Log.d("Maxazure",sql+ "\n");
            Log.d("Maxazure",subSql.toString()+ "\n");
            try {
                database.execSQL(sql);
                database.execSQL(subSql.toString());
            } catch (SQLException e) {
                Log.d("Maxazure","error:  ---"+e.toString());
            }
        }

        database.setTransactionSuccessful();
        database.endTransaction();

        database.close();

    }



}
