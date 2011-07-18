# Upgraded
# use newer version then preferred by default
PREFERRED_VERSION_libmikmod = "3.2.0-beta2"
PREFERRED_VERSION_emacs-x11 = "23.1"
PREFERRED_VERSION_glib-2.0 = "2.29%"
PREFERRED_VERSION_glib-2.0-native = "2.29%"
PREFERRED_VERSION_gobject-introspection = "1.29%"
PREFERRED_VERSION_gobject-introspection-native = "1.29%"
# to replace SRCREV in gobject-introspection_git recipe from oe-core, otherwise LOCALCOUNT is incremented twice every build
SRCREV_pn-gobject-introspection = "8d64bc23d2b837421ecf9c7b0e4b8d5d95ca0d21"
SRCREV_pn-gobject-introspection-native = "8d64bc23d2b837421ecf9c7b0e4b8d5d95ca0d21"

# we need new headers for libc, now OLDEST_KERNEL_linux-gnueabi = "2.6.24"
# and 2.6.31 for fsodeviced
LINUX_LIBC_HEADERS_VERSION ?= "2.6.32"

# git version fails to build
PREFERRED_VERSION_mupdf = "0.6"

PREFERRED_QT_VERSION ?= "4.7.3"
PREFERRED_VERSION_qt4-tools-native = "${PREFERRED_QT_VERSION}"
PREFERRED_VERSION_qt4-tools-sdk = "${PREFERRED_QT_VERSION}"
PREFERRED_VERSION_qt4-embedded = "${PREFERRED_QT_VERSION}"
PREFERRED_VERSION_qt4-embedded-gles = "${PREFERRED_QT_VERSION}"
PREFERRED_VERSION_qt4-x11-free = "${PREFERRED_QT_VERSION}"
PREFERRED_VERSION_qt4-x11-free-gles = "${PREFERRED_QT_VERSION}"

# Downgraded
PREFERRED_VERSION_pulseaudio = "0.9.15"

# Prefer gcc from meta-oe
GCCVERSION ?= "4.6%"
SDKGCCVERSION ?= "4.6%"
