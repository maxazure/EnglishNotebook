package com.railscon.englishnotebook;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.railscon.englishnotebook.provider.ConversationsProvider;
import com.railscon.englishnotebook.services.WorkerService;
import com.railscon.englishnotebook.utilities.DbOpenHelper;
import com.railscon.englishnotebook.utilities.NetworkHelper;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final int CONVERSATION_REQUEST_CODE = 1001;
    private CursorAdapter cursorAdapter;
    private ListView list;

    public static MediaPlayer player;


  //  List<DataItem> mItemList;





    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String version = intent.getStringExtra(WorkerService.MY_SERVICE_PAYLOAD);

            getLoaderManager().restartLoader(0,null,MainActivity.this);

            Toast.makeText(MainActivity.this, version, Toast.LENGTH_SHORT).show();

//            mItemList = Arrays.asList(dataItems);
//            displayData();
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MainActivity.player = new MediaPlayer();



 //      getContentResolver().delete(ConversationsProvider.CONTENT_URI,null,null);
//        try {
//            initInsert();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // insertConversation("new title", "new body");

        bindListVewToDb();


        boolean networkOk = NetworkHelper.hasNetworkAccess(this);
        if (networkOk) {
           /* Intent intent = new Intent(this, WorkerService.class);
            startService(intent);*/
            Toast.makeText(this, "Network available", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Network not available", Toast.LENGTH_SHORT).show();
        }



        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(mBroadcastReceiver,
                        new IntentFilter(WorkerService.MY_SERVICE_MESSAGE));


        pullDataFromInternet();


        getLoaderManager().initLoader(0,null,this);
    }



    private void bindListVewToDb() {
        String[] from = {DbOpenHelper.C_TITLE};
        int[] to = {R.id.tvNote};
        cursorAdapter = new SimpleCursorAdapter(this,
                R.layout.conversation_list_item, null,from, to, 0);

        list = findViewById(android.R.id.list);
        list.setAdapter(cursorAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ConversationActivity.class);
                Uri uri = Uri.parse(ConversationsProvider.CONTENT_URI + "/" + id);
                Log.d("Maxazure","ID:->"+id);
                intent.putExtra(ConversationsProvider.CONTENT_ITEM_TYPE, uri);
                startActivityForResult(intent, CONVERSATION_REQUEST_CODE);
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }
        });
    }

    private void insertConversation(String title, String body) {
        ContentValues values = new ContentValues();
        values.put(DbOpenHelper.C_TITLE, title);
        values.put(DbOpenHelper.C_BODY, body);
        Uri conversationUri = getContentResolver().insert(ConversationsProvider.CONTENT_URI, values);

    }


    private void pullDataFromInternet() {
        Intent intent = new Intent(this, WorkerService.class);
        startService(intent);
    }

//    private void displayData() {
//        if (mItemList != null) {
//            Toast.makeText(MainActivity.this,
//                    "====" + mItemList.toString() + "======",
//                    Toast.LENGTH_SHORT).show();
//        }
//    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, ConversationsProvider.CONTENT_URI,
                null, null, null, DbOpenHelper.C_LESSON_ID);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        cursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);

    }

    public void about(MenuItem item) {
        Intent intent = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    /**
     * 通过读取文件，插入初始化数据
     * @throws IOException
     */
//    private void fInsert() throws IOException {
//        InputStreamReader reader = new InputStreamReader(this.getApplicationContext().getResources().openRawResource(R.raw.conversations));
//        BufferedReader br = new BufferedReader(reader);
//        String s1 = "";
//
//        //通过事务，进行批量插入
//        SQLiteDatabase database;
//        DbOpenHelper helper = new DbOpenHelper(this.getApplicationContext());
//        database = helper.getWritableDatabase();
//        database.beginTransaction();
//
//        while ((s1 = br.readLine()) != null) {
//            database.execSQL(s1);
//        }
//
//        database.setTransactionSuccessful();
//        database.endTransaction();
//
//        br.close();
//        reader.close();
//        database.close();
//
//    }


}
