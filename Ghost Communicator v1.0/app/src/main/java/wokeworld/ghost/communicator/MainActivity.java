package wokeworld.ghost.communicator;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;

public class MainActivity extends AppCompatActivity
{
    static MediaPlayer mediaPlayer;
    ToggleButton emf;
    ToggleButton evp;
    ToggleButton box;
    ToggleButton beep;
    ToggleButton phonetic;
  
		
    Button button2;
    TextView textView;
		TextView score;
    static Activity me;
    static Handler handler;
    int totalDur;
    ToneGenerator tg;
    MediaRecorder recorder;  
    File audiofile = null;  
    static final String TAG = "MediaRecording";  




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        textView = findViewById(R.id.textView);
				score = findViewById(R.id.emfTop);
        addListenerOnButton();
        me = this;
    }
		

		String fileLocation;
    private void startRecord() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);  
        File folder = new File(dir.getAbsolutePath()+"/Spirit Session/");
				if(!folder.exists()){folder.mkdirs();}
				try {  
            audiofile = File.createTempFile("Session_", ".mp3", folder);  
        }
        catch (IOException e) {  
            Log.e(TAG, "external storage access error");  
            return;  
        }  
				fileLocation = audiofile.getAbsolutePath();
        //Creating MediaRecorder and specifying audio source, output format, encoder & output format  
	  		recorder = new MediaRecorder();  
        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
				recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
				recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
				recorder.setAudioEncodingBitRate(32);
				recorder.setAudioSamplingRate(44100);
        recorder.setOutputFile(audiofile.getAbsolutePath());  
        try {
            recorder.prepare();
						recorder.start();
						Toast.makeText(me, "EVP Recording Started", Toast.LENGTH_LONG).show();
						
        }
        catch (IOException e) {}
        catch (IllegalStateException e) {}  

    }

		boolean audioRecording = false;

    public void stopRecording() {  

        //stopping recorder  
						recorder.pause();
						recorder.stop();  
						recorder.release(); 
						Toast.makeText(me, "Audio Saved\n" + fileLocation, Toast.LENGTH_LONG).show();
						audioRecording = false;
        //after stopping the recorder, create the sound file and add it to media library.  
        //addRecordingToMediaLibrary();  
    }

		@Override
		public void onActionModeFinished(ActionMode mode) {
				// TODO: Implement this method
				super.onActionModeFinished(mode);
		}

		@Override
		protected void onDestroy() {
				stopRecording();
				super.onDestroy();
		}  



    protected void addRecordingToMediaLibrary() {  
        //creating content values of size 4  
        ContentValues values = new ContentValues(4);  
        long current = System.currentTimeMillis();  
        values.put(MediaStore.Audio.Media.TITLE, "audio" + audiofile.getName());  
        values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));  
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg");  
        values.put(MediaStore.Audio.Media.DATA, audiofile.getAbsolutePath());  
        //creating content resolver and storing it in the external content uri  
        ContentResolver contentResolver = getContentResolver();  
        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;  
        Uri newUri = contentResolver.insert(base, values);  
        //sending broadcast message to scan the media file so that it can be available  
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri));  
        Toast.makeText(this, "Added File " + newUri, Toast.LENGTH_LONG).show();  
    }  


    double noise = 0;
    int noiseLoads = 0;
    boolean firstload = true;
		String percentRO;

    long startTime = 0;
    long nowTime = 0;
    long lastTime = 0;
		int nextPart;
		int next;

    public class ResponseReceiver extends BroadcastReceiver
    {
        //   public static final String ACTION_RESP = "com.mamlambo.intent.action.MESSAGE_PROCESSED";
        @Override
        public void onReceive(final Context context, Intent intent) {
            double amount = ProccessAmount(intent.getDoubleExtra("microTesla", 0));
            String amountStr = amount + "";
            String noiseStr = (noise + "");
            if (amountStr.contains(".")) {
                amountStr = amountStr.replace(".", "");
            }
            if (noiseStr.contains(".")) {
                noiseStr = noiseStr.replace(".", "");
            }
            int amountLng = Integer.parseInt(amountStr);
            amountStr = amountLng + "";


            if (firstload) {
                firstload = false;
                startTime = SystemClock.uptimeMillis();
            }
            else {
                nowTime = SystemClock.uptimeMillis();
            }

            int dur = 1000;

            if (amountLng > 20) {
                dur = 350;
            }
            if (amountLng > 60) {
                dur = 200;
            }
            if (amountLng > 75) {
                dur = 150;
            }
            if (amountLng > 110) {
                dur = 120;
            }
            if (amountLng > 150) {
                dur = 100;
            }

						final int resDur = dur;



            if (nowTime - lastTime > dur) {
                lastTime = SystemClock.uptimeMillis();
                if (dur != 1000) {
                    new Thread(new Runnable(){

                            @Override
                            public void run() {


																if (beepEnabled()) {
																		tg = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME / 10 * 9);
																		tg.startTone(ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE, 50);
																		SystemClock.sleep(50);
																		tg.stopTone();
																}

																
                            }
                        }).start();
                }

								updateText(score,amountStr);
                updateText(textView, "EMF uT×100: " + amountStr + "\nEnviro Strength : " + noiseStr.substring(0, 3) + ((boxEnabled() || phoneticEnabled() ) ? "\nAUDIO READ :\nDuration : ms " + nextPart + "\nLocation : % " + (percentRO ==null ? "0.0":percentRO) + "": ""));
								
            }
						else
						{
								Random r = new Random();
								boolean go = r.nextInt(100) >= 20;
								if (boxEnabled() && go & ! isPlay) {
										initWordsMediaPlayer();
										new Thread(new Runnable(){

														@Override
														public void run() {
																Random r = new Random();
																next = r.nextInt(mediaMax);
																nextPart = r.nextInt(300-10)+10;
																double percentRead = (double) next / mediaMax * 100;
																percentRO = percentRead + "";
																percentRO = ((percentRO.contains(".") && percentRO.length() >= 6) ? percentRO.substring(0, 5) : percentRO);
																playPart(next, nextPart);

														}
												}).start();

								}
							  go = r.nextInt(100) <= 50;
								if (phoneticEnabled() && go & ! isPlay) {
										initPhoneticPlayer();
										new Thread(new Runnable(){

														@Override
														public void run() {
																Random r = new Random();
																next = r.nextInt(mediaMax);
																nextPart = r.nextInt(200-10)+10;
																double percentRead = (double) next / mediaMax * 100;
																percentRO = percentRead + "";
																percentRO = ((percentRO.contains(".") && percentRO.length() >= 6) ? percentRO.substring(0, 5) : percentRO);
																playPart(next, nextPart);

														}
												}).start();

								}
								
						}
						

        }

    }




    public void updateText(final TextView text, final String msg) {
        me.runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    text.post(new Runnable(){

                            @Override
                            public void run() {
                                text.setText(msg);
                            }
                        });
                }
            });
    }

    public static int mediaPos;
    public static int mediaMax;

    private static void initWordsMediaPlayer() {
        handler = new Handler();
        mediaPlayer = new MediaPlayer();
        mediaPlayer = MediaPlayer.create(me, R.raw.words);
        mediaPos = mediaPlayer.getCurrentPosition();
        mediaMax = mediaPlayer.getDuration();
    }
		
		private static void initPhoneticPlayer() {
        handler = new Handler();
        mediaPlayer = new MediaPlayer();
        mediaPlayer = MediaPlayer.create(me, R.raw.phonetics);
        mediaPos = mediaPlayer.getCurrentPosition();
        mediaMax = mediaPlayer.getDuration();
    }
		
		
    static boolean isPlay = false;
    public static void playPart(final int millis, final int duration) {
        if (!isPlay) {
            isPlay = true;
            //mediaPlayer = MediaPlayer.create(me, R.raw.words);
            mediaPlayer.seekTo(millis);
            mediaPlayer.start();
            new Thread(new Runnable(){
                    @Override
                    public void run() {
                        SystemClock.sleep(duration-(duration/100*20));
                        mediaPlayer.pause();
                        isPlay = false;
                    }
                }).start();
        }
    }

    public double ProccessAmount(Double amount) {
        DecimalFormat dform = new DecimalFormat("#,###.##");

        while (noiseLoads <= 3) {
            if (noiseLoads == 0) {
                noise = amount;
            }
            if (amount < noise) {
                noise = amount;
            }
            noiseLoads ++;
        }
        amount -= (noise+0.2);
        if (amount > 0) {
            return Double.parseDouble(dform.format(amount));
        }
        else {
            return 0;
        }

    }

    public void addListenerOnButton() {

        emf = findViewById(R.id.emf);
        evp = findViewById(R.id.evp);
        box = findViewById(R.id.box);
        beep = findViewById(R.id.beep);
				phonetic = findViewById(R.id.phonetic);



        /* imageButton.setOnTouchListener(new View.OnTouchListener() {
         Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
         @Override
         public boolean onTouch(View v, MotionEvent event) {
         if(event.getAction() == MotionEvent.ACTION_DOWN){

         vibrator.vibrate(3000);
         return true;
         }
         else {
         vibrator.cancel();
         }
         return false;
         }
         });*/

        emf.setOnClickListener(new ToggleButton.OnClickListener() {
                @Override
                public void onClick(View p1) {
                    if (emfEnabled()) {
                        startService(new Intent(getBaseContext(), Detector.class));
												evp.setEnabled(true);
												beep.setEnabled(true);
												box.setEnabled(true);
												phonetic.setEnabled(true);
                    }
                    else {
                        stopService(new Intent(getBaseContext(), Detector.class));
											if(evpEnabled())
											{
												stopRecording();
												}
												evp.setChecked(false);
												evp.setEnabled(false);
												beep.setEnabled(false);
												box.setEnabled(false);
												phonetic.setEnabled(false);
												noiseLoads = 0;
                        noise = 0;
                    }
                }
            });

        evp.setOnClickListener(new ToggleButton.OnClickListener(){
                @Override
                public void onClick(View p1) {
                    if (evpEnabled()) {
												startRecord();
										}
										else {
												stopRecording();
										}
                }
						});
						
				phonetic.setOnClickListener(new ToggleButton.OnClickListener(){

								@Override
								public void onClick(View p1) {
										if(phoneticEnabled())
										{
												box.setChecked(false);
												box.setEnabled(false);
										}else
										{
												box.setEnabled(true);
										}
								}
						});

				
				box.setOnClickListener(new ToggleButton.OnClickListener(){

								@Override
								public void onClick(View p1) {
										if(boxEnabled())
										{
												phonetic.setChecked(false);
												phonetic.setEnabled(false);
										}else
										{
												phonetic.setEnabled(true);
										}
								}
						});

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

    }

    public boolean beepEnabled() {
				return beep.isChecked();
    }
    public boolean emfEnabled() {
        return emf.isChecked();
    }
    public boolean evpEnabled() {
        return evp.isChecked();
    }
    public boolean boxEnabled() {
        return box.isChecked();
    }

		public boolean phoneticEnabled() {
        return phonetic.isChecked();
    }
		
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    ResponseReceiver receiver;
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        receiver = new ResponseReceiver();
        // filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction("wokeworld.ghost.communicator.ACTION_Magnetometer");
        registerReceiver(receiver, filter);

    }
    @Override
    public void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }

}
