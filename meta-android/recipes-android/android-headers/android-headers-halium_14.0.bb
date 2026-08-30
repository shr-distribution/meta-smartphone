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
# still built against a legacy HAL cannot compile with this set, which is the
# concrete argument for selecting the header version by *vendor* rather than by
# GSI - and it is why Droidian still build-depends on android-headers-30 for
# Halium 12-14. This layer goes the other way: libhybris reads
# ANDROID_VERSION_MAJOR from this package to pick a linker generation, so
# headers older than the GSI fail dlopen for every hybris consumer, and
# sargo.conf tracks its GSI instead. The retired-HAL fallout is the price.
ANDROID_HEADERS_BRANCH = "herrie/halium-14.0"

require recipes-android/android-headers/android-headers.inc

# The audio_effects fix (89db8ba) is now on Halium/android-headers, so this
# uses the github SRC_URI from android-headers.inc.
SRCREV = "89db8ba815097af8a9c7650d2d5eefb378248839"

# Android 14 is API level 34. Only names the include/android-${ANDROID_API}
# symlink.
ANDROID_API = "34"
