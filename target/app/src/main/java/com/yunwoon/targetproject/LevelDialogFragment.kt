package com.yunwoon.targetproject

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.yunwoon.targetproject.databinding.FragmentLeveldialogBinding

class LevelDialogFragment(private val playerScore: Int) : DialogFragment() {
    private var _binding: FragmentLeveldialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLeveldialogBinding.inflate(inflater, container, false)
        val view = binding.root

        // layout background transperency
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding.playerScoreNumberTextView.text = playerScore.toString()

        // click game stop button
        binding.nextLevelImageView.setOnClickListener {
            dismiss()
            (context as MainActivity).setNextGameView()
        }

        return view
    }
}