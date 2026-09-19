TUNE_PKGARCH:append = "-halium"
PACKAGE_EXTRA_ARCHS:append = " ${TUNE_PKGARCH}"
# A recipe that forces ARM_INSTRUCTION_SET = "arm" (chromium/webruntime) gets
# a TUNE_PKGARCH without the thumb suffix, so it and its default-tune
# dependents publish/search sstate manifests under different -halium arches.
# Name the *other* instruction-set variant (32-bit arm only) once here, since
# two separate mechanisms need to know about it.
HALIUM_ISET_VARIANT_ARCHS = "${@' '.join(a for a in [d.expand('${ARMPKGARCH}${ARMPKGSFX_DSP}${ARMPKGSFX_EABI}${ARMPKGSFX_ENDIAN}${ARMPKGSFX_FPU}-halium'), d.expand('${ARMPKGARCH}${ARM_THUMB_SUFFIX}${ARMPKGSFX_DSP}${ARMPKGSFX_EABI}${ARMPKGSFX_ENDIAN}${ARMPKGSFX_FPU}-halium')] if a != d.getVar('TUNE_PKGARCH'))}"

# Register it so manifests resolve in both directions.
PACKAGE_EXTRA_ARCHS:append:arm = " ${HALIUM_ISET_VARIANT_ARCHS}"

# And tell the genericity check it is not a device feed. That check allows
# TUNE_PKGARCH only (see generic-rootfs-qa.bbclass), which is complete on
# arm64 but not on arm, where the distro default is thumb: webruntime-clang
# and browser-shell land in the non-thumb variant of the *same* tune, shared
# by every device on this machine rather than tied to one of them.
GENERIC_ROOTFS_ALLOWED_ARCHS:append:arm = " ${HALIUM_ISET_VARIANT_ARCHS}"

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

# The droid PulseAudio modules come from two upstream trees, split because
# the audio HAL and its XML configuration diverged too far to keep in one:
# the jb2q modules cover Android 4.1 to 10, the "modern" ones 11 and newer.
# Default to jb2q, since most halium machines here are Android 9 or older,
# and let an Android 11+ machine ask for the other one. This has to be
# explicit: the modern recipe carries the higher PV and would otherwise win
# everywhere.
PREFERRED_VERSION_pulseaudio-modules-droid ?= "17.0.88%"

MACHINEOVERRIDES =. "halium:"

SKIP_RECIPE[cogl-1.0] ?= "Depends on libgles2 which isn't provided by libhybris"
SKIP_RECIPE[clutter-1.0] ?= "Depends on blacklisted cogl-1.0"
SKIP_RECIPE[mx-1.0] ?= "Depends on blacklisted clutter-1.0"
SKIP_RECIPE[clutter-gtk-1.0] ?= "Depends on blacklisted clutter-1.0"
SKIP_RECIPE[clutter-gst-3.0] ?= "Depends on blacklisted clutter-1.0"
SKIP_RECIPE[packagegroup-core-clutter] ?= "Depends on blacklisted clutter-1.0"
