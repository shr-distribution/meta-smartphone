FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# See the file itself: the MT6739 under-reports its supported commands and BlueZ
# believes it, leaving LE discovery deaf. bluebinder.service already reads
# /var/lib/environment/bluebinder/*.conf, so no drop-in is needed.
SRC_URI:append:mindphone = " file://mt6739-le-commands.conf"

do_install:append:mindphone() {
    install -d ${D}${localstatedir}/lib/environment/bluebinder
    install -m 0644 ${UNPACKDIR}/mt6739-le-commands.conf \
        ${D}${localstatedir}/lib/environment/bluebinder/
}

FILES:${PN} += "${localstatedir}/lib/environment/bluebinder"
