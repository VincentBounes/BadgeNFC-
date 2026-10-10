package com.vincentbounes.badgenfc.nfc

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

/**
 * Service HCE (Host Card Emulation).
 *
 * IMPORTANT — limite connue du HCE Android avec MIFARE Classic :
 * Android HCE implémente ISO 14443-4 (protocole APDU). MIFARE Classic utilise
 * un protocole propriétaire NXP (ISO 14443-3A + CRYPTO1) que HCE ne peut pas
 * reproduire complètement.
 *
 * CE QUI FONCTIONNE : les lecteurs qui interrogent uniquement l'UID au niveau
 * RF (couche ISO 14443-3A) — c'est le cas de la majorité des vieux systèmes
 * de contrôle d'accès. L'UID est géré par le chipset NFC Android directement,
 * pas par ce service.
 *
 * CE QUI NE FONCTIONNE PAS : les lecteurs qui font une vraie authentification
 * MIFARE Classic (lecture des secteurs chiffrés). Dans ce cas, même cette appli
 * ne peut rien — c'est une limite hardware/protocole.
 *
 * Ce service répond aux échanges APDU au cas où un lecteur ISO 14443-4 serait
 * présent (cas moins fréquent sur du contrôle d'accès classique).
 */
class HceService : HostApduService() {

    companion object {
        private const val TAG = "HceService"

        // Réponse succès ISO 7816
        private val SUCCESS_SW = byteArrayOf(0x90.toByte(), 0x00.toByte())
        // Réponse erreur
        private val UNKNOWN_CMD_SW = byteArrayOf(0x00.toByte(), 0x00.toByte())

        // L'UID actif, partagé via companion object (simple pour v1)
        var activeUid: ByteArray? = null
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) return UNKNOWN_CMD_SW

        Log.d(TAG, "APDU reçu: ${commandApdu.toHex()}")

        // SELECT AID
        if (commandApdu.size >= 4 && commandApdu[0] == 0x00.toByte() && commandApdu[1] == 0xA4.toByte()) {
            Log.d(TAG, "SELECT AID reçu")
            val uid = activeUid
            return if (uid != null) {
                // On renvoie l'UID suivi de SW 90 00
                uid + SUCCESS_SW
            } else {
                SUCCESS_SW
            }
        }

        // GET DATA — certains lecteurs demandent les données comme ça
        if (commandApdu.size >= 2 && commandApdu[0] == 0xFF.toByte() && commandApdu[1] == 0xCA.toByte()) {
            val uid = activeUid
            return if (uid != null) {
                uid + SUCCESS_SW
            } else {
                UNKNOWN_CMD_SW
            }
        }

        return SUCCESS_SW
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "HCE désactivé, raison: $reason")
    }

    private fun ByteArray.toHex(): String =
        joinToString(":") { "%02X".format(it) }
}
