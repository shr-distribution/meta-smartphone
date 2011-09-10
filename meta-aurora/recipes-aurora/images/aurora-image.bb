#------------------------------------------------------
# Aurora Image Recipe
#------------------------------------------------------

PV = "1.0"
PR = "r1"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

IMAGE_FEATURES += "package-management ssh-server-openssh"

inherit core-image

IMAGE_LOGIN_MANAGER = "tinylogin"
# Per default we don't want udev and prefer devtmpfs
IMAGE_DEV_MANAGER ?= ""
IMAGE_INIT_MANAGER ?= "sysvinit sysvinit-pidof"
IMAGE_INITSCRIPTS = "initscripts"
SPLASH ?= ""

IMAGE_BOOT ?= " \
  ${IMAGE_INITSCRIPTS} \
  ${IMAGE_DEV_MANAGER} \
  ${IMAGE_INIT_MANAGER} \
  ${IMAGE_LOGIN_MANAGER} \
"

IMAGE_LINGUAS ?= "en-us"
IMAGE_INSTALL += "${IMAGE_BOOT}"
IMAGE_BASENAME = "aurora-image"
IMAGE_INSTALL += " \
  task-core-boot \
  opkg \
"
