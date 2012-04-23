require ${BPN}.inc

SRC_URI += " \
           file://oeventsd-use-opimd-signals.patch \
           file://0001-oeventsd-workaround-buggy-kernel-to-get-full-vibrati.patch \
           file://${PN}.service \
"

SRC_URI[md5sum] = "c69db48f9203de9ac6c932cc469b61cd"
SRC_URI[sha256sum] = "a65298ebf0edba6469280e56f93b0e6df088b9a4f76d9f4520dac3521d183612"
