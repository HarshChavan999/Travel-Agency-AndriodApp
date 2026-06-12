#!/bin/bash
#
# Firebase App Distribution Deployment Script
# Deploys the built APK to testers via Firebase App Distribution
#
# Prerequisites:
#   1. Firebase CLI installed (npm install -g firebase-tools)
#   2. Logged in to Firebase (firebase login)
#      OR use a service account key file
#
# Usage:
#   ./deploy-to-firebase.sh                   # Interactive (uses firebase login)
#   FIREBASE_TOKEN="<token>" ./deploy-to-firebase.sh  # CI/CD (uses token)
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APK_PATH="$SCRIPT_DIR/app/build/outputs/apk/release/app-release.apk"
APP_ID="1:387994411670:android:3898d613d93cf83e9f18b7"
PROJECT_ID="travel-agent-management-29c27"
TESTER_GROUPS="testers"
TESTERS="harshnpc21@gmail.com"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE} Firebase App Distribution Upload${NC}"
echo -e "${BLUE}========================================${NC}"

# Step 0: Check prerequisites
echo ""
echo "📋 Prerequisites Check"
echo "----------------------"

if ! command -v firebase &> /dev/null; then
    echo -e "${RED}❌ Firebase CLI is not installed.${NC}"
    echo "   Install: npm install -g firebase-tools"
    exit 1
fi
echo -e "${GREEN}✅ Firebase CLI found: $(firebase --version)${NC}"

if [ ! -f "$APK_PATH" ]; then
    echo -e "${RED}❌ APK not found at: $APK_PATH${NC}"
    echo ""
    echo "📦 Build Options:"
    echo "   Option 1: ./gradlew assembleRelease"
    echo "   Option 2: ./gradlew assembleDebug"
    echo "   Option 3: ./gradlew bundleRelease (for Play Store AAB)"
    echo ""
    echo "   Example: cd Travel-Agency-AndriodApp && ./gradlew assembleRelease"
    exit 1
fi
echo -e "${GREEN}✅ APK found: $APK_PATH${NC}"

# Step 1: Determine authentication method
echo ""
echo "🔐 Authentication"
echo "----------------"

AUTH_METHOD="login"
if [ -n "$FIREBASE_TOKEN" ]; then
    AUTH_METHOD="token"
elif [ -n "$FIREBASE_SERVICE_ACCOUNT_KEY" ]; then
    AUTH_METHOD="sa"
fi

case "$AUTH_METHOD" in
    "token")
        echo -e "${GREEN}✅ Using FIREBASE_TOKEN environment variable${NC}"
        ;;
    "sa")
        echo "$FIREBASE_SERVICE_ACCOUNT_KEY" > /tmp/firebase-sa-key.json
        export GOOGLE_APPLICATION_CREDENTIALS="/tmp/firebase-sa-key.json"
        echo -e "${GREEN}✅ Using FIREBASE_SERVICE_ACCOUNT_KEY environment variable${NC}"
        ;;
    *)
        echo -e "${YELLOW}⚠️  No authentication method set.${NC}"
        echo ""
        echo "   You can authenticate by:"
        echo "   1. Running: firebase login"
        echo "   2. Setting FIREBASE_TOKEN env var (from CI/CD)"
        echo "   3. Setting FIREBASE_SERVICE_ACCOUNT_KEY env var"
        echo ""
        echo -e "${BLUE}Attempting firebase login...${NC}"
        firebase login --no-localhost
        ;;
esac

# Step 2: Upload APK
echo ""
echo "📤 Uploading APK to Firebase App Distribution"
echo "---------------------------------------------"
echo "   App ID: $APP_ID"
echo "   Groups: $TESTER_GROUPS"
echo "   Testers: $TESTERS"

# Release notes
RELEASE_NOTES="v2.0 - Remote Configuration System

New Features:
• Remote Config integration (Firebase + Firestore)
• Maintenance mode toggle (control from Firebase Console)
• Feature flags (enable/disable chat, booking, wishlist, etc.)
• Force update & optional update prompts
• Dynamic announcements
• Configurable UI strings (search placeholder, banner, etc.)
• Version requirement management

Changes:
• Added ConfigManager singleton
• Added ConfigViewModel
• Added 4 dialog types (Maintenance, Force Update, Optional Update, Announcement)
• Added Firebase Remote Config dependency
• Updated app version to v2.0 (versionCode 2)

Firebase Console Setup Required:
1. Enable Remote Config with keys from REMOTE_CONFIG_REPORT.md
2. Create Firestore collection: app_config / global
3. Update security rules"

# Upload using Firebase CLI
echo ""
firebase appdistribution:distribute "$APK_PATH" \
    --app "$APP_ID" \
    --groups "$TESTER_GROUPS" \
    --release-notes "$RELEASE_NOTES" \
    2>&1

UPLOAD_EXIT_CODE=$?

# Step 3: Result
echo ""
echo "📊 Upload Result"
echo "----------------"

if [ $UPLOAD_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✅ APK uploaded successfully!${NC}"
    echo ""
    echo "   Testers will receive an email notification to download the app."
    echo "   View in Firebase Console:"
    echo "   https://console.firebase.google.com/project/startsmart-8789e/appdistribution"
else
    echo -e "${RED}❌ Upload failed (exit code: $UPLOAD_EXIT_CODE)${NC}"
    echo ""
    echo "   Common fixes:"
    echo "   1. Run 'firebase login' to authenticate"
    echo "   2. Ensure you have Firebase App Distribution Admin role"
    echo "      - Firebase Console > Project Settings > IAM > Add member"
    echo "      - Role: Firebase App Distribution Admin"
    echo "   3. Or use a service account:"
    echo "      - Create SA in GCP Console > IAM > Service Accounts"
    echo "      - Grant: Firebase App Distribution Admin"
    echo "      - export FIREBASE_SERVICE_ACCOUNT_KEY='<json>'"
    echo ""
    echo "   Alternatively, upload manually:"
    echo "   https://console.firebase.google.com/project/startsmart-8789e/appdistribution"
fi

# Clean up
if [ "$AUTH_METHOD" = "sa" ]; then
    rm -f /tmp/firebase-sa-key.json
fi

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE} Deployment Script Complete${NC}"
echo -e "${BLUE}========================================${NC}"
exit $UPLOAD_EXIT_CODE