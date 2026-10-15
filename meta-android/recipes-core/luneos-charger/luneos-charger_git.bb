SUMMARY = "Off-mode charging screen for the Halium initramfs"
DESCRIPTION = "Shown when a charger is plugged into a powered-off device \
(androidboot.mode=charger and its vendor variants): the LuneOS logo, an \
animated battery and the charge level, on DRM/KMS or fbdev, with the \
notification LED red while charging and green when charged; a fast charger \
is shown as such. The power key wakes the screen, holding it boots LuneOS, \
unplugging powers off. Run by \
initramfs-scripts-halium's init.sh, which acts on its exit code."
HOMEPAGE = "https://github.com/webOS-ports/luneos-charger"
SECTION = "base"
# The code is Apache-2.0, and so is the logo (webOS-ports/graphics; shipped
# under Apache-2.0 in org.webosports.app.photos). The text artwork is rendered
# from Prelude, the webOS typeface luna-init ships, which is HP Proprietary
# Software under the Open webOS Software Font License - the licence luna-init
# carries for the fonts themselves.
LICENSE = "Apache-2.0 & Open-webOS-Font-License"
LIC_FILES_CHKSUM = " \
    file://LICENSE;md5=89aea4e17d99a7cacdbeed46a0096b10 \
    file://assets/Prelude_Font_LICENSE.txt;md5=82c3840b8b14267f05b45210c97eaa68 \
"
NO_GENERIC_LICENSE[Open-webOS-Font-License] = "assets/Prelude_Font_LICENSE.txt"

DEPENDS = "libpng"

PV = "2.0.0+git"
# TEMPORARY: herrie/logo-display-size (webOS-ports/luneos-charger PR #1), which
# ships the logo at 448 pixels instead of 800 (89 KB instead of 192 KB in every
# Halium initramfs) - the SM-T520's boot image was 68 KB over its 8 MiB
# partition. Back to branch=main once the PR is merged.
SRC_URI = "git://github.com/webOS-ports/luneos-charger.git;protocol=https;branch=herrie/logo-display-size"
SRCREV = "1db16dfe6a3b06b1973a5365f3b35523efc1803b"

inherit meson pkgconfig

# The googletest suites run on the build host or a device, not here.
EXTRA_OEMESON = "-Dtests=disabled"

FILES:${PN} += "${datadir}/luneos-charger"
