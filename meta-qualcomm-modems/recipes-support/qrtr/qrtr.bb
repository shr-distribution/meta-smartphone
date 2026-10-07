SUMMARY = "QIPCRTR Name Service"
HOMEPAGE = "https://github.com/andersson/rpmsgexport"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=15329706fbfcb5fc5edcc1bc7c139da5"

DEPENDS = "util-linux systemd"

PV = "1.1"

SRC_URI = "git://github.com/linux-msm/qrtr.git;protocol=https;branch=master"

SRCREV = "b6b586f3d099dff7c56b69c824a1931ddad170a4"

inherit meson pkgconfig

# qrtr-ns is the QIPCRTR name service. Without it nothing registers the
# endpoints the modem advertises, so oFono's qrtrqmi plugin finds no modem at
# all on a mainline Qualcomm device - the radio is up and /dev/wwan0qmi0 exists,
# but there is no registry to look it up in.
EXTRA_OEMESON = "-Dqrtr-ns=enabled -Dsystemd-service=enabled -Dsystemd-unit-prefix=${systemd_system_unitdir}"

inherit systemd
SYSTEMD_SERVICE:${PN} = "qrtr-ns.service"
SYSTEMD_AUTO_ENABLE = "enable"
