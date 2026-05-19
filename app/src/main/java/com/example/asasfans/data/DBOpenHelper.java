package com.example.asasfans.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**
 * @author LEN5010
 * @description 本地黑名单、屏蔽词和订阅 UP 数据库的建表与升级助手。
 */
public class DBOpenHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 3;

    public DBOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // blackBvid 是历史表结构，字段名继续保持原样以兼容旧数据。
        String sql="create table if not exists blackBvid(bvid TEXT primary key NOT NULL UNIQUE, PicUrl TEXT, Title TEXT, Duration integer, Author TEXT, ViewNum integer, LikeNum integer, Tname TEXT)";
//        String createAuthorTable="create table blackAuthor(name TEXT primary key NOT NULL UNIQUE)";
//        sqLiteDatabase.execSQL(createAuthorTable);
        sqLiteDatabase.execSQL(sql);
        //version2 建两个新表
        sqLiteDatabase.execSQL("create table if not exists blackMid(mid integer primary key NOT NULL UNIQUE)");
        sqLiteDatabase.execSQL("create table if not exists blackTag(tag Text primary key NOT NULL UNIQUE)");
        createVersion3Tables(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        if (i < 2){
            //建两个新表
            onCreate(sqLiteDatabase);
        }
        if (i < 3) {
            createVersion3Tables(sqLiteDatabase);
        }
    }

    private void createVersion3Tables(SQLiteDatabase sqLiteDatabase) {
        // v3 新增屏蔽词和订阅 UP；旧 blackTag/blackMid/blackBvid 表继续保留。
        sqLiteDatabase.execSQL("create table if not exists blackWord(word TEXT primary key NOT NULL UNIQUE)");
        sqLiteDatabase.execSQL("create table if not exists subscribedUp(mid integer primary key NOT NULL UNIQUE, name TEXT, face TEXT, note TEXT, updatedAt integer)");
    }
}
