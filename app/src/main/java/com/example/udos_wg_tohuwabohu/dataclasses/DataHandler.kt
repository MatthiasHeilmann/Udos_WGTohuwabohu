package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.firestore.DocumentReference

import com.google.firebase.Timestamp

data class DataHandler(var wg: WG?, var contactPerson: ContactPerson?, var user: Roommate?, var roommateList: HashMap<String, Roommate>, var taskList: HashMap<String, Task>, var chat: ArrayList<ChatMessage>) {
    private constructor(): this(null, null, null, HashMap(), HashMap(), ArrayList<ChatMessage>())
    companion object{
        private var instance: DataHandler? = null;

        fun getInstance(): DataHandler = instance ?: synchronized(this){
            instance?: DataHandler().also { instance = it }
        }
    }

    fun addChatMessage(vararg messages: ChatMessage){
        for(m in messages){
            if(!chat.contains(m))
                chat.add(m)
        }
    }

    fun addRoommate(vararg roommates: Roommate){
        for (r in roommates) {
            roommateList[r.docID] = r
        }
    }

    fun addTask(vararg tasks: Task){
        for (t in tasks) {
            taskList[t.docId] = t
        }
    }

    fun getChat(): Array<ChatMessage>{
        return chat.toTypedArray()
    }

    fun getRoommate(uid: String?): Roommate?{
        return roommateList[uid]
    }
    
    fun getTask(uid: String): Task{
        return taskList[uid]!!
    }

    fun getTasks(): HashMap<String, Task>?{
        return taskList
    }

    fun getCalendar(): ArrayList<HashMap<String, Timestamp>>? {
        return wg!!.calendar
    }
}