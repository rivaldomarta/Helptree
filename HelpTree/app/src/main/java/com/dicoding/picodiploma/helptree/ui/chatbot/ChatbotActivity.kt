package com.dicoding.picodiploma.helptree.ui.chatbot

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.picodiploma.helptree.data.Message
import com.dicoding.picodiploma.helptree.databinding.ActivityChatbotBinding
import com.dicoding.picodiploma.helptree.utils.BotResponse
import com.dicoding.picodiploma.helptree.utils.Constants.BOT_HELPTREE
import com.dicoding.picodiploma.helptree.utils.Constants.OPEN_GOOGLE
import com.dicoding.picodiploma.helptree.utils.Constants.RECEIVE_ID
import com.dicoding.picodiploma.helptree.utils.Constants.SEARCH_GOOGLE
import com.dicoding.picodiploma.helptree.utils.Constants.SEND_ID
import com.dicoding.picodiploma.helptree.utils.Time
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import kotlinx.coroutines.*
import org.json.JSONObject

class ChatbotActivity : AppCompatActivity() {

    private var _activityChatbotBinding: ActivityChatbotBinding? = null
    private val activityChatbotBinding get() = _activityChatbotBinding
    private lateinit var adapter: MessageAdapter
    private val botList = listOf("HelptreeBot", "Tree", "Pohon")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _activityChatbotBinding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(activityChatbotBinding?.root)

        val intent = intent
        val nameDisease = intent.getStringExtra("DISEASE_NAME")



        recyclerView()

        clickEvent()

        if (nameDisease != null) {
            if(nameDisease.isNotEmpty()){
                sendMessageDisease(nameDisease)
            }
        }else{
            val random = (0..2).random()
            customMessage("Halo, saat ini kamu sudah terhubung dengan ${botList[random]}, ada yang bisa saya bantu ? ")
        }
    }

    private fun sendMessageDisease(nameDisease: String) {
        val message = nameDisease.toString()
        val timestamp = Time.timeStamp()
        if(message.isNotEmpty()){
            activityChatbotBinding?.etMessage?.setText("")
            adapter.insertMessage(Message(message, SEND_ID, timestamp))
            activityChatbotBinding?.rvMessages?.scrollToPosition(adapter.itemCount-1)
            botResponse(message)
        }
    }

    private fun clickEvent() {
        activityChatbotBinding?.btnSend?.setOnClickListener {
            sendMessage()
        }

        activityChatbotBinding?.etMessage?.setOnClickListener {
            GlobalScope.launch {
                delay(100)
                withContext(Dispatchers.Main){
                    activityChatbotBinding?.rvMessages?.scrollToPosition(adapter.itemCount-1)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        GlobalScope.launch {
            delay(1000)
            withContext(Dispatchers.Main){
                activityChatbotBinding?.rvMessages?.scrollToPosition(adapter.itemCount-1)
            }
        }
    }

    private fun recyclerView() {
        adapter = MessageAdapter()
        activityChatbotBinding?.rvMessages?.adapter = adapter
        activityChatbotBinding?.rvMessages?.layoutManager = LinearLayoutManager(applicationContext)
    }

    private fun sendMessage(){
        val message = activityChatbotBinding?.etMessage?.text.toString()
        val timestamp = Time.timeStamp()
        if(message.isNotEmpty()){
            activityChatbotBinding?.etMessage?.setText("")
            adapter.insertMessage(Message(message, SEND_ID, timestamp))
            activityChatbotBinding?.rvMessages?.scrollToPosition(adapter.itemCount-1)

            botResponse(message)
        }
    }

    private fun botResponse(message: String) {
        val timestamp = Time.timeStamp()

        GlobalScope.launch {
            delay(1000)
            withContext(Dispatchers.Main){
                when(val response = BotResponse.standarResponse(message)){
                    OPEN_GOOGLE->{
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse("https://www.google.com/")
                        startActivity(intent)
                    }
                    SEARCH_GOOGLE->{
                        val intent = Intent(Intent.ACTION_VIEW)
                        val cari = message.substringAfter("cari")
                        intent.data = Uri.parse("https://www.google.com/search?&q=$cari")
                        startActivity(intent)
                    }
                    BOT_HELPTREE->{
                        val url = "https://arik.my.id/helptree/bot?query=$message"
                        val client = AsyncHttpClient()
                        client.get(url, object: AsyncHttpResponseHandler(){
                            override fun onSuccess(statusCode: Int, headers: Array<Header>, responseBody: ByteArray) {
                                try {
                                    val result = String(responseBody)
                                    val responseObject = JSONObject(result)
                                    var hasil = responseObject.getString("response")

                                    adapter.insertMessage(Message(hasil.toString(), RECEIVE_ID, timestamp))
                                    activityChatbotBinding?.rvMessages?.scrollToPosition(adapter.itemCount-1)
                                }catch (e: java.lang.Exception){
                                    Log.d("Exception", e.message.toString())
                                }
                            }
                            override fun onFailure(statusCode: Int, headers: Array<Header>, responseBody: ByteArray, error: Throwable) {
                                Log.d("onFailure", error.message.toString())
                            }
                        })
                    }
                    else->{
                        adapter.insertMessage(Message(response, RECEIVE_ID, timestamp))
                        activityChatbotBinding?.rvMessages?.scrollToPosition(adapter.itemCount-1)
                    }
                }
            }
        }

    }

    private fun customMessage(message: String) {
        GlobalScope.launch {
            delay(1000)
            withContext(Dispatchers.Main){
                val timestamp = Time.timeStamp()
                adapter.insertMessage(Message(message,RECEIVE_ID,timestamp))
                activityChatbotBinding?.rvMessages?.scrollToPosition(adapter.itemCount-1)
            }
        }
    }
}