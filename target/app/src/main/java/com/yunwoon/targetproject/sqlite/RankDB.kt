package com.yunwoon.targetproject.sqlite

import android.provider.BaseColumns

object RankDB {
    object RankEntry : BaseColumns {
        const val TABLE_NAME = "RankDB"
        const val COLUMN_PLAYER_NICKNAME = "nickName"
        const val COLUMN_PLAYER_SCORE = "score"
    }
}