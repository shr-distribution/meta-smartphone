# Upgraded
# use newer version then preferred by default
PREFERRED_VERSION_openssl = "1.0.0%"
PREFERRED_VERSION_openssl-native = "1.0.0%"

# we need new headers for libc, now OLDEST_KERNEL_linux-gnueabi = "2.6.24"
# and 2.6.31 for fsodeviced
OLDEST_KERNEL = "2.6.24"

# We need git version of tslib as the old 1.0 release does not the support the features we
# need for some mobile handset devices
PREFERRED_VERSION_tslib = "1.0+git%"

PREFERRED_VERSION_libnl = "3%"

PREFERRED_QT_VERSION ?= "4.8.3"
PREFERRED_VERSION_qt4-tools-native = "${PREFERRED_QT_VERSION}"
PREFERRED_VERSION_qt4-native = "${PREFERRED_QT_VERSION}"
PREFERRED_VERSION_qt4-nativesdk = "${PREFERRED_QT_VERSION}"
PREFERRED_VERSION_qt4-tools-sdk = "${PREFERRED_QT_VERSION}"
PREFERRED_VERSION_qt4-tools-nativesdk = "${PREFERRED_QT_VERSION}"
PREFERRED_VERSION_qt4-embedded = "${PREFERRED_QT_VERSION}"
PREFERRED_VERSION_qt4-embedded-gles = "${PREFERRED_QT_VERSION}"
PREFERRED_VERSION_qt4-x11-free = "${PREFERRED_QT_VERSION}"
PREFERRED_VERSION_qt4-x11-free-gles = "${PREFERRED_QT_VERSION}"

