#------------------------------------------------------
# FSO-compliant Console Image Recipe
#------------------------------------------------------

PV = "1.0"
PR = "r1"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

IMAGE_FEATURES += " \
  package-management \
  ssh-server-openssh \
  debug-tweaks \
"

inherit core-image

SPLASH ?= ""

RDEPENDS_${PN} += " \
  opkg \
  packagegroup-fso2-compliance \
"

IMAGE_INSTALL += "\
  packagegroup-core-boot \
  opkg \
  packagegroup-fso2-compliance \
  mdbus2 \
  serial-forward \
"
shr_rootfs_gta02_postprocess() {
    curdir=$PWD
    cd ${IMAGE_ROOTFS}/boot
    ln -s uImage uImage-GTA02.bin
    echo 'loglevel=1 quiet splash' > append-GTA02
    cd $curdir
}

shr_rootfs_gta01_postprocess() {
    curdir=$PWD
    cd ${IMAGE_ROOTFS}/boot
    ln -s uImage uImage-GTA01.bin
    echo 'loglevel=1 quiet splash' > append-GTA01
    cd $curdir
}

ROOTFS_POSTPROCESS_COMMAND_append_om-gta02 = " shr_rootfs_gta02_postprocess;"
ROOTFS_POSTPROCESS_COMMAND_append_om-gta01 = " shr_rootfs_gta01_postprocess;"
