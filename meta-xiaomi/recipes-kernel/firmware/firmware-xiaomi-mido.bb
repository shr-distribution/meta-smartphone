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
    install -d ${D}${libdir}/firmware
    
    # Adreno a506 firmware
    install -m 0644 ${S}/firmware/a506_* ${D}${libdir}/firmware/
    
    # wifi firmware
    install -d ${D}${libdir}/firmware/wlan/prima
    install -m 0644 ${S}/firmware/wlan/prima/* ${D}${libdir}/firmware/wlan/prima/
    install -m 0644 ${S}/apnhlos/wcnss.* ${D}${libdir}/firmware/
    
    # cameras
    install -m 0644 ${S}/firmware/cpp_firmware_* ${D}${libdir}/firmware/
    install -m 0644 ${S}/firmware/leia_* ${D}${libdir}/firmware/

    install -m 0644 ${S}/apnhlos/venus.* ${D}${libdir}/firmware/
    
    # modems
    install -m 0644 ${S}/modem/modem.* ${D}${libdir}/firmware/
    install -m 0644 ${S}/modem/mb* ${D}${libdir}/firmware/
    install -m 0644 ${S}/modem/qdsp6m.qdb ${D}${libdir}/firmware/
    # maybe modem_pr/ subfolder should be copied too?

    # sound
    install -m 0644 ${S}/modem/adsp.* ${D}${libdir}/firmware/
    
    # other
    install -m 0644 ${S}/firmware/nvm_* ${D}${libdir}/firmware/
    install -m 0644 ${S}/firmware/rampatch_* ${D}${libdir}/firmware/
}

INSANE_SKIP:${PN} += "arch"
FILES:${PN} = "${libdir}"
