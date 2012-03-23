##############################################################
# kernel image parameters. It resides on a seperate flash partition
#
COMPATIBLE_HOST = "arm.*-linux"
COMPATIBLE_MACHINE = "om-gta01|om-gta02"

S = "${WORKDIR}/linux-${PV}"

DESCRIPTION_${PN} = "Linux kernel for the Openmoko Neo GSM Smartphones"

KERNEL_IMAGE_SYMLINK_NAME_om-gta01 = "uImage-GTA01.bin"
KERNEL_IMAGE_SYMLINK_NAME_om-gta02 = "uImage-GTA02.bin"

CMDLINE = "unused -- bootloader passes ATAG list"

# Do keep OABI compat for om-gta01 to keep the gllin binary working
ARM_KEEP_OABI_om-gta01 = "1"

# Make sure not to use thumb[-interworking]
ARM_INSTRUCTION_SET = "arm"
THUMB_INTERWORKING = "no"

################################################################
# extra stuff we need for openmoko that is not in linux.inc
#

# opkg flash update service
RDEPENDS_kernel-image += "mtd-utils"

pkg_postinst_kernel-image () {

# Get the lastest root file system value of boot args
boot_arg=`cat /proc/cmdline |tr " " "\n"|grep "root=" |tail -n 1`
has_mtdblock=`echo $boot_arg |grep mtdblock`
has_ubi=`echo $boot_arg |grep ubi`

if test "x$D" != "x"; then
        exit 1
elif test "empty$has_mtdblock" != "empty" -o "empty$has_ubi" != "empty"; then
	if [ -f ${sysconfdir}/default/flashkernel ] ; then
		echo "Upgrading Kernel in Flash"
		echo "DO NOT stop this process"

		MTD_KERNEL_PARTITION=`cat /proc/mtd | grep kernel | cut -d':' -f1`
		MTD_KERNEL_PARTITION=/dev/$MTD_KERNEL_PARTITION

		if [ "x$MTD_KERNEL_PARTITION" = "x" ] ; then
			exit 1
		fi

		if [ ! -f /${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE}-${KERNEL_VERSION} ] ; then
			exit 1
		fi

		${sbindir}/flash_eraseall $MTD_KERNEL_PARTITION
		${sbindir}/nandwrite -p $MTD_KERNEL_PARTITION /${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE}-${KERNEL_VERSION}
	else
		touch ${sysconfdir}/default/flashkernel
	fi
else
        echo "You are not booting from NAND, don't flash kernel into mtdblock."
fi
}

