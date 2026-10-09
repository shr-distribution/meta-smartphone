# Samsung Galaxy Tab 3 Lite 7.0 / Tab E Lite Wi-Fi (SM-T113) — LuneOS Halium port status

Machine `goyavewifi`, written 8 Oct 2026. Spreadtrum SC7730S (platform `sc8830`), 4x Cortex-A7, Mali-400,
1 GiB, 1024x600 panel, stock Android 4.4.4 with a 3.10.17 kernel. Approach: the 3.10 hybris kernel and the
16.0 GSI over a vendor converted by `halium-legacy-vendor`, as the A3 (2015) and the SM-T520.

State: **builds, not booted.** Nothing has been flashed or run on the tablet.

| Part | State |
|---|---|
| `linux-samsung-goyavewifi` (3.10.103) | builds with bitbake; boot image deployed (10.6 MB of the 16 MiB KERNEL partition) |
| `initramfs-android-image` | builds |
| `android-system-image-goyavewifi` | builds: GSI 20261008-3 + a vendor.img from the LineageOS **16.0** (Android 9) build, VNDK 28 |
| `luneos-device-config` adaptation | written (`goyavewifi-deviceinfo`), parses; values marked TODO(verify) are unverified |
| nyx-modules machine, systemd units | not written |
| Hardware | untested |

## What it is built from

| Part | Source |
|---|---|
| Kernel | `goyavewifi/3.10/hybris-14.1` in shr-distribution/linux: samsung-sc8830's `hybris-14.1` (f24b6282, 3.10.103) plus nine commits |
| Config | `lineage_goyavewifi_defconfig` of mertsan2007/android_kernel_samsung_sc8830 `lineage-15.1` (c32a0cc6d316) + `luneos.cfg` |
| Device tree | the three dtbs of the kernel (board revisions 2, 3, 4), packed into a Spreadtrum `SPRD` table by `mksprdboot.py` |
| Vendor | LineageOS 16.0 (Android 9) trees of github.com/sc8830-Bringup, built in the Halium 9.0 tree (below), laid out by `assemble-device.py` of the port's working directory |
| System | the plain halium_arm 16.0 GSI 20261008-3 |

## Kernel patches (what each one fixes)

- 0001 `compiler-gcc15.h`: 3.10 stops at the first include on any GCC newer than 5.
- 0002 ARM 8933/1 (upstream 790756c7e022): the Solaris `#alloc/#execinstr` section flags current binutils rejects.
- 0003 `put_user()` keeps its value in r2: GCC 15 put a constant in r3 and the `__asmeq` check stopped the build.
- 0004 `MemAvailable` in /proc/meminfo: memorymanager refuses every app launch without it.
- 0005 `board.h` includes `__board-goyavewifi.h`: the branch only included it for TSHARKGOYAVEWIFI.
- 0006 Mali Kconfig: `r4p1/Kconfig` redefines MALI400 inside the version choice, kconfig recurses and a later
  silentoldconfig drops `MALI_VER_R4P1`, so the kernel builds with **no GPU driver**.
- 0007 `COMMAND_LINE_SIZE` 2048: Halium adds the systemd cgroup switches to CONFIG_CMDLINE; at 1024 bytes the
  A3 lost `androidboot.baseband` without a warning.
- 0008 `renameat2` (flags 0 only): Android 16's bionic has no fallback. The table stops at `finit_module`, so
  `__NR_syscalls` goes to 384 (383 entries padded to a multiple of four); meta-android's 3.4 patch does not apply.
- 0009 multi-device binder (binder, hwbinder, vndbinder), from the LineageOS 15.1 tree: the branch's driver has
  one device, and the config's `ANDROID_BINDER_DEVICES` was silently dropped.

## Build findings (bitbake)

- **CROSS_COMPILE.** The tree's Makefile assigns it with `=`, overriding the environment, so nm, ar, as and
  objdump became the host's; the host nm lists the decompressor's `$d` mapping symbols as bss and the symbol
  check fails. The recipe passes `CROSS_COMPILE=${TARGET_PREFIX}` on the make command line.
- `-fcommon` for the host tools: `scripts/dtc` defines `yylloc` twice.
- `consolemap_deftbl.c` carries the absolute TMPDIR path in its banner (buildpaths QA); rewritten in do_install.
- A standalone build is not a substitute: the `renameat2` commit passed review and broke the syscall table
  count, which only the bitbake build caught.

## The vendor: LineageOS 16.0 (Android 9), built in the Halium 9.0 tree

Built from `github.com/sc8830-Bringup`, branches `lineage-16.0` unless noted, as a local manifest
(`.repo/local_manifests/goyavewifi.xml` of `/media/herrie/HaliumDisk/9.0`): `android_device_samsung_goyavewifi`,
`android_device_samsung_scx30g-common` (only `lineage-15.1` exists there), `android_device_samsung_scx35-common`,
`android_vendor_samsung_goyavewifi`, `android_hardware_sprd` and `android_kernel_samsung_sc8330` (3.10.89, for the
kernel-headers step; the kernel of the machine is the shr branch above), plus `Vuhased/android_device_samsung_sprd-common`
(`lineage-15.1-wip`, the only `sprd-common` there is). Output goes to `/media/herrie/LuneOS/android-out/goyavewifi-16.0`,
built with `lunch lineage_goyavewifi-userdebug`, `m -k systemimage` (about 7 minutes) and `m snod`.

Edits needed in those trees to build in the Halium tree (saved as a diff with the port's scratch files):

- the Halium product (`halium.mk`) instead of `full_base` and the LineageOS mini tablet product: the tree has no
  `d8`, so no Java apps; `scx35-common` drops `full_base_telephony`, `libril`, `rild` and the live-wallpaper
  permissions file (`packages/wallpapers` is not in the tree);
- `libstagefrighthw`: `device/hp/tenderloin-media` of the tree defines the same module name, so hardware/sprd's
  is `libstagefrighthw_sprd` with a `libstagefrighthw.so` link, and its users follow;
- `hardware/sprd/ril/libsecril-client` is not built (hardware/samsung/ril has one) and `libatchannel` includes
  its header from there;
- `vendorsetup.sh` of the device no longer runs `patches/apply.sh` (see below).

What the converter made of it: the composer and allocator are hwbinder in the VINTF manifest, `ro.vndk.version=28`,
and the only unresolved library is `libskia.so`, needed by the camera HAL (`camera.sc8830.so`). Unlike the earlier
8.1 vendor from the LineageOS 15.1 zip (`out-device/`, kept as a fallback), this one has `hwcomposer.sc8830.so`, the
@2.1 composer, allocator and mapper, the camera provider, light, sensors and GNSS HALs, and the manifest in
`/vendor/etc/vintf`. The GSI's VNDK 27 APEX is empty, which is what forced the 8.1 vendor onto VNDK 28.

**Side effect on the shared tree.** The device's `vendorsetup.sh` ran `patches/apply.sh` at the first two
`lunch` calls, which committed ten framework patches into the Halium 9.0 tree: `build/make` (insecure adb),
`frameworks/native` (five: libui gralloc0 and reverts), `system/core` (adb, libsuspend earlysuspend),
`hardware/libhardware` (gralloc1) and `hardware/interfaces` (gps). Dated 8 Oct 2026 20:54 in the reflogs. The libsuspend
one breaks `libsuspend` (its header lacks the declaration), which is the one target that fails in the build and
which the vendor does not need. The patches should be taken back out of the tree; the previous heads are
`build/make` 0f26d85, `frameworks/native` 91862d7, `system/core` 42006d4, `hardware/libhardware` afc68d9 and
`hardware/interfaces` 124f358^ (`git reset --keep` to each; `build/make` has an uncommitted `halium.mk` edit that is
not part of this).

## Not done

- Flash and boot. The kernel boots stock Android only if the bootloader takes the image: LineageOS' unsigned
  image is accepted on `KERNEL`, a signed trailer was not reproduced.
- Odin/recovery packaging (`luneos-odin-package` does not list this machine); the install flow is the
  LineageOS recovery's, as for the A3.
- HAL tuning once it boots: `libskia.so` for the camera HAL, `HALIUM_LEGACY_LD_SHIMS` (the device's
  `TARGET_LD_SHIM_LIBS` names `libgps_shim.so` for gpsd), `HALIUM_LEGACY_SHIM_TARGETS`, the Wi-Fi (bcmdhd firmware in
  `/vendor/etc/wifi`) and Bluetooth (BCM4343) routes.
- `mer_verify_kernel_config` on the built `.config`.

## Test kit (9 Oct 2026)

`~/webos/LuneOS/staging/goyavewifi-staging/` has everything for a tester without the device: the installer zip
(`bitbake luneos-dev-package`, a recovery package like hammerhead's and tenderloin's, wrapping the dev image),
Odin files for the boot image, a debug boot image (`enable_adb`), the recovery they flash first, log collection and
a README with the steps and the debugging guide. Install path: Odin the LineageOS recovery, format data, `adb
sideload` the zip. Checked as files only: the zip, the rootfs inside it (GSI, converted vendor with the bcmdhd
firmware, the goyavewifi adaptation), the boot image structure, the command lines, the checksums.

Found while making it, all fixed in the recipes: the kernel command line lost `androidboot.hardware=sc8830` (linux.inc
replaces `CONFIG_CMDLINE`; the container's init picks `init.sc8830.rc` by it); `waydroid` is removed for this machine
(`packagegroup-luneos-extended.bbappend`); `luneos-components` has a per-architecture work directory shared between
machines and had to be reset once with `bitbake -c clean luneos-components` after hammerhead's earlier build left its
`base-files` in the sysroot. `bitbake luneos-dev-package` ends with "basehash value changed" parse errors after
`do_deploy` has succeeded (the zip is fine); not looked into.
