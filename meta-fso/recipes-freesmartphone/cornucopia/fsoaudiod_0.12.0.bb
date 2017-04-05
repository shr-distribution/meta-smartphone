require ${BPN}.inc

PR = "${INC_PR}.0"

SRC_URI[md5sum] = "498869755116fc3bf5aab7c4b8927e9b"
SRC_URI[sha256sum] = "b71449d09dec7f97c0032ec87c852e9171201742fc04f488252b719078bdd44a"

EXTRA_OECONF += "--enable-vala"
do_configure_prepend() {
    # force re-creation with new gee
    rm -f ${S}/src/lib/sessionhandler.c ${S}/src/plugins/router_alsa/plugin.c
}

SRC_URI += "file://0001-fsoaudiod-upgrade-to-libgee-0.8.patch \
    file://0001-fix-pkg-alsa.patch \
"

PNBLACKLIST[fsoaudiod] ?= "Depends on blacklisted libfso-glib - the recipe will be removed on 2017-09-01 unless the issue is fixed"
