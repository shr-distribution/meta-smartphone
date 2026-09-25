SUMMARY = "FuriOS bootman initramfs, prebuilt, for the FuriPhone FLX1s"
DESCRIPTION = "\
The initramfs this device actually boots with. Not built here - it is FuriLabs' \
own bootman initramfs, redistributed by the UBports FLX1 port and used verbatim, \
exactly as their deviceinfo-radon does: \
\
    deviceinfo_prebuilt_boot_ramdisk=initramfs-furios-bootman-aarch64.cpio.gz \
"

# A prebuilt image of FuriLabs' Debian initramfs. It bundles GPL userspace
# (lvm2, busybox, util-linux, systemd fragments) plus FuriLabs' own bootman and
# furios-recovery; sources are FuriLabs'. Marked CLOSED because this recipe
# redistributes an opaque binary and cannot make a per-component license
# statement about it - do not ship this in a product image without checking
# that first.
LICENSE = "CLOSED"

# WHY THIS EXISTS
#
# LuneOS' own Halium initramfs does not bring this device up. FuriOS repurposes
# the vendor_boot_a partition as its persist store, keeps each installed OS in an
# LVM logical volume, and switches between them with bootman - and LuneOS'
# initramfs (Tofee's tofe/halium-9.0) has no LVM support at all, while FuriOS'
# has ~26 LVM code paths. Reimplementing that as a premount hook was tried and is
# the wrong trade: their script is proven on this exact hardware and lays the
# rootfs out the same way LuneOS expects anyway
# (/halium-system/var/lib/lxc/android/android-rootfs.img, /android,
# /userdata/android-data).
#
# So pair our kernel with their ramdisk. This is also exactly what the working
# Ubuntu Touch port does - it builds no initramfs of its own either.
#
# Pinned to the commit that last touched the file so a rebuild is reproducible
# and an upstream bump is a deliberate act, not a silent one.
#
# unpack=0 matters: the fetcher gunzips a .gz by default, and what has to reach
# the boot image is the compressed cpio exactly as published - the sha256 above
# is of the .gz, and android_bootimg_v2() writes the ramdisk section verbatim.
SRC_URI = "\
    https://gitlab.com/ubports/porting/community-ports/android12/furiphone-flx1/furiphone-krypton/-/raw/${UBPORTS_SRCREV}/initramfs-furios-bootman-aarch64.cpio.gz;downloadfilename=initramfs-furios-bootman-aarch64-${UBPORTS_SRCREV}.cpio.gz;unpack=0 \
"
UBPORTS_SRCREV = "ca191aa651d9a88b2edfeb18335a929840d3ec02"
SRC_URI[sha256sum] = "a87a76e34268d5d5fb61f10b680b14ef715b675db96aa485cd87227b61334f88"

# Nothing is unpacked, so the default S (${UNPACKDIR}/${BP}) never gets created
# and do_unpack warns about it. The single downloaded file lands in UNPACKDIR
# itself, so point S there.
S = "${UNPACKDIR}"

COMPATIBLE_MACHINE = "^radon$"
PACKAGE_ARCH = "${MACHINE_ARCH}"
INHIBIT_DEFAULT_DEPS = "1"

inherit deploy nopackages

# The name kernel_android.bbclass will look for; radon.conf points INITRAMFS_NAME
# at this instead of the initramfs-android-image one.
FURIOS_INITRAMFS_NAME ?= "initramfs-furios-bootman-${MACHINE}.cpio.gz"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_deploy() {
    install -d ${DEPLOYDIR}
    install -m 0644 \
        ${UNPACKDIR}/initramfs-furios-bootman-aarch64-${UBPORTS_SRCREV}.cpio.gz \
        ${DEPLOYDIR}/${FURIOS_INITRAMFS_NAME}
}
addtask deploy before do_build after do_compile
