SUMMARY = "A tool to read/write/update android boot images"
HOMEPAGE = "https://github.com/ggrandou/abootimg"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b234ee4d69f5fce4486a80fdaf4a4263"

DEPENDS = "util-linux"

PV = "0.6+git"

# gitorious.org shut down in 2015 and its git:// endpoint is gone for good.
# ggrandou is abootimg's original author and this is the upstream repository;
# its master is the same commit the recipe already pinned, so this is purely a
# change of location - no version bump, no new code.
SRC_URI = "git://github.com/ggrandou/abootimg.git;protocol=https;branch=master"
SRCREV = "7e127fee6a3981f6b0a50ce9910267cd501e09d4"

EXTRA_OEMAKE = "-e MAKEFLAGS="

BBCLASSEXTEND = "native"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/abootimg ${D}${bindir}
    install -m 0755 ${S}/abootimg-pack-initrd ${D}${bindir}
    install -m 0755 ${S}/abootimg-unpack-initrd ${D}${bindir}
}
