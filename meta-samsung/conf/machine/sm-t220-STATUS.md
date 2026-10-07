# SM-T220 port status

Samsung Galaxy Tab A7 Lite Wi-Fi (SM-T220, codename gta7litewifi).
Approach: Halium (stock vendor kernel + Android container), modelled on `radon`
(MediaTek, arm64, 4.19, Android Clang r416183b) in meta-furilabs.

State (4 Oct 2026): boots LuneOS on the tablet. Confirmed on the device: the
Halium initramfs finding rootfs.img on userdata, the Android container, speaker
audio (PulseAudio route and GStreamer), HTML5 audio through umediaserver (the
Testr app) and app installs through appinstalld2. The maintainer reports
display, touch, Wi-Fi and the camera as working. Not verified: GPS, Bluetooth,
suspend/resume and battery behaviour. See "Findings from bring-up" for what had
to be fixed on the way.

## Sources used

- Kernel: github.com/gta7lite/android_kernel_samsung_ot8, branch `halium-12`,
  4.19.191, `gta7litewifi_defconfig` + the branch's `halium.config`.
- Samsung factory firmware T220XXSAEYE4 (SAMFW, 13 May 2025): boot.img,
  ramdisk fstab, dtbo, vbmeta, and the vendor/odm images out of super.img.
- LineageOS device trees in the same org (`android_device_samsung_mt6765-common`,
  `..._gta7litewifi`, branch lineage-22.2), for the values the firmware does
  not show (super size, density).

## Confirmed against the stock firmware

- Boot image: header v2, page 2048, kernel 0x40080000, ramdisk 0x51b00000,
  tags/dtb 0x47880000, os_version 0x18000195 (Android 12, patch 2025-05)
- The dtb section is a dt_table (0xd7b7ab1e), one entry id 0, not a bare FDT;
  `do_make_dt_table` wraps the kernel's own mt6765.dtb in it, and the same layout
  rebuilds Samsung's table byte for byte
- Single-slot (not A/B): /dev/block/platform/bootdevice/by-name/{boot,recovery,
  dtbo,vbmeta,...}; system/system_ext/vendor/product/odm are logical partitions
  in super (LP table lists exactly those); userdata is f2fs
- Vendor: Android 12 (sdk 31, VNDK 31), ro.product.vendor.device=gta7litewifi,
  model SM-T220, ro.hardware.hwcomposer=mtk_common, ro.hardware.egl=mtk,
  PowerVR userspace behind MediaTek's libGLES_meow wrapper
- The kernel's own mt6765.dts console is ttyS0,921600n1 (8250/MT6577)

## Read from the tablet itself (adb, 3 Oct 2026)

The tablet in question is serial R9JRC01R3YW (gta7litewifixx). A Nexus 5 was on
the same host, so always address it with `adb -s`.

- Running firmware T220XXS6CWL2: Android 13 (sdk 33), vendor Android 12 / VNDK 31,
  kernel 4.19.191 built with Android clang 11.0.1 (r383902), 8 Dec 2023. The
  factory image used for the boot-image work above is the newer T220XXSAEYE4.
- Lock state: ro.boot.flash.locked=1, vbmeta device_state=locked, verifiedboot
  green, sys.oem_unlock_allowed=1 (OEM unlocking enabled in settings, bootloader
  NOT yet unlocked). Knox warranty bit 0.
- eMMC, not UFS: /dev/block/mmcblk0, single slot. by-name: boot=p38, recovery=p39,
  dtbo=p13, vbmeta=p14, vbmeta_system=p40, vbmeta_vendor=p41, super=p45,
  userdata=p51, lk=p12, misc=p22, cache=p48, metadata=p27.
- Display: 800x1340, xDpi 189.906 / yDpi 197.883 (driver-reported; 107.0 x 172.0 mm,
  7.98"), lcd_density 213.
- Features: wifi, bluetooth(_le), gps, 2 cameras (rear + front), touchscreen,
  accelerometer, compass, light sensor; no telephony. Sensors seen: ST lis2doc
  (accel), Memsic mmc5603 (mag), AMS tsl2540 (light).
- /dev/ttyS0 and ttyS1 exist, no ttyMT*. /proc/config.gz is readable, so the
  running kernel's config can be compared with ours.
- Power supply: mt6370_pmu_charger, mtk-gauge, mtk-master-charger.

## Restoring stock: rollback caveat

ro.boot.rp=6 on the tablet (firmware ...XXS6...), while the factory image is
...XXSAEYE4 (binary version A). Flashing that image's bootloader files raises the
bootloader's rollback value and cannot be undone, so an older ...S6... bootloader
could not be flashed afterwards. Our Odin package only touches boot, vbmeta and
userdata, not the bootloader, so it does not do this. Restoring with the YE4
BL/AP/CSC does. Prefer matching firmware for the restore if you want to keep the
option of going back to S6.

## Known difference from stock: battery charging data

Decompiling Samsung's stock dtb and the one built from this source tree shows
they differ only in battery charging data. The source tree is older than the
firmware:

- `battery_cv` and the `jeita_temp_*_cv` values: stock 0x42d560 (4.38 V), this
  tree 0x432380 (4.40 V)
- `ss,cv-ranges`: different current ranges and voltages
- stock has a `gxy_battery_ttf` node that this tree lacks

DECIDED (2 Oct 2026, by the maintainer): left as is. The 20 mV difference
(4.40 V against 4.38 V) is judged not critical and 4.40 V safe, so no patch and
no stock dtb. Recorded here so it is not rediscovered as a bug.

## Findings from bring-up

Each of these cost real time and none is visible from the symptom.

- **Boot loop at about 57 s.** Samsung's tree stubs out `hci_sock_create()` in
  `net/bluetooth/hci_sock.c`; bluebinder opens an HCI socket and hits
  `BUG_ON(!sk)`, and MediaTek's panic handler resets the device. Read from
  `/sys/fs/pstore/console-ramoops-0` in TWRP. Patch 0002 restores the file.
- **Silent speakers, three causes.** PulseAudio's real-time sink threads were
  killed by the kernel when `RLIMIT_RTTIME` expired (SIGKILL from interrupt
  context, no log line; found with the `signal_generate` tracepoint filtered to
  `sig==9`), fixed with `rlimit-rttime = -1`. The vendor
  `libaudio_param_parser` builds its parameter path in an uncleared `malloc`
  buffer, which under libhybris' glibc malloc starts with stale bytes, so the HAL
  never read `MTK_AUDIO_SPEAKER_PATH=int_hp_buf` and never switched the Awinic
  AW87xxx amplifiers on (fixed at runtime by `mtk-audio-param-fixup`). And
  `audiosystem-passthrough`, the helper `module-droid-hidl` spawns, was not in
  the image.
- **Nothing played through the webOS media server.** The kernel has
  `pidfd_open()` but not `waitid(P_PIDFD)` (patch 0003), so GLib's pidfd child
  watch failed every reap; and the shared umediaserver config named a
  `reference-media-pipeline` that no package ships, with a decoder table too small
  for what `g-media-pipeline` requests.
- **`stp_dump3` sat at about 93% CPU** in `top`. `start-android-hals.sh`
  starts every service init left stopped, including this "disabled" one;
  `deviceinfo_android_skip_services` stops it.
- **Flashing.** Odin's userdata write left holes in the rootfs on this tablet,
  so the rootfs goes in from TWRP with `adb push` (about 79 MB/s; ssh over
  Wi-Fi manages under 1 MB/s) and the boot image can be written from LuneOS with
  `dd` to `mmcblk0p38`, read back and compared. Keep the previous `rootfs.img`.
- **No proximity sensor reported.** The sensors HAL answers sensorfw's proximity
  request with "invalid sensor type: 8"; only the grip (SAR) sensors exist. Not
  checked against the spec sheet.

## Still to verify on the device

- [ ] Panel size is set from the driver-reported 107 x 172 mm (196 ppi); check with a ruler
- [ ] GPS, Bluetooth, suspend/resume and charging behaviour; `phone` stays out (no telephony)
- [ ] Whether the UART is physically reachable
- [ ] `luneos.cfg` is a first pass; validate against a running kernel
- [ ] Backlight node for `machine.conf`

Settled by bring-up: bootloader unlock plus an unsigned vbmeta with the
verification-disabled flag is what flashing needs; `deviceinfo_hardware_egl`
"mtk" works; Wi-Fi does not need `firmware_class.path` on the command line.

## Not written (nothing to port yet)

- `mtk-ril-watchdog`, bluebinder, systemd units: the Wi-Fi model has no modem.
  Add only what bring-up shows is needed.
