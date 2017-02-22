DESCRIPTION = "Different utilities from Android - based on the corresponding ubuntu \
package"
SECTION = "console/utils"
LICENSE = "Apache-2.0 & GPL-2.0 & BSD-2-Clause & BSD-3-Clause"
LIC_FILES_CHKSUM = " \
  file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10 \
  file://${COMMON_LICENSE_DIR}/GPL-2.0;md5=801f80980d171dd6425610833a22dbe6 \
  file://${COMMON_LICENSE_DIR}/BSD-2-Clause;md5=8bef8e6712b1be5aa76af1ebde9d6378 \
  file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9 \
  file://${WORKDIR}/debian/copyright;md5=141efd1050596168ca05ced04e4f498b \
"

DEPENDS = "zlib openssl"
RDEPENDS_${PN} = "${PN}-conf"

# Use same version than ubuntu does here
BASE_PV = "4.2.2+git20130218"
PV = "${BASE_PV}-3ubuntu13"

BBCLASSEXTEND += "native"

SRC_URI = " \
    https://launchpad.net/ubuntu/+archive/primary/+files/android-tools_${BASE_PV}.orig.tar.xz;name=source \
    https://launchpad.net/ubuntu/+archive/primary/+files/android-tools_${PV}.debian.tar.gz;name=debian \
    file://add_adbd.patch \
    file://reboot-syscall.patch \
    file://adbd-disable-client-authentication.patch \
    file://disable-selinux-support.patch \
    file://remove-libselinux.patch;patchdir=.. \
    file://android-tools-adbd.service \
"
S = "${WORKDIR}/android-tools"

SRC_URI[source.md5sum] = "0e653b129ab0c95bdffa91410c8b55be"
SRC_URI[source.sha256sum] = "9bfba987e1351b12aa983787b9ae4424ab752e9e646d8e93771538dc1e5d932f"
SRC_URI[debian.md5sum] = "5e409d01caf3c33fc60a2100464754ff"
SRC_URI[debian.sha256sum] = "320757edc8af015f40335c41dc96bf37e2d50c9f3a40a31e64264ff6e2dba5e3"

inherit systemd

SYSTEMD_SERVICE_${PN} = "android-tools-adbd.service"

do_compile() {
    # Setting both variables below causing our makefiles to not work with implicit make
    # rules
    unset CFLAGS
    unset CPPFLAGS

    sed -i "s%^CPPFLAGS+= -I/usr/include%# we don't want to include headers from host CPPFLAGS+= -I/usr/include%g" ${WORKDIR}/debian/makefiles/ext4_utils.mk

    oe_runmake -f ${WORKDIR}/debian/makefiles/adbd.mk -C ${S}/core/adbd clean
    oe_runmake -f ${WORKDIR}/debian/makefiles/adbd.mk -C ${S}/core/adbd

    oe_runmake -f ${WORKDIR}/debian/makefiles/ext4_utils.mk -C ${S}/extras/ext4_utils clean
    oe_runmake -f ${WORKDIR}/debian/makefiles/ext4_utils.mk -C ${S}/extras/ext4_utils
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/core/adbd/adbd ${D}${bindir}
    install -m 0755 ${S}/extras/ext4_utils/make_ext4fs ${D}${bindir}

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/android-tools-adbd.service ${D}${systemd_unitdir}/system
}
