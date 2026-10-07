# Samsung Galaxy A3 (2015) — LuneOS Halium port status

Machine `a3-2015-halium`, written 6 Oct 2026. The Halium counterpart of the mainline machine
`a3-2015` (see a3-2015-STATUS.md); both are kept. Nothing below has been run on the phone yet.

## What it is built from

| Part | Source |
|---|---|
| Kernel | github.com/vlw/android_kernel_samsung_msm8916-caf, `lineage-18.1`, 64f7a77a (3.10.108, 32-bit ARM, `lineageos_a3_defconfig` + `luneos.cfg`) |
| Device tree (QCDT) | `dtFU.img` from vlw/proprietary_vendor_samsung eeb50171 (SM-A300FU's own; other models need theirs, e.g. dtH.img) |
| Vendor | LineageOS 18.1 `lineage_a3lte-userdebug` built in the Halium 11 tree (`/media/herrie/HaliumDisk/11.0`, local manifest `a3lte.xml`), converted by halium-legacy-vendor (VNDK 30) |
| System | the plain halium_arm 16.0 GSI 20261005-2 |

The Android build:

- `~/claude-scratch/a3-2015-halium/build-system.sh` (`m -k systemimage` with `TARGET_NO_RECOVERY=true`; the
  ART apex check fails for this 32-bit product and Lineage's recovery is too big for its partition, neither
  matters for a vendor), then `build-snod.sh` (`m snod`) packs the staged tree with the build's ownership.
- `~/claude-scratch/a3-2015-halium/assemble-device.sh <rev>` completes `/system/lib` (the Android 11 ICU,
  nativehelper and statsd libraries, the system_ext HIDL interface libraries) and writes the device tarball
  `halium-luneos-11.0-<rev>-a3-2015-halium.tar.bz2`. **Not published**; the image recipe uses it as a local
  `file://` URL.
- The vlw blobs are a plain git checkout in the tree's `vendor/samsung` (that directory also holds sm-t520's
  repo projects).

## Kernel patches

- 0001 `ARM: uaccess: keep put_user()'s value in r2`: GCC 15 put a constant in r3, and the `__asmeq`
  check stopped the build (it would otherwise have stored garbage).
- 0002 backport of upstream 790756c7e022 (Solaris-style `.section` flags that current binutils rejects).
- The 3.4 build-fix include (`halium-kernel-3.4.inc`) applies as is.

No syscall backports are needed: the tree has renameat2, memfd_create, getrandom and seccomp.
No overlayfs (3.10); luneos-device-config falls back to bind mounts.

## Boot image

Header version 0 with a QCDT table: dt size in the header word at offset 40, `dtFU.img` after the ramdisk,
image id over all four sections, `SEANDROIDENFORCE` appended (as LineageOS' mkbootimg writes it). Built
by the kernel recipe's `a3_qcdt_bootimg` after kernel_android.bbclass; checked byte by byte on the
built image. 11.2 MB against the 13 MiB boot partition.

## Firmware and mounts

The vendor fstab mounts apnhlos on `/firmware`, modem on `/firmware-modem`, hidden on `/hidden`. The
converter (REV 26) moves them to `/vendor/firmware_mnt`, `/vendor/firmware-modem` and `/vendor/hidden`,
and turns Lineage's `/system/etc/firmware` links (modem.mdt, mba.mbn, cmnlib, ... 124 of them) into
`/vendor/firmware` links to the new places, where the container's ueventd looks.

## Expected problems to look at on first boot

- Model-specific properties: libinit_a3 (LineageOS's own init) does not run under the GSI, so the phone
  reports a3lte / SM-A300F, and the LTE telephony properties it sets from ro.bootloader are missing.
- Bluetooth: WCNSS over SMD; no `BT_HCISMD` in the defconfig, so the route is bluebinder over the Android HAL.
- Camera: passthrough HAL only (no provider service in this vendor).
- `deviceinfo_force_hwc2` is set by analogy with the SM-T520 (HWC 1.x behind composer@2.1), unverified.
- `libdl_android.so` stays unresolved for the framework copies under the camera HAL (also on sm-t520).
- SELinux labels of the libraries added to `/system/lib` of the device image are not set (the build is
  permissive, `androidboot.selinux=permissive`).
