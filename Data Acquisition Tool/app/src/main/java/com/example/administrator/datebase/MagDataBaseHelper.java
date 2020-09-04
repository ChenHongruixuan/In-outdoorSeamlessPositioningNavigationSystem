package com.example.administrator.datebase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;


public class MagDataBaseHelper extends SQLiteOpenHelper {
    private static final String CREATE_MAG = "CREATE TABLE IF NOT EXISTS magneticfield " +
            "( " + " id integer PRIMARY KEY autoincrement, " + " equipmentID integer, " +
            " X real, " + " Y real, " + " L real, " + " T real);";

    private Context mContext;

    public MagDataBaseHelper(Context context, String name,
                             SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_MAG);
        Toast.makeText(mContext,"磁场数据表创建成功",Toast.LENGTH_SHORT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
