ANDROID_HEADERS_BRANCH = "herrie/halium-9.0-proper"

require recipes-android/android-headers/android-headers.inc

SRCREV = "37049b151165c6bf8c383a1d8842906d973733c8"

SRC_URI += "file://0003-system-audio.h-include-string.h-for-strdup.patch"

ANDROID_API = "28"
