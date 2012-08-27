DESCRIPTION = "Firmware for various chips in the Samsung Tuna device"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PR = "r0"

COMPATIBLE_MACHINES = "tuna"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PACKAGES = "${PN}"
ALLOW_EMPTY_${PN} = "1"
FILES_${PN} = "/lib/firmware"

FIRMWARE_FILES = "bcm4330.hcd fw_bcmdhd.bin fw_bcmdhd_p2p.bin mms144_ts_rev32.fw ducati-m3.bin"
FIRMWARE_FILES += "fw_bcmdhd_apsta.bin hdcp.keys mms144_ts_rev31.fw smc_pa_wvdrm.ift"

pkg_postinst_${PN}() {
  #!/bin/sh -e
  if [ x"$D" = "x" ]; then
    mkdir -p /tmp/android-rootfs
    mkdir -p /lib/firmware
    mount /dev/mmcblk0p10 /tmp/android-rootfs
    for f in ${FIRMWARE_FILES}; do
      cp /tmp/android-rootfs/vendor/firmware/$f /lib/firmware
    done
    umount /tmp/android-rootfs
  else
    exit 1
  fi
}

pkg_postrm_${PN}() {
  #!/bin/sh -e
  for f in ${FIRMWARE_FILES}; do
    if [ -f /lib/firmware/$f ] ; then
      rm /lib/firmware/$f
    fi
  done
}
