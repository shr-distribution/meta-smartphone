#
# This recipe is used to create Android device compatible kernel images
#
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

PACKAGE_ARCH = "${MACHINE_ARCH}"

KERNEL_IMAGEDEST ?= "boot"

inherit deploy kernel-artifact-names
do_install[depends] += "virtual/kernel:do_deploy"

do_install() {
    install -d ${D}/${KERNEL_IMAGEDEST}
    install -m 0644 ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGETYPE}.fastboot ${D}/${KERNEL_IMAGEDEST}/boot.img
}

pkg_postinst_ontarget:${PN} () {
    if [ ! -e /boot/boot.img ] ; then
        # if the boot image is not available here something went wrong and we don't
        # continue with anything that can be dangerous
        exit 1
    fi

    BOOT_PARTITION_NAMES="LNX boot boot_a KERNEL"
    for i in $BOOT_PARTITION_NAMES; do
        path=$(find /dev -name "$i"|grep disk| head -n 1)
        [ -n "$path" ] && break
    done

    if [ -z "$path" ] ; then
        echo "Boot partition does not exist!"
        exit 1
    fi

    # lk2nd, where a device boots through it, is itself a boot image at the start of the boot
    # partition and boots the real one from 512 KiB in. Writing there instead of over it: a plain
    # dd to the start replaced lk2nd on a Galaxy A3 (2015) with the kernel image, which then booted
    # without it, and the phone could only be recovered over Samsung's Download mode (8 Oct 2026).
    # The header's command line (offset 64) is "lk2nd" for lk2nd.
    if [ "$(dd if=$path bs=1 count=8 2>/dev/null)" = "ANDROID!" ] && \
       [ "$(dd if=$path bs=1 skip=64 count=5 2>/dev/null)" = "lk2nd" ]; then
        echo "Flashing the new kernel /boot/boot.img to $path behind lk2nd (512 KiB in)"
        dd if=/boot/boot.img of=$path bs=4096 seek=128
    else
        echo "Flashing the new kernel /boot/boot.img to $path"
        dd if=/boot/boot.img of=$path
    fi
    sync
}

FILES:${PN} += "/${KERNEL_IMAGEDEST}/boot.img"
