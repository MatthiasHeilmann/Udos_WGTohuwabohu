package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.firestore.DocumentSnapshot

data class Ansprechpartner(val docID: String, val vorname: String, val nachname: String, val email: String, val IBAN: String, val telNr: String){
    constructor(vals: DocumentSnapshot) :
        this(vals.id,
            vals["vorname"].toString(),
            vals["nachname"].toString(),
            vals["email"].toString(),
            vals["IBAN"].toString(),
            vals["tel_nr"].toString()
        )
}
