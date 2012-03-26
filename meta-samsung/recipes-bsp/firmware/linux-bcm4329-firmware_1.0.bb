DESCRIPTION = "Fetcher for the bcm4329 firmware from the android system partition"
AUTHOR = "Simon Busch"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PV = "1.0"
PR = "r0"

PACKAGES = "${PN}"
ALLOW_EMPTY_${PN} = "1"
PACKAGE_ARCH = "${MACHINE_ARCH}"

pkg_postinst_${PN}() {
  mkdir -p /media/android-rootfs
  mkdir -p /lib/firmware
  mount /dev/mmcblk0p1 /media/android-rootfs
  FIRMWARE_FILES = "bcm4329.bin bcm4329_apsta.bin nvram_net.txt"
  for f in $FIRMWARE_FILES; do
    cp /tmp/android-rootfs/vendor/firmware/$f /lib/firmware
  done
  umount /media/android-rootfs
}

pkg_postrm_${PN}() {
  FIRMWARE_FILES = "bcm4329.bin bcm4329_apsta.bin nvram_net.txt"
  for f in $FIRMWARE_FILES; do
    if [ -f /lib/firmware/$f ] ; then
      rm /lib/firmware/$f
    done
  done
}
