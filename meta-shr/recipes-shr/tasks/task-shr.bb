DESCRIPTION = "SHR Fat Image Feed"
PR = "r10"
PV = "2.0"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit task

PACKAGES = "\
  ${PN}-gtk \
  ${PN}-apps \
  ${PN}-games \
  ${PN}-cli \
"

#  vagalume \
RDEPENDS_${PN}-gtk = "\
  pidgin \
  libpurple-protocol-msn \
  libpurple-protocol-icq \
  tangogps \
"

#	eve \
#	intone \
RDEPENDS_${PN}-apps += "\
   task-shr-minimal-apps \
	opimd-utils-notes \
"
RDEPENDS_${PN}-cli += "\
   task-shr-minimal-cli \
   task-cli-tools-debug \
   task-cli-tools \
        rsync \
	openssh-sftp-server \
"

#	numptyphysics \
RDEPENDS_${PN}-games += "\
"
