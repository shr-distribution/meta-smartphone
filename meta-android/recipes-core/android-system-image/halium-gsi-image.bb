SUMMARY = "The pinned Halium 16.0 GSI as a raw system image, for a userdata image to carry"
DESCRIPTION = "Machines that ship no Android system image in their rootfs \
(VIRTUAL-RUNTIME_android-system-image set empty) take the generic GSI from the \
userdata partition instead, as android-rootfs.img next to rootfs.img. This \
recipe deploys that file; userdataimg.bbclass puts it into the userdata image. \
Nothing is packaged."
LICENSE = "Apache-2.0 & Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

require halium-luneos-gsi-16.inc

PV = "${HALIUM_LUNEOS_GSI16_PV}"

SRC_URI = "${HALIUM_LUNEOS_GSI16_URL};subdir=gsi"
SRC_URI[sha256sum] = "${HALIUM_LUNEOS_GSI16_SHA256}"

INHIBIT_DEFAULT_DEPS = "1"
DEPENDS = "android-tools-native"

# Deployed per machine, next to the boot and userdata images it goes with.
PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit deploy nopackages

S = "${UNPACKDIR}"
B = "${WORKDIR}/build"

do_configure[noexec] = "1"
do_compile[noexec] = "1"
do_install[noexec] = "1"

do_deploy() {
    name=halium-gsi-${HALIUM_LUNEOS_GSI16_PV}-${HALIUM_LUNEOS_GSI16_ARCH}.img
    # The releases have shipped system.img both raw and sparse; the userdata
    # image needs it raw, because the initramfs loop-mounts it as it is.
    magic=`od -An -tx4 -N4 ${UNPACKDIR}/gsi/system.img | tr -d ' '`
    if [ "$magic" = "ed26ff3a" ] ; then
        simg2img ${UNPACKDIR}/gsi/system.img ${DEPLOYDIR}/$name
    else
        install -m 0644 ${UNPACKDIR}/gsi/system.img ${DEPLOYDIR}/$name
    fi
    ln -sf $name ${DEPLOYDIR}/halium-gsi-${MACHINE}.img
}

addtask deploy after do_install before do_build
