package com.vincentbounes.badgenfc.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vincentbounes.badgenfc.data.CardDatabase
import com.vincentbounes.badgenfc.data.CardEntity
import com.vincentbounes.badgenfc.nfc.HceService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CardViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = CardDatabase.getDatabase(application).cardDao()

    val cards: StateFlow<List<CardEntity>> = dao.getAllCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Carte actuellement active pour l'émulation
    private val _activeCard = MutableStateFlow<CardEntity?>(null)
    val activeCard: StateFlow<CardEntity?> = _activeCard

    // UID lu par le scanner NFC (en attente de sauvegarde)
    private val _scannedUid = MutableStateFlow<String?>(null)
    val scannedUid: StateFlow<String?> = _scannedUid

    private val _scannedTagInfo = MutableStateFlow<String?>(null)
    val scannedTagInfo: StateFlow<String?> = _scannedTagInfo

    fun setScannedTag(uid: String, tagInfo: String) {
        _scannedUid.value = uid
        _scannedTagInfo.value = tagInfo
    }

    fun clearScannedTag() {
        _scannedUid.value = null
        _scannedTagInfo.value = null
    }

    fun saveCard(title: String, uid: String, imagePath: String? = null) {
        viewModelScope.launch {
            dao.insertCard(CardEntity(title = title, uid = uid, imagePath = imagePath))
        }
    }

    fun updateCard(card: CardEntity) {
        viewModelScope.launch {
            dao.updateCard(card)
        }
    }

    fun deleteCard(card: CardEntity) {
        viewModelScope.launch {
            // Si on supprime la carte active, on désactive
            if (_activeCard.value?.id == card.id) {
                deactivateCard()
            }
            dao.deleteCard(card)
        }
    }

    /**
     * Active une carte pour l'émulation HCE.
     * Place l'UID dans le service HCE.
     */
    fun activateCard(card: CardEntity) {
        _activeCard.value = card
        // Convertit "24:A7:B2:CF" → ByteArray
        HceService.activeUid = card.uid.split(":").map {
            it.toInt(16).toByte()
        }.toByteArray()
    }

    fun deactivateCard() {
        _activeCard.value = null
        HceService.activeUid = null
    }
}
