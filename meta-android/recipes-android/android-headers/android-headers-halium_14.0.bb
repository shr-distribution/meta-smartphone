# As with 13.0, upstream has no halium-14.0 branch - Halium's android-headers
# repository stops at halium-11.0. This set was generated from a halium-14.0
# tree with libhybris' utils/extract-headers.sh, which needed -v 14.0.0 passed
# by hand because AOSP 14 no longer has build/core/version_defaults.mk for it
# to read the version from. The log/, camera/ and media/ directories and two
# stray headers the script does not emit were added from canonical in-tree
# sources.
#
# AOSP 14 retired the legacy HAL 1.0 headers - hardware/lights.h, thermal.h,
# radio.h, consumerir.h and friends are simply gone, replaced by AIDL. Anything
# still built against a legacy HAL cannot compile with this set, which is a
# concrete reason to select the header version by *vendor* rather than by GSI.
# sargo's vendor is Android 11, so sargo.conf keeps 11.0 whichever GSI it runs;
# Droidian likewise still build-depends on android-headers-30 for Halium 12-14.
ANDROID_HEADERS_BRANCH = "herrie/halium-14.0"

require recipes-android/android-headers/android-headers.inc

SRCREV = "dbe52156af1206a353f76148c4fbec8bc5e65415"

# Android 14 is API level 34. Only names the include/android-${ANDROID_API}
# symlink.
ANDROID_API = "34"
