package com.example.androidfinalexam2

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory

import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.Nullable


class DBOpenHelper(
    @Nullable context: Context?,
    @Nullable name: String?,
    @Nullable factory: CursorFactory?,
    version: Int
) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        //创建数据库sql语句并执行
        val sql =
            "create table user(id integer primary key autoincrement,username varchar(20),password varchar(20),age integer)"
        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
}
