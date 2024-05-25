DESCRIPTION = "Firmware for Xiaomi Mi A1 (tissot)"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

PACKAGE_ARCH = "${MACHINE_ARCH}"

COMPATIBLE_MACHINE = "^tissot$"

PV = "20200802"

SRC_URI = " \
    git://gitlab.com/the-muppets/proprietary_vendor_xiaomi.git;protocol=https;branch=lineage-19.1;subpath=msm8953-common/proprietary/vendor/firmware \
"
SRCREV = "bb09565e99c8a61758588053149a2fc783c811e2"

S = "${WORKDIR}/firmware"

do_install() {
    install -d ${D}${libdir}/firmware
    
    # Adreno a506 firmware
    install -m 0644 ${S}/a506_* ${D}${libdir}/firmware/
    install -m 0644 ${S}/a530* ${D}${libdir}/firmware/
    
    # cameras
    install -m 0644 ${S}/cpp_firmware_* ${D}${libdir}/firmware/
}

INSANE_SKIP:${PN} += "arch"
FILES:${PN} = "${libdir}"
