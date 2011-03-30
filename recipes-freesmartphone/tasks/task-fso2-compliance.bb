DESCRIPTION = "The freesmartphone.org Framework 2.0. \
Install this task to make your distribution FSO 2.0-compliant."
SECTION = "fso/base"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${TOPDIR}/meta-shr/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
PV = "1.9.0"
PR = "r7"

inherit task

RDEPENDS_${PN} = "\
  libfsobasics \
  libfsotransport \
  libfsoframework \
  libfsoresource \
  \
  fsodatad \
  fsodeviced \
  fsogsmd \
  fsotdld \
  fsonetworkd \
  fsousaged \
  \
  fso-apm \
"
#  fsomusicd \
#  connman \
#  connman-scripts \
#  connman-plugin-bluetooth \
#  connman-plugin-dhclient \
#  connman-plugin-dnsproxy \
#  connman-plugin-ethernet \
#  connman-plugin-fake \
#  connman-plugin-loopback \
#  connman-plugin-pppd \
#  connman-plugin-resolvconf \
#  connman-plugin-udhcp \
#  connman-plugin-wifi \
#  connman-test-utils \

RRECOMMENDS_${PN} = "\
  wmiconfig \
  tzdata \
  tzdata-africa \
  tzdata-americas \
  tzdata-asia \
  tzdata-australia \
  tzdata-europe \
"
