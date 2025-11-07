# Splash Browser

Splash Browser adalah aplikasi peramban Android berbasis GeckoView yang dipadukan dengan sekumpulan utilitas produktivitas—mulai dari manajemen tab modern, speed dial kustom, pemblokir iklan berbasis daftar host, hingga workspace otomasi untuk membangun dataset CAPTCHA. Proyek ini sepenuhnya ditulis dengan Java dan memanfaatkan Android View system tradisional.

## Sorotan Fitur
- **Mesin GeckoView 135** – `CosmicExplorer` mengelola `GeckoRuntime` tunggal dengan Enhanced Tracking Protection dan opsi debug jarak jauh.
- **Manajemen tab persisten** – `TabViewModel`, `TabManager`, dan `TabsManagementFragment` menyajikan tab yang tersinkronisasi dengan LiveData, termasuk penyimpanan ulang otomatis via `TabStorage` (SharedPreferences + Gson).
- **Home dengan Speed Dial** – `HomeFragment` + `SpeedDialAdapter` memberi grid favorit (5 kolom) yang bisa ditambah, ubah, dan hapus melalui bottom sheet, tersimpan di `SpeedDialPrefs`.
- **Pemblokir iklan ringan** – `AdBlocker` memuat daftar host khusus dari `app/src/main/assets/hosts` dan dipakai oleh `AdBlockerWebView` / Gecko interceptor untuk mematikan request ke domain iklan.
- **Riwayat, bookmark, dan sandi** – `BrowserDatabaseHelper` (SQLite) menyimpan tabel `history`, `bookmarks`, dan `passwords`; modul manajer menyediakan CRUD dan pencarian (`HistoryBrowsingFragment`, `BookmarkManager`, `PasswordManager`).
- **Dataset CAPTCHA on-device** – `CaptchaDataManager`, `CaptchaViewerFragment`, dan `CaptchaAdapter` menangkap data Base64, menyimpannya sebagai berkas gambar di `Android/data/.../Datasets/database/Images`, serta menyiapkan pagination + ekspor database SQLite.
- **Workspace otomasi legacy** – `StartWorking` menyediakan kanvas multi-webview/GeckoView dengan overlay pointer, konsol, integrasi `WebAppInterface`, dan klien `OkHttp` untuk mengirim hasil labeling ke backend pribadi.
- **Drawer utilitas** – Navigation drawer (`nav_header.xml`) memuat shortcut ke modul lain (Downloads, Files, Notes, Photos, Music, Videos, V2Ray, Console, dll). Sebagian besar fragmen saat ini adalah stub siap dikembangkan.
- **Tooling tambahan** – `CalculatorSetress` (berbasis exp4j) menangani ekspresi ilmiah, `UrlValidator` mendukung perintah slash (`/yt`, `/ig`, `/go kata kunci`, dll), `GlideFaviconFetcher` mengambil favicon dinamis melalui layanan Google.

## Arsitektur & Modul
| Modul | Deskripsi | Lokasi utama |
| --- | --- | --- |
| **Aplikasi & Engine** | `CosmicExplorer` (subkelas `Application`) menyiapkan `GeckoRuntime` tunggal, `GeckoSessionPool`, dan profil tab opsional. | `app/src/main/java/alv/splash/browser/CosmicExplorer.java`, `util/GeckoSessionPool.java` |
| **UI utama** | `MainActivity` mengaktifkan Edge-to-Edge, drawer, `SlidingUpPanelLayout`, dan mengorkestrasi `HomeFragment`, `GeckoViewFragment`, `TabsManagementFragment`. | `app/src/main/java/alv/splash/browser/MainActivity.java`, `ui/fragment/*` |
| **Lapisan data** | SQLite (`BrowserDatabaseHelper`, `CaptchaDbHelper`) + SharedPreferences (`TabStorage`, `SpeedDialPrefs`). Riwayat, bookmark, sandi, dan dataset CAPTCHA berada di sini. | `app/src/main/java/alv/splash/browser/*.java` |
| **Otomasi** | `StartWorking`, `WebAppInterface`, `LogView`, `CalculatorSetress`, serta script JS inline untuk mendeteksi elemen CAPTCHA dan mengirimkan ke backend internal. | `app/src/main/java/alv/splash/browser/StartWorking.java` |
| **Sumber daya** | Layout View System (`res/layout`), ikon, dan konfigurasi jaringan (`res/xml/network_security_config.xml`, `res/xml/backup_rules.xml`). | `app/src/main/res` |

Struktur tingkat tinggi:
```
splash-browser-app/
├── app/
│   ├── src/main/java/alv/splash/browser/
│   │   ├── ui/{fragment,adapter}
│   │   ├── util/ (TabStorage, GeckoSessionPool, dll)
│   │   ├── model/TabItem.java
│   │   └── legacy workspace (StartWorking, WebAppInterface, dll)
│   ├── src/main/res/{layout,drawable,xml,...}
│   ├── build.gradle
│   └── proguard-rules.pro
├── gradle/libs.versions.toml
├── asDebug.sh / asRelease.sh / clean.sh
└── settings.gradle
```

## Dependensi Utama
- Android Gradle Plugin 8.7.2, Kotlin tidak digunakan (seluruh kode Java 17).
- AndroidX: AppCompat 1.6.1, Material 1.10.0, Activity 1.8.0, ConstraintLayout 2.1.4.
- GeckoView (`org.mozilla.geckoview`) – kanal & versi dapat diganti lewat properti `geckoviewChannel`/`geckoviewVersion` di `app/build.gradle`.
- OkHttp 4.9.3 untuk HTTP klien, Glide 4.16.0 untuk pemuatan gambar/favicons, Gson 2.8.9 untuk serialisasi Tab/Speed Dial, exp4j 0.4.8 untuk kalkulator.
- SQLite AndroidX 2.4.0 + `androidx.swiperefreshlayout`, `androidx.recyclerview`, dsb (melalui dependensi transitif).

## Membangun & Menjalankan
### Prasyarat
- Android Studio Jellyfish/Ladybug atau CLI dengan JDK 17.
- Android SDK Platform 35, Build Tools 35.x.
- Perangkat/Emulator dengan minimal Android 5.0 (API 21). Release build default hanya menghasilkan ABI `arm64-v8a` (lihat blok `splits` di `app/build.gradle`).

### Android Studio
1. Clone repositori dan buka folder `splash-browser-app`.
2. Pastikan `local.properties` menunjuk ke direktori SDK lokal.
3. Sinkronkan Gradle, pilih varian `app` → `debug` atau `release`.
4. Jalankan di perangkat fisik/emulator melalui konfigurasi `MainActivity`.

### Gradle CLI
```bash
./gradlew assembleDebug        # build debug
./gradlew installDebug         # push ke device default
./gradlew testDebugUnitTest    # unit test JVM (jika tersedia)
./gradlew connectedDebugAndroidTest  # instrumented test (butuh device terhubung)
```
Script singkat tersedia:
- `./asDebug.sh` → `./gradlew clean app:assembleDebug`
- `./asRelease.sh` → `./gradlew clean app:assembleRelease`
- `./clean.sh` → `./gradlew clean`

## Konfigurasi Penting
| Kebutuhan | Lokasi | Catatan |
| --- | --- | --- |
| **GeckoView channel & versi** | `app/build.gradle` (`ext.geckoviewChannel`, `ext.geckoviewVersion`) | Sesuaikan dengan rilis GeckoView yang kompatibel. |
| **Keystore rilis** | `keystore.properties` (tidak versi kontrol) | Properti yang dibaca: `storeFile`, `storePassword`, `keyAlias`, `keyPassword`. Boleh juga lewat ENV (`KEYSTORE_FILE`, `STORE_PASSWORD`, dll) untuk CI. |
| **Endpoint backend & token** | Saat ini dikodekan di `StartWorking` sebagai konstanta `SERVER_URL` & `API_KEY`. **Pindahkan ke `local.properties`, Gradle properties, atau secrets manager** sebelum distribusi; gunakan `BuildConfig`/`gradle.properties` agar kredensial tidak tersimpan di VCS maupun README. |
| **Konfigurasi jaringan** | `app/src/main/res/xml/network_security_config.xml` | Mengizinkan CA sistem & pengguna, serta cleartext traffic untuk endpoint internal StartWorking. Revisi sesuai kebijakan keamanan Anda. |
| **Daftar blokir iklan** | `app/src/main/assets/hosts` | Format daftar domain newline. Perbarui lewat script eksternal jika perlu. |
| **Speed Dial default** | `SharedPreferences` `SpeedDialPrefs` (diset saat runtime) | Gunakan UI tambahkan/edit di Home. |

## Data, Penyimpanan & Perizinan
- **Penyimpanan internal & eksternal** – Dataset CAPTCHA disimpan ke `Context.getExternalFilesDir("Datasets")/database`. Aplikasi meminta `READ/WRITE_EXTERNAL_STORAGE`, `MANAGE_EXTERNAL_STORAGE`, dan `REQUEST_INSTALL_PACKAGES`. Evaluasi ulang izin untuk rilisan publik.
- **Overlay & Automasi** – `StartWorking` menggunakan `SYSTEM_ALERT_WINDOW`, `WAKE_LOCK`, `DISABLE_KEYGUARD`, serta akses sensor (kamera, mikrofon, lokasi) sesuai fitur di nav drawer.
- **Notifikasi & Query apps** – Manifest meminta `POST_NOTIFICATIONS` (Android 13+) dan `QUERY_ALL_PACKAGES` guna enumerasi aplikasi; izinkan hanya jika fitur benar-benar memerlukannya.
- **SQLite** – File `browser.db` menyimpan riwayat, bookmark, sandi. Terapkan enkripsi/obfuskasi jika menyimpan data sensitif pengguna.

## Modul Dataset CAPTCHA
1. **Pengambilan**: `WebAppInterface` di `StartWorking` memantau DOM target dan mengirim Base64 gambar ke `CaptchaDataManager.saveCaptchaData()` bersamaan label manual.
2. **Penyimpanan**: Gambar disimpan sebagai file individual (`<hash>.<ext>`) dan meta-data dicatat dalam `captchas` SQLite.
3. **Viewer**: `CaptchaViewerFragment` (layout `captcha_viewer_db.xml`) menyediakan grid 2 kolom dengan paging, tarik-untuk-refresh, serta tombol Next/Prev.
4. **Ekspor**: Gunakan `ExternalDatabaseManager` jika perlu memuat database eksternal; modul menyiapkan tabel otomatis bila belum ada.
5. **Opsional**: Integrasi API privat (melalui OkHttp) bisa diarahkan ulang dengan memindahkan `SERVER_URL`/`API_KEY` ke konfigurasi aman.

## Teknis Tambahan
- **Address bar animasi** – `AddressBarUtils` menyelaraskan tampilan judul ↔ kolom URL dengan animasi alpha.
- **Slash command** – Input `/go kata`, `/dg kata`, `/yt`, `/ig`, `/fb`, `/rd`, `/br`, dll akan diarahkan otomatis ke mesin pencari/URL terkait (`UrlValidator`).
- **TabsAdapter** – Meng-highlight tab aktif dengan background berbeda (`cosmic_gradient_bg*`). Favicon diambil lewat `GlideFaviconFetcher` yang memanggil `https://www.google.com/s2/favicons`.
- **Split ABI** – Release build hanya menghasilkan APK `arm64-v8a`. Set `splits.abi.include` atau `universalApk true` jika membutuhkan ABI lain.
- **Network debugging** – `GeckoRuntimeSettings` mengaktifkan `remoteDebuggingEnabled(true)` dan `consoleOutput(true)` secara default.

## Pengujian & Kualitas
Saat ini belum ada suite pengujian khusus selain template JUnit/Instrumentation bawaan. Rekomendasi:
1. Tambahkan unit test untuk `UrlValidator`, `CalculatorSetress`, dan manajer database via `androidTest`.
2. Gunakan `./gradlew lint` & `./gradlew detekt` (bila diaktifkan) sebelum commit.
3. Untuk modul GeckoView, manfaatkan `adb logcat GeckoThread` guna menganalisis crash.

## Roadmap & Ide Pengembangan
- Melengkapi implementasi fragmen stub (Downloads, Files, Notes, Photos, Music, Videos, V2Ray, Console, Extensions).
- Memigrasikan seluruh konstanta rahasia (server URL/API key) ke konfigurasi aman dan menerapkan enkripsi pada `PasswordManager`.
- Menambah dukungan banyak bahasa dan tema gelap otomatis.
- Membuka dukungan ABI tambahan + App Bundle.
- Menambahkan test coverage & pipeline CI (GitHub Actions) untuk lint/unit test.
- Menyediakan dokumentasi UI/UX serta panduan kontribusi khusus.

## Lisensi
Proyek ini dirilis di bawah [MIT License](LICENSE).
