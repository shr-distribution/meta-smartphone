require ${BPN}.inc

PR = "${INC_PR}.1"

SRC_URI[md5sum] = "601e5aa0f7212bb20d9003e1ea18f2a6"
SRC_URI[sha256sum] = "ed06fc4eb17108695a7ffd57f989371c4c5b46290300c38ba1397e67b1d1b730"

EXTRA_OECONF += "--enable-vala"
do_configure_prepend() {
    # force re-creation with new gee
    rm -f ${S}/src/lib/audioplayer.c ${S}/src/plugins/audio/plugin.c ${S}/src/plugins/router_alsa/plugin.c ${S}/src/plugins/kernel_idle/plugin.c \
          ${S}/src/plugins/kernel_input/plugin.c
}   

SRC_URI += "file://0001-fsodeviced-upgrade-to-libgee-0.8.patch"
