package com.wm.remusic.downmusic;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.wm.remusic.json.MusicDetailNet;
import com.wm.remusic.net.BMA;
import com.wm.remusic.net.HttpUtil;
import com.wm.remusic.uitl.PreferencesUtility;

import java.io.File;

/**
 * Created by wm on 2016/5/30.
 */
public class Down {
    private static Gson gson;

    public static void downMusic(final Context context ,final String id,final String name){

        new AsyncTask<String, String, MusicDetailNet>() {
            @Override
            protected MusicDetailNet doInBackground(final String... name) {
                gson = new Gson();
                JsonArray jsonArray  =  HttpUtil.getResposeJsonObject(BMA.Song.songInfo(id).trim()).get("songurl")
                        .getAsJsonObject().get("url").getAsJsonArray();
                int len = jsonArray.size();

                int downloadBit = PreferencesUtility.getInstance(context).getDownMusicBit();
                MusicDetailNet musicDetailNet;
                for(int i = len-1; i>-1;i--){
                    int bit = Integer.parseInt(jsonArray.get(i).getAsJsonObject().get("file_bitrate").toString());
                    if(bit == downloadBit){
                        musicDetailNet = gson.fromJson(jsonArray.get(i), MusicDetailNet.class);
                        return musicDetailNet;
                    }else if(bit < downloadBit && bit >= 64) {
                        musicDetailNet = gson.fromJson(jsonArray.get(i), MusicDetailNet.class);
                        return musicDetailNet;
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(MusicDetailNet musicDetailNet) {

                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    File file = new File("/storage/emulated/0/remusic/");
                    if(!file.exists()){
                        file.mkdir();
                    }
                    DownloadTask task = new DownloadTask.Builder(context, musicDetailNet.getShow_link())
                            .setFileName(name)
                            .setSaveDirPath("/storage/emulated/0/remusic/").build();
                    DownloadManager.getInstance(context).addDownloadTask(task);

                } else {
                    return;
                }
            }
        }.execute();
    }
}
