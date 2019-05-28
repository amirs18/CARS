package com.example.carscanner;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Trip extends AppCompatActivity implements View.OnClickListener {
    TextView time;
    TextView dis;
    TextView speed;
    TextView fuel;
    private String selectedName;
    private int selectedID;
    Button button;
    Double lon;
    Double lan;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        button=findViewById(R.id.button);
        time=findViewById(R.id.runtime);
        dis=findViewById(R.id.dis);
        speed=findViewById(R.id.max_speed);
//        fuel =findViewById(R.id.fuel);
        button.setOnClickListener(this);
        Intent intent = getIntent();
        DatabaseHelper databaseHelper=new DatabaseHelper(this);
        selectedID = intent.getIntExtra("id",-1);
        Cursor cursor=databaseHelper.gettable(selectedID);
        if (cursor!=null)
            xx(cursor);



    }
    private void xx(Cursor cursor){
        while (cursor.moveToNext()) {
            String dis=cursor.getString(cursor.getColumnIndex(DatabaseHelper.Column_Distance));
            String time = cursor.getString(cursor.getColumnIndex(DatabaseHelper.Column_Engine_runtime));
//            String speed =cursor.getString();
            String speed=cursor.getString(cursor.getColumnIndex(DatabaseHelper.Column_Max_Speed));
            lan=cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Column_Location_latitude));
            lon=cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Column_Location_longitude));
            this.time.setText(time);
            this.dis.setText(dis);
            this.speed.setText(speed);

        }

    }

    @Override
    public void onClick(View v) {
        String uri = "http://maps.google.com/maps?daddr=" + lan + "," + lon + " (" + "parking" + ")";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
//        Uri gmmIntentUri = Uri.parse("geo:"+lan+","+lon);
//        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//        mapIntent.setPackage("com.google.android.apps.maps");
//        if (mapIntent.resolveActivity(getPackageManager()) != null) {
//            startActivity(mapIntent);
//        }

    }
}
