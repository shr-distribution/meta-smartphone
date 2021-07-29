TUNE_PKGARCH:append = "-halium"
PACKAGE_EXTRA_ARCHS:append = " ${TUNE_PKGARCH}"

PREFERRED_PROVIDER_virtual/libgl ?= "mesa-gl"
PREFERRED_PROVIDER_virtual/libgbm ?= "mesa-gl"
PREFERRED_PROVIDER_virtual/egl ?= "libhybris"
PREFERRED_PROVIDER_virtual/mesa ?= "libhybris"
PREFERRED_PROVIDER_virtual/libgles1 ?= "libhybris"
PREFERRED_PROVIDER_virtual/libgles2 ?= "libhybris"

# Use Halium-based init scripts
VIRTUAL-RUNTIME_android-initramfs-scripts = "initramfs-scripts-halium"

PREFERRED_PROVIDER_virtual/android-headers = "android-headers-halium"

MACHINEOVERRIDES =. "halium:"

PNBLACKLIST[cogl-1.0] ?= "Depends on libgles2 which isn't provided by libhybris"
PNBLACKLIST[clutter-1.0] ?= "Depends on blacklisted cogl-1.0"
PNBLACKLIST[mx-1.0] ?= "Depends on blacklisted clutter-1.0"
PNBLACKLIST[clutter-gtk-1.0] ?= "Depends on blacklisted clutter-1.0"
PNBLACKLIST[clutter-gst-3.0] ?= "Depends on blacklisted clutter-1.0"
PNBLACKLIST[packagegroup-core-clutter] ?= "Depends on blacklisted clutter-1.0"
