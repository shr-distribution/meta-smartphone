DESCRIPTION = "Set of init services to automatically load firmware from device partitions"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=0f65aef04124f95e86aaba0fb6e0e974"

PV = "1.5.0"

SRC_URI = " \
    git://gitlab.com/postmarketOS/msm-firmware-loader.git;protocol=https;branch=master \
"
SRCREV = "462649669e4bb46fefa6b0d85b653b20871c7650"
S = "${WORKDIR}/git"

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = "msm-firmware-loader.service msm-firmware-loader-unpack.service"

do_install() {
    # Create mountpoint for the scripts
    install -d "${D}${libdir}/firmware/msm-firmware-loader"
    
    install -d "${D}${sbindir}"
    install -m 0755 msm-firmware-loader.sh "${D}${sbindir}/msm-firmware-loader.sh"
    install -m 0755 msm-firmware-loader-unpack.sh "${D}${sbindir}/msm-firmware-loader-unpack.sh"

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${S}/msm-firmware-loader.service ${D}${systemd_unitdir}/system/
    install -m 0644 ${S}/msm-firmware-loader-unpack.service ${D}${systemd_unitdir}/system/
}

FILES:${PN} = "${libdir} ${sbindir}/msm-firmware-loader.sh ${sbindir}/msm-firmware-loader-unpack.sh"
