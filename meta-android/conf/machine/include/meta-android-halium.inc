TUNE_PKGARCH:append = "-halium"
PACKAGE_EXTRA_ARCHS:append = " ${TUNE_PKGARCH}"

PREFERRED_PROVIDER_virtual/libgl ?= "mesa-gl"
PREFERRED_PROVIDER_virtual/libgbm ?= "mesa-gl"
PREFERRED_PROVIDER_virtual/egl ?= "libhybris"
PREFERRED_PROVIDER_virtual/mesa ?= "libhybris"
PREFERRED_PROVIDER_virtual/libgles1 ?= "libhybris"
PREFERRED_PROVIDER_virtual/libgles2 ?= "libhybris"
PREFERRED_PROVIDER_virtual/libgles3 ?= "libhybris"

# Use Halium-based init scripts
VIRTUAL-RUNTIME_android-initramfs-scripts = "initramfs-scripts-halium"

# Use oFono from Mer/SFOS for Halium targets
VIRTUAL-RUNTIME_ofono = "ofono-halium"
VIRTUAL-RUNTIME_ofono-tests = "ofono-halium-tests"

PREFERRED_PROVIDER_virtual/android-headers = "android-headers-halium"

MACHINEOVERRIDES =. "halium:"

SKIP_RECIPE[cogl-1.0] ?= "Depends on libgles2 which isn't provided by libhybris"
SKIP_RECIPE[clutter-1.0] ?= "Depends on blacklisted cogl-1.0"
SKIP_RECIPE[mx-1.0] ?= "Depends on blacklisted clutter-1.0"
SKIP_RECIPE[clutter-gtk-1.0] ?= "Depends on blacklisted clutter-1.0"
SKIP_RECIPE[clutter-gst-3.0] ?= "Depends on blacklisted clutter-1.0"
SKIP_RECIPE[packagegroup-core-clutter] ?= "Depends on blacklisted clutter-1.0"
