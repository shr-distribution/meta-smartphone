require ${BPN}.inc

PR = "${INC_PR}.0"

SRC_URI[md5sum] = "a52a3133883596bbd283c1ace2f766d7"
SRC_URI[sha256sum] = "7eecc8bc9e55bd3343ffec826a93b2fdf5e78af346e762d6acab45b75c7d4106"

EXTRA_OECONF = "--enable-palmpre-hsuart"
