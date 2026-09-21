DESCRIPTION = "Halium init script to boot on an Android device with an Halium image"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PACKAGE_ARCH = "${MACHINE_ARCH}"
PACKAGES = "${PN}"

RDEPENDS:${PN} = "busybox-mdev e2fsprogs-e2fsck e2fsprogs-resize2fs"

SRC_URI += " \
  file://init.sh \
  file://machine.conf \
  file://distro.conf \
  git://github.com/Tofee/initramfs-tools-halium.git;branch=tofe/halium-9.0;protocol=https \
  file://0001-halium-find-the-Android-image-instead-of-assuming-whe.patch \
  file://0002-halium-size-userdata-from-sysfs-not-proc-partitions.patch \
  file://0003-halium-fail-loudly-when-the-rootfs-mount-fails.patch \
  file://functions \
  file://pkvm-modprobe \
"

SRCREV = "0a2275aafe651d19e9eb3aa7a801c4d28550298f"

do_install:append() {
    install -m 0755 ${UNPACKDIR}/init.sh ${D}/init
    install -m 0644 ${UNPACKDIR}/machine.conf ${D}/machine.conf
    install -m 0644 ${UNPACKDIR}/distro.conf ${D}/distro.conf

    install -m 0644 ${S}/scripts/halium ${D}/halium-boot.sh
    install -m 0644 ${UNPACKDIR}/functions ${D}/functions

    # The kernel execs CONFIG_MODPROBE_PATH itself, before /init, to load the
    # pKVM early modules named in kvm-arm.protected_modules
    # (arch/arm64/kvm/pkvm.c, __pkvm_request_early_module). Android's GKI
    # config sets that to /system/bin/modprobe, where stock's generic ramdisk
    # has toybox and ours had nothing - so on gs101/gs201 exynos-pd and
    # pkvm_s2mpu never registered with the hypervisor and the USB PHY later
    # took a fatal CFI panic. Harmless on any device whose kernel never asks.
    install -d ${D}/system/bin
    install -m 0755 ${UNPACKDIR}/pkvm-modprobe ${D}/system/bin/modprobe
}

FILES:${PN} += " \
    /init /machine.conf /distro.conf \
    /halium-boot.sh /functions \
    /system/bin/modprobe \
"
