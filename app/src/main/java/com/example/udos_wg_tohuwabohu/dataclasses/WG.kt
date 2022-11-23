package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.firestore.DocumentSnapshot

data class WG(val docID: String, val bezeichung: String, val ansprechpartner: Ansprechpartner?, val einkaufsliste: Map<String,Boolean>?, var taskList: ArrayList<Aufgabe>){
    constructor(vals: DocumentSnapshot, ansprechpartner: Ansprechpartner?) : this(
        vals.id,
        vals["bezeichnung"].toString(),
        ansprechpartner,
        vals["einkaufsliste"] as Map<String, Boolean>,
        taskList = ArrayList<Aufgabe>()
    )

    public fun appendTask(a: Aufgabe){
        taskList.add(a);
    }

    public fun appendTasks(a: ArrayList<Aufgabe>){
        taskList.addAll(a);
    }
}
