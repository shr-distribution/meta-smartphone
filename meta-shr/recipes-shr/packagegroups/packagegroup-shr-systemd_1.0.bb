DESCRIPTION = "Systemd services needed for shr images"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit packagegroup

PACKAGE_ARCH = "${MACHINE_ARCH}"

PR = "r11"

GPSD_PROVIDER = "gpsd-systemd"

CALIBRATOR_PROVIDER = "xinput-calibrator"
CALIBRATOR_PROVIDER_crespo = ""

RPROVIDES_${PN} += "task-shr-systemd"
RREPLACES_${PN} += "task-shr-systemd"
RCONFLICTS_${PN} += "task-shr-systemd"
RDEPENDS_${PN} = "\
  keymaps-systemd \
  ${CALIBRATOR_PROVIDER} \
  phonefsod-systemd \
  frameworkd-systemd \
  fsodeviced-systemd \
  fsotdld-systemd \
  fsoaudiod-systemd \
  ${GPSD_PROVIDER} \
"
