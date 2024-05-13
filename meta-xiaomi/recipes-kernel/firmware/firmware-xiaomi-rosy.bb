DESCRIPTION = "Firmware for Xiaomi Redmi Note 4X"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

PACKAGE_ARCH = "${MACHINE_ARCH}"

COMPATIBLE_MACHINE = "rosy"

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
    
    # wifi firmware
#    install -d ${D}${libdir}/firmware/wlan/prima
#    install -m 0644 ${S}/proprietary/vendor/firmware/wlan/prima/* ${D}${libdir}/firmware/wlan/prima/
#    install -m 0644 ${S}/apnhlos/wcnss.* ${D}${libdir}/firmware/
    
    # cameras
    install -m 0644 ${S}/proprietary/vendor/firmware/cpp_firmware_* ${D}${libdir}/firmware/
    install -m 0644 ${S}/proprietary/vendor/firmware/leia_* ${D}${libdir}/firmware/

#    install -m 0644 ${S}/apnhlos/venus.* ${D}${libdir}/firmware/
    
    # modems
#    install -m 0644 ${S}/modem/modem.* ${D}${libdir}/firmware/
#    install -m 0644 ${S}/modem/mb* ${D}${libdir}/firmware/
#    install -m 0644 ${S}/modem/qdsp6m.qdb ${D}${libdir}/firmware/
    # maybe modem_pr/ subfolder should be copied too?

    # sound
#    install -m 0644 ${S}/modem/adsp.* ${D}${libdir}/firmware/
    
    # other
#    install -m 0644 ${S}/firmware/nvm_* ${D}${libdir}/firmware/
#    install -m 0644 ${S}/firmware/rampatch_* ${D}${libdir}/firmware/
}

INSANE_SKIP:${PN} += "arch"
FILES:${PN} = "${libdir}"
