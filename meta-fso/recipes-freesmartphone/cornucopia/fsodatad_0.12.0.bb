require ${BPN}.inc

PR = "${INC_PR}.0"

SRC_URI[md5sum] = "8b8816dea753b2320090d6318b247789"
SRC_URI[sha256sum] = "340aeffc2b8be49324d5916d1d66852b34b81495a70507670c9856fe120f1d09"

EXTRA_OECONF += "--enable-vala"
do_configure_prepend() {
    # force re-creation with new gee
    rm -f ${S}/src/lib/mbpi.c
}

SRC_URI += "file://0001-fsodatad-upgrade-to-libgee-0.8.patch"

PNBLACKLIST[fsodatad] ?= "Depends on blacklisted libfso-glib"
