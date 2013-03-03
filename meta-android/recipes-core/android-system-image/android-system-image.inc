DESCRIPTION = "Tiny Android system to leverage android hardware drivers through libhybris"
LICENSE = "Apache-2.0 & Properietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

PACKAGE_ARCH = "${MACHINE_ARCH}"

RPROVIDES_${PN} += "virtual/android-system-image"

INC_PR = "r0"

do_install() {
    cp -rv ${WORKDIR}/system ${D}
    ln -sf /system/vendor ${D}/vendor

    # The binaries are without any permission set so we have to restore them here
    chmod -R 0755 ${D}/system/bin || true
    chmod -R 0755 ${D}/system/xbin || true
    chmod -R 0755 ${D}/system/vendor/bin || true
}

PACKAGES = "${PN}"
FILES_${PN} = "/system /vendor"
