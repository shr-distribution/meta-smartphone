SUMMARY = "FuriLabs boot manager - the LVGL OS picker that runs in the initramfs"
DESCRIPTION = "bootman is FuriLabs' boot menu for FuriOS. It runs inside the \
initramfs, lists the operating systems installed in LVM logical volumes, and on \
selection flashes that OS's own boot image to the boot partition and reboots \
into it. LuneOS ships it so that a phone running LuneOS can get back to FuriOS \
(or to Sailfish) without a host and a USB cable."
HOMEPAGE = "https://github.com/furilabs/bootman"
LICENSE = "GPL-3.0-or-later"
LIC_FILES_CHKSUM = "file://COPYING;md5=1ebbd3e34237af26da5dc08a4e440464"

# LVGL and lv_drivers are git submodules compiled straight into the binary
# (meson.build globs their sources), so the fetcher has to be gitsm:// rather
# than git://. Without it meson fails at configure time in run_command
# find-lvgl-sources.sh, which returns nothing.
SRC_URI = "gitsm://github.com/furilabs/bootman.git;branch=forky;protocol=https \
           file://0001-main-include-fcntl.h-for-open-and-O_RDONLY.patch \
           file://bootman.conf \
"
# furilabs/bootman forky, "CI: initial forky build", 23 Aug 2025.
SRCREV = "bd41c60381a9d0c8e8fb1af7315e319c7ddf13bf"

PV = "0.1.0+git"
S = "${UNPACKDIR}/${BP}"

DEPENDS = "libinih libinput libxkbcommon libdrm"

inherit meson pkgconfig

# minui is Android's recovery UI library (libminui), which this build system
# looks for as a pkg-config module. It does not exist in OE and is not wanted:
# it is the backend FuriOS itself selects, but it needs the Android recovery
# stack underneath. Disable it explicitly rather than relying on the "auto"
# feature quietly not finding it - an auto dependency that appears later would
# change the build without anyone asking for it.
#
# DRM is enabled, and is the right backend here: this device's panel driver
# (CONFIG_DRM_MEDIATEK, CONFIG_DRM_PANEL_TRULY_FT8756_VDO) is built into the
# kernel, so DRM comes up in the initramfs with no vendor userspace at all.
EXTRA_OEMESON = "-Dwith-minui=disabled -Dwith-drm=enabled"

# Disabling the minui *backend* is not enough to stop its driver being built.
# meson.build does not list the lv_drivers sources by hand - it runs
# find-lvgl-sources.sh, which is `find lv_drivers -name '*.c'`, so every driver
# in the submodule is compiled whatever the feature flags say. That includes
# lv_drivers/display/minui.c, whose first act is
#
#   #include "minui/minui.h"     ->  fatal error: minui/minui.h: No such file
#
# FuriLabs never see it because their Debian build has libminui-dev installed.
# Delete the file instead: with -Dwith-minui=disabled nothing references it
# (main.c guards its own include with #if USE_MINUI), so the glob simply stops
# finding it.
#
# Done in do_configure:prepend rather than as a patch because the file belongs
# to the lv_drivers submodule, not to bootman, and a patch against a submodule
# path would have to be re-rolled every time that submodule moves.
do_configure:prepend() {
    rm -f ${S}/lv_drivers/display/minui.c
}

# Ship our own config rather than the upstream default. Two differences from
# FuriLabs' debian/bootman.furios.conf, both forced by the above: backend=drm
# instead of minui, and a timeout so an unattended boot cannot sit on the menu
# forever.
do_install:append() {
    install -d ${D}${sysconfdir}
    install -m 0644 ${UNPACKDIR}/bootman.conf ${D}${sysconfdir}/bootman.conf
}

FILES:${PN} += "${sysconfdir}/bootman.conf"

# The initramfs has no package manager and no shared-library policy of its own;
# whatever bootman links against has to be installed alongside it. These are
# what ldd on the built binary resolves to - LVGL and lv_drivers are static.
RDEPENDS:${PN} = "libinput libxkbcommon libdrm"
