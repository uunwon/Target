package com.yunwoon.targetproject.sqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class DBHelper(
    context: Context?,
    name: String?, // 데이터 베이스 이름
    factory: SQLiteDatabase.CursorFactory? = null, // null 을 기본 값으로 함
    version: Int // 데이터 베이스 버전
) : SQLiteOpenHelper(context, name, factory, version){

    override fun onCreate(db: SQLiteDatabase) {
        var sql : String = "CREATE TABLE if not exists ${RankDB.RankEntry.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                "${RankDB.RankEntry.COLUMN_PLAYER_NICKNAME} TEXT UNIQUE," +
                "${RankDB.RankEntry.COLUMN_PLAYER_SCORE} INT);"

        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val sql : String = "DROP TABLE if exists ${RankDB.RankEntry.TABLE_NAME}"

        db.execSQL(sql)
        onCreate(db)
    }

}