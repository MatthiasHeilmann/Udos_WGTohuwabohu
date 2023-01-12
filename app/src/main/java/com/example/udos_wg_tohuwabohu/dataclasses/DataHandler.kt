package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.firestore.DocumentReference

data class DataHandler(var wg: WG?, var contactPerson: ContactPerson?, var user: Roommate?, var roommateList: HashMap<String, Roommate>, var taskList: HashMap<String, Task>, var chat: ArrayList<ChatMessage>) {
    private constructor(): this(null, null, null, HashMap(), HashMap(), ArrayList<ChatMessage>())
    companion object{
        private var instance: DataHandler? = null;

        fun getInstance(): DataHandler = instance ?: synchronized(this){
            instance?: DataHandler().also { instance = it }
        }
    }

    fun addChatMessage(vararg messages: ChatMessage){
        messages.forEach { msg ->
            if(!chat.contains(msg))
                chat.add(msg)
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
}