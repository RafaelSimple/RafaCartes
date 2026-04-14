# FR : 
## 💳 RafaCartes - Gestionnaire de Cartes de Fidélité

RafaCartes est une application Android native développée en Java qui vous permet de stocker et organiser toutes vos cartes de fidélité au même endroit.
On ne peut pas encore scanner ou numériser avec la caméra, c'est pour bientôt hihi 👀
En attendant, vous pouvez demander à une IA de vous les digitaliser : exporter un JSON depuis mon app pour que l'IA puisse connaître la structure, donnez lui ensuite les photos de vos cartes et elle vous générera le JSON, vous n'aurez plus qu'à l'importer dans l'app !!

### ✨ Fonctionnalités Principales

* **👤 Gestion Multi-Profils :** Créez jusqu'à 3 profils avec des couleurs personnalisées (idéal pour séparer vos cartes pro/perso ou pour toute la famille).
* **📸 Personnalisation des Cartes :** Ajoutez le nom de l'enseigne, une description, et associez-y le logo du magasin depuis votre galerie d'images.
* **🔤 Génération de Codes-barres :** Saisissez simplement votre numéro de client, l'application génère automatiquement un code-barres universel (format `CODE_128`) prêt à être scanné en caisse.
* **🗂️ Tri Intelligent :** Retrouvez facilement vos cartes en les triant par ordre alphabétique, par date d'ajout ou par fréquence d'utilisation (les plus scannées en premier).
* **🔄 Import / Export JSON :** Sauvegardez vos cartes en exportant un fichier JSON, et restaurez-les facilement sur un autre appareil. L'importation gère automatiquement les doublons !

(cette partie à été écrite à l'aide d'une IA)

### 🛠️ Technologies Utilisées

* **Langage :** Java
* **IDE :** Android Studio
* **Interface :** XML, Material Components for Android
* **Génération de Codes-barres :** [ZXing (Zebra Crossing)](https://github.com/zxing/zxing) via `com.journeyapps:zxing-android-embedded`
* **Stockage de données :** JSON (sérialisation)

###  Installation & Lancement

Vous pouvez simplement télécharger et lancer l'apk à chaque release !

Sinon, pour les impatients :

1. Clonez ce dépôt : `git clone https://github.com/votre-nom/rafacartes.git`
2. Ouvrez le projet dans **Android Studio**.
3. Assurez-vous d'avoir synchronisé Gradle (pour inclure la dépendance ZXing).
4. Lancez l'application sur un émulateur ou un appareil physique.

Merci de votre soutient !!


# EN :

## 💳 RafaCartes - Loyalty Card Manager

RafaCartes is a native Android application built with Java that allows you to store, and organize all your loyalty cards in one convenient place.
It doesn't support scanning and digitalizing cards with the phone's camera yet, it'll arrive soon 👀;
Meanwhile, you can use an AI, give it pictures of you card and ask it to generate à JSON (first export one from my app to give it as a model).
Then simply import it and there you have it !

### ✨ Key Features

* **👤 Multi-Profile Support:** Create up to 3 profiles with custom color tags (perfect for separating business/personal cards or sharing with family members).
* **📸 Card Customization:** Add the store name, a quick description, and attach the store's logo directly from your image gallery.
* **🔤 Barcode Generation:** Simply enter your loyalty number, and the app automatically generates a universal barcode (`CODE_128` format) ready to be scanned at the checkout.
* **🗂️ Smart Sorting:** Easily find your cards by sorting them alphabetically, by date added, or by usage frequency (most scanned cards appear first).
* **🔄 JSON Import / Export:** Back up your cards by exporting them to a JSON file, and effortlessly restore them on another device. The import feature automatically handles duplicates!

(this paragraphe was generated using AI)

### 🛠️ Tech Stack

* **Language:** Java
* **IDE:** Android Studio
* **UI/UX:** XML, Material Components for Android
* **Barcode Generation:** [ZXing (Zebra Crossing)](https://github.com/zxing/zxing) via `com.journeyapps:zxing-android-embedded`
* **Data Storage:** `SharedPreferences` (JSON serialization)

###  Getting Started

You can simply download the .apk from the latest release !

If you want to use the version in developpment :
1. Clone this repository: `git clone https://github.com/your-username/rafacartes.git`
2. Open the project in **Android Studio**.
3. Sync Gradle to ensure the ZXing dependency is downloaded.
4. Run the app on an emulator or a physical device.

Thank you all for your support !

