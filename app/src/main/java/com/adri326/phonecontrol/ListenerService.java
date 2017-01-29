package com.adri326.phonecontrol;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.adri326.phonecontrol.MainActivity.isChecked;
import static com.adri326.phonecontrol.MainActivity.url;
import static java.sql.DriverManager.println;

/**
 * Created by adrien on 28/01/17.
 */

public class ListenerService extends Service {

    String urlS = "http://adri326.890m.com/phoneControl/client.php";

    Context context;
    AudioManager audioManager;

    public void run() {
        Looper.prepare();
        Log.i("========TEST=========", "Started thread");
        int l = 0;
        while (true) {
            if (isChecked) try {
                mThread.sleep(1000);

                URL url = new URL(urlS);
                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setConnectTimeout(10000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setInstanceFollowRedirects(true);

                urlConnection.connect();

                int response = urlConnection.getResponseCode();
                Log.i("Response code", ""+response);
                InputStream inputStream = urlConnection.getInputStream();
                int len = urlConnection.getContentLength();
                Log.i("Response length", ""+len);
                if (response==200) {
                    char[] buffer = new char[len>0?len:100];
                    ((Reader) new InputStreamReader(inputStream, "UTF-8")).read(buffer);
                    String r = new String(buffer);
                    //Log.i("Response", r);
                    treatenData(r);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } else {
                try {
                    mThread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //Log.i("========TEST=========", url);

        }
    }
    Thread mThread;
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("========TEST=========", "Started service");
        context = getApplicationContext();
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        SharedPreferences preferences = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
        if (url == null) {
            //preferences.getString("URL", urlS);
        } else {
            /*SharedPreferences.Editor editor = preferences.edit();
            editor.putString("URL", url.getText().toString());
            editor.commit();*/
            urlS = url.getText().toString();
        }
        mThread = new Thread(new Runnable() {
            public void run() {
                ListenerService.this.run();
            }
        });
        mThread.start();
        return START_STICKY;
    }
    public IBinder onBind(Intent intent) {
        return null;
    }
    private String version = "-9999";
    private void treatenData(String s) {
        if (!s.equals("")) {
            String[] instructions = s.split("\n");
            try {
                boolean ended = false;
                for (int i = 0; i < instructions.length+1; i++) {
                    ended = ended || instructions[i].equals("end");
                }
                if (instructions.length>1&&ended&&!(version.equals(instructions[1]))) {
                    println(" === Started code execution === ");
                    for (int i = 0; i < instructions.length; i++) {
                        if (instructions[i].equals("play")) {
                            toggleNativePlayer(context, true);
                            Log.i("Command", i+": play");
                        }
                        if (instructions[i].equals("pause")) {
                            toggleNativePlayer(context, false);
                            Log.i("Command", i+": pause");
                        }
                        if (instructions[i].equals("volumeDown")) {
                            volumeDown();
                            Log.i("Command", i+": volumeDown");
                        }
                        if (instructions[i].equals("volumeUp")) {
                            volumeUp();
                            Log.i("Command", i+": volumeUp");
                        }
                        if (instructions[i].equals("nextTrack")) {
                            nativePlayerNext(context);
                            Log.i("Command", i+": nextTrack");
                        }
                        if (instructions[i].equals("previousTrack")) {
                            nativePlayerPrevious(context);
                            Log.i("Command", i+": previousTrack");
                        }
                    }
                    version = instructions[1];
                    Log.i("Command", " === Finished code execution === ");
                } else {
                    //println("Code not executed : "+(instructions.length>1)+", "+ended+", "+(!version.equals(instructions[1])));
                }
            } catch (Exception e) {
                //println(" === Error while executing program === ");
                //println(e.toString());
            }
        } else {
            //println(" === No data ===");
        }
    }


    void toggleNativePlayer(Context c) {
        Intent intent = new Intent("com.android.music.musicservicecommand");
        intent.putExtra("command", "togglepause");
        c.sendBroadcast(intent);
    }
    void toggleNativePlayer(Context c, boolean b) {
        Intent intent = new Intent("com.android.music.musicservicecommand");
        intent.putExtra("command", b?"play":"pause");
        c.sendBroadcast(intent);
    }
    void nativePlayerNext(Context c) {
        Intent intent = new Intent("com.android.music.musicservicecommand");
        intent.putExtra("command", "next");
        c.sendBroadcast(intent);
    }
    void nativePlayerPrevious(Context c) {
        Intent intent = new Intent("com.android.music.musicservicecommand");
        intent.putExtra("command", "previous");
        c.sendBroadcast(intent);
    }
    void volumeDown() {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }
    void volumeUp() {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }
}
