package com.example.udos_wg_tohuwabohu.dataclasses

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot

data class ContactPerson(
    val docID: String,
    var forename: String?,
    var surname: String?,
    var email: String?,
    var IBAN: String?,
    var telNr: String?
) {
    constructor(vals: DocumentSnapshot) :
            this(
                vals.id, null, null, null, null, null
            ) {
        update(vals)
    }

    fun update(vals: DocumentSnapshot) {
        this.forename = vals["vorname"].toString()
        this.surname = vals["nachname"].toString()
        this.email = vals["email"].toString()
        this.IBAN = vals["IBAN"].toString()
        this.telNr = vals["tel_nr"].toString()
    }
}
