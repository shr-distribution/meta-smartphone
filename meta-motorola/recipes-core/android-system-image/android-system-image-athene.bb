require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "athene"

PV = "20171230-2"

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
SRC_URI[md5sum] = "1103f36923a10e21fa84c494f58e66a8"
SRC_URI[sha256sum] = "195b66b61340be1015628c90106d537c071476b1acb6946bfa2ac9be11a577d2"

FILES_${PN}-dbg += "\
  ${WORKDIR}/system/bin/.debug/linker \
  ${WORKDIR}/system/lib/.debug/*.so \
  ${WORKDIR}/system/lib/hw/.debug/*.so \
"
