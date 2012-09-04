DESCRIPTION = "SHR Fat Image Feed"
PR = "r13"
PV = "2.0"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit packagegroup allarch

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
