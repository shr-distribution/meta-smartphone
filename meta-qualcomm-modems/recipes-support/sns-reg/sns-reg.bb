SUMMARY = "Qualcomm Sensor Registry Server"
HOMEPAGE = "https://gitlab.com/msm8996-mainline/sns-reg"
LICENSE = "GPL-3.0-or-later"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1ebbd3e34237af26da5dc08a4e440464"

DEPENDS = "qrtr"

PV = "1.1"

SRC_URI = "git://gitlab.com/msm8996-mainline/sns-reg.git;protocol=https;branch=master \
           file://meson.build \
           file://meson.options \
"

SRCREV = "ad37ad305cde8b24544cb106215fec9ae4a2b135"
S = "${WORKDIR}/git"

inherit meson
inherit pkgconfig
inherit systemd

SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = "${PN}.service"

EXTRA_OEMESON += "-Dsystemd_system_unit_dir=${systemd_system_unitdir}"

do_configure:prepend() {
    cp ${WORKDIR}/meson.build ${S}
    cp ${WORKDIR}/meson.options ${S}
}
