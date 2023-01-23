package com.example.udos_wg_tohuwabohu.dataclasses

enum class Collections(val call: String) {
    Roommate("mitbewohner"),
    WG("wg"),
    CHAT("chat_files"),
    ContactPerson("ansprechtpartner"),
    Task("tasks");

    override fun toString(): String {
        return call
    }
}