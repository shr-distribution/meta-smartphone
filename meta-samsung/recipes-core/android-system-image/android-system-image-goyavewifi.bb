require recipes-core/android-system-image/android-system-image-legacy-gsi.inc

# The same 32-bit GSI the Galaxy A3 (2015) pins (see android-system-image-a3-2015-halium.bb for what 20261008-3
# has over the shared include's default): the sphal namespace links libbinder.so and libui.so from the VNDK
# APEX, which a vendor's gralloc needs.
HALIUM_LUNEOS_GSI16_PV = "20261008-3"
HALIUM_LUNEOS_GSI16_SHA256 = "7795e2d7756707ea28a4dfc3d4f00406afe47339abe306168bc0035d2e6712c3"
HALIUM_LUNEOS_GSI16_RELEASE = "halium-luneos-20261005"

COMPATIBLE_MACHINE = "^goyavewifi$"

# The 32-bit 16.0 GSI, on a vendor built from the LineageOS 16.0 (Android 9, VNDK 28) trees of the tablet
# (github.com/sc8830-Bringup, branches lineage-16.0: android_device_samsung_goyavewifi, ..._scx35-common,
# android_vendor_samsung_goyavewifi, android_hardware_sprd and android_kernel_samsung_sc8330; scx30g-common only has
# lineage-15.1 there, and device/samsung/sprd-common is Vuhased's lineage-15.1-wip fork).
#
# That build has no vendor partition either: it puts the vendor under /system/vendor and keeps the board's init
# scripts and fstab in the boot ramdisk. So it goes through the same conversion as the A3 (2015), the SM-T520,
# hammerhead, mako and tenderloin (halium-legacy-vendor), with the board's init scripts to move. Android 9 is
# VNDK 28, which the GSI populates (224 libraries). The earlier LineageOS 15.1 (Android 8.1) vendor does not work
# as well: the GSI declares com.android.vndk.v27 but its APEX is empty, so the 8.1 vendor had to be linked
# against VNDK 28 all the same, and its build has no composer HAL.
HALIUM_LEGACY_VNDK = "28"

# The "device" tarball is the system.img of that build laid out like a monolithic Halium build: a root holding
# system/ (the vendor under system/vendor) plus the board's init scripts, ueventd rules and fstab from the
# build's root/. The Halium 9.0 tree builds it as lunch lineage_goyavewifi-userdebug, "m -k systemimage" and
# then "m snod" (the one failing target, libsuspend, is a system library the vendor does not use); see
# goyavewifi-STATUS.md. tools/assemble-device.py of the port's working directory lays it out, with the build's
# own ownership and modes. Not published anywhere yet, so it is a local file.
# TODO: publish to webOS-ports/halium-images and use an https URL.
GOYAVEWIFI_DEVICE_TARBALL ?= "file:///home/herrie/webos/LuneOS/samsung/sm-t113/out-device-16/halium-luneos-9.0-20261008-2-goyavewifi.tar.bz2"
HALIUM_LEGACY_DEVICE_URL = "${GOYAVEWIFI_DEVICE_TARBALL}"
HALIUM_LEGACY_DEVICE_SHA256 = "c90ae61df88d71eed5d29f2ba84d942c10024fc3447cffe5293ac19c71978096"

# The board scripts of the boot ramdisk, which move to /vendor/etc/init/hw. init.sc8830.usb.rc is left out on
# purpose: the container's init must not write the USB gadget the host's adbd owns. The ueventd rules become
# /vendor/etc/ueventd.rc.
HALIUM_LEGACY_INIT_FILES = "init.sc8830.rc init.board.rc init.wifi.rc ueventd.sc8830.rc"

# The plain GSI: nothing of the old image goes into it.
HALIUM_LEGACY_GSI_AUTODETECT = "0"
