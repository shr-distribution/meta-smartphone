require recipes-android/android-headers/android-headers.inc

SRCREV = "2c6ac3dcc4f8db593dd69906b0ec22822abfed91"

# Android 11 is API level 30; this said 28, which is Android 9. It only names
# the include/android-${ANDROID_API} symlink, so nothing was broken by it.
ANDROID_API = "30"
