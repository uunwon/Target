package com.yunwoon.targetproject

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.yunwoon.targetproject.databinding.FragmentRankdialogBinding
import com.yunwoon.targetproject.databinding.ItemRankBinding
import com.yunwoon.targetproject.sqlite.DBHelper
import com.yunwoon.targetproject.sqlite.RankDB

data class RankItem(val rank: Int, val player_name: String, val player_score: Int)

class RankDialogFragment(private val playerNickName: String, private val playerScore: Int) : DialogFragment() {
    private var _binding: FragmentRankdialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper : DBHelper

    private val mDelayHandler: Handler by lazy { Handler() } // 종료할 때 딜레이 관련

    private lateinit var customAdapter: CustomAdapter
    private val rankItemArrayList = ArrayList<RankItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRankdialogBinding.inflate(inflater, container, false)
        val view = binding.root

        // layout background transperency
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 최초 실행시 데이터베이스 세팅
        dbHelper = DBHelper(context, "RankDB", null, 1)
        storeScore()

        // 랭크 리스트뷰 세팅
        setRankListView()

        // click game end button
        binding.rankImageView.setOnClickListener {
            Toast.makeText(context, "다음에 또 만나요 🖐", Toast.LENGTH_SHORT).show()
            mDelayHandler.postDelayed(::endGame, 1000L)
        }

        return view
    }

    private fun endGame() {
        dismiss()
        (context as MainActivity).finish()
    }

    // 여기까지 오면 이름이랑 점수 sqlite 에 저장
    private fun storeScore() {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(RankDB.RankEntry.COLUMN_PLAYER_NICKNAME, playerNickName)
            put(RankDB.RankEntry.COLUMN_PLAYER_SCORE, playerScore)
        }

        // 새 행을 삽입하고 새 행의 기본 키 값을 반환
        db?.insert(RankDB.RankEntry.TABLE_NAME, null, values)
    }

    // 리스트뷰 데이터 세팅
    private fun setRankListView() {
        // 랭크 sqlite 불러오기
        val db = dbHelper.readableDatabase
        var i = 1
        val cursor = db.rawQuery("select nickName, score from RankDB order by score desc", null)

        while(cursor.moveToNext()) {
            val nickName = cursor.getString(0)
            val score = cursor.getInt(1)

            rankItemArrayList.add(RankItem(i++, nickName, score))
        }

        customAdapter = CustomAdapter(rankItemArrayList, requireContext())
        binding.rankListView.adapter = customAdapter
    }

    // 랭크 리스트뷰 어댑터
    class CustomAdapter(private val rankItemArrayList: ArrayList<RankItem>, context: Context) : BaseAdapter() {

        private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        private lateinit var binding: ItemRankBinding

        override fun getCount(): Int = rankItemArrayList.size

        override fun getItem(p0: Int): Any = rankItemArrayList[p0]

        override fun getItemId(p0: Int): Long =p0.toLong()

        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            binding = ItemRankBinding.inflate(inflater, p2, false)

            binding.rankTextView.text = rankItemArrayList[p0].rank.toString()
            binding.nickNameTextView.text = rankItemArrayList[p0].player_name
            binding.scoreTextView.text = rankItemArrayList[p0].player_score.toString()

            return binding.root
        }
    }

}