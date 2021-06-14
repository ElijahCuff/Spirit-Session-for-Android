package wokeworld.ghost.communicator;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;

public class Detector extends Service implements SensorEventListener
{
	 
    private SensorManager mSensorManager;
    private Sensor mMagnetometer;
    private Vibrator vibrator;

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        double mt = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
		
        if (mt > 70) {
			//  Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			//  vibrator.vibrate(100);
		}
        Intent broadcastIntent = new Intent();
		//  broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.setAction("wokeworld.ghost.communicator.ACTION_Magnetometer");
        broadcastIntent.putExtra("microTesla", mt);
        sendBroadcast(broadcastIntent);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
         mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_FASTEST);
        return START_STICKY;
    }

	/* final static String MY_ACTION = "MY_ACTION";
	 public class MyThread extends Thread{

	 @Override
	 public void run() {
	 // TODO Auto-generated method stub
	 for(int i=0; i<10; i++){
	 try {
	 Thread.sleep(5000);
	 Intent intent = new Intent();
	 intent.setAction(MY_ACTION);
	 sendBroadcast(intent);
	 } catch (InterruptedException e) {
	 // TODO Auto-generated catch block
	 e.printStackTrace();
	 }
	 }
	 stopSelf();
	 }

	 } */

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        mSensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
