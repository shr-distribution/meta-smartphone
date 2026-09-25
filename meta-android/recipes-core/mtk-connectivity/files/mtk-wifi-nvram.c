// SPDX-License-Identifier: Apache-2.0
/*
 * Push the MediaTek Wi-Fi NVRAM blob into the combo driver, the way Android's
 * WLAN HAL does - because under Halium nothing else will.
 *
 * WHY THIS EXISTS
 *
 * Powering the chip on is a write of "1" to /dev/wmtWifi, and that is what
 * mtk-connectivity-wifi.service does. But the driver refuses unless it already
 * holds the calibration data, and the failure is silent about the reason -
 * wmt_cdev_wifi.c just logs:
 *
 *     [MTK-WIFI] WIFI_write[E]: WMT turn on WIFI fail!
 *
 * The chain behind that message:
 *
 *   wlanPreCalPwrOn() (chips/common/pre_cal.c) is conninfra's pre-calibration
 *   callback, and its very first act is
 *
 *       while (g_NvramFsm != NVRAM_STATE_READY) {
 *               kalMsleep(100);
 *               if (++retryCount > MAX_NVRAM_READY_COUNT)   // 10 -> 1 second
 *                       return CONNINFRA_CB_RET_CAL_FAIL_POWER_OFF;   // == 1
 *       }
 *
 *   which is exactly the "fail [1]" conninfra then reports:
 *
 *       conninfra@(opfunc_subdrv_cal_pwr_on:1225) [opfunc_subdrv_cal_pwr_on] fail [1]
 *       conninfra@(opfunc_subdrv_cal_do_cal:1248)  [opfunc_subdrv_cal_do_cal]  fail [1]
 *
 *   g_NvramFsm only reaches NVRAM_STATE_READY in wlanNvramBufHandler(), which
 *   is registered as BUF_TYPE_NVRAM and reached by writing a buffer that starts
 *   with the 12 bytes "WR-BUF:NVRAM" to /dev/wmtWifi. On Android the WLAN HAL
 *   does that write during Wi-Fi bring-up. LuneOS has no Android framework, so
 *   on a Halium device nobody ever does, the pre-calibration times out after one
 *   second, and Wi-Fi AND Bluetooth are both dead - they share the combo chip,
 *   and the pre-cal is what powers it.
 *
 * Verified against FuriLabs' own FuriOS on the same hardware, which uses the
 * same kernel and the same vendor partition and does get Wi-Fi: there the
 * pre-calibration logs no failure at all and wlan0 appears ~1.3s later.
 *
 * ONE write() CALL. wmt_cdev_wifi.c's handler does `copy_size = count - 12;
 * buf += 12;` on the single buffer it is given, so the tag and the payload have
 * to arrive together. That is why this is C and not a shell pipeline - `printf
 * WR-BUF:NVRAM | cat - WIFI > /dev/wmtWifi` is two writes and the driver reads
 * the first twelve bytes of the blob as its tag.
 */

#include <errno.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <unistd.h>

#define WMT_WIFI_DEV "/dev/wmtWifi"
#define NVRAM_TAG    "WR-BUF:NVRAM"
#define NVRAM_TAGLEN 12  /* strlen(NVRAM_TAG); the driver hardcodes 12 too */

/* MediaTek's NVRAM layout. The BT half of the same directory is what
 * mtk-bt-address.sh already reads for the adapter MAC. */
static const char *const nvram_paths[] = {
	"/mnt/vendor/nvdata/APCFG/APRDEB/WIFI",
	"/data/nvram/APCFG/APRDEB/WIFI",      /* older MTK layout */
	NULL
};

int main(void)
{
	const char *const *p;
	const char *path = NULL;
	struct stat st;
	unsigned char *buf;
	size_t total;
	ssize_t n;
	int fd;

	for (p = nvram_paths; *p; p++) {
		if (stat(*p, &st) == 0 && st.st_size > 0) { path = *p; break; }
	}
	if (!path) {
		/* Not a MediaTek device, or nvdata is not mounted. The unit's
		 * ConditionPathExists normally keeps us from running at all. */
		fprintf(stderr, "mtk-wifi-nvram: no Wi-Fi NVRAM found, nothing to do\n");
		return 0;
	}

	/* uint16_t length in the driver's handler, and it rejects anything
	 * larger than its own g_aucNvram. Real blobs are ~6 KiB. */
	if (st.st_size > 0xffff - NVRAM_TAGLEN) {
		fprintf(stderr, "mtk-wifi-nvram: %s is %lld bytes, too large\n",
			path, (long long)st.st_size);
		return 1;
	}

	total = NVRAM_TAGLEN + (size_t)st.st_size;
	buf = malloc(total);
	if (!buf) { perror("mtk-wifi-nvram: malloc"); return 1; }
	memcpy(buf, NVRAM_TAG, NVRAM_TAGLEN);

	fd = open(path, O_RDONLY | O_CLOEXEC);
	if (fd < 0) {
		fprintf(stderr, "mtk-wifi-nvram: open %s: %s\n", path, strerror(errno));
		free(buf);
		return 1;
	}
	n = read(fd, buf + NVRAM_TAGLEN, (size_t)st.st_size);
	close(fd);
	if (n != (ssize_t)st.st_size) {
		fprintf(stderr, "mtk-wifi-nvram: short read on %s (%zd of %lld)\n",
			path, n, (long long)st.st_size);
		free(buf);
		return 1;
	}

	fd = open(WMT_WIFI_DEV, O_WRONLY | O_CLOEXEC);
	if (fd < 0) {
		fprintf(stderr, "mtk-wifi-nvram: open %s: %s\n",
			WMT_WIFI_DEV, strerror(errno));
		free(buf);
		return 1;
	}
	n = write(fd, buf, total);
	close(fd);
	free(buf);

	if (n < 0) {
		fprintf(stderr, "mtk-wifi-nvram: write %s: %s\n",
			WMT_WIFI_DEV, strerror(errno));
		return 1;
	}
	/* The driver returns the full count on success and -ENOTSUPP if no
	 * handler was registered, i.e. if the wlan driver is not up yet. */
	if ((size_t)n != total) {
		fprintf(stderr, "mtk-wifi-nvram: short write (%zd of %zu)\n", n, total);
		return 1;
	}

	printf("mtk-wifi-nvram: loaded %lld bytes from %s\n",
	       (long long)st.st_size, path);
	return 0;
}
