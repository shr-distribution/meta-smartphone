// SPDX-License-Identifier: Apache-2.0
/*
 * Initialise the MediaTek ConnInfra combo driver from glibc userspace.
 *
 * WHY THIS EXISTS
 *
 * MediaTek's connectivity drivers build two ways, and which one a port gets
 * decides whether Wi-Fi and Bluetooth need Android userspace at all.
 *
 *   As modules (CONFIG_MTK_COMBO=m) - mp01, q25
 *       drivers/misc/mediatek/connectivity/Makefile leaves
 *       MTK_WCN_REMOVE_KERNEL_MODULE undefined, so conninfra_dev_init() ends
 *       with an unguarded call to conninfra_dev_do_drv_init(). Loading the
 *       module IS the initialisation. mtk-load-modules.sh force-loading the
 *       vendor .ko files is therefore the whole bring-up, and no Android
 *       binary is on the path.
 *
 *   Built in (CONFIG_MTK_COMBO=y) - radon
 *       The same Makefile defines MTK_WCN_REMOVE_KERNEL_MODULE, and that call
 *       is wrapped in `#ifndef MTK_WCN_REMOVE_KERNEL_MODULE`. The driver
 *       registers /dev/conninfra_dev at boot and then deliberately does
 *       nothing. It waits for userspace to send CONNINFRA_IOCTL_DO_MODULE_INIT.
 *       Until that arrives g_conninfra_init_status stays CONNINFRA_INIT_NOT_START
 *       and every other ioctl falls out of a wait_event_timeout() with -EIO.
 *
 * On the vendor's own Android that ioctl comes from /vendor/bin/conninfra_loader.
 * Under Halium that binary runs inside the container against the GSI's /system,
 * and on radon it SIGSEGVs in liblog before it gets there - so the combo chip is
 * never initialised, and wlan0 and /dev/stpbt never come to life.
 *
 * There is no reason to go through Android for this. The ioctl takes no
 * arguments and the kernel does all the work behind it:
 *
 *     conninfra_conf_init()          read the platform config (request_firmware)
 *     consys_hw_init()               power/clock/reset for the CONSYS block
 *     conninfra_core_init()
 *     ... register fb/devapc/pmic/thermal/power-throttling callbacks ...
 *     g_conninfra_init_status = CONNINFRA_INIT_DONE
 *     do_connectivity_driver_init(chipid)    <- wlan, bt, gps and fm sub-drivers
 *
 * That last line is the payoff: one ioctl brings up every sub-driver, which is
 * exactly what conninfra_loader is for. It is synchronous, it reports failure
 * through its return value, and conninfra_dev_do_drv_init() guards itself with
 * a static init_done, so running this twice is harmless.
 *
 * Safe to ship on every Halium machine. On a module-mode MediaTek device the
 * ioctl handler returns 0 immediately ("KO mode") because initialisation
 * already happened at insmod; on a non-MediaTek device /dev/conninfra_dev does
 * not exist and we exit 0 without complaint. The systemd unit has a
 * ConditionPathExists on top of that, so normally we are not even started.
 */

#include <errno.h>
#include <fcntl.h>
#include <stdio.h>
#include <string.h>
#include <sys/ioctl.h>
#include <unistd.h>

/* conninfra/src/conninfra_dev.c */
#define CONNINFRA_DEV_PATH "/dev/conninfra_dev"
#define CONNINFRA_DEV_IOC_MAGIC 0xc2
#define CONNINFRA_IOCTL_GET_CHIP_ID _IOR(CONNINFRA_DEV_IOC_MAGIC, 0, int)
#define CONNINFRA_IOCTL_DO_MODULE_INIT _IOR(CONNINFRA_DEV_IOC_MAGIC, 2, int)

int main(void)
{
	int fd, ret, chipid;

	fd = open(CONNINFRA_DEV_PATH, O_RDWR | O_CLOEXEC);
	if (fd < 0) {
		if (errno == ENOENT) {
			/* Not a ConnInfra device. Nothing to do, not an error. */
			printf("mtk-conninfra-init: no %s, nothing to do\n",
			       CONNINFRA_DEV_PATH);
			return 0;
		}
		fprintf(stderr, "mtk-conninfra-init: open %s: %s\n",
			CONNINFRA_DEV_PATH, strerror(errno));
		return 1;
	}

	ret = ioctl(fd, CONNINFRA_IOCTL_DO_MODULE_INIT, 0);
	if (ret < 0) {
		fprintf(stderr, "mtk-conninfra-init: DO_MODULE_INIT: %s\n",
			strerror(errno));
		close(fd);
		return 1;
	}

	/*
	 * Read the chip ID back as a liveness check. In built-in mode this is
	 * the first call that has to get past the CONNINFRA_INIT_DONE gate, so
	 * it proves the init above actually took rather than merely returning.
	 * A failure here is worth reporting but not worth failing the unit for:
	 * the sub-drivers are already up by this point.
	 */
	chipid = ioctl(fd, CONNINFRA_IOCTL_GET_CHIP_ID, 0);
	if (chipid < 0)
		fprintf(stderr, "mtk-conninfra-init: initialised, but GET_CHIP_ID: %s\n",
			strerror(errno));
	else
		printf("mtk-conninfra-init: ConnInfra up, chip id 0x%04x\n", chipid);

	close(fd);
	return 0;
}
