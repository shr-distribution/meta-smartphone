# SM-T520 port status

Samsung Galaxy Tab Pro 10.1 Wi-Fi (SM-T520, LineageOS codename `n2awifi`,
Samsung codename `picassowifi`). Exynos 5420, 32-bit, 3.4 kernel.
Approach: Halium with the 16.0 GSI, as hammerhead/mako/tenderloin. The vendor
is built from the exynos5420 community's LineageOS 18.1 (Android 11) trees and
made Treble-shaped by `halium-legacy-vendor`.

State: everything works on the tablet (maintainer, 8 Oct 2026). It first booted LuneOS and
showed the UI on 4 Oct 2026. See "State after the first day on hardware" at the end for what
was learnt; the sections in between were written before the first boot, and the "Not working
yet" list there is what was open on 4 Oct, since resolved.

## Why Halium and not mainline

postmarketOS has `samsung-n2awifi` on a mainline 6.18 kernel
(`gitlab.com/exynos5-mainline/linux`, tag `v6.18.33-exynos5-lts`, with an
`exynos5420-n2awifi.dts`). That was the alternative; the maintainer chose
Halium. The pmOS wiki feature table could not be read (Anubis block), so how
complete the mainline port is has not been checked.

## Sources used

- Stock firmware T520XXUAOI2 (SAMFW factory image): `boot.img`, the ramdisk's
  `fstab.universal5420`, `system.img` build.prop. Android 4.4.2 (KOT49H),
  `ro.product.device=picassowifi`, board `universal5420`, armeabi-v7a.
- exynos5420 GitHub organisation, branch `lineage-18.1`:
  `android_device_samsung_{n2awifi,n2a-common,universal5420-common}`,
  `android_kernel_samsung_exynos5420` (3.4.113), `android_vendor_samsung_*`
  (blobs), `android_hardware_samsung_slsi_exynos5420`. LineageOS's own
  `android_hardware_samsung*` and `android_device_samsung_slsi_sepolicy`.
- The Halium 11 tree on the build host, with
  `.repo/local_manifests/n2awifi.xml` declaring those projects (additive).

## Confirmed against the stock firmware

- Boot image: header v0, page 2048, kernel 0x10008000, ramdisk 0x11000000,
  second 0x10f00000, tags 0x10000100, empty cmdline, no dtb. The LineageOS 17.1
  build of the tablet uses the same layout.
- Board-file kernel: no Exynos 5420 device tree in the tree; ATAGs.
- eMMC behind dw_mmc.0 with partitions BOOT, RECOVERY, SYSTEM, PERSDATA, EFS,
  CACHE, USERDATA under `/dev/block/platform/dw_mmc.0/by-name`; the microSD
  slot is dw_mmc.2. BOOT is 8 MiB (Lineage's BoardConfig).

## Kernel

`linux-samsung-sm-t520`: the lineage-18.1 head (de0cdcf1, 10 Sep 2023) with
`lineageos_n2awifi_defconfig` and `luneos.cfg` on top.

- `SYSVIPC` (and with it `IPC_NS`) is off, as on every recent port; `USER_NS`
  and `FANOTIFY` are off too. hammerhead-halium's 3.4 kernel runs with
  `SYSVIPC`, `IPC_NS`, `USER_NS` and `FANOTIFY` all on, so those are known to be
  harmless on 3.4; leaving them off is the untested direction. If the container
  will not start, `USER_NS` or `SYSVIPC` is the one-line change.
- `RT_GROUP_SCHED` is off (it makes `sched_setscheduler(SCHED_FIFO)` fail for
  tasks in non-root cgroups), `ANDROID_PARANOID_NETWORK` is off as in the Halium
  hammerhead tree.
- No modules (`CONFIG_MODULES` is not set); bcmdhd and everything else are
  built in.
- `0001-ARM-add-renameat2-for-the-flags-0-case.patch` is meta-android's patch
  rebased onto this tree (it already has memfd_create at 385). Android 16's
  bionic needs renameat2 for every `rename()`.
- The five loop-driver fixes hammerhead carries do **not** apply here
  (`drivers/block/loop.c` differs) and are not ported. hammerhead needed them,
  or a udev rule, for a hang while mounting the APEX images; whether this tree
  has the same bug is unknown.

## Vendor

The vendor image is derived from the monolithic LineageOS 18.1 `system.img`
(vendor under `/system/vendor`) with `lib/halium/legacy_vendor.py`, VNDK 30.
A first check of the Android 11 build's own vendor image found 30 libraries
that its binaries need and that are not in the vendor, the LLNDK list or the
GSI's VNDK 30 snapshot, among them `libcsc`, `libexynos*`, `libhwjpeg`,
`libion_exynos`, `libhidltransport`, `libhwbinder` and the `vendor.lineage.*`
HIDL libraries. That is the same situation the module solves for hammerhead.

## Still to verify on the device

- [ ] That the stock bootloader (sboot) accepts the Odin package, in particular
      that the PIT names the userdata file `userdata.img`
- [ ] Whether the boot image fits the 8 MiB BOOT partition: the kernel is LZMA
      compressed, the Halium initramfs has to be small. The Odin recipe fails
      the build when it does not fit.
- [ ] Panel orientation and physical size (299 ppi is from the nominal diagonal)
- [ ] Touch driver, sensors, camera, GPS, Bluetooth (Broadcom, hciattach vs the
      Android HAL)
- [ ] `deviceinfo_hardware_egl`: "mali" for `libGLES_mali.so`
- [ ] Whether the Mali blob works through libhybris at all
- [ ] The loop-driver hang (see Kernel)
- [ ] `/proc/config.gz` of the running kernel against `luneos.cfg`

---

# State after the first day on hardware (4 Oct 2026)

Everything above this line was written before the tablet was booted; where it disagrees
with this section, this section wins.

## What runs
LuneOS boots on the SM-T520 from the Odin package / TWRP-written images: kernel,
initramfs, switch_root, systemd, the Android container, the compositor with the tablet
UI on the 2560x1600 panel, touch, Wi-Fi (scan and WPA2 connect), Bluetooth icon, battery,
sound at boot, `adb shell` over USB (dev image). Testr installs (`opkg`) and runs.

## Findings that cost a lot of time (all verified on the tablet)
- **The bootloader ignores the boot image's cmdline.** Only CONFIG_CMDLINE (recipe
  CMDLINE) and the bootloader's own string reach the kernel; it adds `oops=panic`.
- **An image whose header had `os_version` 0 and an empty cmdline never ran** (no log, a
  reset to the Galaxy Tab logo); the same kernel and ramdisk with the LineageOS header
  values did. `abootimg` cannot write `os_version`, so `kernel_android.bbclass` patches it
  into the v0 header (offset 44) and the recipe sets `ANDROID_BOOTIMG_OS_VERSION` and
  `ANDROID_BOOTIMG_CMDLINE`. Not yet known whether one of the two is enough.
- **The panel is natively landscape**, 2560x1600 (the framebuffer mode is
  `U:2560x1600p-60`). Lineage's TARGET_SCREEN_WIDTH/HEIGHT are the other way round.
- **`deviceinfo_force_hwc2="1"` is needed**, as on the SM-T220: without it the compositor
  opens Samsung's hwc1 module itself and aborts.
- **The HIDL composer@2.1 accepts one client.** A second one fails with "failed to create
  composer client" and the screen stays black with only the backlight.
- **`deviceinfo_tablet_ui="true"`** is needed for the tablet keyboard and shell
  (`luna-platform.conf` otherwise overrides luna.conf's `TabletUi=true` with false).
- **`luneos-image` has no `/etc/usb-debugging-enabled`**, so adbd and the USB gadget never
  start; `luneos-dev-image` creates it. The Odin package is built from the dev image.
- **Wi-Fi**: the bcmdhd driver reads `/system/etc/wifi/nvram_net.txt` itself (the GSI
  lacked it), the container's Android Wi-Fi HAL starts a second wpa_supplicant that takes
  wlan0 (`deviceinfo_android_skip_services="vendor.wifi_hal_legacy"`), and the driver
  refused every WPA2 connect because the host supplicant offers two AKM suites (patch 0013).
- **Vendor image**: libraries that are symlinks into /apex in the old /system/lib were
  dumped as 0-byte files into /vendor/lib (legacy_vendor.py, REV 20 fixes it).
- **The GSI has almost no free inodes**; files added to it are refused with "no free inode
  left" unless something is pruned (HALIUM_LEGACY_GSI_PRUNE).
- **Debug traps I laid myself**: a flight-recorder `init` left running after switch_root
  burned a core and made the UI sluggish; a test client holding the composer; `dd` to a
  device node that does not exist creates a plain file ("success", nothing written).

## Kernel patches (linux-samsung-sm-t520)
0001-0007 build fixes (GCC 15, 3.4), 0008 pidns inum (systemd oops), 0009 mxts.c open()
must not wait for firmware (CONFIG_VT's kbd handler opened the touchscreen from probe: a
90 s boot stall), 0010 per-net ifindex (lxc-start BUG), 0011 fimc-is open must not BUG,
0013 bcmdhd multiple AKM, 0014 MFC sysmmu fault before the MFC is set up. The old 0012
(s3c-fb debug print) is left out of SRC_URI; the file stays in the directory.

## Not working yet (as of 4 Oct 2026; all of it works now, see the update below)
- **Sound after the first minutes / "not always"**: see PENDING-TESTS.md in the scratch
  directory; a mixer-file experiment made it worse and was backed out, untested.
- **Camera**: the camera HAL needs libnativehelper.so, which the GSI does not provide, and
  the ISP reports bad calibration data (no /efs). gpsd links a missing
  SensorManager symbol.
- **Bluetooth**: bt_vendor.conf was missing from the GSI (the inode problem above).
- **GStreamer**: gst-plugin-scanner opens every /dev/video*; the V4L2 plugins are moved
  aside on the tablet until the Samsung camera and MFC drivers survive it (0011, 0014).
- **ALS and brightness**: luneos-device-config detects an ambient light sensor only over
  IIO; the nyx display module is a stub. LuneOS work.
- **UI black pill** at the top right of the launcher over the search field
  (screenshot 20261004195706.png), cause unknown.
- Container services crash-looping every few seconds: livedisplay, touch HAL, gpsd,
  camera provider.

## Live-only changes on the test tablet (not in any image)
`/usr/bin/t520-fixups.sh` and `sm-t520-fixups.service` (library binds, Wi-Fi directory
bind, stop the Android Wi-Fi HAL), `/etc/sysctl.d/90-sm-t520-nopanic.conf` (the dev image
now ships its own), `libgstvideo4linux2.so` and `libgstv4l2codecs.so` renamed to
`*.disabled`, `/usr/bin/strace-dbg` and `gdbserver-dbg`, Testr installed by `opkg`.

---

# Update (8 Oct 2026)

Everything works on the SM-T520, as reported by the maintainer after testing the tablet:
the items listed under "Not working yet" above no longer apply. This update records only
that outcome. The individual fixes for those items are not written down here, so check the
git history of this layer and of the kernel recipe for what changed, and move anything
worth keeping into the sections above.
