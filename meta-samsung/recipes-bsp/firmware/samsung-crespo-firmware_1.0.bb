DESCRIPTION = "Fetcher for the bcm4329 firmware from the android system partition"
AUTHOR = "Simon Busch"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PV = "1.0"
PR = "r2"

PACKAGES = "${PN}"
ALLOW_EMPTY_${PN} = "1"
PACKAGE_ARCH = "${MACHINE_ARCH}"
# maybe other devices too, but it has crespo in name and 
# we don't want to build it e.g. as part of qemuarm world build
COMPATIBLE_MACHINE = "crespo"

pkg_postinst_${PN}() {
  #!/bin/sh -e
  if [ x"$D" = "x" ]; then
    mkdir -p /media/android-rootfs
    mkdir -p /lib/firmware
    mount /dev/mmcblk0p1 /media/android-rootfs
    FIRMWARE_FILES="fw_bcm4329.bin fw_bcm4329_apsta.bin nvram_net.txt"
    for f in $FIRMWARE_FILES; do
      cp /media/android-rootfs/vendor/firmware/$f /lib/firmware
    done
    umount /media/android-rootfs
  else
    exit 1
  fi
}

pkg_postrm_${PN}() {
  #!/bin/sh -e
  FIRMWARE_FILES="fw_bcm4329.bin fw_bcm4329_apsta.bin nvram_net.txt"
  for f in $FIRMWARE_FILES; do
    if [ -f /lib/firmware/$f ] ; then
      rm /lib/firmware/$f
    fi
  done
}
