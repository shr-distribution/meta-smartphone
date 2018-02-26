require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "angler"

PV = "20180212-1"

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
SRC_URI[md5sum] = "c3dfb7c81d6c9c5d39dfe92a9acd2fa1"
SRC_URI[sha256sum] = "ace79f7f8914bacefb5e6ba4e379ca604aa110a78758cf79515a45bb526d1b86"

do_install_prepend() {
#    rm -rf ${WORKDIR}/system/lib/
rm ${WORKDIR}/system/vendor
}

FILES_${PN}-dbg += "\
  ${WORKDIR}/system/bin/.debug/linker \
  ${WORKDIR}/system/lib/.debug/*.so \
  ${WORKDIR}/system/lib/hw/.debug/*.so \
"
