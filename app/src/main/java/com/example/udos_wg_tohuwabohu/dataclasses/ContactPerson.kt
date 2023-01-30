package com.example.udos_wg_tohuwabohu.dataclasses

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot

data class ContactPerson(val docID: String, var forename: String?, var surname: String?, var email: String?, var IBAN: String?, var telNr: String?){
    constructor(vals: DocumentSnapshot) :
        this(vals.id,null, null, null, null, null
        )
    {
            update(vals)
        }
    fun update(vals: DocumentSnapshot){
        this.forename = vals["vorname"].toString()
        this.surname = vals["nachname"].toString()
        this.email = vals["email"].toString()
        this.IBAN = vals["IBAN"].toString()
        this.telNr = vals["tel_nr"].toString()
        val TAG = "[CONTACT]"
        Log.d(TAG,vals.toString())
        Log.d(TAG,vals["vorname"].toString())
        Log.d(TAG,vals["nachname"].toString())
        Log.d(TAG,vals["email"].toString())
        Log.d(TAG,vals["IBAN"].toString())
        Log.d(TAG,vals["tel_nr"].toString())
        Log.d(TAG,this.forename.toString())
        Log.d(TAG,this.surname.toString())
        Log.d(TAG,this.email.toString())
        Log.d(TAG,this.IBAN.toString())
        Log.d(TAG,this.telNr.toString())
    }
}
