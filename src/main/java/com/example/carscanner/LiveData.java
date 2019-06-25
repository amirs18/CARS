package com.example.carscanner;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.control.DistanceSinceCCCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.RuntimeCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;

public class LiveData extends AppCompatActivity implements View.OnClickListener, LocationListener {

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        break;
                }
            }
        }
    };
    private boolean n = true;
    private CircularProgressIndicator temp;
    private CircularProgressIndicator RPM;
    private CircularProgressIndicator circularProgressIndicator1;
    private CircularProgressIndicator runtime;
    private Handler handler1;
    private Button button;
    private Button send;
    private boolean x = true;
    private Bundle bundle1;
    DatabaseHelper dbh = new DatabaseHelper(this);
    double lon ;
    double lan  ;
    LocationManager locationManager;
    int startkm;
    int max_speed ;

    public LiveData() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_data);
        final Time time1 = new Time();
        time1.setToNow();
        final ArrayList devices = new ArrayList();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        ArrayList deviceStrs = new ArrayList();
        button = findViewById(R.id.bt1);
        RPM = findViewById(R.id.RPM);
        circularProgressIndicator1 = findViewById(R.id.speed);
        temp = findViewById(R.id.temp);
        runtime=findViewById(R.id.runtime);
        button.setOnClickListener(this);
        CheckPermission();


        registerReceiver(mReceiver, filter);
        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // set up list selection handlers
            for (BluetoothDevice device : pairedDevices) {
                deviceStrs.add(device.getName() + "\n" + device.getAddress());
                devices.add(device.getAddress());
            }

            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.select_dialog_singlechoice,
                    deviceStrs.toArray(new String[deviceStrs.size()]));

            alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    String deviceAddress = (String) devices.get(position);
                    // TODO save deviceAddress

                    BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);
                    livedata livedata = new livedata(handler1, device);
                    livedata.start();
                }
            });

            alertDialog.setTitle("Choose Bluetooth device");
            alertDialog.show();
            handler1 = new Handler(new Handler.Callback() {
                @SuppressLint("MissingPermission")
                @Override
                public boolean handleMessage(Message msg) {
                    temp.setProgress(msg.getData().getFloat("float temp"), 130);
                    RPM.setProgress(msg.getData().getInt("int rpm"), 10000);
                    runtime.setProgress(Double.parseDouble(msg.getData().getString("string runc")),9999999999.999999);
                    circularProgressIndicator1.setProgress(msg.getData().getInt("int speed"), 200);
                    max_speed = Math.max(max_speed, msg.getData().getInt("int speed"));//TODO insert into database


                    if (x&&msg.getData().getInt("int rpm")>0)  {
                        startkm=msg.getData().getInt("int dis");
                        x=false;

                    }
                    if (n&&msg.getData().getFloat("float temp")>100)  {
                        addNotification();
                        startService(new Intent(LiveData.this,Myservice.class));
                        n=false;

                    }
                    bundle1 = msg.getData();
                    bundle1.putString("string dis",msg.getData().getInt("int dis")-startkm+"km");
                    bundle1.putInt("max speed",max_speed);
                    bundle1.putString("start_time", time1.monthDay + "." + (time1.month +1)+ "." + time1.year + " " + time1.hour + ":" + time1.minute);
                    bundle1.putDouble("double long",lon);
                    bundle1.putDouble("double lan",lan);

                    return true;
                }
            });
        }
    }

    private void addNotification() {
        // Builds your notification
        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "chanel")
                .setSmallIcon(R.drawable.example_picture)
                .setContentTitle("CarScanner")
                .setContentText("your car is overheating");

        // Creates the intent needed to show the notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(101, builder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "overheating";
            String description = "";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("chanel", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onClick(View v) {

        if (v == button) {
            final Time time1 = new Time();
            time1.setToNow();

//           bundle1.putString("end_time",time1.monthDay+"."+time1.month+"."+time1.year+" "+time1.hour+":"+time1.minute);
            if (bundle1 != null)
                if (!dbh.AddData(bundle1)) {
                    Log.d("fff", "faild to up");
                }


            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }

    }

    class livedata extends Thread {
        BluetoothSocket socket = null;
        Handler handler;
        BluetoothDevice device;

        public livedata(Handler handler1, BluetoothDevice device) {
            this.handler = handler1;
            this.device = device;
        }


        @Override
        public void run() {
            super.run();
            String tmp;

            try {

                socket = connect(device);
                try {
                    new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                    new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                    new TimeoutCommand(125).run(socket.getInputStream(), socket.getOutputStream());
                    new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());

                } catch (Exception e) {
                    Log.d("myee", e + "");
                    // handle errors
                }
            } catch (IOException e) {
                Log.d("myee", e.getMessage());
            }


            while (true) {
                EngineCoolantTemperatureCommand r = new EngineCoolantTemperatureCommand();
                RPMCommand rpm = new RPMCommand();
                SpeedCommand speedCommand = new SpeedCommand();
                DistanceSinceCCCommand distanceSinceCCCommand = new DistanceSinceCCCommand();
//                ConsumptionRateCommand consumptionRateCommand = new ConsumptionRateCommand();
                RuntimeCommand runtimeCommand = new RuntimeCommand();


                try {
                    speedCommand.run(socket.getInputStream(), socket.getOutputStream());
                    r.run(socket.getInputStream(), socket.getOutputStream());
                    rpm.run(socket.getInputStream(), socket.getOutputStream());
                   distanceSinceCCCommand.run(socket.getInputStream(), socket.getOutputStream());
//                   consumptionRateCommand.run(socket.getInputStream(), socket.getOutputStream());
                   runtimeCommand.run(socket.getInputStream(), socket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putFloat("float temp", r.getTemperature());
                bundle.putString("string temp", r.getFormattedResult());
                bundle.putInt("int rpm", rpm.getRPM());
                bundle.putInt("int speed", speedCommand.getMetricSpeed());
              bundle.putInt("int dis", distanceSinceCCCommand.getKm());
               bundle.putString("string runc", runtimeCommand.getCalculatedResult());

                bundle.putString("string enginerun", runtimeCommand.getFormattedResult());
                message.setData(bundle);
                handler.sendMessage(message);
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static BluetoothSocket connect(BluetoothDevice dev) throws IOException {
        int i = 0;
        BluetoothSocket sock = null;
        BluetoothSocket sockFallback;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.cancelDiscovery();

        Log.d("myee", "Starting Bluetooth connection..");

        try {
            sock = dev.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            sock.connect();
        } catch (Exception e1) {
            Log.e("myee", "There was an error while establishing Bluetooth connection. Falling back..", e1);
            if (sock != null) {
                Class<?> clazz = sock.getRemoteDevice().getClass();
                Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
                try {
                    Log.e("", "trying fallback...");

                    sock = (BluetoothSocket) dev.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(dev, 1);
                    sock.connect();

                    Log.e("myee", "Connected");
                } catch (Exception e2) {
                    Log.e("myee", "Couldn't establish Bluetooth connection!");
                }
            }
        }


        return sock;
    }
    public void onResume() {
        super.onResume();
        getLocation();
    }
    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }
    public void getLocation() {
        try {
            locationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    5000, 5, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    public void CheckPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new
                    String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Please Enable GPS and Internet",
        Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onLocationChanged(Location location) {
// Getting reference to TextView tv_longitude

        lon = location.getLongitude();
        lan =location.getLatitude();
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }
    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider!" + provider,
                Toast.LENGTH_SHORT).show();

    }

}
