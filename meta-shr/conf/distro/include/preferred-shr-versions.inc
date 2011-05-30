# Upgraded
# use newer version then preferred by default
PREFERRED_VERSION_libmikmod = "3.2.0-beta2"
PREFERRED_VERSION_emacs-x11 = "23.1"
PREFERRED_VERSION_glib-2.0 = "2.29%"
PREFERRED_VERSION_glib-2.0-native = "2.29%"

# we need new headers for libc, now OLDEST_KERNEL_linux-gnueabi = "2.6.24"
# and 2.6.31 for fsodeviced
LINUX_LIBC_HEADERS_VERSION ?= "2.6.32"

# git version fails to build
PREFERRED_VERSION_mupdf = "0.6"

# Downgraded
PREFERRED_VERSION_pulseaudio = "0.9.15"

# Prefer gcc from meta-oe
GCCVERSION ?= "4.6"
SDKGCCVERSION ?= "4.6"
