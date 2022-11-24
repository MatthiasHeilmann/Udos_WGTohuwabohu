package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.firestore.DocumentSnapshot

data class ContactPerson(val docID: String, val forename: String, val surname: String, val email: String, val IBAN: String, val telNr: String){
    constructor(vals: DocumentSnapshot) :
        this(vals.id,
            vals["vorname"].toString(),
            vals["nachname"].toString(),
            vals["email"].toString(),
            vals["IBAN"].toString(),
            vals["tel_nr"].toString()
        )
}
