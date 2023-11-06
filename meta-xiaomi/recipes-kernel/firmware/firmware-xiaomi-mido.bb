DESCRIPTION = "Firmware for Xiaomi Redmi Note 4X"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

PACKAGE_ARCH = "${MACHINE_ARCH}"

COMPATIBLE_MACHINE = "mido"

PV = "20200802"

SRC_URI = " \
    git://github.com/Kiciuk/proprietary_firmware_mido.git;protocol=https;branch=master; \
"
SRCREV = "bc001cbb255a0ded2b58af07b93f712cd9322483"

S = "${WORKDIR}/git"

do_install() {
    install -d ${D}/lib/firmware
    
    # Adreno a506 firmware
    install -m 0644 ${S}/firmware/a506_zap.mdt ${D}/lib/firmware/a506_zap.mdt
    install -m 0644 ${S}/firmware/a506_zap.b02 ${D}/lib/firmware/a506_zap.b02
}

INSANE_SKIP:${PN} += "arch"
FILES:${PN} = "/lib"
