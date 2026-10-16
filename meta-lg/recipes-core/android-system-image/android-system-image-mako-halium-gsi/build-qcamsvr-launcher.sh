#!/bin/sh
# Rebuilds mm-qcamera-daemon-launcher from qcamsvr_launcher.c with the clang, bionic headers, crt objects
# and libraries of a built Halium 16.0 tree (lunch halium_arm). Not run by the recipe, which ships the
# result: bitbake has no bionic toolchain.
#
#   ./build-qcamsvr-launcher.sh /path/to/halium-16.0
set -e
H=${1:?usage: $0 <halium 16.0 tree>}
CLANG=$H/prebuilts/clang/host/linux-x86/clang-r574158/bin/clang
I=$H/out/soong/.intermediates
$CLANG --target=armv7a-linux-androideabi28 -march=armv7-a -mfpu=neon -mfloat-abi=softfp \
    -O2 -Wall -Wextra -Werror -fPIE -fstack-protector-strong -D_FORTIFY_SOURCE=2 \
    -nostdinc -isystem $H/bionic/libc/include -isystem $H/bionic/libc/kernel/uapi \
    -isystem $H/bionic/libc/kernel/uapi/asm-arm -isystem $H/bionic/libc/kernel/android/uapi \
    -isystem $H/system/logging/liblog/include \
    -isystem "$(dirname "$($CLANG -print-resource-dir)/include/.")" \
    -c qcamsvr_launcher.c -o qcamsvr_launcher.o
$CLANG --target=armv7a-linux-androideabi28 -fuse-ld=lld -nostdlib -pie \
    -Wl,-dynamic-linker,/system/bin/linker -Wl,-z,relro -Wl,-z,now -Wl,--hash-style=gnu -Wl,--build-id=sha1 \
    $I/bionic/libc/crtbegin_dynamic/android_arm_armv7-a-neon/crtbegin_dynamic.o qcamsvr_launcher.o \
    $I/bionic/libdl/libdl/android_arm_armv7-a-neon_shared/libdl.so \
    $I/bionic/linker/ld-android/android_arm_armv7-a-neon_shared/ld-android.so \
    $I/system/logging/liblog/liblog/android_arm_armv7-a-neon_shared/liblog.so \
    $I/bionic/libc/libc/android_arm_armv7-a-neon_shared/libc.so \
    $($CLANG --target=armv7a-linux-androideabi -print-libgcc-file-name) \
    $I/bionic/libc/crtend_android/android_arm_armv7-a-neon/crtend_android.o -o mm-qcamera-daemon-launcher
rm -f qcamsvr_launcher.o
