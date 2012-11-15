RDEPENDS_${PN} += "shr-splash"

inherit update-alternatives

FILES_${PN} = "${datadir}/shr-splash/themes/${PN}"
FILES_${PN} += "/boot"

LICENSE = "MIT BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=f523ab5986cc79b52a90d2ac3d5454a2"

INC_PR = "r8"

ALTERNATIVE_NAME = "shr-splash-theme"
ALTERNATIVE_LINK = "${datadir}/shr-splash/theme"
ALTERNATIVE_PATH = "${datadir}/shr-splash/themes/${PN}"
ALTERNATIVE_PRIORITY ?= "1"

SS ?= "${S}"

do_install() {
    install -d ${D}${datadir}/shr-splash/themes/${PN}
    # install /boot/logo.bmp (if available) to display a SHR logo when using the gta04-init system
    if [ -e ${SS}/logo.bmp ]; then
        install -d ${D}/boot
        install -m 0644 ${SS}/logo.bmp ${D}/boot/logo.bmp 
    fi;

    install -m 0644 ${SS}/xsplash-vga.ppm ${D}${datadir}/shr-splash/themes/${PN}/xsplash-vga.ppm
    install -m 0644 ${SS}/boot.fbz ${D}${datadir}/shr-splash/themes/${PN}/boot.fbz
    install -m 0644 ${SS}/shutdown.fbz ${D}${datadir}/shr-splash/themes/${PN}/shutdown.fbz
    install -m 0644 ${S}/name ${D}${datadir}/shr-splash/themes/${PN}/name
    install -m 0644 ${S}/author ${D}${datadir}/shr-splash/themes/${PN}/author
    install -m 0644 ${SS}/preview.png ${D}${datadir}/shr-splash/themes/${PN}/preview.png
}

