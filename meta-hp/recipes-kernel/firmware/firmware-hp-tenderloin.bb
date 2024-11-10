DESCRIPTION = "Firmware for HP Touchpad (tenderloin)"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

PACKAGE_ARCH = "${MACHINE_ARCH}"

COMPATIBLE_MACHINE = "^tenderloin$"

PV = "20171224"

SRC_URI = " \
    git://github.com/Evervolv/android_vendor_hp.git;protocol=https;branch=p-9.0;subpath=tenderloin/proprietary/etc/firmware \
"
SRCREV = "d235aa8c15cc5fc45efe6a00922867eccc4724ac"

S = "${WORKDIR}/firmware"

do_install() {
    install -d ${D}${libdir}/firmware/qcom
    
    # Adreno a220 firmware
    install -m 0644 ${S}/leia_* ${D}${libdir}/firmware/qcom/
    
    # Audio firmwares
    install -m 0644 ${S}/a6* ${D}${libdir}/firmware/qcom/
    install -m 0644 ${S}/q6* ${D}${libdir}/firmware/qcom/
    
    # media
    install -m 0644 ${S}/vidc_* ${D}${libdir}/firmware/qcom/
    install -m 0644 ${S}/wm8958_* ${D}${libdir}/firmware/qcom/
}

INSANE_SKIP:${PN} += "arch"
FILES:${PN} = "${libdir}"
