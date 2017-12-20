require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "onyx"

PV = "20171204-2"

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

SRC_URI = "http://build.webos-ports.org/halium-wop-7.1/halium-wop-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "7a0bc4afac084bd5035ace65f95de0d9"
SRC_URI[sha256sum] = "df93b14c9858efb8cb92789373f790748a6c625f9ecad7ac89b3f586805cb71b"

do_install_prepend() {
    rm -rf ${WORKDIR}/system/lib/
}

FILES_${PN}-dbg += "\
  ${WORKDIR}/system/bin/.debug/linker \
  ${WORKDIR}/system/lib/.debug/*.so \
  ${WORKDIR}/system/lib/hw/.debug/*.so \
"
