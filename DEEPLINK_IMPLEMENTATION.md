# Deep Link Implementasyonu - Invite Flow

## Özet

`medtrack://invite?invitationId=...&token=...` deep link'i için tam bir akış implementasyonu yapıldı.

## Yapılan Değişiklikler

### 1. MainActivity.kt
- `extractInviteRoute()` fonksiyonu güncellendi
- Hem `https://medtrack.app/invite` hem de `medtrack://invite` scheme'lerini destekliyor
- Deep link geldiğinde:
  - Kullanıcı authenticated ise → direkt navigate
  - Değilse → MainViewModel'e pending olarak kaydediliyor

### 2. MainViewModel.kt
- `pendingDeepLink` state eklendi
- `setPendingDeepLink()` - deep link'i bekletmeye al
- `consumePendingDeepLink()` - pending deep link'i tüket ve döndür
- Login sonrası otomatik yönlendirme için kullanılıyor

### 3. NavGraph.kt
- `medtrack://invite?invitationId={invitationId}&token={token}` deep link pattern'i eklendi
- Login ve Register ekranlarında `onSuccess` callback'leri güncellendi
- Pending deep link varsa login/register sonrası otomatik olarak invite ekranına yönlendiriliyor

### 4. AndroidManifest.xml
Zaten mevcuttu, değişiklik yapılmadı:
```xml
<!-- Custom Scheme Deep Link -->
<data
    android:scheme="medtrack"
    android:host="invite" />
```

### 5. HandleInviteScreen.kt
Zaten mevcuttu ve güzel bir şekilde implement edilmişti:
- Auth kontrolü yapıyor
- Authenticated değilse login'e yönlendiriyor
- Authenticated ise daveti kabul ediyor

## Akış Diyagramı

### Senaryo 1: Kullanıcı Giriş Yapmamış

```
Deep Link (medtrack://invite?...)
    ↓
MainActivity.extractInviteRoute()
    ↓
Auth kontrolü → NULL
    ↓
MainViewModel.setPendingDeepLink()
    ↓
Login Screen açılır
    ↓
Kullanıcı giriş yapar
    ↓
Login success callback
    ↓
MainViewModel.consumePendingDeepLink()
    ↓
HandleInviteScreen'e navigate
    ↓
Davet kabul edilir
```

### Senaryo 2: Kullanıcı Zaten Giriş Yapmış

```
Deep Link (medtrack://invite?...)
    ↓
MainActivity.extractInviteRoute()
    ↓
Auth kontrolü → OK
    ↓
Direkt HandleInviteScreen'e navigate
    ↓
Davet kabul edilir
```

## Test Komutları

### Manuel Test
```bash
# Custom scheme
adb -s emulator-5556 shell am start -a android.intent.action.VIEW -d "medtrack://invite?invitationId=TEST&token=TEST"

# HTTPS scheme
adb -s emulator-5556 shell am start -a android.intent.action.VIEW -d "https://medtrack.app/invite?invitationId=TEST&token=TEST"
```

### Script ile Test
```bash
./test_invite_deeplink.sh
```

## Beklenen Sonuçlar

### Test 1: Giriş Yapılmamış
1. Deep link komutu çalıştırılır
2. Uygulama açılır ve Login ekranı gösterilir
3. Kullanıcı email/password ile giriş yapar
4. Login başarılı olunca otomatik olarak HandleInviteScreen açılır
5. Ekranda invitation parametreleri gösterilir
6. Davet işleme alınır

### Test 2: Giriş Yapılmış
1. Deep link komutu çalıştırılır
2. Uygulama açılır ve direkt HandleInviteScreen gösterilir
3. Ekranda invitation parametreleri gösterilir
4. Davet işleme alınır

## Dosya Yapısı

```
app/src/main/java/com/medtracking/app/
├── MainActivity.kt                     ✅ Güncellendi
├── MainViewModel.kt                    ✅ Güncellendi
├── presentation/
│   ├── navigation/
│   │   ├── NavGraph.kt                ✅ Güncellendi
│   │   └── Screen.kt                  ✓ Zaten hazırdı
│   ├── auth/
│   │   ├── LoginScreen.kt             ✓ Değişiklik gerekmedi
│   │   └── LoginViewModel.kt          ✓ Değişiklik gerekmedi
│   └── invite/
│       ├── HandleInviteScreen.kt      ✓ Zaten hazırdı
│       └── HandleInviteViewModel.kt   ✓ Zaten hazırdı
```

## Notlar

- Sistem tamamen compose navigation'ın deep link desteğini kullanıyor
- MainActivity'de minimal parsing yapılıyor, asıl iş NavGraph seviyesinde
- Auth state ve pending deep link yönetimi MainViewModel'de merkezi olarak yapılıyor
- HandleInviteScreen zaten güzel bir şekilde implemente edilmişti
- Hem custom scheme (`medtrack://`) hem HTTPS scheme (`https://`) destekleniyor

## İleride Eklenebilecekler

- Deep link analytics (hangi linkten geldi, ne zaman, vb.)
- Deep link validasyonu (expired token, invalid invitation, vb.)
- Deep link için özel splash/loading ekranı
- Universal Links desteği (iOS için App Links eşdeğeri)

