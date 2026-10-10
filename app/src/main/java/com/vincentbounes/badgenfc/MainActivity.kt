package com.vincentbounes.badgenfc

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vincentbounes.badgenfc.nfc.NfcReader
import com.vincentbounes.badgenfc.ui.AddCardScreen
import com.vincentbounes.badgenfc.ui.CardListScreen
import com.vincentbounes.badgenfc.ui.CardViewModel
import com.vincentbounes.badgenfc.ui.theme.BadgeNFCTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CardViewModel by viewModels()
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        setContent {
            BadgeNFCTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Active le mode foreground dispatch : l'appli intercepte les tags NFC
        nfcAdapter?.enableReaderMode(
            this,
            { tag -> handleTag(tag) },
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }

    private fun handleTag(tag: Tag) {
        val uid = NfcReader.extractUid(tag)
        val info = NfcReader.describeTag(tag)
        // Mise à jour du ViewModel sur le thread principal
        runOnUiThread {
            viewModel.setScannedTag(uid, info)
        }
    }
}

@Composable
fun AppNavigation(viewModel: CardViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            CardListScreen(
                viewModel = viewModel,
                onAddCard = { navController.navigate("add") }
            )
        }
        composable("add") {
            AddCardScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
