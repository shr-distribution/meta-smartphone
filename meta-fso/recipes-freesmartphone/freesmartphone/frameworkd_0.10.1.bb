require ${BPN}.inc

SRC_URI += " \
           file://oeventsd-use-opimd-signals.patch \
           file://0001-oeventsd-workaround-buggy-kernel-to-get-full-vibrati.patch \
"

SRC_URI[md5sum] = "09ca159b3f569ab85380105b6c8658c7"
SRC_URI[sha256sum] = "45f98e814efa8e962ee8280ead54be1e1d0a242129abceab38b7d329bc4d81c5"
