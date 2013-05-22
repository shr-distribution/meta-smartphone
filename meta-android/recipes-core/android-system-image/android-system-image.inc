DESCRIPTION = "Tiny Android system to leverage android hardware drivers through libhybris"
LICENSE = "Apache-2.0 & Properietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

# don't run package_do_shlibs on our packages as this will add a RDEPENDS from ${PN} to
# ${PN}-dbg
EXCLUDE_FROM_SHLIBS = "1"

PACKAGE_ARCH = "${MACHINE_ARCH}"

RPROVIDES_${PN} += "virtual/android-system-image"

INC_PR = "r4"

do_install() {
    cp -rv ${WORKDIR}/system ${D}
    ln -sf /system/vendor ${D}/vendor

    # The binaries are without any permission set so we have to restore them here
    chmod -R 0755 ${D}/system/bin || true
    chmod -R 0755 ${D}/system/xbin || true
    chmod -R 0755 ${D}/system/vendor/bin || true

    install -d ${D}/system/symbols
    cp -rv ${WORKDIR}/symbols ${D}/system

    install -d ${D}${base_sbindir}
    install -m 0755 ${WORKDIR}/root/init ${D}${base_sbindir}/init.android
    install -m 0755 ${WORKDIR}/root/sbin/adbd ${D}${base_sbindir}
    install -m 0755 ${WORKDIR}/root/default.prop ${D}
}

# 400+ elf binaries without GNU_HASH and we cannot fix it
INSANE_SKIP_${PN} = "ldflags"
INSANE_SKIP_${PN}-dbg = "ldflags"
# Already stripped and we cannot fix it, this will hide a lot of warnings about files already stripped
INHIBIT_PACKAGE_STRIP = "1"
PACKAGES = "${PN}-dbg ${PN}"
FILES_${PN}-dbg = "/system/symbols"
FILES_${PN} = "/system /vendor /sbin /default.prop"
