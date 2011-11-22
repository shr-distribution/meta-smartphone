DESCRIPTION = "The freesmartphone.org Framework 2.0. \
Install this task to make your distribution FSO 2.0-compliant."
SECTION = "fso/base"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
PV = "1.9.0"
PR = "r13"

inherit task

RDEPENDS_${PN} = "\
  libfsobasics \
  libfsotransport \
  libfsoframework \
  libfsoresource \
  \
  fsodatad \
  fsodeviced \
  fsodeviced-systemd \
  fsogsmd \
  fsotdld \
  fsotdld-systemd \
  fsonetworkd \
  fsousaged \
  fsoaudiod \
  fsoaudiod-systemd \
  fsosystemd \
  fsosystemd-systemd \
  \
  fso-apm \
  connman \
  connman-plugin-bluetooth \
  connman-plugin-ethernet \
  connman-plugin-fake \
  connman-plugin-loopback \
  connman-plugin-wifi \
"

RRECOMMENDS_${PN} = "\
  wmiconfig \
  tzdata \
  tzdata-africa \
  tzdata-americas \
  tzdata-asia \
  tzdata-australia \
  tzdata-europe \
"
