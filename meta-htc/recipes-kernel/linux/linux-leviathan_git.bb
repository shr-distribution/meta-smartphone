require linux.inc
DEPENDS += "android-image-utils-native"

LIC_FILES_CHKSUM = "file://COPYING;md5=d7810fab7487fb0aad327b76f1be7cd7"

PV = "2.6.32+${PR}+gitr${SRCREV}"
PR = "r0"

COMPATIBLE_MACHINE = "htcdream"
CMDLINE = "console=tty1 root=/dev/mmcblk0p1 rootdelay=8 fbcon=rotate:1 panic=30 mem=110M"

SRCREV = "cf1af2ebaa38e265bf0b76038a7a169ef3f3585c"

SRC_URI = "\
  git://gitorious.org/htc-msm-2-6-32/leviathan-incoming.git;protocol=git;branch=android-msm-2.6.32-rebase \
  file://defconfig \
"
S = "${WORKDIR}/git"

do_deploy_append() {
    if [ ! -e empty.cpio.gz ];then
        echo -n | sudo cpio -o -H newc | sudo gzip > empty.cpio.gz
    fi
    mkbootimg --kernel ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGE_BASE_NAME}.bin \
              --ramdisk empty.cpio.gz \
              --cmdline "${CMDLINE}" \
              --output ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGE_BASE_NAME}.fastboot
}
