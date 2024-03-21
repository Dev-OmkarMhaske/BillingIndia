package com.tsysinfo.billing;

import android.database.Cursor;
 
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;

import java.util.ArrayList;

public class TownActivity extends AppCompatActivity {
    private ListView list;
    TownRowAdapter townRowAdapter;
    ArrayList<Town> town;
    DataBaseAdapter  dba;
    Models  models;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_town);

        town= new ArrayList<>();
        dba= new DataBaseAdapter(this);
        models= new Models();
        list = (ListView) findViewById(R.id.list);

        dba.open();
        String sql="select * from town";

        Cursor cursor=DataBaseAdapter.ourDatabase.rawQuery(sql,null);
        if (cursor.getCount()>0)
        {
            while (cursor.moveToNext())
                {
                    town.add(new Town(cursor.getString(cursor.getColumnIndex("id")),cursor.getString(cursor.getColumnIndex("townname"))));

                }

                townRowAdapter= new TownRowAdapter(TownActivity.this,town);
                list.setAdapter(townRowAdapter);

        }




        dba.close();



    }
}
