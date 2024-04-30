package com.example.convoix.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.convoix.ChatViewModel

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val chatId = intent?.getStringExtra("CHAT_ID") ?: return
        val content = intent.getStringExtra("CONTENT") ?: return
        val senderId = intent.getStringExtra("SENDER_ID") ?: return
        val viewModel = ChatViewModel.getInstance()
        if(chatId!=""){
            viewModel.sendReply(chatId = chatId, msg = content, senderId = senderId)
            viewModel.cancelScheduledMsg(chatId, content, senderId)
        }
        else{
            viewModel.updateStatus(false, senderId)
        }
    }
}