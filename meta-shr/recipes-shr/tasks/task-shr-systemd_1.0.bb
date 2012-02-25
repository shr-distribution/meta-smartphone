DESCRIPTION = "Systemd services needed for shr images"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit task allarch

PR = "r6"

RDEPENDS_${PN} = "\
  keymaps-systemd \
  xinput-calibrator-systemd \
  openssh-sshd-systemd \
  phonefsod-systemd \
  frameworkd-systemd \
  fsodeviced-systemd \
  fsotdld-systemd \
  fsoaudiod-systemd \
  fsosystemd-systemd \
  gpsd-systemd \
"
RSUGGESTS_${PN} = "\
  fso-gpsd-systemd \
"
#  shr-splash-systemd // will be replaced maybe
