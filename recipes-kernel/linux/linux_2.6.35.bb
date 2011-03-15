require linux.inc

PR = "r4"

# Mark archs/machines that this kernel supports
DEFAULT_PREFERENCE = "-1"
DEFAULT_PREFERENCE_nokia900 = "1"

SRC_URI_nokia900 = "\
	${KERNELORG_MIRROR}/pub/linux/kernel/v2.6/${P}.tar.bz2;name=kernel \
	${KERNELORG_MIRROR}/pub/linux/kernel/v2.6/patch-${PV}.3.bz2;apply=yes;name=stable2patch \
	file://linux-2.6.36-fix-unprotected-acess-to-task-credentials.patch \
	file://linux-2.6.36-battery.patch \
	file://linux-2.6.36-battery2.patch \
	file://linux-2.6.35-stable-cherry-picks.patch \
	file://linux-2.6.35-ac-2010-08-24.patch \
	file://linux-2.6.35-ac-pending.patch \
	file://linux-2.6.35-ac-revert-mmc-hacks.patch \
	file://linux-2.6-Hacks-for-Nokia-N900.patch \
	file://linux-2.6.36-Introduce-and-enable-tsc2005-driver.patch \
	file://linux-2.6-SGX-PVR-driver-for-N900.patch \
	file://linux-2.6-Bluetooth-Support-for-n900-bluetooth-hardware.patch \
	file://linux-2.6-mfd-twl4030-Driver-for-twl4030-madc-module.patch \
	file://linux-2.6.36-omap-rx51-Platform-support-for-tsl2563-ALS.patch \
	file://linux-2.6.36-omap-rx51-Platform-support-for-lis3lv02d-acceleromet.patch \
	file://linux-2.6.36-FM-TX-headphone-TV-out-and-basic-jack-detection-supp.patch \
	file://linux-2.6-Earpiece-and-headset-support-for-N900.patch \
	file://linux-2.6.36-wl1251-Use-MODULE_ALIAS-macro-at-correct-postion-for.patch \
	file://linux-2.6-n900-modem-support.patch \
	file://linux-2.6.37-EEM-support-for-g_nokia.patch \
	file://linux-2.6-omap3isp-rx51.patch \
	file://linux-2.6.36-tidspbridge.patch \
	file://linux-2.6.37-omap3-rx51-Platform-support-for-lp5523-led-chip.patch \
	file://linux-2.6.37-omap-rx51-add-support-for-USB-chargers.patch \
	file://linux-2.6.37-power_supply-add-isp1704-charger-detection-driver.patch \
	file://linux-2.6.37-power_supply-add-types-for-USB-chargers.patch \
	file://linux-2.6-usb-musb-ignore-spurious-SESSREQ-interrupts.patch \
	file://linux-2.6.29-dont-wait-for-mouse.patch \
	file://linux-2.6-usb-uvc-autosuspend.patch \
	file://linux-2.6-usb-bt-autosuspend.patch \
	file://linux-2.6.33-vfs-tracepoints.patch \
	file://linux-2.6.33-ahci-alpm-accounting.patch \
	file://linux-2.6.35-rc4-annotate-device-pm.patch \
	file://linux-2.6.36-powertop-timer-tracing.patch \
	file://linux-2.6.35-slab-timer.patch \
	file://linux-2.6.35-dont-skew-the-tick.patch \
	file://inconsistent-mmc-fix-2.6.35.patch \
	file://defconfig "

CMDLINE_nokia900_shr = "snd-soc-rx51.hp_lim=42 snd-soc-tlv320aic3x.hp_dac_lim=6 console=tty1 root=/dev/mmcblk1p1 rootwait panic=20 debug"

SRC_URI[kernel.md5sum] = "091abeb4684ce03d1d936851618687b6"
SRC_URI[kernel.sha256sum] = "18b2e2c336032e366c942622b77302cb05fc034fb19018f086a4ebc9ed41bfcf"
SRC_URI[stable2patch.md5sum] = "a921f7789b7047b84f30a6f283cf6d07"
SRC_URI[stable2patch.sha256sum] = "94d321099f20f47dc681304a630391322e0e4d6672bb1106a621e6347c44db83"
