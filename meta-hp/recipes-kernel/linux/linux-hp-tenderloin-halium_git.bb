require recipes-kernel/linux/linux.inc
require recipes-kernel/linux/halium-kernel.inc
require recipes-kernel/linux/halium-kernel-3.4.inc

DESCRIPTION = "Linux kernel for HP Touchpad (Halium based)"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "tenderloin-halium"

# kernel.bbclass sets S = "${STAGING_KERNEL_DIR}", and do_symlink_kernsrc only
# moves the unpacked tree there when the recipe points S somewhere else.
S = "${UNPACKDIR}/${BP}"

# herrie/tenderloin-3.4-gsi: tenderloin/3.4/halium-9.0 plus the fixes of a comparison with HP's 2.6.35
# kernel (none of them run on a TouchPad yet):
#  - the multimedia ION heap kept inside SMI (it ran 2 MB past its end);
#  - hsuart_tty wakes the line discipline when a transmit buffer is free, so Bluetooth (BCSP over
#    /dev/ttyHS0) no longer stalls once the UART's buffers fill;
#  - the gyroscope power-up sequence of 2.6.35, and PM8058 L12 back at 1.8V;
#  - PM8901 MPP0 configured and the external 5V switched for USB host power;
#  - the lm8502 vibrator reports its remaining time in milliseconds, not nanoseconds.
# It also has renameat2 for the flags == 0 case, which Android 16's bionic needs; halium-kernel-3.4.inc
# adds it as a patch for the other 3.4 kernels, so say that this one has it.
KERNEL_HAS_RENAMEAT2 = "1"
# Further commits on the branch, so not taken from halium-kernel-3.4.inc's patches:
#  - PR_SET_VMA names only the part of each VMA inside the range (Android's init crashed the kernel in
#    rb_insert_color);
#  - Bluetooth from the Linux backports 4.2-rc7 stack with BCSP (the 3.4 stack's mgmt 1.0 never answers
#    StartDiscovery);
#  - no runtime debug options (DEBUG_OBJECTS, DEBUG_MUTEXES, DEBUG_LIST, FTRACE, SCHEDSTATS, ...);
#  - the touch input boost driver built again (it does nothing until /sys/kernel/cpu_input_boost is set);
#  - clock_gettime64 and clock_getres_time64: without them glibc makes two syscalls per time query, and
#    WebAppMgr used half a core at idle.
SRC_URI = "git://github.com/shr-distribution/linux.git;branch=herrie/tenderloin-3.4-gsi;protocol=https"

CMDLINE = "androidboot.selinux=permissive  androidboot.hardware=tenderloin"

do_configure:prepend() {
    cp -v -f ${S}/arch/arm/configs/tenderloin_android_defconfig ${WORKDIR}/defconfig
}

# The backports' Bluetooth options are not visible to kconfig until BACKPORT_LINUX is set, so the oldconfig
# pass that sets it drops them. Put them back and run oldconfig again until they stay (as sm-t520 does).
do_configure:append() {
    for pass in 1 2 3; do
        grep -E '^(# CONFIG_BT is not set|CONFIG_BACKPORT|CONFIG_CRYPTO_CMAC)' ${WORKDIR}/defconfig >> ${B}/.config
        yes '' | oe_runmake_call -C ${S} O=${B} oldconfig > /dev/null
    done
    grep -q '^CONFIG_BACKPORT_BT_HCIUART_BCSP=y' ${B}/.config || bbfatal "the backports Bluetooth options did not stay in .config"
}

do_deploy[depends] += "initramfs-android-image:do_image_complete"
DEPENDS += "u-boot-mkimage-native"
KERNEL_OUTPUT ?= "${KERNEL_OUTPUT_DIR}/${KERNEL_IMAGETYPE}"

SRCREV = "fa84927843a0802519286e3ae73092532e77373a"

LINUX_VERSION = "3.4.113"
PV = "${LINUX_VERSION}+git"

# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

INITRAMFS_NAME = "initramfs-android-image-${MACHINE}.cpio.gz"
INITRAMFS_UIMAGE = "initramfs-android-image-${MACHINE}.uImage"

do_deploy:append() {
    if [ ! -e ${DEPLOY_DIR_IMAGE}/${INITRAMFS_NAME} ] ; then
        bbfatal "Required initramfs image ${DEPLOY_DIR_IMAGE}/${INITRAMFS_NAME} is not available!"
    fi

    cp ${DEPLOYDIR}/${KERNEL_IMAGETYPE} ${B}/${KERNEL_OUTPUT}.orig

    # pack initramfs as uboot image
    echo "pack initramfs as uboot image..."
    uboot-mkimage -A arm -O Linux -T ramdisk -n 'HP Touchpad boot initrd' -C none \
        -e 0 -a 0 -d ${DEPLOY_DIR_IMAGE}/${INITRAMFS_NAME} \
        ${B}/${INITRAMFS_UIMAGE}

    # now pack kernel and initramfs together
    echo "now pack kernel and initramfs together..."
    uboot-mkimage  -A arm -O Linux -T multi -n 'HP Touchpad boot' -C none \
        -e 0 -a 0 -d ${B}/${KERNEL_OUTPUT}.orig:${B}/${INITRAMFS_UIMAGE} \
        ${DEPLOYDIR}/${KERNEL_IMAGETYPE}
}

do_install:append() {
    # make headers_install leaves kbuild's ..install.cmd bookkeeping behind, and
    # linux.inc ships everything under ${exec_prefix}/src/linux* as kernel-headers.
    # Those files record absolute command lines, which wrynose rejects as
    # "contains reference to TMPDIR [buildpaths]".
    find ${D}${exec_prefix}/src -name '..install.cmd' -delete 2>/dev/null || true
}
