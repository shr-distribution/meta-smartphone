DESCRIPTION = "SHR Lite Image Feed"
PR = "r54"
PV = "2.0"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit task

PACKAGE_ARCH = "${MACHINE_ARCH}"

PACKAGES += "\
    ${PN}-base \
    ${PN}-cli \
    ${PN}-fso \
    ${PN}-audio \
    ${PN}-x \
    ${PN}-apps \
    ${PN}-gtk \
"

RDEPENDS_${PN}-base = "\
  ${MACHINE_TASK_PROVIDER} \
  task-base \
  task-boot \
  glibc-utils \
  glibc-charmap-utf-8 \
  netbase \
  sysfsutils \
  modutils-initscripts \
  module-init-tools-depmod \
  fbset \
  fbset-modes \
  cronie \
  logrotate\
  util-linux-fdisk \
  shr-splash \
  task-shr-systemd \
"

RDEPENDS_${PN}-cli = "\
  nano \
  mtd-utils \
"

RDEPENDS_${PN}-fso = "\
  fsoraw \
  opimd-utils-cli \
  python-codecs \
  python-gst \
"

RDEPENDS_${PN}-audio = "\
  alsa-utils-alsactl \
  alsa-utils-alsamixer \
  alsa-utils-aplay \
  alsa-utils-amixer \
"

RDEPENDS_${PN}-x = "\
  task-x11-illume \
  task-fonts-truetype-core \
  shr-wizard \
  shr-theme-gry \
  xcursor-transparent-theme \
  xinput-calibrator \
  libx11-locale \
"

RDEPENDS_${PN}-apps = "\
  task-fso2-compliance \
  phoneui-apps-messages \
  phoneui-apps-contacts \
  phoneui-apps-dialer \
  phoneui-apps-quick-settings \
  phonefsod \
  phoneuid \
  libphone-ui \
  libphone-ui-shr \
  ffalarms \
  shr-settings \
  shr-theme \
  ecalc \
  iliwi \
  ca-certificates \
"

# hicolor-icon-theme to make gdk-pixbuf-loader postinst happy

RDEPENDS_${PN}-gtk = "\
  gtk-theme-e17lookalike \
  hicolor-icon-theme \
  vala-terminal \
  ffphonelog \
  matchbox-keyboard-im \
  gtk-immodule-xim \
"
