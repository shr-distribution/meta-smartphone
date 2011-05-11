# Upgraded
# use newer version then preferred by default
PREFERRED_VERSION_libmikmod = "3.2.0-beta2"
PREFERRED_VERSION_strace = "4.5.15"
PREFERRED_VERSION_emacs-x11 = "23.1"
PREFERRED_VERSION_glib-2.0 = "2.29%"
PREFERRED_VERSION_glib-2.0-native = "2.29%"

# we need new headers for libc, now OLDEST_KERNEL_linux-gnueabi = "2.6.24"
# and 2.6.31 for fsodeviced
LINUX_LIBC_HEADERS_VERSION ?= "2.6.32"

ANGSTROM_QT_VERSION ?= "4.7.2"
PREFERRED_VERSION_qt4-tools-native = "${ANGSTROM_QT_VERSION}"
PREFERRED_VERSION_qt4-tools-sdk = "${ANGSTROM_QT_VERSION}"
PREFERRED_VERSION_qt4-embedded = "${ANGSTROM_QT_VERSION}"
PREFERRED_VERSION_qt4-embedded-gles = "${ANGSTROM_QT_VERSION}"
PREFERRED_VERSION_qt4-x11-free = "${ANGSTROM_QT_VERSION}"
PREFERRED_VERSION_qt4-x11-free-gles = "${ANGSTROM_QT_VERSION}"

# git version fails to build
PREFERRED_VERSION_mupdf = "0.6"

# Downgraded
PREFERRED_VERSION_pulseaudio = "0.9.15"

# Prefer gcc from meta-oe
GCCVERSION ?= "4.5"
