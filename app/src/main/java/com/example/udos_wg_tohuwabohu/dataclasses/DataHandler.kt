package com.example.udos_wg_tohuwabohu.dataclasses

data class DataHandler(var wg: WG?, var user: Mitbewohner?, var mitbewohnerList: HashMap<String, Mitbewohner>, var aufgabeList: HashMap<String, Aufgabe>) {
    constructor(): this(null, null, HashMap(), HashMap())
    companion object{
        private var instance: DataHandler? = null;

        fun getInstance(): DataHandler = instance ?: synchronized(this){
            instance?: DataHandler().also { instance = it }
        }
    }

    fun addMitbewohner(vararg m: Mitbewohner){
        for (mitbewohner in m) {
            mitbewohnerList[mitbewohner.userID] = mitbewohner
        }
    }

    fun addAufgabe(vararg t: Aufgabe){
        for (aufgabe in t) {
            aufgabeList[aufgabe.docId] = aufgabe
        }
    }
    
    fun getMitbewohner(uid: String): Mitbewohner{
        return mitbewohnerList[uid]!!
    }
    
    fun getAufgabe(uid: String): Aufgabe{
        return aufgabeList[uid]!!
    }
}