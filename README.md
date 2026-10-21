# Mürekkep — Android Tablet Not Alma Uygulaması (Kotlin)

Bu proje, Goodnotes tarzı tam teşekküllü bir dijital defter uygulamasıdır.

## Özellikler

1. **Google Kalem Motoru (ML Kit Digital Ink Recognition)**
   - `studio.murekkep.ink.GoogleInkEngine` sınıfı Türkçe (`tr`) ve çok dilli el yazısını tanır.
   - Stylus vuruşlarını zaman damgası ve basınç bilgileriyle işler.

2. **Palm Rejection (Avuç İçi Reddi)**
   - `studio.murekkep.input.PalmRejectionFilter` sınıfı.
   - Stylus algılandığı an parmakla çizim engellenir; avuç teması (büyük dokunma alanı + düşük basınç) elenir.

3. **Parmakla Yakınlaştır / Uzaklaştır (Pinch-to-Zoom & Pan)**
   - Jetpack Compose `detectTransformGestures` ile %25 - %400 arasında odak noktası sabit yakınlaştırma.

4. **PDF Açma & Üzerine Not Alma**
   - `studio.murekkep.pdf.PdfRendererHelper` sınıfı Android yerel `PdfRenderer` ile PDF sayfalarını render eder ve stylus ile üzerine not alınmasını sağlar.

5. **Klasörleme & Çoklu Sayfa**
   - Hiyerarşik klasör mimarisi, renk etiketleri, çizgili/kareli/noktalı/PDF zeminleri.

## APK Çıktısı Alma Adımları

Android APK dosyaları bir Android işletim sistemi ikilisi (`.apk`) olduğu için
Android SDK, Android Gradle Plugin ve Java JDK gerektirir.

### 1. Yöntem: Android Studio ile Tek Tıkla APK Alma (Önerilen)
1. Bu projeyi indirin (ZIP olarak).
2. **Android Studio** (Hedgehog / Iguana / Koala / 2024+) programını açın.
3. **File -> Open** diyerek açılan klasörü seçin.
4. Gradle senkronizasyonu tamamlandığında üst menüden:
   **Build -> Build Bundle(s) / APK(s) -> Build APK(s)**
   seçeneğine tıklayın.
5. Çıktı: `app/build/outputs/apk/debug/app-debug.apk` konumunda hazır olacaktır.
   Bu APK'yı doğrudan Android tabletinize yükleyebilirsiniz.

### 2. Yöntem: Terminal / Komut Satırı ile
```bash
# Proje dizininde
./gradlew assembleDebug
# Çıktı: app/build/outputs/apk/debug/app-debug.apk
```

### 3. Yöntem: GitHub Actions ile Ücretsiz Bulutta Otomatik APK Derleme
Projede yer alan `.github/workflows/build-apk.yml` dosyası sayesinde
projeyi bir GitHub deposuna gönderdiğinizde Actions sekmesinden
doğrudan indirilmeye hazır `.apk` dosyası üretilir.
