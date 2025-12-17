# Firebase Setup Instructions

## خطوات إعداد Firebase Authentication

### 1. إضافة Web Client ID للـ Google Sign In

1. افتح [Firebase Console](https://console.firebase.google.com/)
2. اختر مشروعك
3. اذهب إلى **Project Settings** (⚙️)
4. في قسم **Your apps**، ابحث عن **Web app** (أو أنشئ واحدة إذا لم تكن موجودة)
5. انسخ **Web Client ID** (يبدأ بـ `xxxxx.apps.googleusercontent.com`)
6. افتح `app/src/main/res/values/strings.xml`
7. استبدل `YOUR_WEB_CLIENT_ID` بـ Web Client ID الذي نسخته:

```xml
<string name="default_web_client_id">YOUR_WEB_CLIENT_ID_HERE</string>
```

### 2. تفعيل Authentication Methods

1. في Firebase Console، اذهب إلى **Authentication**
2. اضغط على **Sign-in method**
3. فعّل:
   - ✅ **Email/Password**
   - ✅ **Google**

### 3. إضافة SHA-1 Fingerprint (لـ Google Sign In)

1. في Android Studio، افتح Terminal
2. نفذ الأمر:
   ```bash
   keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
   ```
3. انسخ **SHA-1** fingerprint
4. في Firebase Console > Project Settings > Your Apps > Android App
5. أضف SHA-1 fingerprint

### 4. التحقق من الإعدادات

- ✅ `google-services.json` موجود في `app/` folder
- ✅ Web Client ID مضاف في `strings.xml`
- ✅ Email/Password و Google Sign In مفعلين في Firebase Console
- ✅ SHA-1 fingerprint مضاف في Firebase Console

## ملاحظات

- إذا ظهرت رسالة خطأ "Network error" أو "socket failed"، تأكد من:
  - الاتصال بالإنترنت يعمل
  - Firebase Authentication مفعل في Console
  - `google-services.json` صحيح ومحدث

- إذا Google Sign In لا يعمل:
  - تأكد من إضافة Web Client ID
  - تأكد من إضافة SHA-1 fingerprint
  - تأكد من تفعيل Google Sign In في Firebase Console

