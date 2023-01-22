package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.Timestamp

data class DataHandler(var wg: WG?, var contactPerson: ContactPerson?, var user: Roommate?, var roommateList: HashMap<String, Roommate>, var taskList: HashMap<String, Task>) {
    private constructor(): this(null, null, null, HashMap(), HashMap())
    companion object{
        private var instance: DataHandler? = null;

        fun getInstance(): DataHandler = instance ?: synchronized(this){
            instance?: DataHandler().also { instance = it }
        }
    }

    fun addRoommate(vararg roommates: Roommate){
        for (r in roommates) {
            roommateList[r.docID] = r
        }
    }

    fun addTask(vararg t: Task){
        for (task in t) {
            taskList[task.docId] = task
        }
    }
    
    fun getRoommate(uid: String): Roommate{
        return roommateList[uid]!!
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