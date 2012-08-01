# Upgraded
# use newer version then preferred by default
PREFERRED_VERSION_libmikmod = "3.2.0-beta2"
PREFERRED_VERSION_emacs-x11 = "23.1"
PREFERRED_VERSION_openssl = "1.0.0%"
PREFERRED_VERSION_openssl-native = "1.0.0%"
PREFERRED_VERSION_subversion = "1.7.%"
PREFERRED_VERSION_subversion-native = "1.7.%"
PREFERRED_VERSION_dbus = "1.5%"
PREFERRED_VERSION_dbus-native = "1.5%"

# we need new headers for libc, now OLDEST_KERNEL_linux-gnueabi = "2.6.24"
# and 2.6.31 for fsodeviced
OLDEST_KERNEL = "2.6.24"

# We need git version of tslib as the old 1.0 release does not the support the features we
# need for some mobile handset devices
PREFERRED_VERSION_tslib = "1.0+git%"

PREFERRED_VERSION_libnl = "3%"
