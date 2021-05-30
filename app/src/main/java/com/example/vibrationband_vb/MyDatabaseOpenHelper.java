package com.example.vibrationband_vb;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MyDatabaseOpenHelper extends SQLiteOpenHelper {

    public static final String tableName = "container";

    public MyDatabaseOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    // 디비 생성(테이블 생성)
    public void createTable(SQLiteDatabase db){
        String sql = "CREATE TABLE " + tableName + "(name text)";
        try{
            db.execSQL(sql);
        } catch(SQLException e){

        }
    }


    // 디비 삽입
    public void insertName(SQLiteDatabase db, String name){
        db.beginTransaction();
        try{
            String sql = "INSERT INTO " + tableName + "(name)" + "values('" + name + "')";
            db.execSQL(sql);
            db.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            db.endTransaction();
        }
    }

}
