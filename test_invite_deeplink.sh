#!/bin/bash

# MedTracking Deep Link Test Script
# Bu script invite deep link'lerini test eder

EMULATOR="emulator-5556"
PACKAGE="com.medtracking.app"
ACTIVITY="${PACKAGE}.MainActivity"

echo "========================================"
echo "MedTracking Invite Deep Link Test"
echo "========================================"
echo ""
echo "Emulator: $EMULATOR"
echo "Package: $PACKAGE"
echo ""

# Test 1: Custom scheme (medtrack://)
echo "[TEST 1] Custom Scheme Deep Link (medtrack://)"
echo "Command: adb -s $EMULATOR shell am start -a android.intent.action.VIEW -d \"medtrack://invite?invitationId=TEST123&token=TOKEN456\""
echo ""
adb -s $EMULATOR shell am start -a android.intent.action.VIEW -d "medtrack://invite?invitationId=TEST123&token=TOKEN456"
echo ""
echo "Bekleyin..."
sleep 3
echo ""

# Test 2: HTTPS scheme
echo "[TEST 2] HTTPS Deep Link (https://medtrack.app/invite)"
echo "Command: adb -s $EMULATOR shell am start -a android.intent.action.VIEW -d \"https://medtrack.app/invite?invitationId=PROD789&token=TOKENPROD\""
echo ""
adb -s $EMULATOR shell am start -a android.intent.action.VIEW -d "https://medtrack.app/invite?invitationId=PROD789&token=TOKENPROD"
echo ""
echo "Bekleyin..."
sleep 3
echo ""

echo "========================================"
echo "Test tamamlandı!"
echo ""
echo "Beklenen davranış:"
echo "1. Eğer kullanıcı giriş yapmamışsa:"
echo "   - Login ekranı açılmalı"
echo "   - Login sonrası otomatik olarak InviteAcceptScreen'e yönlendirilmeli"
echo "   - Ekranda invitationId ve token görünmeli"
echo ""
echo "2. Eğer kullanıcı zaten giriş yaptıysa:"
echo "   - Direkt InviteAcceptScreen açılmalı"
echo "   - Ekranda invitationId ve token görünmeli"
echo "========================================"

