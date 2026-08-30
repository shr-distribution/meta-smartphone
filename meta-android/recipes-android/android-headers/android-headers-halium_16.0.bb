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
# sargo.conf and halium-arm64.conf both select this, and not by the usual rule
# that android-headers describes the *vendor* HAL interface - that would say
# 12.0 for sargo's Android 12L vendor, which is the line Droidian takes.
# libhybris gates its Android 16 linker support on ANDROID_VERSION_MAJOR from
# this package: against older headers the A16 half of bionic_elf_tls.cpp
# compiles out while linker_finalize_static_tls() still calls into it, so q.so
# links with StaticTlsLayout::finish_layout() undefined and every hybris
# consumer dies at dlopen. The headers therefore have to track the GSI.
#
# Note the legacy HAL 1.0 headers AOSP 14 retired are still gone here, so
# anything built against a legacy HAL cannot compile against this set.
ANDROID_HEADERS_BRANCH = "herrie/halium-16.0"

require recipes-android/android-headers/android-headers.inc

SRCREV = "ad5912eee8bb56968af63de46ba3b594510ead3f"

# Android 16 is API level 36. Only names the include/android-${ANDROID_API}
# symlink.
ANDROID_API = "36"
