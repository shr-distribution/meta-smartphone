DESCRIPTION = "Systemd services needed for shr images"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit task

PR = "r1"

RDEPENDS_${PN} = "\
  keymaps-systemd \
  xinput-calibrator-systemd \
  openssh-sshd-systemd \
  phonefsod-systemd \
"
#  shr-splash-systemd // will be replaced maybe
