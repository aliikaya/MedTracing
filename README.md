# medTracking - İlaç ve Vitamin Takip Uygulaması

## Genel Bakış

MedTracking, kullanıcıların ilaç ve vitamin alımlarını takip etmelerine olanak tanıyan offline-first bir Android uygulamasıdır. Kendiniz, çocuklarınız veya yaşlı ebeveynleriniz için ilaç takibi yapabilirsiniz.

## Faz 1 - Proje İskeleti

Bu sürüm, uygulamanın temel mimari iskeletini içerir:

### Teknoloji Stack

- **Dil**: Kotlin
- **UI**: Jetpack Compose
- **Mimari**: Clean Architecture + MVVM + Repository Pattern
- **Dependency Injection**: Hilt
- **Veritabanı**: Room (offline-first)
- **Async**: Coroutines + Flow
- **Navigation**: Jetpack Compose Navigation
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34

### Proje Yapısı

```
app/
├── data/                      # Data Katmanı
│   ├── local/
│   │   ├── entity/           # Room Entity'leri
│   │   ├── dao/              # Room DAO'ları
│   │   └── converter/        # Type Converter'lar
│   ├── mapper/               # Domain-Entity mapper'lar
│   └── repository/           # Repository implementasyonları
├── domain/                    # Domain Katmanı
│   ├── model/                # Domain modelleri
│   ├── repository/           # Repository arayüzleri
│   └── usecase/              # Use case'ler
├── presentation/             # Presentation Katmanı
│   ├── profiles/             # Profiller ekranı
│   ├── today/                # Bugünkü ilaçlar ekranı
│   └── navigation/           # Navigation
├── di/                       # Hilt modülleri
└── ui/theme/                 # UI tema dosyaları
```

### Domain Modelleri

1. **Profile**: İlaçları takip edilen kişi (Ben, Annem, Çocuğum, vb.)
2. **Medication**: İlaç veya takviye bilgisi
3. **Intake**: Planlanmış veya gerçekleşmiş ilaç alımı

### Özellikler (Faz 1)

✅ Temiz mimari yapısı kuruldu
✅ Room veritabanı yapılandırıldı
✅ Hilt DI entegrasyonu
✅ Repository pattern implementasyonu
✅ Use case'ler
✅ Placeholder UI ekranları
✅ Navigation kurulumu

### Henüz İmplemente Edilmedi

⏳ Bildirimler ve hatırlatıcılar
⏳ Stok takibi
⏳ İstatistikler ve raporlar
⏳ Gerçek UI/UX tasarımı
⏳ Veri validasyonu
⏳ Unit ve UI testleri

## Kurulum

### Gereksinimler

- Android Studio Hedgehog (2023.1.1) veya üzeri
- JDK 17
- Android SDK 34
- Gradle 8.2

### Projeyi Çalıştırma

1. Projeyi Android Studio'da açın
2. Gradle sync'i bekleyin
3. Emulator veya fiziksel cihaz seçin
4. Run'a basın

### Build

```bash
./gradlew build
```

### Clean Build

```bash
./gradlew clean build
```

## Mimari Notları

### Clean Architecture Katmanları

- **Presentation**: UI ve ViewModel'ler. Android framework'e bağımlı.
- **Domain**: İş mantığı. Framework'den bağımsız, pure Kotlin.
- **Data**: Veri kaynakları ve repository implementasyonları.

### Veri Akışı

```
UI ← ViewModel ← UseCase ← Repository ← DAO ← Room Database
```

### Dependency Kuralları

- Presentation → Domain ← Data
- Domain katmanı hiçbir katmana bağımlı değil
- Data ve Presentation katmanları Domain'e bağımlı

## Sonraki Adımlar (Faz 2)

1. Profil ekleme/düzenleme UI'ı
2. İlaç ekleme/düzenleme formu
3. Intake oluşturma mantığı (schedule'dan otomatik)
4. Bildirim sistemi
5. Dashboard ve istatistikler
6. Stok yönetimi
7. Veri backup/restore

## Lisans

Bu proje bir örnek proje olup, eğitim amaçlıdır.

