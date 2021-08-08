package com.yunwoon.targetproject

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.yunwoon.targetproject.databinding.FragmentResetdialogBinding

class ResetDialogFragment(private val playerScore: Int) : DialogFragment() {
    private var _binding: FragmentResetdialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResetdialogBinding.inflate(inflater, container, false)
        val view = binding.root

        // layout background transperency
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding.playerScoreNumberTextView.text = playerScore.toString()

        // click game reset button
        binding.resetImageView.setOnClickListener {
            dismiss()
            (context as MainActivity).setReGameView()
        }

        return view
    }
}