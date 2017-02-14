require ${BPN}.inc

PR = "${INC_PR}.0"

SRC_URI[md5sum] = "23d1e358f93e5684f8c3f6f2d31c7d94"
SRC_URI[sha256sum] = "258cc25474ff1d27728b432aff39fbcdf06f4c095fea927f8e1074e415e78d9c"

EXTRA_OECONF += "--enable-vala"
do_configure_prepend() {
    # force re-creation with new gee
    rm -f ${S}/fsotransport/commandqueue.c ${S}/fsoframework/subsystem.c
}
SRC_URI += "file://0001-libfsoframework-switch-to-gee-0.8-remove-unnecessary.patch"

PNBLACKLIST[libfsoframework] ?= "Depends on blacklisted libfso-glib"
