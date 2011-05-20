#------------------------------------------------------
# SHR Image Recipe
#------------------------------------------------------

PV = "2.0"
PR = "r18"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

DEPENDS += "task-shr-minimal"

#
# tinylogin, getty
# shadow doesn't provide adduser which was find with oe.dev, but busybox from oe-core doesn't provide it neither
#
IMAGE_LOGIN_MANAGER = "tinylogin"

#
# udev, devfsd, busybox-mdev (from busybox) or none
#
IMAGE_DEV_MANAGER ?= "${@base_contains("MACHINE_FEATURES", "kernel26",  "udev","",d)} "
IMAGE_DEV_MANAGER_om-gta01 = ""
IMAGE_DEV_MANAGER_om-gta02 = ""
IMAGE_DEV_MANAGER_htcdream = ""
IMAGE_DEV_MANAGER_nokia900 = ""

#
# sysvinit, upstart
#
IMAGE_INIT_MANAGER ?= "sysvinit sysvinit-pidof"

#
# initscripts, initscripts-shr
#
# IMAGE_INITSCRIPTS ?= "initscripts"
IMAGE_INITSCRIPTS = "initscripts-shr"

# set sane default for the SPLASH variable
SPLASH ?= ""

IMAGE_BOOT ?= "${IMAGE_INITSCRIPTS} \
               ${IMAGE_DEV_MANAGER} \
               ${IMAGE_INIT_MANAGER} \
               ${IMAGE_LOGIN_MANAGER}"

# some default locales
# IMAGE_LINGUAS ?= "de-de fr-fr en-gb"
IMAGE_LINGUAS ?= "en-us"

IMAGE_INSTALL += "${IMAGE_BOOT}"

RDEPENDS_${PN} += "\
  task-shr-minimal-base \
  task-shr-minimal-cli \
  task-shr-minimal-fso \
  task-shr-minimal-apps \
  task-shr-minimal-audio \
  task-shr-minimal-gtk \
  task-shr-minimal-x \
"

IMAGE_INSTALL += "\
  task-shr-minimal-base \
  task-shr-minimal-cli \
  task-shr-minimal-fso \
  task-shr-minimal-apps \
  task-shr-minimal-audio \
  task-shr-minimal-gtk \
  task-shr-minimal-x \
"

inherit image

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

# We don't want /boot directory in jffs2/ubi/ubifs images on gta01
IMAGE_CMD_jffs2_om-gta01 = "rm -rf ${IMAGE_ROOTFS}-boot; mv ${IMAGE_ROOTFS}/boot ${IMAGE_ROOTFS}-boot && mkfs.jffs2 -x lzo --root=${IMAGE_ROOTFS} --faketime --output=${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.rootfs.jffs2 ${EXTRA_IMAGECMD}; mv ${IMAGE_ROOTFS}-boot ${IMAGE_ROOTFS}/boot"
IMAGE_CMD_ubi_om-gta01 = "rm -rf ${IMAGE_ROOTFS}-boot; mv ${IMAGE_ROOTFS}/boot ${IMAGE_ROOTFS}-boot && echo \[ubifs\] > ubinize.cfg ; echo mode=ubi >> ubinize.cfg ; echo image=${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.rootfs.ubifs >> ubinize.cfg ; echo vol_id=0 >> ubinize.cfg ; echo vol_type=dynamic >> ubinize.cfg ; echo vol_name=${UBI_VOLNAME} >> ubinize.cfg ; echo vol_flags=autoresize >> ubinize.cfg;mkfs.ubifs -r ${IMAGE_ROOTFS} -o ${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.rootfs.ubifs ${MKUBIFS_ARGS} && ubinize -o ${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.rootfs.ubi ${UBINIZE_ARGS} ubinize.cfg; mv ${IMAGE_ROOTFS}-boot ${IMAGE_ROOTFS}/boot"
IMAGE_CMD_ubifs_om-gta01 = "rm -rf ${IMAGE_ROOTFS}-boot; mv ${IMAGE_ROOTFS}/boot ${IMAGE_ROOTFS}-boot && mkfs.ubifs -r ${IMAGE_ROOTFS} -o ${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.ubifs.img ${MKUBIFS_ARGS}; mv ${IMAGE_ROOTFS}-boot ${IMAGE_ROOTFS}/boot"

ROOTFS_POSTPROCESS_COMMAND += " rootfs_update_timestamp;"
#ROOTFS_POSTPROCESS_COMMAND += " install_linguas;"

ROOTFS_POSTPROCESS_COMMAND_append_om-gta02 = " shr_rootfs_gta02_postprocess;"
ROOTFS_POSTPROCESS_COMMAND_append_om-gta01 = " shr_rootfs_gta01_postprocess;"
