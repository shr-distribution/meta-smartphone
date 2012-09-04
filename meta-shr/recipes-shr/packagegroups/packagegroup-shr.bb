DESCRIPTION = "SHR Fat Image Feed"
PR = "r14"
PV = "2.0"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit packagegroup allarch

RPROVIDES_${PN} += "task-shr"
RPROVIDES_${PN}-gtk += "task-shr-gtk"
RPROVIDES_${PN}-apps += "task-shr-apps"
RPROVIDES_${PN}-games += "task-shr-games"
RPROVIDES_${PN}-cli += "task-shr-cli"
RREPLACES_${PN} += "task-shr"
RREPLACES_${PN}-gtk += "task-shr-gtk"
RREPLACES_${PN}-apps += "task-shr-apps"
RREPLACES_${PN}-games += "task-shr-games"
RREPLACES_${PN}-cli += "task-shr-cli"
RCONFLICTS_${PN} += "task-shr"
RCONFLICTS_${PN}-gtk += "task-shr-gtk"
RCONFLICTS_${PN}-apps += "task-shr-apps"
RCONFLICTS_${PN}-games += "task-shr-games"
RCONFLICTS_${PN}-cli += "task-shr-cli"

PACKAGES = "\
  ${PN}-gtk \
  ${PN}-apps \
  ${PN}-games \
  ${PN}-cli \
"

RDEPENDS_${PN}-gtk = "\
  pidgin \
  libpurple-protocol-msn \
  libpurple-protocol-icq \
  foxtrotgps \
  vagalume \
"

RDEPENDS_${PN}-apps += "\
  packagegroup-shr-minimal-apps \
  opimd-utils-notes \
  eve \
  intone \
"
RDEPENDS_${PN}-cli += "\
  packagegroup-shr-minimal-cli \
  packagegroup-cli-tools-debug \
  packagegroup-cli-tools \
  rsync \
  openssh-sftp-server \
"

RDEPENDS_${PN}-games += "\
  numptyphysics \
"
