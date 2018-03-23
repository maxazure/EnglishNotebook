package com.railscon.englishnotebook;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.railscon.englishnotebook.model.LineInfo;
import com.railscon.englishnotebook.model.LyricInfo;
import com.railscon.englishnotebook.provider.ConversationsProvider;
import com.railscon.englishnotebook.provider.SentencesProvider;
import com.railscon.englishnotebook.retrofit.RetrofitClient;
import com.railscon.englishnotebook.retrofit.model.Sentence;
import com.railscon.englishnotebook.utilities.DbOpenHelper;
import com.railscon.englishnotebook.utilities.DownloadHelper;
import com.railscon.englishnotebook.utilities.FileHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConversationActivity extends AppCompatActivity {

    private String noteFilter;

//    private ListView listView;

    private FileHelper fileHelper;


    private SeekBar seekBar;
    private Handler mHandler;
    private Runnable mRunnable;
    private LyricView mLyricView;

    private Boolean mIsPause = false;


    private String audioFileName = "";
    private ImageView playButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);


        mHandler = new Handler();


        mLyricView = (LyricView) findViewById(R.id.custom_lyric_view);
        LyricInfo lyricInfo = loadArticle();
        mLyricView.setLyricFile(lyricInfo, lyricInfo.songLines);


        //    listView = (ListView)findViewById(R.id.content);
//        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.content_list_item,android.R.id.text1,loadArticleBody()));

        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        // set audio-play-button status
        playButton = findViewById(R.id.music_play);
        playButton.setOnClickListener(mPlayClickListener);


        // file and music player
        fileHelper = new FileHelper(this);


        if (MainActivity.player != null && MainActivity.player.isPlaying()) {
            playButton.setImageResource(R.drawable.ic_pause_24dp);
            changePlaybuttonImgWhenCompletion();
            initializeSeekBar();

        } else {
            if (MainActivity.player == null)
                MainActivity.player = new MediaPlayer();

            playButton.setImageResource(R.drawable.ic_play_arrow_24dp);
        }

        // set LyricView to click available
        mLyricView.setOnPlayerClickListener(new LyricView.OnPlayerClickListener() {
            @Override
            public void onPlayerClicked(long progress, String content) {

                final long prog = progress;
                if(MainActivity.player==null || !MainActivity.player.isPlaying()){
                    FileHelper fileHelper = new FileHelper(getApplicationContext());
                    try {
                        MainActivity.player = new MediaPlayer();
                        MainActivity.player.setDataSource(getApplicationContext(), Uri.fromFile(fileHelper.getFile(audioFileName)));
                        MainActivity.player.prepareAsync();
                        MainActivity.player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mediaPlayer) {
                                mediaPlayer.start();
                                seekBar.setMax(mediaPlayer.getDuration());
                                initializeSeekBar();
                                MainActivity.player.seekTo((int) prog);
                            }
                        });
                        changePlaybuttonImgWhenCompletion();
                    } catch (IOException e) {
                        Log.d("Maxazure", e.toString());
                    }

                    Log.d("Maxazure","LyricView: "+Uri.fromFile(fileHelper.getFile(audioFileName)).toString());
                }else {
                    MainActivity.player.seekTo((int) progress);
                }

            }
        });


        ViewTreeObserver vto = mLyricView.getViewTreeObserver();

        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                mLyricView.moveToTop((int)(mLyricView.getHeight()*0.5f)-17);
                ViewTreeObserver obs = mLyricView.getViewTreeObserver();
                obs.removeOnGlobalLayoutListener(this);

            }
        });

    }

    protected void initializeSeekBar() {
        seekBar.setMax(MainActivity.player.getDuration());

        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (MainActivity.player != null) {
                    int mCurrentPosition = MainActivity.player.getCurrentPosition(); // In milliseconds
                    mLyricView.setCurrentTimeMillis(mCurrentPosition);
                    seekBar.setProgress(mCurrentPosition);

                }
                mHandler.postDelayed(mRunnable, 100);
            }
        };
        mHandler.postDelayed(mRunnable, 100);
    }

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && MainActivity.player != null && MainActivity.player.isPlaying()) {
                MainActivity.player.seekTo(progress);
                Log.d("Maxazure", "seekto:" + progress);
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    // play Audio of lesson
    private View.OnClickListener mPlayClickListener = new View.OnClickListener() {
        public void onClick(View v) {
          //  Log.d("Maxazure", "Error is here ........ is playing....");


            if (MainActivity.player != null && MainActivity.player.isPlaying()) {

                     pauseMusic(v);
                mIsPause= true;
                ((ImageView) v).setImageResource(R.drawable.ic_play_arrow_24dp);
            } else {

                if(mIsPause)
                    MainActivity.player.start();

                else {
                    MainActivity.player = new MediaPlayer();
                    playMusic(v);
                }
                mIsPause= false;
                ((ImageView) v).setImageResource(R.drawable.ic_pause_24dp);
            }

        }
    };

    private LyricInfo loadArticle() {
        //TODO 这里有错误 要改
        LyricInfo lyricInfo = new LyricInfo();
        List<LineInfo> songlines = new ArrayList<LineInfo>();

        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra(ConversationsProvider.CONTENT_ITEM_TYPE);
        noteFilter = "_id =" + uri.getLastPathSegment();

        Cursor cursor = getContentResolver().query(uri,
                DbOpenHelper.ALL_COLUMNS_IN_CONVERSATIONS, noteFilter, null, null);

        Uri uriSentence = Uri.parse(SentencesProvider.CONTENT_URI.toString() + "/1");
        Cursor cursor2 = getContentResolver().query(uriSentence,
                DbOpenHelper.ALL_COLUMNS_IN_SENTENCES, "lesson_id=" + uri.getLastPathSegment(), null, DbOpenHelper.S_SEQUENCE);

        Log.d("Maxazure", "uri:"+uri.toString());
        if (cursor != null) {
            cursor.moveToFirst();
            lyricInfo.songAlbum = "";
            lyricInfo.songArtist = "";
            lyricInfo.songTitle = cursor.getString(cursor.getColumnIndex(DbOpenHelper.C_TITLE));
            audioFileName = cursor.getString(cursor.getColumnIndex(DbOpenHelper.C_VOICE));
            setTitle(cursor.getString(cursor.getColumnIndex(DbOpenHelper.C_TITLE)));
            //  List <Sentence> sentences = cursor.getColumnIndex(DbOpenHelper.C_BODY);
        }

        try {
            while (cursor2.moveToNext()) {
              //  Log.d("Maxazure",cursor2.getString(cursor2.getColumnIndex(DbOpenHelper.S_EN_SENTENCE))+" | "+cursor2.getLong(cursor2.getColumnIndex(DbOpenHelper.S_START_TIME)));
                songlines.add(new LineInfo(cursor2.getLong(cursor2.getColumnIndex(DbOpenHelper.S_START_TIME)), cursor2.getString(cursor2.getColumnIndex(DbOpenHelper.S_EN_SENTENCE))));
            }
        } catch(NullPointerException ex){
            Log.d("Maxazure", ex.toString());
        } finally{
            cursor2.close();
        }

        lyricInfo.songLines = songlines;

        return lyricInfo;
    }

//    private List<String> loadArticleBody() {
//        Intent intent = getIntent();
//        Uri uri = intent.getParcelableExtra(ConversationsProvider.CONTENT_ITEM_TYPE);
//        noteFilter = DbOpenHelper.C_ID + "=" + uri.getLastPathSegment();
//
//        Cursor cursor = getContentResolver().query(uri,
//                DbOpenHelper.ALL_COLUMNS_IN_CONVERSATIONS, noteFilter, null, null);
//        String oldText;
//
//        if (cursor != null) {
//            cursor.moveToFirst();
//            oldText = cursor.getString(cursor.getColumnIndex(DbOpenHelper.C_BODY)).replace("\r","");
//            audioFileName = cursor.getString(cursor.getColumnIndex(DbOpenHelper.C_VOICE));
//            setTitle(cursor.getString(cursor.getColumnIndex(DbOpenHelper.C_TITLE)));
//        }else {
//            oldText = getString(R.string.nothing);
//        }
//
//        List tmplist = new ArrayList<>(Arrays.asList(oldText.split("\n")));
//            tmplist.removeAll(Arrays.asList("",null));
//        return tmplist;
//    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finishing();
                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishing();

    }

    private void finishing() {
        finish();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
        try {
            MainActivity.player.stop();
            MainActivity.player.release();
            MainActivity.player = null;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

    }

    public void playMusic(View view) {

        // if(!fileHelper.copyAssetToStorage(audioFileName)) return;


        // TODO: When the network doesn't works.
        File file = fileHelper.getFile(audioFileName);

        if (file.exists()) {
            playAudio(Uri.fromFile(file));
          //  Toast.makeText(this, "Playing the local file..." + file.getPath(), Toast.LENGTH_SHORT).show();
        } else {
            Uri audioUri = Uri.parse(RetrofitClient.BASE_URL + "lesson-mp3/" + audioFileName);
            // audioPath=RetrofitClient.BASE_URL+"lesson-mp3/" + audioFileName;

            DownloadHelper downloadHelper = new DownloadHelper(getApplicationContext());
            downloadHelper.downloadMP3(audioFileName);

            downloadHelper.setCallbackdown(new DownloadHelper.CallbackDown() {
                @Override
                public void onComplate(File mp3file) {

                    playAudio(Uri.fromFile(mp3file));
                }
            });


        }


    }

    private void playAudio(Uri audioName) {

        try {

                MainActivity.player = new MediaPlayer();
                MainActivity.player.setDataSource(getApplicationContext(), audioName);
                MainActivity.player.prepareAsync();
                MainActivity.player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                        mediaPlayer.setLooping(true);
                        seekBar.setMax(mediaPlayer.getDuration());
                        initializeSeekBar();

                    }
                });
                changePlaybuttonImgWhenCompletion();

        } catch (IOException e) {
            Log.d("Maxazure", e.toString());
        }
    }


    private void changePlaybuttonImgWhenCompletion() {
        MainActivity.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                ImageView playButton = findViewById(R.id.music_play);
                playButton.setImageResource(R.drawable.ic_play_arrow_24dp);
                MainActivity.player.release();
                MainActivity.player = null;
            }
        });
    }

    public void pauseMusic(View view) {
        if (MainActivity.player != null) {
            try {
                MainActivity.player.pause();
               // MainActivity.player.release();
               // MainActivity.player = null;
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }
}
