package com.example.carscanner;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String Database_name = "database.db";
    public static final String Table_name = "tb";
    public static final String Column_id = "ID";
    public static final String Column_Location_latitude = "LOCLAN";
    public static final String Column_Location_longitude = "LOCLON";
    public static final String Column_Time_start = "STARTTIME";
    public static final String Column_Fuel = "FUEL";
    public static final String Column_Engine_runtime = "ENGRUNTIME";
    public static final String Column_Distance="DISTANCE";
    public static final String Column_Max_Speed="SPEED";

    private static final String Create_table = "CREATE TABLE tb (ID INTEGER PRIMARY KEY AUTOINCREMENT,LOCLAN DOUBLE,LOCLON DOUBLE,ENGRUNTIME VARCHAR," +
            "FUEL VARCHAR,STARTTIME VARCHAR,DISTANCE VARCHAR,SPEED VARCHAR)";

    SQLiteDatabase db;

    public DatabaseHelper(Context context) {
        super(context, Database_name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Create_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Table_name);
        onCreate(db);
    }

    public void open() {
        db = this.getWritableDatabase();
        Log.d("11", "db open");
    }

    public void close() {
        db.close();
    }

    public boolean AddData(Bundle bundle) {

        db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(Column_Fuel, bundle.getString("string fuelcon"));
        contentValues.put(Column_Time_start, bundle.getString("start_time"));
        contentValues.put(Column_Engine_runtime,bundle.getString("string enginerun"));
        contentValues.put(Column_Distance,bundle.getString("string dis"));
        contentValues.put(Column_Location_latitude,bundle.getDouble("double lan") );
        contentValues.put(Column_Location_longitude,bundle.getDouble("double long"));
        contentValues.put(Column_Max_Speed,String.valueOf(bundle.getInt("max speed")));
        long result = db.insert(Table_name, null, contentValues);
        if (result == -1) {
            return false;
        }


        return true;
    }

    public ArrayList<String> getData() {
        String[] all = {Column_id, Column_Fuel, Column_Time_start, Column_Location_longitude, Column_Location_latitude, Column_Engine_runtime};
        ArrayList<String> arrayList = new ArrayList<String>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + Table_name, null);
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(this.Column_id));
            String time = cursor.getString(cursor.getColumnIndex(this.Column_Time_start));
            arrayList.add(time);
        }
        return arrayList;

    }

    public Cursor gettable(int name) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT  *  FROM " + Table_name +
                " WHERE " + Column_id + " = '" + name + "'";
        Cursor data = db.rawQuery(query, null);
        return data;

    }

    public Cursor getItemID(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + Column_id + " FROM " + Table_name +
                " WHERE " + Column_Time_start + " = '" + name + "'";
        Cursor data = db.rawQuery(query, null);
        return data;
    }





    }

