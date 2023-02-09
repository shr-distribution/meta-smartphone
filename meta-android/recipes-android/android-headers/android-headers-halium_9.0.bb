ANDROID_HEADERS_BRANCH = "herrie/halium-9.0-proper"

require recipes-android/android-headers/android-headers.inc

PKGV .= "+gitr${SRCPV}"
SRCREV = "37049b151165c6bf8c383a1d8842906d973733c8"

ANDROID_API = "28"
