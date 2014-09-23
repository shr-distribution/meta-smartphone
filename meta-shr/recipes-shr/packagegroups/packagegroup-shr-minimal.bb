DESCRIPTION = "SHR Lite Image Feed"

PV = "2.1"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PACKAGE_ARCH = "${MACHINE_ARCH}"
inherit packagegroup

RPROVIDES_${PN} += "task-shr-minimal"
RPROVIDES_${PN}-base += "task-shr-minimal-base"
RPROVIDES_${PN}-cli += "task-shr-minimal-cli"
RPROVIDES_${PN}-fso += "task-shr-minimal-fso"
RPROVIDES_${PN}-audio += "task-shr-minimal-audio"
RPROVIDES_${PN}-x += "task-shr-minimal-x"
RPROVIDES_${PN}-apps += "task-shr-minimal-apps"
RPROVIDES_${PN}-gtk += "task-shr-minimal-gtk"
RREPLACES_${PN} += "task-shr-minimal"
RREPLACES_${PN}-base += "task-shr-minimal-base"
RREPLACES_${PN}-cli += "task-shr-minimal-cli"
RREPLACES_${PN}-fso += "task-shr-minimal-fso"
RREPLACES_${PN}-audio += "task-shr-minimal-audio"
RREPLACES_${PN}-x += "task-shr-minimal-x"
RREPLACES_${PN}-apps += "task-shr-minimal-apps"
RREPLACES_${PN}-gtk += "task-shr-minimal-gtk"
RCONFLICTS_${PN} += "task-shr-minimal"
RCONFLICTS_${PN}-base += "task-shr-minimal-base"
RCONFLICTS_${PN}-cli += "task-shr-minimal-cli"
RCONFLICTS_${PN}-fso += "task-shr-minimal-fso"
RCONFLICTS_${PN}-audio += "task-shr-minimal-audio"
RCONFLICTS_${PN}-x += "task-shr-minimal-x"
RCONFLICTS_${PN}-apps += "task-shr-minimal-apps"
RCONFLICTS_${PN}-gtk += "task-shr-minimal-gtk"

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
  packagegroup-base \
  packagegroup-boot \
  glibc-utils \
  glibc-charmap-utf-8 \
  netbase \
  init-ifupdown \
  sysfsutils \
  modutils-initscripts \
  module-init-tools-depmod \
  fbset \
  fbset-modes \
  cronie \
  logrotate\
  util-linux-fdisk \
  shr-splash \
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
  packagegroup-x11-illume \
  packagegroup-fonts-truetype-core \
  shr-wizard \
  shr-theme-gry \
  xcursor-transparent-theme \
  xinput-calibrator \
  libx11-locale \
"

RDEPENDS_${PN}-apps = "\
  packagegroup-fso2-compliance \
  phoneui-apps-messages \
  phoneui-apps-contacts \
  phoneui-apps-dialer \
  phoneui-apps-quick-settings \
  phonefsod \
  phoneuid \
  opimd-utils \
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
  terminology \
  ffphonelog \
  matchbox-keyboard-im \
  gtk-immodule-xim \
"
