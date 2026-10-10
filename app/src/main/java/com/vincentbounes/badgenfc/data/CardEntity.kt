package com.vincentbounes.badgenfc.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val uid: String,           // ex: "24:A7:B2:CF"
    val imagePath: String? = null,  // chemin local vers la photo choisie
    val createdAt: Long = System.currentTimeMillis()
)
