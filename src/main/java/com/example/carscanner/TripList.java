package com.example.carscanner;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class TripList extends AppCompatActivity {
    private static final String TAG = "ListDataActivity";

    DatabaseHelper mDatabaseHelper;

    private ListView mListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_list);
        mListView = (ListView) findViewById(R.id.listview);
        mDatabaseHelper = new DatabaseHelper(this);


       populateListView();
    }

    private void populateListView() {
        Log.d(TAG, "populateListView: Displaying data in the ListView.");

        //get the data and append to a list

        ListAdapter adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, mDatabaseHelper.getData());
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            String name = adapterView.getItemAtPosition(i).toString();
            Log.d(TAG, "onItemClick: You Clicked on " + name);
            Cursor data = mDatabaseHelper.getItemID(name); //get the id associated with that name
            int itemID = -1;
            while(data.moveToNext()){
                itemID = data.getInt(data.getColumnIndex("ID"));
            }
            if(itemID > -1){
                Log.d(TAG, "onItemClick: The ID is: " + itemID);
                Intent intent = new Intent(TripList.this, Trip.class);
                intent.putExtra("id",itemID);
                startActivity(intent);
            }
            else{

            }
        }


    });
    }
    }

