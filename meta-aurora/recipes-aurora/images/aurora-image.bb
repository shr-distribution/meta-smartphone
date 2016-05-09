#------------------------------------------------------
# Aurora Image Recipe
#------------------------------------------------------

PV = "1.0"
PR = "r7"

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

TOUCH = ' ${@bb.utils.contains("MACHINE_FEATURES", "touchscreen", "tslib tslib-calibrate tslib-tests", "",d)}'

# FIXME: We need to separate the things below into a task later
IMAGE_INSTALL += " \
  packagegroup-core-boot \
  \
  opkg \
  packagegroup-fso2-compliance \
  aurora-daemon \
  aurora-systemmanager \
  \
  packagegroup-core-x11-xserver \
  packagegroup-core-x11-utils \
  ${XSERVER} \
  packagegroup-fonts-truetype-core \
  xcursor-transparent-theme \
  xinput-calibrator \
  libx11-locale \
  \
  alsa-utils-alsactl \
  alsa-utils-alsamixer \
  alsa-utils-aplay \
  alsa-utils-amixer \
"

# We're including fsoaudiod here as it is removed from packagegroup-fso2-compliance until SHR
# 2012.01 is released.
IMAGE_INSTALL += " fsoaudiod"

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
