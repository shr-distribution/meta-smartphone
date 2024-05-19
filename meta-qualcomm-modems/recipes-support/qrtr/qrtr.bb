SUMMARY = "QIPCRTR Name Service"
HOMEPAGE = "https://github.com/andersson/rpmsgexport"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=15329706fbfcb5fc5edcc1bc7c139da5"

DEPENDS = "util-linux"

PV = "1.1"

SRC_URI = "git://github.com/linux-msm/qrtr.git;protocol=https;branch=master"

SRCREV = "b6b586f3d099dff7c56b69c824a1931ddad170a4"
S = "${WORKDIR}/git"

inherit meson

EXTRA_OEMESON = "-Dqrtr-ns=disabled -Dsystemd-service=disabled"
