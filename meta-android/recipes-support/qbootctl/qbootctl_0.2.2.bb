SUMMARY = "Qualcomm A/B slot control for a Linux userspace"
DESCRIPTION = "qbootctl is a port of Qualcomm's bootctrl HAL to plain Linux. \
On a Qualcomm A/B device the slot's retry counter, priority and success flag \
live in the GPT partition attribute bits rather than in the misc partition, so \
they cannot be manipulated with dd; this reads and writes them properly."
HOMEPAGE = "https://github.com/linux-msm/qbootctl"
LICENSE = "GPL-3.0-only"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7475d4a045b602c247a1b641ad13d139"

# Only Qualcomm A/B devices have these GPT attributes to manage. The unit below
# is additionally conditional on the device actually being A/B at runtime, so a
# non-A/B halium machine installs it harmlessly.
COMPATIBLE_MACHINE = "^halium$"

SRC_URI = "git://github.com/linux-msm/qbootctl.git;branch=main;protocol=https \
           file://mark-boot-successful.service \
"
# Tag 0.2.2. Note this is the commit the annotated tag points at - "git
# rev-parse 0.2.2" returns the tag object, which bitbake cannot fetch.
SRCREV = "39a6e6daaf029fb0a083777679a15ea2c18f72de"

inherit meson pkgconfig systemd

SYSTEMD_SERVICE:${PN} = "mark-boot-successful.service"

do_install:append() {
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/mark-boot-successful.service ${D}${systemd_system_unitdir}
}
