# Badge NFC

Appli Android pour émuler des badges NFC MIFARE Classic sur Samsung Galaxy A23 5G.

## Ce que ça fait

- **Scan** : approcher un badge → l'UID est lu automatiquement
- **Stockage** : enregistrer le badge avec un titre et une photo optionnelle
- **Émulation** : appuyer sur une carte → le téléphone se comporte comme ce badge
- **Multi-badges** : autant de cartes que voulu, une seule active à la fois

## Limite connue

Fonctionne uniquement si le lecteur vérifie **l'UID seul** (très courant sur les vieux systèmes).
Ne fonctionne PAS si le lecteur fait une authentification MIFARE complète (secteurs chiffrés).
→ Un test de 30 secondes sur un lecteur suffit à le savoir.

## Build (Android Studio)

1. Ouvrir le dossier `BadgeNFC/` dans Android Studio Hedgehog (2023.1.1) ou plus récent
2. Laisser Gradle synchroniser (première fois : ~2 min, télécharge les dépendances)
3. Brancher le Galaxy A23 en USB, activer le débogage USB
4. Appuyer sur ▶ Run

## Build (ligne de commande)

```bash
cd BadgeNFC
./gradlew assembleDebug
# APK généré dans : app/build/outputs/apk/debug/app-debug.apk
```

Transférer l'APK sur le téléphone et installer (autoriser les sources inconnues une fois).

## Utilisation

1. Ouvrir l'appli
2. Appuyer sur **+**
3. Approcher le badge à scanner (antenne NFC = haut du A23)
4. Donner un nom, optionnellement une photo
5. **Enregistrer**
6. Sur la liste, appuyer sur la carte pour **activer l'émulation**
7. Approcher le téléphone du lecteur comme on ferait avec le badge

## Structure

```
app/src/main/java/com/vincentbounes/badgenfc/
├── MainActivity.kt          ← Navigation, capture NFC foreground
├── data/
│   ├── CardEntity.kt        ← Modèle Room (id, title, uid, imagePath)
│   ├── CardDao.kt           ← Requêtes BDD
│   └── CardDatabase.kt      ← Singleton Room
├── nfc/
│   ├── NfcReader.kt         ← Extraction UID depuis Tag
│   └── HceService.kt        ← Émulation HCE (réponses APDU)
└── ui/
    ├── CardViewModel.kt     ← État, logique métier
    ├── CardListScreen.kt    ← Écran principal
    ├── AddCardScreen.kt     ← Ajout/scan
    └── theme/               ← Material You
```

## v2 prévue (si v1 fonctionne)

- Sync Supabase (sauvegarde cloud, récupération sur nouveau téléphone)
- Auth Google
- Export/import des badges
