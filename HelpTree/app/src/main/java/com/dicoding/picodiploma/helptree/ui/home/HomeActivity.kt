package com.dicoding.picodiploma.helptree.ui.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.dicoding.picodiploma.helptree.R
import com.dicoding.picodiploma.helptree.databinding.ActivityHomeBinding
import com.dicoding.picodiploma.helptree.ui.chatbot.ChatbotActivity
import com.dicoding.picodiploma.helptree.ui.picture.TakePictureActivity

class HomeActivity : AppCompatActivity(), View.OnClickListener {

    private var _activityHomeBinding: ActivityHomeBinding? = null
    private val activityHomeBinding get() = _activityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _activityHomeBinding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(activityHomeBinding?.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        activityHomeBinding?.viewPager?.adapter = sectionsPagerAdapter
        activityHomeBinding?.tabs?.setupWithViewPager(activityHomeBinding?.viewPager)

        activityHomeBinding?.btnCamera?.setOnClickListener(this)
        activityHomeBinding?.btnChat?.setOnClickListener(this)
        supportActionBar?.elevation = 0f

    }

    override fun onClick(view: View?) {
        when ( view!!.id)
        {
            R.id.btn_camera->
            {
                val intent = Intent(this, TakePictureActivity::class.java)
                startActivity(intent)
            }
            R.id.btn_chat->
            {
                val intent = Intent(this, ChatbotActivity::class.java)
                startActivity(intent)
            }
        }
    }
}