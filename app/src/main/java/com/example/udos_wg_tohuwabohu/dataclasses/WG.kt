package com.example.udos_wg_tohuwabohu.dataclasses

data class WG(val docID: String, val bezeichung: String, val ansprechpartner: Ansprechpartner?, val einkaufsliste: Map<String,Boolean>)
