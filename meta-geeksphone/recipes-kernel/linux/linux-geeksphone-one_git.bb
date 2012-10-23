require recipes-kernel/linux/linux.inc
DEPENDS += "android-image-utils-native"

LIC_FILES_CHKSUM = "file://COPYING;md5=d7810fab7487fb0aad327b76f1be7cd7"

SRCREV = "804f3c76e933cb8310a2f16f8f5ccc0f8cac64ac"
PV = "2.6.35+${PR}+gitr${SRCREV}"

COMPATIBLE_MACHINE = "geeksphone-one"
CMDLINE = "\
mem=32M@0x1A000000 mem=21M@0x1C000000 mem=32M@0x20000000 mem=32M@0x22000000 mem=32M@0x24000000 mem=32M@0x26000000 \
console=ttyMSM2,115200n8 androidboot.hardware=qcom \
"
BASE = "1A000000"

SRC_URI = "\
  git://github.com/vquicksilver/gp_one_kernel.git;protocol=git;branch=shr-2.6.35.7 \
  file://defconfig \
"

S = "${WORKDIR}/git"

do_deploy_append() {
    if [ ! -e empty.cpio.gz ];then
        echo -n | cpio -o -H newc | gzip > empty.cpio.gz
    fi
    mkbootimg --base ${BASE} \
              --kernel ${S}/${KERNEL_OUTPUT} \
              --ramdisk empty.cpio.gz \
              --cmdline "${CMDLINE}" \
              --output ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGE_BASE_NAME}-boot.img
}

