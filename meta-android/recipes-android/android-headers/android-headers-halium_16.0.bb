# As with 13.0 and 14.0, upstream has no halium-16.0 branch - Halium's
# android-headers repository stops at halium-11.0. This set was generated from a
# halium-16.0 tree (lineage-23.2 / android-16.0.0_r4) with libhybris'
# utils/extract-headers.sh, taken from the Android 16 update on
# Herrie82/libhybris herrie/android16-tls.
#
# Two things about regenerating it. The script cannot autodetect the version on
# a 16 tree - it reads build/core/version_defaults.mk, and the version now comes
# from the release config - so it needs -v 16.0.0 passed by hand. And it copies
# directory arguments with a plain cp, so system/audio_effects is skipped and
# has to be added afterwards; the audio_effect.h beside it includes from there.
#
# Nothing selects this, and sargo deliberately does not: android-headers
# describes the *vendor* HAL interface rather than the system image, and sargo's
# vendor is Android 11, so it stays on 11.0 whichever GSI it runs. Droidian does
# the same. This is here for a tree whose vendor is Android 16 - and note that
# the legacy HAL 1.0 headers AOSP 14 retired are still gone here, so anything
# built against a legacy HAL cannot compile against this set.
ANDROID_HEADERS_BRANCH = "herrie/halium-16.0"

require recipes-android/android-headers/android-headers.inc

SRCREV = "ad5912eee8bb56968af63de46ba3b594510ead3f"

# Android 16 is API level 36. Only names the include/android-${ANDROID_API}
# symlink.
ANDROID_API = "36"
