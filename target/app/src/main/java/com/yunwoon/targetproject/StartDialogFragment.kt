package com.yunwoon.targetproject

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.yunwoon.targetproject.databinding.FragmentStartdialogBinding
import com.yunwoon.targetproject.sqlite.DBHelper

class StartDialogFragment : DialogFragment() {
    private var _binding: FragmentStartdialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper : DBHelper

    private val mDelayHandler: Handler by lazy { Handler() } // 과녁 이미지 딜레이 관련
    private var playerNickName = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStartdialogBinding.inflate(inflater, container, false)
        val view = binding.root

        // layout background transperency
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // click game start button
        binding.startImageView.setOnClickListener {
            playerNickName = binding.nickNameEditText.text.toString()

            if(playerNickName.isNotEmpty()) {
                if(checkSameNickName())
                    Toast.makeText(context, "중복된 닉네임입니다.", Toast.LENGTH_SHORT).show()
                else
                    mDelayHandler.postDelayed(::startGame, 1000L)
            }
            else
                Toast.makeText(context, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun startGame() {
        dismiss()
        (context as MainActivity).setGameView(playerNickName)
    }

    private fun checkSameNickName(): Boolean {
        dbHelper = DBHelper(context, "RankDB", null, 1)
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("select nickName from RankDB where nickName = '$playerNickName'", null)

        if(cursor.count > 0)
            return true

        return false
    }
}