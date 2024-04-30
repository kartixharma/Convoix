package com.example.convoix.schedule

import com.example.convoix.schedule.AlarmItem

interface AlarmSch {
    fun schedule(item: AlarmItem)
    fun cancel(item: AlarmItem)
}