require ${BPN}.inc

PR = "${INC_PR}.1"

SRC_URI[md5sum] = "3f9614adcd33b2b639bb9c3e1625cb11"
SRC_URI[sha256sum] = "1ca7e6f3cac146ff8c2de665e8932a4e2847ff35ea76526e5e7fef51d7206aaf"

EXTRA_OECONF = "--enable-palmpre-hsuart"
