FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:tenderloin = "\
    file://S01-mount-boot.sh \
    file://K99-move-boot.sh \
"

COMPATIBLE_MACHINE:tenderloin = "tenderloin"

do_install:append:tenderloin() {
    install -d ${D}/scripts/local-premount
    install -d ${D}/scripts/local-bottom
    install -m 0755 ${UNPACKDIR}/S01-mount-boot.sh ${D}/scripts/local-premount/S01-mount-boot.sh
    install -m 0755 ${UNPACKDIR}/K99-move-boot.sh ${D}/scripts/local-bottom/K99-move-boot.sh
    echo ". /scripts/local-premount/S01-mount-boot.sh" >> ${D}/scripts/local-premount/ORDER
    echo ". /scripts/local-bottom/K99-move-boot.sh" >> ${D}/scripts/local-bottom/ORDER

    # Install Adreno 200 (Z180) GPU firmware for early boot
    # Must be /lib/firmware (not /usr/lib) for kernel firmware loader
    install -d ${D}/lib/firmware
    install -m 0644 ${UNPACKDIR}/leia_pm4_470.fw ${D}/lib/firmware/leia_pm4_470.fw
    install -m 0644 ${UNPACKDIR}/leia_pfp_470.fw ${D}/lib/firmware/leia_pfp_470.fw

    # WiFi firmware for AR6003 - must be in initramfs so it's available at probe time
    # (rootfs not mounted until ~19s after boot; AR6003 BMI bootloader exits at ~30s
    # if no BMI command arrives, and ath6kl_sdio probes at ~t=12s when built-in,
    # so firmware must already be present in initramfs at that moment.)
    install -d ${D}/lib/firmware/ath6k/AR6003/hw2.1.1
    install -m 0644 ${UNPACKDIR}/ath6k/AR6003/hw2.1.1/athwlan.bin ${D}/lib/firmware/ath6k/AR6003/hw2.1.1/athwlan.bin
    install -m 0644 ${UNPACKDIR}/ath6k/AR6003/hw2.1.1/athtcmd_ram.bin ${D}/lib/firmware/ath6k/AR6003/hw2.1.1/athtcmd_ram.bin
    install -m 0644 ${UNPACKDIR}/ath6k/AR6003/hw2.1.1/bdata.bin ${D}/lib/firmware/ath6k/AR6003/hw2.1.1/bdata.bin
    install -m 0644 ${UNPACKDIR}/ath6k/AR6003/hw2.1.1/bdata.SD32.bin ${D}/lib/firmware/ath6k/AR6003/hw2.1.1/bdata.SD32.bin
    install -m 0644 ${UNPACKDIR}/ath6k/AR6003/hw2.1.1/bdata.SD32_3G.bin ${D}/lib/firmware/ath6k/AR6003/hw2.1.1/bdata.SD32_3G.bin
    install -m 0644 ${UNPACKDIR}/ath6k/AR6003/hw2.1.1/otp.bin ${D}/lib/firmware/ath6k/AR6003/hw2.1.1/otp.bin
    install -m 0644 ${UNPACKDIR}/ath6k/AR6003/hw2.1.1/fw-3.bin ${D}/lib/firmware/ath6k/AR6003/hw2.1.1/fw-3.bin
    install -m 0644 ${UNPACKDIR}/ath6k/AR6003/hw2.1.1/data.patch.bin ${D}/lib/firmware/ath6k/AR6003/hw2.1.1/data.patch.bin

    # cfg80211 regulatory database: required for iw scan / wpa_supplicant on
    # mainline kernels.  cfg80211 loads /lib/firmware/regulatory.db at module
    # init time; on this device ath6kl is built-in and registers wiphy before
    # rootfs is mounted, so the db must be in the initramfs.  Without it,
    # cfg80211 falls back to the world regulatory domain which on AR6003 +
    # mainline rejects iw scan with EBUSY (the chip's WMI command path can't
    # confirm channel legality and returns busy).
    install -m 0644 ${UNPACKDIR}/regulatory.db ${D}/lib/firmware/regulatory.db
    install -m 0644 ${UNPACKDIR}/regulatory.db.p7s ${D}/lib/firmware/regulatory.db.p7s
}

FILES:${PN} += "/scripts/local-premount /scripts/local-bottom"
