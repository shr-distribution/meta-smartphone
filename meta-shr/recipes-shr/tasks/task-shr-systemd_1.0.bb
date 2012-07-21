DESCRIPTION = "Systemd services needed for shr images"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit task

PACKAGE_ARCH = "${MACHINE_ARCH}"

PR = "r7"

GPSD_PROVIDER = "gpsd-systemd"
GPSD_PROVIDER_om-gta02 = "fso-gpsd-systemd"

CALIBRATOR_PROVIDER = "xinput-calibrator-systemd"
CALIBRATOR_PROVIDER_crespo = ""

RDEPENDS_${PN} = "\
  keymaps-systemd \
  ${CALIBRATOR_PROVIDER} \
  openssh-sshd-systemd \
  phonefsod-systemd \
  frameworkd-systemd \
  fsodeviced-systemd \
  fsotdld-systemd \
  fsoaudiod-systemd \
  fsosystemd-systemd \
  ${GPSD_PROVIDER} \
"
