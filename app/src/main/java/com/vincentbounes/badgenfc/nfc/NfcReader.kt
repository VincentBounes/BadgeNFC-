package com.vincentbounes.badgenfc.nfc

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.NfcA

/**
 * Lit un tag NFC et extrait l'UID.
 * Fonctionne avec MIFARE Classic et NfcA générique.
 */
object NfcReader {

    /**
     * Extrait l'UID d'un tag NFC sous forme de String "XX:XX:XX:XX"
     */
    fun extractUid(tag: Tag): String {
        val uid = tag.id
        return uid.joinToString(":") { "%02X".format(it) }
    }

    /**
     * Extrait l'UID brut en ByteArray
     */
    fun extractUidBytes(tag: Tag): ByteArray = tag.id

    /**
     * Retourne des infos lisibles sur le type de tag
     */
    fun describeTag(tag: Tag): String {
        val techs = tag.techList.map { it.substringAfterLast('.') }
        return when {
            techs.contains("MifareClassic") -> {
                val mc = MifareClassic.get(tag)
                val type = when (mc?.type) {
                    MifareClassic.TYPE_CLASSIC -> "MIFARE Classic"
                    MifareClassic.TYPE_PLUS -> "MIFARE Plus"
                    MifareClassic.TYPE_PRO -> "MIFARE Pro"
                    else -> "MIFARE"
                }
                val size = mc?.size ?: 0
                "$type ${size / 1024}K"
            }
            techs.contains("NfcA") -> "NFC-A (ISO 14443-3A)"
            techs.contains("IsoDep") -> "ISO 14443-4"
            else -> techs.joinToString(", ")
        }
    }
}
