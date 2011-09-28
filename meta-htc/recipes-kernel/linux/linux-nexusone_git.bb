require linux.inc
DEPENDS += "android-image-utils-native"

PV = "2.6.37+${PR}+gitr${SRCREV}"
PR = "r0"

COMPATIBLE_MACHINE = "nexusone"
CMDLINE = "root=/dev/mmcblk0p1 rw rootwait noinitrd fbcon=rotate:1"

SRCREV = "268cdd427f19e36ef77b9a45824cf4789e93fb6b"

SRC_URI = "\
  https://git.gitorious.org/htc-msm-2-6-32/leviathan-incoming.git;protocol=git;branch=nexusone \
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
              --base 0x20000000 \
              --output ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGE_BASE_NAME}.fastboot
}
