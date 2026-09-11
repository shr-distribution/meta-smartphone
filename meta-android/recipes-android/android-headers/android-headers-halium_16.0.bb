require recipes-android/android-headers/android-headers.inc

# Halium's own `halium-16.0` branch is a dead end: it is byte-identical to
# `halium-11.0` (same tree b5c701a34c08, same tip 217a38e92cc6) and still
# declares ANDROID_VERSION_MAJOR 11, so selecting it changes nothing. The real
# Android 16 headers live on Herrie's branch, which is where the actual
# `Android 16 headers for halium-16.0` commit (c5b1c8f) landed.
#
# This matters beyond the version macro. libhybris gates its Android 16 support
# on `ANDROID_VERSION_MAJOR >= 16`, so with the 11.0 headers every one of those
# code paths compiles out silently. And three headers genuinely change between
# 11.0 and this branch, all of them on the libhybris <-> vendor-EGL interop
# surface:
#
#   system/window.h        ANativeWindow - what libGLES_mali.so is handed
#   system/graphics.h      pixel formats
#   vndk/hardware_buffer.h AHardwareBuffer / gralloc interop
#
# (hardware/gralloc.h and hardware/hwcomposer2.h are identical between the two,
# which is why an earlier ABI-safety check that only looked at those concluded,
# wrongly, that the bump was a no-op.)
ANDROID_HEADERS_BRANCH = "herrie/halium-16.0"
SRCREV = "ad5912eee8bb56968af63de46ba3b594510ead3f"

ANDROID_API = "36"
