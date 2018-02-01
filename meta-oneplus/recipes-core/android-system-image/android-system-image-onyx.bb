require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "onyx"

PV = "20180128-1"

# Fixing QA errors for not matching architecture for the following binaries:
# - /system/etc/firmware/vidc.b00
# - /system/etc/firmware/vidcfw.elf
# - /system/etc/firmware/vidc.mdt
INSANE_SKIP_${PN} += "arch"
INSANE_SKIP_${PN}-dbg += "arch"
# Fixing QA no GNU_HASH error for all binaries
INSANE_SKIP_${PN} += "ldflags"
# Fixing QA relocations in .text error for all binaries
INSANE_SKIP_${PN} += "textrel"
# Skip already-stripped check, because it's prebuilt outside OE so we cannot easily prevent stripping it here
INSANE_SKIP_${PN} += "already-stripped"

INSANE_SKIP_${PN} += "dev-so"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "95799b41a85c3f65f2191ce8d041c78a"
SRC_URI[sha256sum] = "c3f9a92809e3a3dac0b65c7eaa830aaf86a861e1c73dc70f46a0fd1f823f398e"

FILES_${PN}-dbg += "\
  ${WORKDIR}/system/bin/.debug/linker \
  ${WORKDIR}/system/lib/.debug/*.so \
  ${WORKDIR}/system/lib/hw/.debug/*.so \
"
