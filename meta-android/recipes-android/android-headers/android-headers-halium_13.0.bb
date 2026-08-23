# The default branch for this version would be halium-13.0, which upstream does
# not have - Halium's android-headers repository stops at halium-11.0. This set
# was generated from a halium-13.0 tree with libhybris' own
# utils/extract-headers.sh, plus the log/, camera/ and media/ directories that
# script does not emit, taken from their canonical in-tree sources.
#
# Nothing selects this yet, and sargo deliberately does not: android-headers
# describes the *vendor* HAL interface rather than the system image, and sargo's
# vendor is Android 11, so sargo.conf pins 11.0 whichever GSI it runs. Droidian
# does the same, still build-depending on android-headers-30 for Halium 12 and
# 13. This is here for a tree whose vendor is Android 13.
ANDROID_HEADERS_BRANCH = "herrie/halium-13.0"

require recipes-android/android-headers/android-headers.inc

SRCREV = "87ed817d7add2d18827adb28880382408eeb21d0"

# Android 13 is API level 33. Only names the include/android-${ANDROID_API}
# symlink.
ANDROID_API = "33"
