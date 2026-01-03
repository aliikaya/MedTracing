# Deep Link Test Rehberi

## 1. ADB Komutu ile Test (En Hızlı Yöntem)

### Terminal'den test etmek için:

```bash
adb shell am start -W -a android.intent.action.VIEW \
  -d "https://medtrack.app/invite?invitationId=test123&token=token456" \
  com.medtracking.app
```

### Test Script'i Kullanmak:

1. Script'e çalıştırma izni verin:
```bash
chmod +x test_deeplink.sh
```

2. Script'i çalıştırın:
```bash
./test_deeplink.sh test123 token456
```

Veya parametresiz (default değerlerle):
```bash
./test_deeplink.sh
```

---

## 2. Android Studio'dan Test

### Logcat ile Test Edin:

1. Android Studio'da Logcat'i açın
2. Filtre: `deep` veya `invite` veya `MainActivity`
3. ADB komutunu çalıştırın
4. Logcat'te şunları arayın:
   - "extractInviteRoute" logları
   - "pendingDeepLink" güncellemeleri
   - Navigation logları

### Debugging için Log Ekleyin:

MainActivity.kt'de `extractInviteRoute` fonksiyonuna log ekleyebilirsiniz:

```kotlin
private fun extractInviteRoute(intent: Intent?): String? {
    val data: Uri? = intent?.data
    android.util.Log.d("MainActivity", "Deep link data: $data")
    
    if (data != null && data.scheme == "https" && data.host == "medtrack.app") {
        val path = data.path
        android.util.Log.d("MainActivity", "Path: $path")
        
        if (path?.startsWith("/invite") == true) {
            val invitationId = data.getQueryParameter("invitationId")
            val token = data.getQueryParameter("token")
            android.util.Log.d("MainActivity", "InvitationId: $invitationId, Token: $token")
            
            if (!invitationId.isNullOrBlank() && !token.isNullOrBlank()) {
                val route = Screen.Invite.createRoute(invitationId, token)
                android.util.Log.d("MainActivity", "Route: $route")
                return route
            }
        }
    }
    return null
}
```

---

## 3. Farklı Senaryoları Test Etme

### Senaryo 1: Uygulama Kapalıyken Test

1. Uygulamayı tamamen kapatın (Recent apps'ten swipe out)
2. ADB komutunu çalıştırın
3. Uygulama açılmalı ve HandleInviteScreen görünmeli

### Senaryo 2: Uygulama Açıkken Test (onNewIntent)

1. Uygulamayı açık tutun (herhangi bir ekranda)
2. ADB komutunu çalıştırın
3. HandleInviteScreen görünmeli (onNewIntent tetiklenmeli)

### Senaryo 3: Authenticated Olmadan Test

1. Uygulamadan logout yapın
2. ADB komutunu çalıştırın
3. "Giriş Yap" butonu görünmeli

### Senaryo 4: Authenticated Olarak Test

1. Uygulamaya login yapın
2. ADB komutunu çalıştırın
3. Loading → Success → Profiles ekranına yönlendirme olmalı

---

## 4. Gerçek URL ile Test (Opsiyonel)

### Web sayfası oluşturun (test.html):

```html
<!DOCTYPE html>
<html>
<head>
    <title>Deep Link Test</title>
</head>
<body>
    <h1>Deep Link Test</h1>
    <a href="https://medtrack.app/invite?invitationId=real123&token=real456">
        Profil Davet Linki
    </a>
    
    <br><br>
    
    <button onclick="window.location.href='https://medtrack.app/invite?invitationId=button123&token=button456'">
        Buton ile Test
    </button>
</body>
</html>
```

1. Bu HTML dosyasını telefonunuzda açın
2. Link'e tıklayın
3. Uygulama açılmalı

---

## 5. Debugging Checklist

Deep link çalışmıyorsa kontrol edin:

- [ ] AndroidManifest.xml'de intent-filter doğru mu?
  ```xml
  <intent-filter>
      <action android:name="android.intent.action.VIEW" />
      <category android:name="android.intent.category.DEFAULT" />
      <category android:name="android.intent.category.BROWSABLE" />
      <data
          android:scheme="https"
          android:host="medtrack.app"
          android:pathPrefix="/invite" />
  </intent-filter>
  ```

- [ ] NavGraph'te deep link tanımı var mı?
  ```kotlin
  deepLinks = listOf(
      navDeepLink {
          uriPattern = "https://medtrack.app/invite?invitationId={invitationId}&token={token}"
      }
  )
  ```

- [ ] Logcat'te hata var mı?
- [ ] ADB komutu doğru mu? (package name: com.medtracking.app)
- [ ] Emulator/cihaz bağlı mı? (`adb devices` ile kontrol edin)

---

## 6. Hızlı Test Komutları

```bash
# Cihazları listele
adb devices

# Uygulama açık mı kontrol et
adb shell pidof com.medtracking.app

# Uygulamayı kapat
adb shell am force-stop com.medtracking.app

# Deep link test
adb shell am start -W -a android.intent.action.VIEW \
  -d "https://medtrack.app/invite?invitationId=test123&token=token456" \
  com.medtracking.app

# Logcat'te deep link loglarını izle
adb logcat | grep -i "deep\|invite\|MainActivity"
```

---

## 7. Beklenen Sonuçlar

### Başarılı Test:
- ✅ Uygulama açılır
- ✅ Splash screen gösterilir (eğer kapalıysa)
- ✅ HandleInviteScreen açılır
- ✅ Loading → Success/Error mesajı gösterilir
- ✅ Authenticated ise Profiles ekranına yönlendirilir

### Hata Durumları:
- ❌ "App not found" → Package name yanlış
- ❌ "No Activity found" → Intent-filter yanlış
- ❌ Boş sayfa → Navigation route yanlış veya query parameter parse edilemedi
- ❌ Uygulama açılmıyor → Deep link tanımı eksik





