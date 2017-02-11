require ${BPN}.inc

PR = "${INC_PR}.0"

SRC_URI += "file://no-tests.patch"
SRC_URI += "file://0001-fsotdld-upgrade-to-gee-0.8.patch"

EXTRA_OECONF += "--enable-vala"
do_configure_prepend() {
    # force re-creation with new gee
    rm -f ${S}/src/lib/gpsreceiver.c ${S}/src/plugins/contextmanager/plugin.c ${S}/src/plugins/provider_gps_nmea/plugin.c \
          ${S}/src/plugins/sync_time/plugin.c
}

SRC_URI[md5sum] = "ce5367f615aa8d1849adf995f6e80b57"
SRC_URI[sha256sum] = "3ab0e470b122500eaae05789ef9307f6ddf36ed6545dfd7f2a50bdd59c150aba"

PNBLACKLIST[fsotdld] ?= "Depends on blacklisted libfso-glib"
