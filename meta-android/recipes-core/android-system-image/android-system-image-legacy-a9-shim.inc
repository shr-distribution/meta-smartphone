# Only libpermissioncache_shim.so of the Android 9 services set (android-system-image-legacy-a9-services.inc),
# for a device that needs the shim but none of the services: a vendor whose own services stay, but which has
# libraries the 16.0 GSI's linker or VNDK will not load as they are. The library supplies
# android::PermissionCache for the vendor's copies of libgui and libmedia, and lowers the target SDK version
# of the process that loads it to 22 from a constructor, so the Android 16 linker loads libraries with text
# relocations. A device preloads it where it needs it (LD_PRELOAD in its own rc).

FILESEXTRAPATHS:prepend := "${THISDIR}/android-system-image-legacy-a9-services:"

SRC_URI += "file://halium-legacy-a9-services.tar.xz;subdir=a9"

HALIUM_LEGACY_EXTRA_FILES += "a9/halium-legacy-a9-services/lib/libpermissioncache_shim.so:/lib/libpermissioncache_shim.so:644"
