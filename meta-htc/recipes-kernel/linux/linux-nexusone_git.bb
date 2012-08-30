require recipes-kernel/linux/linux.inc
DEPENDS += "android-image-utils-native"

PE = "2"
PV = "2.6.37+${PR}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

LIC_FILES_CHKSUM = "file://COPYING;md5=d7810fab7487fb0aad327b76f1be7cd7"

COMPATIBLE_MACHINE = "nexusone"
CMDLINE = "root=/dev/mmcblk0p1 rw rootwait noinitrd fbcon=rotate:1"

SRCREV = "666efa5a22aed7bf001ea20822b0b63828df7859"

SRC_URI = "\
  git://github.com/shr-distribution/linux.git;protocol=git;branch=nexusone/2.6.37/master \
  file://defconfig \
"
S = "${WORKDIR}/git"

do_deploy_append() {
    if [ ! -e empty.cpio.gz ];then
        echo -n | cpio -o -H newc | gzip > empty.cpio.gz
    fi
    mkbootimg --kernel ${S}/${KERNEL_OUTPUT} \
              --ramdisk empty.cpio.gz \
              --cmdline "${CMDLINE}" \
              --base 0x20000000 \
              --output ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGE_BASE_NAME}.fastboot

    cd ${DEPLOYDIR}
    rm -f ${KERNEL_IMAGE_SYMLINK_NAME}.fastboot
    ln -sf ${KERNEL_IMAGE_BASE_NAME}.fastboot ${KERNEL_IMAGE_SYMLINK_NAME}.fastboot
}
