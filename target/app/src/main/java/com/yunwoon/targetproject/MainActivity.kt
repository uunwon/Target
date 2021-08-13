package com.yunwoon.targetproject

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.yunwoon.targetproject.databinding.ActivityMainBinding
import com.yunwoon.targetproject.sqlite.DBHelper
import java.util.*
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var handler: Handler = Handler()
    private var runnableTargetImageView: Runnable = Runnable {  }
    private var runnableArrowImageView: Runnable = Runnable {  }
    private val mDelayHandler: Handler by lazy { Handler() } // 과녁 이미지 딜레이 관련

    private var time = 0
    private var timerTask : Timer? = null
    private var imageStatus: Boolean = true
    private var shootStatus: Boolean = false

    private var lifeLength = 0
    private var playerNickName = ""
    private var playerScore = 0
    private var thisPlayerScore = 0 // 현재 stage 스코어
    private var stage = 1

    private var targetPositionY = 0f // 과녁 좌표
    private var arrowPositionY = 0f // 화살 좌표

    private val backgroundImageArray: Array<Int> = arrayOf(R.drawable.b_1,
                R.drawable.b_2, R.drawable.b_3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val window = window
        window.setFlags( // full screen setting
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        setStartDialog()
        
        binding.playerImageView.setOnTouchListener { _: View, event:MotionEvent ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> { // 터치했을 때
                    if(!shootStatus) // 화살 날라가고 있을 때는 안움직여
                        Glide.with(this).load(R.raw.cat_player_2).override(250,250).into(binding.playerImageView)
                }
                MotionEvent.ACTION_UP -> { // 누르고 있다가 땠을 때
                    Glide.with(this).load(R.raw.cat_player_01).override(250,250).into(binding.playerImageView)

                    if(!shootStatus) // 화살 안날라가고 있을 때만 날려
                        shoot()
                }
            }
            true
        }
    }

    // 상태바 높이 구하기
    private fun statusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId)
        else 0
    }

    // 하단바 높이 구하기
    private fun navigationBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId)
        else 0
    }

    // 시작 다이얼로그 버튼 불러오기
    private fun setStartDialog() {
        val dialog = StartDialogFragment()
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "DialogStartFragment")
    }

    // 시간 끝나거나 활 다 쏘면 다이얼로그 세팅
    private fun setDialog() {
        if(thisPlayerScore > 1 && stage <= 2)
            mDelayHandler.post(::setNextLevelDialog)
        else if(thisPlayerScore > 1 && stage > 2)
            mDelayHandler.post(::setRankDialog)
        else
            mDelayHandler.post(::setResetDialog)
    }

    // 다 멈추고, 리셋 다이얼로그 버튼 불러오기
    private fun setResetDialog() {
        stopTimer() // 타이머 멈추고
        mDelayHandler.removeCallbacksAndMessages(null) // 과녁 이미지 동작 멈춤
        val score = playerScore

        val dialog = ResetDialogFragment(score)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "DialogResetFragment")
    }

    // 다음 stage 로 넘어가는 버튼 불러오기
    private fun setNextLevelDialog() {
        stopTimer() // 타이머 멈추고
        mDelayHandler.removeCallbacksAndMessages(null) // 과녁 이미지 동작 멈춤
        val score = playerScore

        val dialog = LevelDialogFragment(score)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "DialogLevelFragment")
    }

    // three stage 까지 완 -> Rank 판 보여주기
    private fun setRankDialog() {
        stopTimer() // 타이머 멈추고
        mDelayHandler.removeCallbacksAndMessages(null) // 과녁 이미지 동작 멈춤
        val score = playerScore

        val dialog = RankDialogFragment(playerNickName, score)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "DialogRankFragment")
    }

    // 게임 뷰 세팅하기
    fun setGameView(playerNickName: String) {
        binding.mainConstraintLayout.setPadding(0, statusBarHeight(this), navigationBarHeight(this), 0)
        Glide.with(this).load(R.raw.cat_player_01).override(250,250).into(binding.playerImageView)

        binding.stageTextView.visibility = View.VISIBLE
        binding.stageNumberTextView.visibility = View.VISIBLE
        binding.playerLifeTextView.visibility = View.VISIBLE
        binding.timeLifeMinuteTextView.visibility = View.VISIBLE
        binding.timeLifeSecondTextView.visibility = View.VISIBLE
        binding.playerScoreTextView.visibility = View.VISIBLE

        binding.playerScoreNumberTextView.text = playerScore.toString()
        binding.targetImageView.setImageResource(R.drawable.ic_target)

        binding.playerLifeTextView.text = "➹➹➹➹➹" // 생명 세팅
        lifeLength = binding.playerLifeTextView.length()
        this.playerNickName = playerNickName // 닉네임 세팅

        startTimer()

        if(imageStatus)
            downImageMove()
        else
            upImageMove()
    }

    // 게임 오바 되었을 때 뷰 재세팅
    fun setReGameView() {
        stage = 1
        playerScore = 0
        thisPlayerScore = 0
        binding.playerScoreNumberTextView.text = playerScore.toString()
        binding.stageNumberTextView.text = stage.toString()

        binding.mainConstraintLayout.background = resources.getDrawable(backgroundImageArray[stage - 1])

        binding.playerLifeTextView.text = "➹➹➹➹➹" // 생명 세팅
        lifeLength = binding.playerLifeTextView.length()

        startTimer()

        if(imageStatus)
            downImageMove()
        else
            upImageMove()
    }

    // 레벨업 되었을 때 뷰 세팅
    fun setNextGameView() { // 게임 next level!!
        stage++
        thisPlayerScore = 0
        binding.stageNumberTextView.text = stage.toString()

        binding.mainConstraintLayout.background = resources.getDrawable(backgroundImageArray[stage - 1])

        binding.playerLifeTextView.text = "➹➹➹➹➹" // 생명 세팅
        lifeLength = binding.playerLifeTextView.length()

        startTimer()

        if(imageStatus)
            downImageMove()
        else
            upImageMove()
    }

    // 타이머 동작
    private fun startTimer() {
        timerTask = timer(period = 10) {
            time++
            val sec = 59 - (time / 100)

            runOnUiThread {
                if(sec < 10)
                    binding.timeLifeSecondTextView.text = "0$sec"
                else
                    binding.timeLifeSecondTextView.text = "$sec"
            }

            if(sec == 0)
                mDelayHandler.post(::setDialog)
        }
    }

    private fun shoot() {
        lifeLength-- // 한번 쏠 때마다, 생명 줄음 (생명 5개)

        moveArrow() // 화살 가보자고
        binding.playerLifeTextView.text = "➹".repeat(lifeLength)

        if(lifeLength == 0) // 생명 0 되면
            mDelayHandler.postDelayed(::setDialog, 2200L)
    }

    // 화살 움직이는 thread
    private fun moveArrow() {
        binding.arrowImageView.visibility = View.VISIBLE
        binding.arrowImageView.x = 350f

        runnableArrowImageView = Runnable { // 이미지 내려가기
            ObjectAnimator.ofFloat(binding.arrowImageView, "translationX", 1150f).apply {
                this.duration = 2000L
                start()
            }

            runOnUiThread {
                shootStatus = true
                arrowPositionY = binding.arrowImageView.y
            }
        }

        val runnableArrowImageView2 = Runnable {
            runOnUiThread {
                targetPositionY = binding.targetImageView.y + 30f // 과녁의 y 좌표 받아서
            }
        }

        val runnableArrowImageView3 = Runnable {
            runOnUiThread {
                binding.arrowImageView.visibility = View.INVISIBLE
                shootStatus = false

                if(arrowPositionY < targetPositionY + 100f && arrowPositionY > targetPositionY - 100f) {
                    playerScore++
                    thisPlayerScore++
                    getScore()
                    binding.playerScoreNumberTextView.text = playerScore.toString()
                }

                Log.d("좌표", "과녁 y표: $targetPositionY, 화살 y표: $arrowPositionY")
                Log.d("현재 스테이지 스코어", "$thisPlayerScore, 총점: $playerScore")
            }
        }

        handler.post(runnableArrowImageView)
        handler.postDelayed(runnableArrowImageView2, 1970L)
        handler.postDelayed(runnableArrowImageView3, 2000L)
    }

    // 점수 획득 시 하트 아이콘 움직이는 thread
    private fun getScore() {
        binding.scoreImageView.visibility = View.VISIBLE
        binding.scoreImageView.y = 500f
        Log.d("좌표", "하트 현 y 위치 : ${binding.scoreImageView.y}")

        val runnableScoreImageView = Runnable {
            ObjectAnimator.ofFloat(binding.scoreImageView,"translationY", -100f).apply {
                this.duration = 400L
                start()
            }
        }

        val runnableScoreImageView2 = Runnable {
            runOnUiThread {
                binding.scoreImageView.visibility = View.INVISIBLE
            }
        }

        handler.post(runnableScoreImageView)
        handler.postDelayed(runnableScoreImageView2, 400L)
    }

    // 과녁 이미지 위ㆍ아래 시간 차 두기
    private fun waitImageMove() {
        imageStatus = !imageStatus

        if(imageStatus)
            mDelayHandler.postDelayed(::downImageMove, 5000L)
        else
            mDelayHandler.postDelayed(::upImageMove, 5000L)
    }

    // 과녁 이미지 위로 움직이기
    private fun upImageMove() {
        runnableTargetImageView = Runnable { // 이미지 내려가기
            ObjectAnimator.ofFloat(binding.targetImageView, "translationY", 0f).apply {
                this.duration = 5000L
                start()
            }
        }
        mDelayHandler.post(runnableTargetImageView)
        waitImageMove()
    }

    // 과녁 이미지 아래로 움직이기
    private fun downImageMove() {
        runnableTargetImageView = Runnable { // 이미지 내려가기
            ObjectAnimator.ofFloat(binding.targetImageView, "translationY", 550f).apply {
                this.duration = 5000L
                start()
            }
        }
        mDelayHandler.post(runnableTargetImageView)
        waitImageMove()
    }

    private fun stopTimer() {
        timerTask?.cancel() // 타이머 정지
        time = 0
    }

    override fun onStart() {
        super.onStart()
        Log.d("생명주기", "onStart 켜짐")
    }

    override fun onResume() {
        super.onResume()
        Log.d("생명주기", "onResume 켜짐")
    }

    override fun onPause() {
        super.onPause()
        Log.d("생명주기", "onPause 켜짐")
    }

    override fun onStop() {
        super.onStop()
        Log.d("생명주기", "onStop 켜짐")

        stopTimer() // 타이머 정지
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("생명주기", "onDestroy 켜짐")
    }
}