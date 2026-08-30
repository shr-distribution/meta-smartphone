# The default branch for this version would be halium-13.0, which upstream does
# not have - Halium's android-headers repository stops at halium-11.0. This set
# was generated from a halium-13.0 tree with libhybris' own
# utils/extract-headers.sh, plus the log/, camera/ and media/ directories that
# script does not emit, taken from their canonical in-tree sources.
#
# Nothing selects this. Note the rule this layer follows is not the obvious one:
# android-headers does describe the *vendor* HAL interface, and by that argument
# sargo would pin 12.0 for its Android 12L vendor - which is what Droidian does,
# still build-depending on android-headers-30 for Halium 12 and 13. But
# libhybris also reads ANDROID_VERSION_MAJOR from this package to decide which
# linker generation to build, so headers older than the GSI break dlopen
# outright. sargo.conf therefore tracks its GSI. This set is here for a tree on
# Android 13 either way.
ANDROID_HEADERS_BRANCH = "herrie/halium-13.0"

require recipes-android/android-headers/android-headers.inc

SRCREV = "87ed817d7add2d18827adb28880382408eeb21d0"

# Android 13 is API level 33. Only names the include/android-${ANDROID_API}
# symlink.
ANDROID_API = "33"
