FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:tenderloin = "\
    file://S01-mount-boot.sh \
    file://K99-move-boot.sh \
    file://leia_pm4_470.fw \
    file://leia_pfp_470.fw \
"

COMPATIBLE_MACHINE:tenderloin = "tenderloin"

do_install:append:tenderloin() {
    install -d ${D}/scripts/local-premount
    install -d ${D}/scripts/local-bottom
    install -m 0755 ${WORKDIR}/S01-mount-boot.sh ${D}/scripts/local-premount/S01-mount-boot.sh
    install -m 0755 ${WORKDIR}/K99-move-boot.sh ${D}/scripts/local-bottom/K99-move-boot.sh
    echo ". /scripts/local-premount/S01-mount-boot.sh" >> ${D}/scripts/local-premount/ORDER
    echo ". /scripts/local-bottom/K99-move-boot.sh" >> ${D}/scripts/local-bottom/ORDER

    # Install Adreno 200 (Z180) GPU firmware for early boot
    # Must be /lib/firmware (not /usr/lib) for kernel firmware loader
    install -d ${D}/lib/firmware
    install -m 0644 ${WORKDIR}/leia_pm4_470.fw ${D}/lib/firmware/leia_pm4_470.fw
    install -m 0644 ${WORKDIR}/leia_pfp_470.fw ${D}/lib/firmware/leia_pfp_470.fw
}

FILES:${PN} += "/scripts/local-premount /scripts/local-bottom /lib/firmware"

# Skip usrmerge check - initramfs needs /lib/firmware for kernel firmware loader
INSANE_SKIP:${PN}:append:tenderloin = " usrmerge"
