package com.example.udos_wg_tohuwabohu.dataclasses

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.MutableSnapshot
import androidx.compose.runtime.snapshots.SnapshotMutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.google.firebase.Timestamp

data class DataHandler(
    var wg: SnapshotStateList<WG>,
    var contactPerson: ContactPerson?,
    var user: Roommate?,
    var roommateList: SnapshotStateMap<String, Roommate>,
    var taskList: HashMap<String, Task>,
    var financeEntries: SnapshotStateList<FinanceEntry>,
    var chat: SnapshotStateList<ChatMessage>
) {
    private constructor() : this(
        mutableStateListOf<WG>(), null, null, mutableStateMapOf(), HashMap(), mutableStateListOf<FinanceEntry>(), mutableStateListOf<ChatMessage>() )

    companion object {
        private var instance: DataHandler? = null;

        fun getInstance(): DataHandler = instance ?: synchronized(this) {
            instance ?: DataHandler().also { instance = it }
        }
    }

    fun addFinanceEntry(vararg finances: FinanceEntry) {
        for (f in finances) {
            if (!financeEntries.contains(f)) {
                financeEntries.add(f)
            }
        }
    }
    fun addChatMessage(vararg messages: ChatMessage) {
        for (m in messages) {
            if (!chat.contains(m)) {
                chat.add(m)
            }
        }
    }

    fun addRoommate(vararg roommates: Roommate) {
        for (r in roommates) {
            roommateList[r.docID] = r
        }
    }

    fun addTask(vararg tasks: Task) {
        for (t in tasks) {
            taskList[t.docId] = t
        }
    }


    fun getFinancesEntries(): Array<FinanceEntry>{
        return financeEntries.toTypedArray()
    }

    fun getChat(): Array<ChatMessage> {
        return chat.toTypedArray()
    }

    fun getAllRoommates(): Array<Roommate>{
        return roommateList.values.toTypedArray()
    }

    fun getRoommate(uid: String?): Roommate? {
        return roommateList[uid]
    }

    fun getTask(uid: String): Task {
        return taskList[uid]!!
    }

    fun getTasks(): HashMap<String, Task>? {
        return taskList
    }

    fun getCalendar(): MutableList<MutableMap<String, Timestamp>>? {
        return wg.first().calendar
    }

}

