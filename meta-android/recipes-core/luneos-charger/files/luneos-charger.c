// SPDX-License-Identifier: Apache-2.0
/*
 * luneos-charger - the off-mode charging screen, run from the initramfs.
 *
 * WHY THIS EXISTS
 *
 * Plugging a charger into a powered-off phone makes the bootloader start the
 * kernel with androidboot.mode=charger. Android answers that with its own
 * charger UI. Halium's script hands the boot to Android's init for it, which on
 * a LuneOS image has nothing to run: the BlackBerry KEY2 (athena) sits on the
 * bootloader logo for as long as it is plugged in, and powers off ~20 s after
 * it is unplugged. init.sh now keeps charger-mode boots in the initramfs and
 * runs this instead.
 *
 * WHAT IT DOES
 *
 *   - Draws the LuneOS logo, a battery that fills to the charge level with a
 *     band sweeping up from it, and the percentage, on the framebuffer.
 *   - Turns the screen off after a timeout; a short press of the power key
 *     toggles it.
 *   - Holding the power key exits with EXIT_BOOT: init.sh reboots, and the warm
 *     reboot comes up as a normal boot (the bootloader only picks charger mode
 *     on a cold power-on by the charger).
 *   - Unplugging the charger exits with EXIT_UNPLUGGED: init.sh powers off.
 *   - Lights the notification LED, if the device has one: red while charging,
 *     green when charged.
 *
 * Anything it cannot do - no /dev/fb0, no power key - exits with EXIT_FAIL and
 * init.sh falls back to a headless hold. DRM-only devices therefore get the
 * hold but no screen; a KMS backend would be the next step for those.
 *
 * The percentage is the one LuneOS shows: batteryd stretches 0..95 % of the
 * gauge over 0..100 % (getUiPercent() in com.webos.service.battery), so this
 * screen says 93 % where the gauge says 89 %, exactly like the status bar
 * after boot.
 *
 * Artwork is loaded from /usr/share/luneos-charger (see gen_assets.py there);
 * if it is missing the battery and LED still work.
 */
#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <linux/fb.h>
#include <linux/input.h>
#include <poll.h>
#include <stdarg.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/ioctl.h>
#include <sys/mman.h>
#include <time.h>
#include <unistd.h>
#include <zlib.h>

#define EXIT_BOOT 10
#define EXIT_UNPLUGGED 20
#define EXIT_FAIL 1

#ifndef ASSET_DIR
#define ASSET_DIR "/usr/share/luneos-charger"
#endif
#define PSY "/sys/class/power_supply"

static int opt_timeout_ms = 10000;   /* screen-on time after a wake */
static int opt_hold_ms = 2000;       /* power-key hold that means "boot" */
static int opt_unplug_ms = 3000;     /* charger gone this long -> power off */
static int opt_level = -1;           /* --level: fake gauge level, for testing */
static int opt_no_leds;
static const char *opt_backlight;    /* --backlight: brightness file */
static int opt_brightness = -1;      /* --brightness: value for it */

/* ------------------------------------------------------------------ util */

static void logf_(const char *fmt, ...)
{
    struct timespec ts;
    va_list ap;

    clock_gettime(CLOCK_MONOTONIC, &ts);
    printf("[%5ld.%03ld] ", (long)ts.tv_sec, ts.tv_nsec / 1000000);
    va_start(ap, fmt);
    vprintf(fmt, ap);
    va_end(ap);
    putchar('\n');
    fflush(stdout);
}

static long now_ms(void)
{
    struct timespec ts;

    clock_gettime(CLOCK_MONOTONIC, &ts);
    return ts.tv_sec * 1000L + ts.tv_nsec / 1000000;
}

static int read_str(const char *path, char *buf, int len)
{
    int fd = open(path, O_RDONLY), n;

    if (fd < 0)
        return -1;
    n = read(fd, buf, len - 1);
    close(fd);
    if (n < 0)
        return -1;
    while (n > 0 && (buf[n - 1] == '\n' || buf[n - 1] == ' '))
        n--;
    buf[n] = 0;
    return n;
}

static int read_int(const char *path, int def)
{
    char buf[32];

    return read_str(path, buf, sizeof(buf)) > 0 ? atoi(buf) : def;
}

static void write_str(const char *path, const char *val)
{
    int fd = open(path, O_WRONLY);

    if (fd < 0)
        return;
    if (write(fd, val, strlen(val)) < 0)
        logf_("write %s: %s", path, strerror(errno));
    close(fd);
}

static void write_int(const char *path, int v)
{
    char buf[16];

    snprintf(buf, sizeof(buf), "%d", v);
    write_str(path, buf);
}

/* ---------------------------------------------------------------- power */

/* Supplies are told apart by their "type" attribute, as init.sh's
 * battery_capacity() does, because the names differ per vendor: "battery",
 * "usb", "dc" on Qualcomm; "mtk-master-charger", "ac" on MediaTek. */
static char bat_dir[300];

static void power_find(void)
{
    DIR *d = opendir(PSY);
    struct dirent *e;

    if (!d)
        return;
    while ((e = readdir(d)) && !bat_dir[0]) {
        char path[512], type[32];
        if (e->d_name[0] == '.')
            continue;
        snprintf(path, sizeof(path), PSY "/%s/type", e->d_name);
        if (read_str(path, type, sizeof(type)) > 0 && !strcmp(type, "Battery"))
            snprintf(bat_dir, sizeof(bat_dir), PSY "/%s", e->d_name);
    }
    closedir(d);
    logf_("battery: %s", bat_dir[0] ? bat_dir : "none found");
}

/* Charger present: any non-battery supply that says it is online. */
static int charger_online(void)
{
    DIR *d = opendir(PSY);
    struct dirent *e;
    int online = 0;

    if (!d)
        return 1;           /* cannot tell: do not power off on a guess */
    while ((e = readdir(d)) && !online) {
        char path[512], type[32];
        if (e->d_name[0] == '.')
            continue;
        snprintf(path, sizeof(path), PSY "/%s/type", e->d_name);
        if (read_str(path, type, sizeof(type)) > 0 &&
            (!strcmp(type, "Battery") || !strcmp(type, "BMS")))
            continue;
        snprintf(path, sizeof(path), PSY "/%s/online", e->d_name);
        online = read_int(path, 0) > 0;
    }
    closedir(d);
    return online;
}

static int battery_raw(void)
{
    char path[512];

    if (opt_level >= 0)
        return opt_level;
    if (!bat_dir[0])
        return -1;
    snprintf(path, sizeof(path), "%s/capacity", bat_dir);
    return read_int(path, -1);
}

/* The level as LuneOS shows it - see the comment at the top. */
static int battery_level(void)
{
    int raw = battery_raw();

    if (raw < 0)
        return raw;
    return (raw > 95 ? 95 : raw) * 100 / 95;
}

/* "Full" by any signal a device gives: LuneOS showing 100 %, the gauge's
 * status, or Qualcomm's charge_done. Not every gauge ever reports Full:
 * athena's stays "Charging" with charge_done=0 at 100 %. */
static int battery_full(void)
{
    char path[512], st[32];

    if (battery_level() >= 100)
        return 1;
    if (opt_level >= 0 || !bat_dir[0])
        return 0;
    snprintf(path, sizeof(path), "%s/status", bat_dir);
    if (read_str(path, st, sizeof(st)) > 0 && !strcmp(st, "Full"))
        return 1;
    snprintf(path, sizeof(path), "%s/charge_done", bat_dir);
    return read_int(path, 0) > 0;
}

/* ---------------------------------------------------------------- LEDs */

/* Notification LED: red while charging, green when full.
 *
 * Found by name in /sys/class/leds, covering the conventions in use:
 *   - bare colour names: "red", "green", "blue" (athena, many Qualcomm phones),
 *   - "colour:function" and "device:colour:function"
 *     (Documentation/leds/leds-class.rst): "red:status", "green:charging",
 *   - multicolour class devices ("rgb:status"), which carry multi_index and
 *     multi_intensity and are driven as one LED.
 * Names containing "charg" win over "status"/"indicator", which win over the
 * rest. Flash, torch, keyboard, button and backlight LEDs are never touched.
 * The kernel's battery-* triggers are not used: they follow status=Full,
 * which some gauges never report (see battery_full()). */
enum { LED_RED, LED_GREEN, LED_BLUE, LED_N };
static const char *led_colour[LED_N] = { "red", "green", "blue" };
static char led_dir[LED_N][300];
static int led_score[LED_N];
static char led_multi[300];

static int name_has_part(const char *name, const char *part)
{
    size_t n = strlen(part);
    const char *p = name;

    for (;;) {
        if (!strncmp(p, part, n) && (p[n] == 0 || p[n] == ':'))
            return 1;
        if (!(p = strchr(p, ':')))
            return 0;
        p++;
    }
}

/* Higher is a better match for a charge indicator; 0 means "not one". */
static int led_rank(const char *name)
{
    static const char *never[] = { "flash", "torch", "kbd", "keyboard",
                                   "button", "backlight", "lcd", "mmc",
                                   "wlan", "wifi", "bt", "camera", 0 };

    for (int i = 0; never[i]; i++)
        if (strstr(name, never[i]))
            return 0;
    if (strstr(name, "charg"))
        return 3;
    if (strstr(name, "status") || strstr(name, "indicator"))
        return 2;
    return 1;
}

static void leds_find(void)
{
    DIR *d;
    struct dirent *e;

    if (opt_no_leds)
        return;
    for (int c = 0; c < LED_N; c++)
        if (led_dir[c][0])
            led_score[c] = 100;     /* given on the command line */
    if (!(d = opendir("/sys/class/leds")))
        return;
    while ((e = readdir(d))) {
        char path[512];
        int rank;
        if (e->d_name[0] == '.' || !(rank = led_rank(e->d_name)))
            continue;
        snprintf(path, sizeof(path), "/sys/class/leds/%s/multi_index",
                 e->d_name);
        if (!access(path, F_OK)) {
            if (!led_multi[0])
                snprintf(led_multi, sizeof(led_multi), "/sys/class/leds/%s",
                         e->d_name);
            continue;
        }
        for (int c = 0; c < LED_N; c++)
            if (name_has_part(e->d_name, led_colour[c]) &&
                rank > led_score[c]) {
                snprintf(led_dir[c], sizeof(led_dir[c]), "/sys/class/leds/%s",
                         e->d_name);
                led_score[c] = rank;
            }
    }
    closedir(d);
    for (int c = 0; c < LED_N; c++)
        if (led_dir[c][0])
            logf_("led %s: %s", led_colour[c], led_dir[c]);
    if (led_multi[0])
        logf_("led multicolour: %s", led_multi);
}

static void led_attr(const char *dir, const char *attr, const char *val)
{
    char path[512];

    snprintf(path, sizeof(path), "%s/%s", dir, attr);
    write_str(path, val);
}

static void led_set(const char *dir, int on)
{
    char path[512], v[16];

    snprintf(path, sizeof(path), "%s/max_brightness", dir);
    snprintf(v, sizeof(v), "%d", on ? read_int(path, 255) : 0);
    led_attr(dir, "trigger", "none");
    led_attr(dir, "brightness", v);
}

enum { SHOW_OFF, SHOW_CHARGING, SHOW_FULL };

static void leds_show(int state)
{
    static int shown = -1;

    if (state == shown)
        return;
    shown = state;
    if (led_dir[LED_RED][0] || led_dir[LED_GREEN][0]) {
        if (led_dir[LED_BLUE][0])
            led_set(led_dir[LED_BLUE], 0);
        /* Off first, so the two never show together as amber. */
        if (led_dir[LED_RED][0] && state != SHOW_CHARGING)
            led_set(led_dir[LED_RED], 0);
        if (led_dir[LED_GREEN][0] && state != SHOW_FULL)
            led_set(led_dir[LED_GREEN], 0);
        if (led_dir[LED_RED][0] && state == SHOW_CHARGING)
            led_set(led_dir[LED_RED], 1);
        if (led_dir[LED_GREEN][0] && state == SHOW_FULL)
            led_set(led_dir[LED_GREEN], 1);
    } else if (led_multi[0]) {
        /* multi_intensity takes one value per channel, in multi_index order. */
        char idx[96], out[96] = "", path[512], *save = 0, *tok;
        snprintf(path, sizeof(path), "%s/multi_index", led_multi);
        if (read_str(path, idx, sizeof(idx)) <= 0)
            return;
        for (tok = strtok_r(idx, " ", &save); tok; tok = strtok_r(0, " ", &save)) {
            int v = (state == SHOW_CHARGING && !strcmp(tok, "red")) ||
                    (state == SHOW_FULL && !strcmp(tok, "green")) ? 255 : 0;
            snprintf(out + strlen(out), sizeof(out) - strlen(out), "%s%d",
                     out[0] ? " " : "", v);
        }
        led_attr(led_multi, "trigger", "none");
        led_attr(led_multi, "multi_intensity", out);
        led_set(led_multi, state != SHOW_OFF);
    } else {
        return;
    }
    logf_("led %s", state == SHOW_FULL ? "green" :
                    state == SHOW_CHARGING ? "red" : "off");
}

/* --------------------------------------------------------------- assets */

/* Just enough PNG for our own artwork: 8-bit greyscale or RGBA, not
 * interlaced. gen_assets.py only writes those. */
struct image {
    int w, h, bpp;
    unsigned char *px;
};

static uint32_t be32(const unsigned char *p)
{
    return (uint32_t)p[0] << 24 | (uint32_t)p[1] << 16 | (uint32_t)p[2] << 8 | p[3];
}

static int paeth(int a, int b, int c)
{
    int p = a + b - c, pa = abs(p - a), pb = abs(p - b), pc = abs(p - c);

    return pa <= pb && pa <= pc ? a : (pb <= pc ? b : c);
}

static int png_load(const char *path, struct image *img)
{
    FILE *f = fopen(path, "rb");
    unsigned char *file = 0, *idat = 0, *raw = 0;
    long size;
    size_t nidat = 0, stride;
    uLongf rawlen;
    int ok = 0;

    memset(img, 0, sizeof(*img));
    if (!f)
        goto out;
    fseek(f, 0, SEEK_END);
    size = ftell(f);
    rewind(f);
    if (size < 33 || !(file = malloc(size)) || !(idat = malloc(size)) ||
        fread(file, 1, size, f) != (size_t)size ||
        memcmp(file, "\x89PNG\r\n\x1a\n", 8))
        goto out;
    for (long p = 8; p + 12 <= size;) {
        uint32_t len = be32(file + p);
        const unsigned char *type = file + p + 4, *data = file + p + 8;
        if (len > (uint32_t)(size - p - 12))
            goto out;
        if (!memcmp(type, "IHDR", 4)) {
            img->w = be32(data);
            img->h = be32(data + 4);
            if (data[8] != 8 || data[12] != 0 ||
                (data[9] != 0 && data[9] != 6))
                goto out;
            img->bpp = data[9] == 6 ? 4 : 1;
        } else if (!memcmp(type, "IDAT", 4)) {
            memcpy(idat + nidat, data, len);
            nidat += len;
        } else if (!memcmp(type, "IEND", 4)) {
            break;
        }
        p += 12 + len;
    }
    if (!img->bpp || img->w <= 0 || img->h <= 0 || img->w > 4096 || img->h > 8192)
        goto out;
    stride = (size_t)img->w * img->bpp;
    rawlen = (stride + 1) * img->h;
    if (!(raw = malloc(rawlen)) || !(img->px = malloc(stride * img->h)) ||
        uncompress(raw, &rawlen, idat, nidat) != Z_OK ||
        rawlen != (stride + 1) * img->h)
        goto out;
    for (int y = 0; y < img->h; y++) {
        const unsigned char *in = raw + y * (stride + 1) + 1;
        unsigned char *row = img->px + y * stride;
        const unsigned char *up = y ? row - stride : 0;
        int filter = in[-1];
        for (size_t x = 0; x < stride; x++) {
            int a = x >= (size_t)img->bpp ? row[x - img->bpp] : 0;
            int b = up ? up[x] : 0;
            int c = up && x >= (size_t)img->bpp ? up[x - img->bpp] : 0;
            int v = in[x];
            switch (filter) {
            case 1: v += a; break;
            case 2: v += b; break;
            case 3: v += (a + b) / 2; break;
            case 4: v += paeth(a, b, c); break;
            }
            row[x] = v;
        }
    }
    ok = 1;
out:
    if (f)
        fclose(f);
    free(file);
    free(idat);
    free(raw);
    if (!ok) {
        free(img->px);
        memset(img, 0, sizeof(*img));
        logf_("cannot load %s", path);
    }
    return ok;
}

/* The text atlas: one 8-bit coverage image plus text.idx naming its parts. */
struct sprite {
    const unsigned char *a;     /* first pixel, rows are atlas.w apart */
    int w, h;
};

static struct image logo, atlas;
static struct sprite digits[10], pct, lbl_charging, lbl_charged, lbl_hint;

static void assets_load(void)
{
    char line[128], name[32];
    int x, y, w, h;
    FILE *f;

    png_load(ASSET_DIR "/logo.png", &logo);
    if (logo.bpp != 4)
        logo.w = logo.h = 0;
    if (!png_load(ASSET_DIR "/text.png", &atlas) || atlas.bpp != 1 ||
        !(f = fopen(ASSET_DIR "/text.idx", "r")))
        return;
    while (fgets(line, sizeof(line), f)) {
        struct sprite s, *dst = 0;
        if (sscanf(line, "%31s %d %d %d %d", name, &x, &y, &w, &h) != 5 ||
            x < 0 || y < 0 || w <= 0 || h <= 0 ||
            x + w > atlas.w || y + h > atlas.h)
            continue;
        s.a = atlas.px + (size_t)y * atlas.w + x;
        s.w = w;
        s.h = h;
        if (!strncmp(name, "glyph_", 6) && name[6] >= '0' && name[6] <= '9' && !name[7])
            dst = &digits[name[6] - '0'];
        else if (!strcmp(name, "glyph_pct")) dst = &pct;
        else if (!strcmp(name, "charging")) dst = &lbl_charging;
        else if (!strcmp(name, "charged")) dst = &lbl_charged;
        else if (!strcmp(name, "hint")) dst = &lbl_hint;
        if (dst)
            *dst = s;
    }
    fclose(f);
}

/* ------------------------------------------------------------- display */

static struct {
    int fd;
    struct fb_var_screeninfo var;
    struct fb_fix_screeninfo fix;
    uint8_t *mem;
    int w, h, stride, page, pages, on;
} fb;

static uint32_t *canvas;            /* w*h, 0x00RRGGBB, copied into fb */

/* Pages still needing a full copy; otherwise only the dirty rectangle (the
 * battery's interior, the only thing that animates) is copied. Redrawing the
 * whole 1080x1620 screen every frame managed ~10 fps on athena; this ~30. */
static int full_pages = 2;
static int dirty_x, dirty_y, dirty_w, dirty_h;

static char bl_path[300];
static int bl_on;

/* The panel backlight: athena's MDSS driver leaves it at 0 after an unblank,
 * so it has to be set explicitly. The LED-class lcd-backlight is what MDSS
 * listens to there; elsewhere a backlight-class device is the usual one. */
static void backlight_find(void)
{
    DIR *d;
    struct dirent *e;

    if (opt_backlight) {
        snprintf(bl_path, sizeof(bl_path), "%s", opt_backlight);
    } else if (!access("/sys/class/leds/lcd-backlight/brightness", W_OK)) {
        snprintf(bl_path, sizeof(bl_path), "/sys/class/leds/lcd-backlight/brightness");
    } else if ((d = opendir("/sys/class/backlight"))) {
        while ((e = readdir(d)) && !bl_path[0])
            if (e->d_name[0] != '.')
                snprintf(bl_path, sizeof(bl_path),
                         "/sys/class/backlight/%s/brightness", e->d_name);
        closedir(d);
    }
    if (bl_path[0]) {
        char max[512];
        snprintf(max, sizeof(max), "%.*s/max_brightness",
                 (int)(strrchr(bl_path, '/') - bl_path), bl_path);
        bl_on = opt_brightness >= 0 ? opt_brightness : read_int(max, 255) * 5 / 8;
        logf_("backlight: %s = %d", bl_path, bl_on);
    }
}

static int fb_open(void)
{
    fb.fd = open("/dev/fb0", O_RDWR);
    if (fb.fd < 0 && (fb.fd = open("/dev/graphics/fb0", O_RDWR)) < 0) {
        logf_("open fb0: %s", strerror(errno));
        return -1;
    }
    if (ioctl(fb.fd, FBIOGET_VSCREENINFO, &fb.var) < 0 ||
        ioctl(fb.fd, FBIOGET_FSCREENINFO, &fb.fix) < 0) {
        logf_("fb info: %s", strerror(errno));
        return -1;
    }
    if (fb.var.bits_per_pixel != 32) {
        logf_("unsupported bpp %d", fb.var.bits_per_pixel);
        return -1;
    }
    fb.w = fb.var.xres;
    fb.h = fb.var.yres;
    fb.stride = fb.fix.line_length;
    fb.pages = fb.var.yres_virtual >= fb.var.yres * 2 ? 2 : 1;
    logf_("fb0 %s %dx%d stride %d pages %d R%d G%d B%d A%d/%d", fb.fix.id,
          fb.w, fb.h, fb.stride, fb.pages, fb.var.red.offset,
          fb.var.green.offset, fb.var.blue.offset, fb.var.transp.offset,
          fb.var.transp.length);
    fb.mem = mmap(NULL, fb.fix.smem_len, PROT_READ | PROT_WRITE, MAP_SHARED,
                  fb.fd, 0);
    if (fb.mem == MAP_FAILED) {
        logf_("mmap fb0: %s", strerror(errno));
        return -1;
    }
    canvas = calloc((size_t)fb.w * fb.h, 4);
    fb.on = -1;
    return canvas ? 0 : -1;
}

static void screen_set(int on)
{
    if (on == fb.on)
        return;
    if (on) {
        if (ioctl(fb.fd, FBIOBLANK, FB_BLANK_UNBLANK) < 0)
            logf_("unblank: %s", strerror(errno));
        if (bl_path[0])
            write_int(bl_path, bl_on);
        full_pages = 2;
    } else {
        if (bl_path[0])
            write_int(bl_path, 0);
        if (ioctl(fb.fd, FBIOBLANK, FB_BLANK_POWERDOWN) < 0)
            logf_("blank: %s", strerror(errno));
    }
    fb.on = on;
    logf_("screen %s", on ? "on" : "off");
}

/* Copy the canvas into the hidden page in the framebuffer's own pixel
 * layout, then pan to it. On MDSS the pan is also what commits the frame. */
static void present(void)
{
    int page = fb.pages == 2 ? !fb.page : 0;
    uint8_t *base = fb.mem + (size_t)page * fb.h * fb.stride;
    int ro = fb.var.red.offset, go = fb.var.green.offset,
        bo = fb.var.blue.offset;
    uint32_t amask = fb.var.transp.length ? 0xffu << fb.var.transp.offset : 0;
    int x0 = 0, y0 = 0, w = fb.w, h = fb.h;

    if (full_pages > 0) {
        full_pages -= fb.pages == 2 ? 1 : 2;
    } else {
        x0 = dirty_x; y0 = dirty_y; w = dirty_w; h = dirty_h;
    }
    for (int y = y0; y < y0 + h; y++) {
        uint32_t *dst = (uint32_t *)(base + (size_t)y * fb.stride);
        const uint32_t *src = canvas + (size_t)y * fb.w;
        for (int x = x0; x < x0 + w; x++) {
            uint32_t c = src[x];
            dst[x] = ((c >> 16 & 0xff) << ro) | ((c >> 8 & 0xff) << go) |
                     ((c & 0xff) << bo) | amask;
        }
    }
    fb.var.yoffset = page * fb.h;
    fb.var.activate = FB_ACTIVATE_VBL;
    if (ioctl(fb.fd, FBIOPAN_DISPLAY, &fb.var) < 0) {
        /* Some drivers only commit on a full var write. */
        fb.var.activate = FB_ACTIVATE_NOW | FB_ACTIVATE_FORCE;
        if (ioctl(fb.fd, FBIOPUT_VSCREENINFO, &fb.var) < 0)
            logf_("pan/put: %s", strerror(errno));
    }
    fb.page = page;
}

/* ------------------------------------------------------------- drawing */

static inline uint32_t rgb(int r, int g, int b)
{
    return (uint32_t)r << 16 | (uint32_t)g << 8 | (uint32_t)b;
}

static inline uint32_t blend(uint32_t dst, uint32_t src, int a)
{
    int r = ((src >> 16 & 0xff) * a + (dst >> 16 & 0xff) * (255 - a)) / 255;
    int g = ((src >> 8 & 0xff) * a + (dst >> 8 & 0xff) * (255 - a)) / 255;
    int b = ((src & 0xff) * a + (dst & 0xff) * (255 - a)) / 255;

    return rgb(r, g, b);
}

static inline void plot(int x, int y, uint32_t c, int a)
{
    if (a && x >= 0 && y >= 0 && x < fb.w && y < fb.h)
        canvas[(size_t)y * fb.w + x] = blend(canvas[(size_t)y * fb.w + x], c, a);
}

/* Coverage (0..255) of pixel (x,y) inside a rounded rectangle, with a
 * one-pixel anti-aliased edge at the corners. */
static int rrect_cov(int x, int y, int x0, int y0, int w, int h, int r)
{
    int cx, cy;
    long dx, dy, d2, rin, rout;

    if (x < x0 || y < y0 || x >= x0 + w || y >= y0 + h)
        return 0;
    cx = x < x0 + r ? x0 + r : (x >= x0 + w - r ? x0 + w - r - 1 : x);
    cy = y < y0 + r ? y0 + r : (y >= y0 + h - r ? y0 + h - r - 1 : y);
    if (cx == x && cy == y)
        return 255;
    dx = x - cx;
    dy = y - cy;
    d2 = dx * dx + dy * dy;
    rin = (long)(r - 1) * (r - 1);
    rout = (long)r * r;
    if (d2 <= rin)
        return 255;
    if (d2 >= rout)
        return 0;
    return (int)(255 * (rout - d2) / (rout - rin));
}

static void fill_rrect(int x0, int y0, int w, int h, int r, uint32_t c)
{
    for (int y = y0; y < y0 + h; y++)
        for (int x = x0; x < x0 + w; x++)
            plot(x, y, c, rrect_cov(x, y, x0, y0, w, h, r));
}

static void draw_sprite(const struct sprite *s, int x0, int y0, uint32_t c)
{
    for (int y = 0; y < s->h; y++)
        for (int x = 0; x < s->w; x++)
            plot(x0 + x, y0 + y, c, s->a[(size_t)y * atlas.w + x]);
}

static void draw_centered(const struct sprite *s, int y0, uint32_t c)
{
    if (s->a)
        draw_sprite(s, (fb.w - s->w) / 2, y0, c);
}

static void draw_logo(int x0, int y0)
{
    for (int y = 0; y < logo.h; y++)
        for (int x = 0; x < logo.w; x++) {
            const unsigned char *s = &logo.px[((size_t)y * logo.w + x) * 4];
            plot(x0 + x, y0 + y, rgb(s[0], s[1], s[2]), s[3]);
        }
}

static void draw_percent(int level, int y0, uint32_t c)
{
    char s[16];
    int width = 0, x;

    snprintf(s, sizeof(s), "%d", level);
    for (char *p = s; *p; p++)
        if (!digits[*p - '0'].a)
            return;
    if (!pct.a)
        return;
    for (char *p = s; *p; p++)
        width += digits[*p - '0'].w;
    width += pct.w;
    x = (fb.w - width) / 2;
    for (char *p = s; *p; p++) {
        draw_sprite(&digits[*p - '0'], x, y0, c);
        x += digits[*p - '0'].w;
    }
    draw_sprite(&pct, x, y0, c);
}

/* Layout for a portrait screen; everything is centred horizontally and placed
 * off the screen height (tuned on athena's 1080x1620). */
#define BAT_W 240
#define BAT_H 400
#define BAT_R 34
#define BAT_EDGE 12
#define BAT_GAP 10
#define NUB_W 96
#define NUB_H 28

static int lay_logo_y, lay_bat_y, lay_pct_y, lay_label_y, lay_hint_y;

static void layout(void)
{
    int s = fb.h, glyph_h = digits[0].a ? digits[0].h : s / 8;

    lay_logo_y = s * 9 / 100;
    lay_bat_y = lay_logo_y + (logo.h ? logo.h : s / 6) + s * 6 / 100;
    lay_pct_y = lay_bat_y + BAT_H + s * 3 / 100;
    lay_label_y = lay_pct_y + glyph_h + s / 100;
    lay_hint_y = s - s * 8 / 100 - lbl_hint.h;
}

/* One frame; phase runs 0..999 over the animation cycle. Only the battery's
 * interior animates, so the rest is redrawn when the level or state change. */
static void render(int level, int full, int phase)
{
    static int last_level = -2, last_full = -1;
    int bx = (fb.w - BAT_W) / 2, by = lay_bat_y;
    int ix = bx + BAT_EDGE + BAT_GAP, iy = by + BAT_EDGE + BAT_GAP;
    int iw = BAT_W - 2 * (BAT_EDGE + BAT_GAP);
    int ih = BAT_H - 2 * (BAT_EDGE + BAT_GAP);
    int ir = BAT_R - BAT_EDGE - BAT_GAP / 2;
    int lvl = level < 0 ? 0 : (level > 100 ? 100 : level);
    int low = !full && level >= 0 && level < 15;
    int fill_top, sweep_top;
    uint32_t outline = rgb(0xd8, 0xe2, 0xee), label = rgb(0xa8, 0xb8, 0xcc);

    dirty_x = ix; dirty_y = iy; dirty_w = iw; dirty_h = ih;
    if (level != last_level || full != last_full) {
        last_level = level;
        last_full = full;
        full_pages = 2;
        memset(canvas, 0, (size_t)fb.w * fb.h * 4);
        draw_logo((fb.w - logo.w) / 2, lay_logo_y);
        /* Battery body: outline ring, then the terminal nub. */
        fill_rrect(bx, by, BAT_W, BAT_H, BAT_R, outline);
        fill_rrect(bx + BAT_EDGE, by + BAT_EDGE, BAT_W - 2 * BAT_EDGE,
                   BAT_H - 2 * BAT_EDGE, BAT_R - BAT_EDGE, 0);
        fill_rrect((fb.w - NUB_W) / 2, by - NUB_H + 6, NUB_W, NUB_H, 10,
                   outline);
        if (level >= 0)
            draw_percent(level, lay_pct_y, rgb(0xff, 0xff, 0xff));
        draw_centered(full ? &lbl_charged : &lbl_charging, lay_label_y, label);
        draw_centered(&lbl_hint, lay_hint_y, rgb(0x6c, 0x78, 0x88));
    }

    /* The interior sits on the black gap inside the outline ring. */
    for (int y = iy; y < iy + ih; y++)
        memset(&canvas[(size_t)y * fb.w + ix], 0, (size_t)iw * 4);

    /* Charge in the logo's blues (red-orange when low), and a dimmer band
     * sweeping from the level to the top while it is still charging. */
    fill_top = iy + ih - ih * lvl / 100;
    sweep_top = full ? fill_top : fill_top - (fill_top - iy) * phase / 1000;
    for (int y = iy; y < iy + ih; y++) {
        int t = (y - iy) * 255 / ih;        /* 0 at the top, 255 at the bottom */
        uint32_t c;
        if (y >= fill_top)
            c = low ? rgb(0xff, 0x6a - t / 6, 0x2a)
                    : rgb(0x4f - t * 0x3b / 255, 0xc8 - t * 0x78 / 255,
                          0xff - t * 0x4b / 255);
        else if (y >= sweep_top)
            c = low ? rgb(0x70, 0x30, 0x18) : rgb(0x1c, 0x46, 0x70);
        else
            continue;
        for (int x = ix; x < ix + iw; x++)
            plot(x, y, c, rrect_cov(x, y, ix, iy, iw, ih, ir));
    }
}

/* --------------------------------------------------------------- input */

#define MAX_IN 16
static struct pollfd in_fds[MAX_IN];
static int n_in;

static int has_key(int fd, int code)
{
    unsigned long bits[KEY_MAX / (8 * sizeof(long)) + 1];

    memset(bits, 0, sizeof(bits));
    if (ioctl(fd, EVIOCGBIT(EV_KEY, sizeof(bits)), bits) < 0)
        return 0;
    return !!(bits[code / (8 * sizeof(long))] & (1UL << (code % (8 * sizeof(long)))));
}

/* Every input device that can report KEY_POWER (on athena: the PMIC's
 * qpnp_pon, and nav_key). */
static int open_power_keys(void)
{
    DIR *d = opendir("/dev/input");
    struct dirent *e;

    if (!d) {
        logf_("opendir /dev/input: %s", strerror(errno));
        return 0;
    }
    while ((e = readdir(d)) && n_in < MAX_IN) {
        char path[300], name[64] = "?";
        int fd;
        if (strncmp(e->d_name, "event", 5))
            continue;
        snprintf(path, sizeof(path), "/dev/input/%s", e->d_name);
        if ((fd = open(path, O_RDONLY | O_NONBLOCK)) < 0)
            continue;
        if (!has_key(fd, KEY_POWER)) {
            close(fd);
            continue;
        }
        ioctl(fd, EVIOCGNAME(sizeof(name)), name);
        logf_("power key on %s (%s)", path, name);
        in_fds[n_in].fd = fd;
        in_fds[n_in].events = POLLIN;
        n_in++;
    }
    closedir(d);
    return n_in;
}

/* ---------------------------------------------------------------- main */

static void usage(void)
{
    fprintf(stderr,
            "usage: luneos-charger [--timeout ms] [--hold ms] [--unplug ms]\n"
            "         [--backlight file] [--brightness n] [--level n]\n"
            "         [--led-red dir] [--led-green dir] [--led-blue dir] [--no-leds]\n");
    exit(2);
}

int main(int argc, char **argv)
{
    long wake_until, press_at = -1, unplug_at = -1, start, polled;
    int level, full;

    for (int i = 1; i < argc; i++) {
        const char *a = argv[i];
        if (!strcmp(a, "--no-leds")) {
            opt_no_leds = 1;
            continue;
        }
        if (i + 1 >= argc)
            usage();
        if (!strcmp(a, "--timeout")) opt_timeout_ms = atoi(argv[++i]);
        else if (!strcmp(a, "--hold")) opt_hold_ms = atoi(argv[++i]);
        else if (!strcmp(a, "--unplug")) opt_unplug_ms = atoi(argv[++i]);
        else if (!strcmp(a, "--backlight")) opt_backlight = argv[++i];
        else if (!strcmp(a, "--brightness")) opt_brightness = atoi(argv[++i]);
        else if (!strcmp(a, "--level")) opt_level = atoi(argv[++i]);
        else if (!strcmp(a, "--led-red")) snprintf(led_dir[LED_RED], sizeof(led_dir[0]), "%s", argv[++i]);
        else if (!strcmp(a, "--led-green")) snprintf(led_dir[LED_GREEN], sizeof(led_dir[0]), "%s", argv[++i]);
        else if (!strcmp(a, "--led-blue")) snprintf(led_dir[LED_BLUE], sizeof(led_dir[0]), "%s", argv[++i]);
        else usage();
    }

    if (fb_open() < 0)
        return EXIT_FAIL;
    if (!open_power_keys()) {
        logf_("no power key found");
        return EXIT_FAIL;
    }
    assets_load();
    layout();
    backlight_find();
    power_find();
    leds_find();

    screen_set(1);
    start = polled = now_ms();
    wake_until = start + opt_timeout_ms;
    level = battery_level();
    full = battery_full();
    leds_show(full ? SHOW_FULL : SHOW_CHARGING);
    logf_("battery %d%% (gauge %d%%) %s, charger %s", level, battery_raw(),
          full ? "full" : "charging", charger_online() ? "online" : "offline");

    for (;;) {
        long t = now_ms();
        int timeout;

        if (!charger_online()) {
            if (unplug_at < 0)
                unplug_at = t;
            else if (t - unplug_at >= opt_unplug_ms) {
                logf_("charger removed for %d ms - exiting to power off",
                      opt_unplug_ms);
                screen_set(0);
                leds_show(SHOW_OFF);
                return EXIT_UNPLUGGED;
            }
        } else {
            unplug_at = -1;
        }

        if (press_at >= 0 && t - press_at >= opt_hold_ms) {
            logf_("power key held %d ms - exiting to boot", opt_hold_ms);
            screen_set(0);
            leds_show(SHOW_OFF);
            return EXIT_BOOT;
        }

        if (fb.on && t >= wake_until && press_at < 0)
            screen_set(0);

        /* Battery state at most once a second, screen on or off: the LED
         * needs it while the screen is dark too. */
        if (t - polled >= 1000) {
            int n = battery_level(), f = battery_full();
            polled = t;
            if (n != level || f != full) {
                level = n;
                full = f;
                logf_("battery %d%% (gauge %d%%) %s", level, battery_raw(),
                      full ? "full" : "charging");
            }
            leds_show(full ? SHOW_FULL : SHOW_CHARGING);
        }

        if (fb.on) {
            render(level, full, (int)((t - start) % 1600) * 1000 / 1600);
            present();
            timeout = 33 - (int)(now_ms() - t);     /* ~30 fps */
            if (timeout < 1)
                timeout = 1;
        } else {
            timeout = press_at >= 0 ? 50 : 1000;
        }

        if (poll(in_fds, n_in, timeout) <= 0)
            continue;
        for (int i = 0; i < n_in; i++) {
            struct input_event ev;
            if (!(in_fds[i].revents & POLLIN))
                continue;
            while (read(in_fds[i].fd, &ev, sizeof(ev)) == sizeof(ev)) {
                if (ev.type != EV_KEY || ev.code != KEY_POWER)
                    continue;
                t = now_ms();
                if (ev.value == 1) {
                    press_at = t;
                } else if (ev.value == 0 && press_at >= 0) {
                    /* A short press toggles the screen. */
                    press_at = -1;
                    if (fb.on) {
                        screen_set(0);
                    } else {
                        screen_set(1);
                        wake_until = t + opt_timeout_ms;
                    }
                }
            }
        }
    }
}
