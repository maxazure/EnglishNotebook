package com.railscon.englishnotebook.utilities;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.railscon.englishnotebook.retrofit.RetrofitClient;
import com.railscon.englishnotebook.retrofit.services.DownService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadHelper {

    private File storageDirectory;
    private Context context;
    private static final String TAG = "MaxDownload";

    CallbackDown cdown;
    public interface CallbackDown {
         void onComplate(File file);
    }
    public void setCallbackdown(CallbackDown callbackDown){
        cdown = callbackDown;
    }


    public DownloadHelper(Context context) {
        this.context = context;
        storageDirectory = context.getFilesDir();
    }


    public Boolean downloadMP3(final String filename){

        DownService downloadService = RetrofitClient.getClient().create(DownService.class);

        Log.d(TAG,"lesson-mp3/"+filename);
        Call<ResponseBody> call = downloadService.downloadFileWithDynamicUrlSync("lesson-mp3/"+filename);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                boolean writtenToDisk = false;
                if (response.body() != null) {
                    Log.d(TAG,response.body().contentType().toString());
                    writtenToDisk = writeResponseBodyToDisk(response.body(), filename);
                }


                cdown.onComplate(new File(storageDirectory ,filename));

                Log.d(TAG, "file download was a success? " + writtenToDisk);

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "error");
            }
        });
        return true;
    }

    private boolean writeResponseBodyToDisk(ResponseBody body, String filenamestr) {
        try {
            // todo change the file location/name according to your needs
            File file = new File(storageDirectory ,filenamestr);
            isFolderExists(file);

            Log.d(TAG,file.getAbsolutePath());
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(file);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d("Maxazure", "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isFolderExists(File file) {
        if (!file.exists()) {
            if(!file.isDirectory()){
                file = new File(file.getParent());
            }

            if (file.mkdirs()) {
                return true;
            } else {
                return false;

            }
        }
        return true;

    }



}
