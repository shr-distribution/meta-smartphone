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

IMAGE_INSTALL += " \
  task-core-boot \
  ${IMAGE_BOOT} \
  opkg \
  task-fso2-compliance \
  aurora-theme \
  aurora-applications \
  aurora-daemon \
  aurora-components \
  aurora-systemmanager \
  \
  ${TOUCH} \
  pointercal \
  \
  libqt-embeddedcore4 \
  libqt-embeddeddbus4 \
  libqt-embeddedgui4 \
  libqt-embeddedmultimedia4 \
  libqt-embeddednetwork4 \
  libqt-embeddedscript4 \
  libqt-embeddedscripttools4 \
  libqt-embeddedsql4 \
  libqt-embeddedsvg4 \
  libqt-embeddedwebkit4 \
  libqt-embeddedxml4 \
  qt4-embedded-conf \
  qt4-embedded-fonts-ttf-dejavu \
  qt4-embedded-fonts-ttf-vera \
  qt4-embedded-plugin-iconengine-svgicon \
  qt4-embedded-plugin-imageformat-jpeg \
  qt4-embedded-plugin-imageformat-svg \
  qt4-embedded-plugin-phonon-backend-gstreamer \
  qt4-embedded-plugin-script-dbus \
  qt4-embedded-plugin-sqldriver-sqlite \
  qt4-embedded-plugin-gfxdriver-directfbscreen \
"
