/*
 * Raise the CPU frequency when the HP TouchPad's screen is touched.
 *
 * Touches reach the host through the "HPTouchpad" uinput device that ts_srv in the Android container
 * creates. Each touch frame starts a boost pulse of the interactive governor: the governor raises the
 * CPUs to hispeed_freq for boostpulse_duration from its own thread, the way it does for load. This
 * replaces the kernel's cpu_input_boost driver, which changed the cpufreq policy synchronously from a
 * worker for every touch.
 *
 * Copyright (C) 2026 Herman van Hazendonk <github.com@herrie.org>
 * SPDX-License-Identifier: MIT
 */
#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <linux/input.h>
#include <stdio.h>
#include <string.h>
#include <sys/ioctl.h>
#include <time.h>
#include <unistd.h>

#define TOUCH_NAME "HPTouchpad"
#define GOV_DIR "/sys/devices/system/cpu/cpufreq/interactive"
/* Pulse length in microseconds, and the shortest time between two pulses in milliseconds. */
#define PULSE_US "500000"
#define MIN_INTERVAL_MS 250

static int write_str(const char *path, const char *val)
{
	int fd = open(path, O_WRONLY | O_CLOEXEC);
	int ok;

	if (fd < 0)
		return -1;
	ok = write(fd, val, strlen(val)) == (ssize_t)strlen(val);
	close(fd);
	return ok ? 0 : -1;
}

static long long now_ms(void)
{
	struct timespec ts;

	clock_gettime(CLOCK_MONOTONIC, &ts);
	return ts.tv_sec * 1000LL + ts.tv_nsec / 1000000;
}

/* Open the event device whose name is TOUCH_NAME, or return -1. */
static int open_touch(void)
{
	DIR *dir = opendir("/dev/input");
	struct dirent *de;
	char path[64], name[64];
	int fd = -1;

	if (!dir)
		return -1;
	while ((de = readdir(dir))) {
		if (strncmp(de->d_name, "event", 5))
			continue;
		snprintf(path, sizeof(path), "/dev/input/%s", de->d_name);
		fd = open(path, O_RDONLY | O_CLOEXEC);
		if (fd < 0)
			continue;
		memset(name, 0, sizeof(name));
		if (ioctl(fd, EVIOCGNAME(sizeof(name) - 1), name) >= 0 && !strcmp(name, TOUCH_NAME))
			break;
		close(fd);
		fd = -1;
	}
	closedir(dir);
	return fd;
}

int main(void)
{
	struct input_event ev[64];
	long long last = 0;
	int tuned = 0, touched = 0;
	int fd;

	for (;;) {
		/* ts_srv creates the device only once the Android container runs. */
		while ((fd = open_touch()) < 0)
			sleep(2);
		fprintf(stderr, "watching " TOUCH_NAME "\n");

		for (;;) {
			ssize_t n = read(fd, ev, sizeof(ev));
			size_t i;

			if (n < 0 && errno == EINTR)
				continue;
			if (n <= 0)
				break;
			for (i = 0; i < n / sizeof(ev[0]); i++) {
				if (ev[i].type == EV_ABS || ev[i].type == EV_KEY) {
					touched = 1;
				} else if (ev[i].type == EV_SYN && ev[i].code == SYN_REPORT && touched) {
					long long t = now_ms();

					touched = 0;
					if (t - last < MIN_INTERVAL_MS)
						continue;
					last = t;
					/*
					 * The governor's files exist only while it is active, so set the pulse length
					 * again after a pulse could not be written.
					 */
					if (!tuned)
						tuned = !write_str(GOV_DIR "/boostpulse_duration", PULSE_US);
					if (write_str(GOV_DIR "/boostpulse", "1"))
						tuned = 0;
				}
			}
		}
		fprintf(stderr, TOUCH_NAME " went away\n");
		close(fd);
	}
}
