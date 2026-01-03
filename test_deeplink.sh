#!/bin/bash

# Deep link test script
# Kullanım: ./test_deeplink.sh invitationId token

INVITATION_ID=${1:-"test123"}
TOKEN=${2:-"token456"}

echo "Testing deep link: https://medtrack.app/invite?invitationId=$INVITATION_ID&token=$TOKEN"

adb shell am start -W -a android.intent.action.VIEW \
  -d "https://medtrack.app/invite?invitationId=$INVITATION_ID&token=$TOKEN" \
  com.medtracking.app

echo "Deep link sent to app"





