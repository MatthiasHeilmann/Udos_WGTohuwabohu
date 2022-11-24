package com.example.udos_wg_tohuwabohu.dataclasses

data class DataHandler(var wg: WG?, var contactPerson: ContactPerson?, var user: Roommate?, var roommateList: HashMap<String, Roommate>, var taskList: HashMap<String, Task>) {
    constructor(): this(null, null, null, HashMap(), HashMap())
    companion object{
        private var instance: DataHandler? = null;

        fun getInstance(): DataHandler = instance ?: synchronized(this){
            instance?: DataHandler().also { instance = it }
        }
    }

    fun addRoommate(vararg m: Roommate){
        for (mitbewohner in m) {
            roommateList[mitbewohner.docID] = mitbewohner
        }
    }

    fun addTask(vararg t: Task){
        for (aufgabe in t) {
            taskList[aufgabe.docId] = aufgabe
        }
    }
    
    fun getRoommate(uid: String): Roommate{
        return roommateList[uid]!!
    }
    
    fun getTask(uid: String): Task{
        return taskList[uid]!!
    }
}