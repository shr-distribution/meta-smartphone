#------------------------------------------------------
# Aurora Image Recipe
#------------------------------------------------------

PV = "1.0"
PR = "r3"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

IMAGE_FEATURES += " \
  package-management \
  ssh-server-openssh \
  debug-tweaks \
"

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
IMAGE_BASENAME = "aurora-image"

RDEPENDS_${PN} += " \
  opkg \
  task-fso2-compliance \
"

TOUCH = ' ${@base_contains("MACHINE_FEATURES", "touchscreen", "tslib tslib-calibrate tslib-tests", "",d)}'

# FIXME: We need to separate the things below into a task later
IMAGE_INSTALL += " \
  task-core-boot \
  ${IMAGE_BOOT} \
  \
  opkg \
  task-fso2-compliance \
  aurora-theme \
  aurora-applications \
  aurora-daemon \
  aurora-components \
  aurora-systemmanager \
  \
  task-x11-server \
  task-x11-utils \
  ${XSERVER} \
  task-fonts-truetype-core \
  xcursor-transparent-theme \
  xinput-calibrator \
  libx11-locale \
  \
  alsa-utils-alsactl \
  alsa-utils-alsamixer \
  alsa-utils-aplay \
  alsa-utils-amixer \
"
