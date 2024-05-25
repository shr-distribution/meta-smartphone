DESCRIPTION = "Firmware for Xiaomi Redmi 5 (rosy)"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

PACKAGE_ARCH = "${MACHINE_ARCH}"

COMPATIBLE_MACHINE = "^rosy$"

PV = "20200802"

SRC_URI = " \
    git://github.com/Herrie82/android_vendor_xiaomi_rosy-1.git;protocol=https;branch=lineage-18.1; \
"
SRCREV = "4e5bdb5db80c33acbb79532816112cd8fa537ac0"

S = "${WORKDIR}/git"

do_install() {
    install -d ${D}${libdir}/firmware
    
    # Adreno a506 firmware
    install -m 0644 ${S}/proprietary/vendor/firmware/a506_* ${D}${libdir}/firmware/
    install -m 0644 ${S}/proprietary/vendor/firmware/a530* ${D}${libdir}/firmware/
    
    # cameras
    install -m 0644 ${S}/proprietary/vendor/firmware/cpp_firmware_* ${D}${libdir}/firmware/
    install -m 0644 ${S}/proprietary/vendor/firmware/leia_* ${D}${libdir}/firmware/
}

INSANE_SKIP:${PN} += "arch"
FILES:${PN} = "${libdir}"
