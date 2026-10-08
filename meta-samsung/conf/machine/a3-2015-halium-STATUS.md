# Samsung Galaxy A3 (2015) — LuneOS Halium port status

Machine `a3-2015-halium`, written 6 Oct 2026. The Halium counterpart of the mainline machine
`a3-2015` (see a3-2015-STATUS.md); both are kept. Updated 8 Oct 2026: it boots on an SM-A300FU through
lk2nd with the UI, the modem, Wi-Fi, Bluetooth, audio, the flashlight and the camera (preview and still
pictures) working; see "On the phone" below.

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
- `power: rt5033: stop flooding the kernel log on every property read`: journald fed on the fuel gauge's
  per-read register dumps and never finished starting.
- `ARM: raise COMMAND_LINE_SIZE to 2048` and `net: ipc_router: let group net_raw bind without paranoid
  networking`: the two boot-loop fixes described under "On the phone".
- Debug aids still on the branch (lk2nd ramoops region and console copy, initcall trace, boot-progress bands
  painted into the splash framebuffer); bring-up only, to be dropped before release.
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

## On the phone (8 Oct 2026)

Booted through lk2nd (`fastboot flash boot` in lk2nd writes behind lk2nd on the boot partition).

### Boot loop: modem crash about 33 s into every boot (fixed)

The phone reset about 10 s after the modem came out of reset. Without its EFS the modem crashes, and its
restart level resets the SoC. Two separate faults kept rmt_storage, which serves the modem its EFS
partitions (`modemst1`, `modemst2`, `fsc`, `fsg`), from running:

1. **Kernel command line cut at 1024 bytes.** CONFIG_CMDLINE, the boot image's command line and the 775
   bytes the Samsung boot loader passes through lk2nd do not fit in 3.10's COMMAND_LINE_SIZE. Everything
   after `lcd_id` was lost: `androidboot.serialno`, `androidboot.mode`, `androidboot.baseband` and
   `mdss_mdp.panel`. Without `androidboot.baseband` the container's init sets `ro.baseband=unknown`, and
   rmt_storage stops itself ("Remote storage service is not supported on unknown target"). Fixed by raising
   COMMAND_LINE_SIZE to 2048.
2. **IPC router refusing rmt_storage.** The vendor rmt_storage drops its privileges itself: `setgid(3004)`
   (AID_NET_RAW), `setuid(9999)`, and only CAP_BLOCK_SUSPEND kept. On Android, ANDROID_PARANOID_NETWORK makes
   group net_raw count as CAP_NET_RAW, which the IPC router's bind check accepts. luneos.cfg turns paranoid
   networking off (it would limit sockets to the inet group), so the bind failed ("msm_ipc_router_bind:
   rmt_storage Do not have permissions"). Fixed by accepting group net_raw in the IPC router's own check.

With both, rmt_storage registers and serves the modem (EFS writes to `modemst1`), the modem and WCNSS
subsystems are ONLINE, `apr_tal` reports the modem up, qmuxd and rild run, and the phone stays up.

### Charger mode on every hard reset with a cable

The boot loader starts the phone in charger mode (`androidboot.mode=charger androidboot.baseband=lpm`)
whenever it comes up from a PMIC hard reset with a cable attached: off-mode charging, a long press of the
power key, lk2nd's `fastboot reboot`, and a plain `reboot` (msm-poweroff does a hard reset when no reboot
argument is given). Crash resets and a reboot *with* an argument (warm reset, restart reason 0x12345678)
come up normally. Before the command-line fix the kernel never saw the mode, so charger boots started
LuneOS in full with `baseband=lpm` and the phone never charged.

Consequences:
- The initramfs has to hold in charger mode instead of booting LuneOS (Herrie82/meta-smartphone#6 does
  this for all Halium devices with a charging screen). Its "hold power to boot" path reboots with
  `reboot -f`, which on this phone is a hard reset and so lands in charger mode again; it needs a reboot
  with an argument (LINUX_REBOOT_CMD_RESTART2) here.
- LuneOS's own reboot should pass an argument too, or a reboot with the cable attached ends in charger mode.

### Ruled out

- Battery: 96 to 99 % throughout; USB is stable while the phone holds in the initramfs.
- USB cable and port: the drops were the phone resetting.

### Display and compositor

Three gaps, each one hiding the next:
1. The composer HAL `hwcomposer.msm8916.so` links the framework copies of libhwui and libmedia; libhwui
   needs libft2.so, which the vendor namespace does not get from the GSI. Fixed with
   `HALIUM_LEGACY_EXTRA_LIBS = "libft2.so"` (as sm-t520 and tenderloin-halium).
2. Those framework copies then miss full-libbinder/libmedia symbols
   (`android::MetaDataBase::writeToParcel`). Fixed with sm-t520's `HALIUM_LEGACY_SHIM_TARGETS`.
3. `gralloc.msm8916.so` cannot load in the sphal namespace (libqdutils.so needs libui.so), so
   media.swcodec aborts ("gralloc-mapper is missing") and surface-manager waits for ever. The same as the
   TouchPad's gralloc.msm8660.so; the GSI's linkerconfig change (generator/variableloader.cc) that lets
   sphal link libbinder.so and libui.so from the VNDK APEX was for VNDK 28 only and now covers VNDK 30.

The UI comes up. qt6-qpa-hwcomposer-plugin with patches 0002 and 0003 is the one to use (rebuilt for this
machine; same version string as before, so reinstall with --force-reinstall).

### Wi-Fi

- The prima WLAN driver is built in (CONFIG_PRONTO_WLAN=y) and starts only when "sta" is written to
  /sys/module/wlan/parameters/fwpath; a3-prima-wlan.service (meta-samsung systemd-machine-units) does that
  once wcnss_version stops reading INVALID, which is when the WCNSS control channel is up.
- The vendor's hciattach (init.qcom.bt.sh) must not be force-started at boot: it powered WCNSS up through
  the Bluetooth path before wcnss_service had triggered it ("regulator get of riva_vddmx failed (-19)",
  "Failed to execute wcnss_wlan_power"), the control channel never came up, and neither WLAN nor Bluetooth
  worked. It is in deviceinfo_android_skip_services; the Bluetooth HAL starts it itself later, safely.
- EFS (see below) gives wcnss_service the real MAC from /efs/wifi/.mac.info.

### EFS and Bluetooth

- Samsung's EFS (Bluetooth address, Wi-Fi MAC, calibration) had no fstab line in LineageOS either; the
  vendor image now ships /vendor/etc/fstab.efs, which mount-android.sh mounts read-only at /android/efs.
- The Bluetooth HAL aborted in initialize() ("Open: No Bluetooth Address!"): ro.bt.bdaddr_path comes from
  msm8916-common's system.prop, now carried into the vendor build.prop (HALIUM_LEGACY_SYSTEM_PROP_PREFIXES),
  and the address file is radio:net_bt_stack 0640, so zz-a3-bluetooth-efs.rc gives the HAL group 3008
  (the GSI's init no longer knows the name net_bt_stack).
- bluebinder_post.sh copied ro.bt.bdaddr_path on the host, where the path is under /android
  (bluebinder patch 0004, meta-luneos). Kernel: BT_HCIVHCI and RFKILL in luneos.cfg.
- hci0 comes up and scans. The controller still reports Qualcomm's default address (00:A0:C6:...), not
  the EFS one.

### Flashlight

The rt5033 flash driver offers the torch only as /sys/class/camera/flash/rear_flash (1 on, 0 off), not as
an LED class device. nyx-modules learned that shape (led_torch, branch herrie/samsung-rear-flash), and
luneos-device-config's 35-torch-app no longer hides the app where rear_flash exists.

### Battery

- The gauge is right; the battery really ran down during the boot loops on 500 mA USB.
- MemAvailable is missing from 3.10's /proc/meminfo, and memorymanager then refused every app launch
  ("Failed to reclaim required memory"); the tenderloin MemAvailable commit is cherry-picked.
- deviceinfo states 1900 mAh (LineageOS' power_profile.xml) for full and design capacity. sec-battery
  puts its charging mode in charge_now (1), which nyx now ignores (nyx-modules herrie/samsung-rear-flash).
- current_now stays 0: the driver reports no current.

### Camera

1. LineageOS loads the camera provider in-process (passthrough), which the GSI's cameraserver cannot. The
   provider service (android.hardware.camera.provider@2.4-service) is built in the same Halium 11 tree and
   carried in device tarball 20261008-1; HALIUM_LEGACY_BINDERIZED_HALS makes the manifest hwbinder. The
   HAL then finds both cameras (rear mounted at 90 degrees, front at 270).
2. White preview: libmmcamera_faceproc.so has text relocations, which the Android 16 linker refuses at a
   target SDK of 23 or more, so mm-qcamera-daemon had no faceproc module and could not link the preview
   stream ("mct_stream_start_link ... Null", "Failed to config stream"). zz-a3-camera-textrel.rc preloads
   libpermissioncache_shim.so (lowers the target SDK to 22) into the daemon and the provider, through
   android-system-image-legacy-a9-shim.inc. Only three vendor libraries have text relocations, all camera.
3. Taking a picture hung: a deadlock between the HAL's capture thread (holding the HAL's API lock, calling
   unregisterMemory back) and minimediaservice's only HwBinder thread (busy in the shutter notification,
   calling disableMsgType into the HAL). droidmedia's minimedia.cpp now configures five HIDL threads as
   cameraserver does. Affects every HAL1 device with that HAL behaviour, not only this one.

Preview, autofocus and still pictures work in the camera app.

### Releases

Both published in the halium-luneos-20261005 release of webOS-ports/halium-images and fetched from
there by android-system-image-a3-2015-halium:
- device tarball halium-luneos-11.0-20261008-1-a3-2015-halium.tar.bz2 (sha256 a95d0f7d...);
- GSI halium-luneos-16.0-20261008-3-halium_arm.tar.bz2 (sha256 7795e2d7..., system.img 76791e9f...), with
  the linkerconfig VNDK 30 change and the minimediaservice thread pool. The A3 recipe pins it itself; the
  shared android-system-image-legacy-gsi.inc still names 20261007-1 for the other legacy devices.

### Still open

- Bluetooth address: the controller keeps its default instead of the EFS one.
- perfd does not link (`__android_log_print` missing); harmless so far.
- Telephony on top of the working modem (rild runs; not tested end to end).
- Video recording not tested.
- android-kernel-bootimg's postinst wrote over lk2nd once (fixed in the recipe: it now writes 512 KiB in
  when the partition starts with lk2nd).
- A plain reboot with a cable attached comes up in charger mode; reboot with an argument
  (systemctl reboot --reboot-argument=normal) for a normal boot.
