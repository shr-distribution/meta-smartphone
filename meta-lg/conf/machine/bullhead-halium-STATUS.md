# LG Nexus 5X (bullhead) — LuneOS Halium port status

Machine `bullhead-halium`, written 9 Oct 2026. Qualcomm MSM8992 (Snapdragon 808), 2 GiB, 1080x1920 JDI
panel, launched on Android 6 without a vendor partition. Approach: the 3.10 kernel of nexus5x-dev's
LineageOS 21 tree and the plain arm64 16.0 GSI over a vendor converted by `halium-legacy-vendor` from
that LineageOS 21 build, as the A3 (2015) and the SM-T520.

State: **round 7 (10 Oct 2026) reached the LuneOS UI**: container up, surface-manager on hwcomposer, bootd
done, no kernel BUG in three boots (the tester ended each with the power key). The tester saw very slow touch,
no Wi-Fi and repeating sound. WebAppMgr's in-process GPU thread crashed (NULL) about 1 s after every start, 58
times in one boot: kgsl_get_unmapped_area failed with -ENOMEM for its first SVM buffer, so EGL never
initialised. The Adreno 418's SVM range is the CPU range below 3 GB - 16 MB; surface-manager maps there fine,
so something in the 64-bit WebAppMgr fills it (unknown). sensorfwd restarted every ~6 s (no sensors: the
vendor's flash-nanohub-fw has no `user`, Android 16 init rejected it, and nanohub logged err_cnt every second).
pulseaudio and audiod never crashed; the repeating sound is most likely CPU starvation (not proven). Wi-Fi:
qcacld-2.0 waits for "sta" in /sys/module/wlan/parameters/fwpath and nothing wrote it. First boot spent 178 s
in ldconfig (Rebuild Dynamic Linker Cache); the round-6 stall did not recur. lmkd still aborts in a
libmemevents static constructor (no_fatal holds). No kgsl SVM change in the Halium or UT bullhead kernels.
Round 8 kit (bullhead_20261010-8.zip): kernel 255dcede (kgsl prints the mmap layout below 3 GB on that
-ENOMEM), wam-maps-capture (WebAppMgr maps to /var/log/wam-maps), wlan-dynamic-start writes fwpath behind CNSS
(meta-webos-ports), flash-nanohub-fw with a user, sensorfwd RestartSec=30.
Round 6 (10 Oct 2026) stalled in early systemd, the container never started. PID 1 logged nothing
for 300.0 s after "Starting Create Static Device Nodes in /dev" (11.8 s -> 311.8 s; that unit and "Coldplug All
udev Devices" finished together), then again after systemd-tmpfiles-setup started (311.9 s) until the journal
ends at 611 s; the kernel kept logging (nanohub) throughout. Only android-system changed since round 5, and its
hook runs at container start, which was never reached; round 5's first boot passed the same units in 0.1 s.
Cause not known; 5 minutes is systemd-userwork's RUNTIME_MAX_USEC (nsswitch has `systemd` for passwd/group, and
userdbd started just before), not confirmed. ramoops held only the TWRP install session.
Round 7 kit (bullhead_20261010-7.zip): kernel efc28ad3 with Halium's SECURITY_ANDROID_GID_CAPABILITIES and the
config options Halium's halium-7.1 and the Ubuntu Touch port's halium-9.0 bullhead kernels set (CHECKPOINT_RESTORE,
FANOTIFY, CGROUP_PERF, BLK_CGROUP, NET_CLS_CGROUP); the tester starts it twice to see whether the stall repeats.
Round 5 (10 Oct 2026): udevd/logind fine with systemd 0009, **the Android container started** (init, lshal), lmkd
exited 4 times before boot completed, Android init shut the container down, and the teardown hit the 3.10 BUG()
in shrink_dcache_for_umount_subtree ("dentry ... still in use (1) [unmount of tmpfs tmpfs]", from lxc-start's
mntns_put) -> panic. No earlier fix for it in the 3.4/3.10 LuneOS kernels. Round 6 kit (bullhead_20261010-6.zip):
init.svc_debug.no_fatal.lmkd and logcatd persistence through the new 65-extra-init hook (meta-android).
Round 4 (10 Oct 2026) booted LuneOS again, USB oops gone, container still down. lxc-android.log:
every LXC cgroup failed on cpuset.cpus (EINVAL): the root cpuset listed CPUs 0-5 (cpus_requested set to all of
NR_CPUS) with nr_cpus=4, and update_cpumask() refuses CPUs that are not present; kernel 8261bf75 trims it. The
journal was nine tenths register dumps for unimplemented syscalls (statx, rseq, pidfd_open, clone3, faccessat2),
silenced by 9cd99212. systemd-udevd/logind/userdbd/connman-vpn still 226/NAMESPACE with 0008 in place: most
likely pivot_root + umount2(".") on a pre-3.15 umount lookup; 0009 is now applied on all Halium machines when
the kernel is older than 3.15. Round 5 kit (bullhead_20261010-5.zip) carries these.
Before: **round 3 (10 Oct 2026) booted into LuneOS** (systemd, webOS services, compositor at 556 s) **without the
Android container** (lxc-start "ABORTING"; its log, /var/log/lxc-android.log, was not collected). Also seen:
226/NAMESPACE for systemd-udevd/logind/userdbd and connman-vpn (hidepid=invisible on 3.10: systemd 0007/0008
now applied on all Halium machines in meta-webos-ports), an oops in ffs_function_enable from android-gadget-setup's
"functions ffs" (kernel 228df660), a journald watchdog kill at 191 s (cause unknown), nanohub errors (no
container to set it up). Round 4 kit (bullhead_20261010-4.zip) carries those fixes.
Earlier: **two remote tests (9 Oct 2026, LGH790, bootloader BHZ11h), both kernel panics.**
Round 1: the install from TWRP worked; the six-core kernel stopped 0.3 s into boot with "failed to lock
a57_pll1 PLL" (clock-pll.c) when the CPU clock driver enabled the clocks of the online A57 cores, then hung in
"Reboot failed -- System halted". The ramoops console survived the reset and TWRP read it. The kernel now runs
on the four A53s only (`boot_cpus=0-3 maxcpus=4 nr_cpus=4`, the last made effective by a kernel commit), as TWRP
does on the same phone. Round 2 (A53s only, same BHZ11h firmware) reached 2.86 s and died in lpm_probe: with
nr_cpus=4 the A57 cluster's CPUs match no possible CPU, and parse_cluster()'s error path list_del()ed a cluster
never put on a list. Fixed by skipping clusters without possible CPUs (9cdee0c1). The old firmware is not what
stopped the boot; BHZ32c / radio 2.6.42.5.03 only matter if modem, audio or Wi-Fi fail. Whether the phone has
the 5X's big-core fault is open. Test kit:
`~/webos/LuneOS/staging/bullhead-staging` on the build host.

| Part | State |
|---|---|
| `linux-lg-bullhead-halium` (3.10.108) | builds with bitbake; boot image 19.9 MB of the 32 MiB boot partition, header as LineageOS' |
| `initramfs-android-image` | builds; finds userdata by partition name, no `machine.conf` needed |
| `android-system-image-bullhead-halium` | builds: GSI `20261007-1` (arm64, unmodified) + `bullhead-halium-vendor.img` from the `halium-luneos-20261005` release |
| `luneos-dev-package` | builds; installed from TWRP (official 3.7.0_9-0, RAM-booted) |
| `luneos-device-config` adaptation | `adaptations/bullhead/deviceinfo` (meta-webos-ports): display DPI only, from the panel's DT dimensions |
| Hardware | untested |

## What it is built from

| Part | Source |
|---|---|
| Kernel | `bullhead/3.10/lineage-21.0` in shr-distribution/linux: nexus5x-dev `kernel_lge_bullhead` `lineage-21.0` (995f521bafa3, 3.10.108) plus eight commits (below) |
| Config | `halium_bullhead_defconfig` in that branch: `lineageos_bullhead_defconfig` + what Halium/LuneOS need |
| Vendor | `lineage-21.0-20240911-UNOFFICIAL-bullhead.zip` of nexus5x-dev/OTA (Android 14, RELEASE_DEPRECATE_VNDK: no `ro.vndk.version`, `HALIUM_LEGACY_VNDK = ""`), its system.img converted; published as `bullhead-halium-vendor.img` |
| System | the plain halium_arm64 16.0 GSI `20261007-1` |

## Kernel commits (what each one fixes)

- arm64 `proc.S`: the `#alloc, #execinstr` section flags the OE binutils rejects.
- qcacld-2.0 without `-Werror`: GCC 15 diagnoses ~20 files.
- `halium_bullhead_defconfig`: SysV IPC, IPC/UTS/PID namespaces, devtmpfs, fhandle, autofs, device cgroup,
  devpts instances, vndbinder, no paranoid networking. `USER_NS` stays off (on 3.10 it forces
  UIDGID_STRICT_TYPE_CHECKS and binder does not build).
- `MemAvailable` in /proc/meminfo (the tenderloin commit): memorymanager refuses every app launch without it.
- IPC router: group net_raw may bind without paranoid networking (as on the A3): `pm-service` runs as
  system:net_raw, `rmt_storage` drops its capabilities.
- `BT_HCIVHCI` for bluebinder.
- arm64 smp: `nr_cpus=` honoured when building the possible CPU mask (it stopped at NR_CPUS), so `nr_cpus=4`
  keeps the A57s out of the system: `boot_cpus`/`maxcpus` alone leave them hotpluggable, and the vendor's
  init.bullhead.power.sh onlines cpu4 at boot.
- renameat2 wired into both syscall tables: the tree has the 3.15 backport, but the seccomp backport left its
  slots (276, compat 382) on `sys_ni_syscall`; Android 16's bionic renames through renameat2 with no fallback.
- `execveat()` through `/proc/self/fd`: the arm64 Halium glibc is built with `OLDEST_KERNEL` 4.9
  (`defaulttunes.inc`), so it assumes execveat (`__ASSUME_EXECVEAT`) and `fexecve()` has no fallback; lxc-attach
  (built with `ENFORCE_MEMFD_REXEC`) re-executes itself through it. The same glibc assumes `mlock2` (4.4),
  which this kernel lacks; nothing in the rootfs imports it. statx, close_range, clone3 and faccessat2 keep
  their glibc fallbacks. This is the oldest kernel the arm64 rootfs has been built for.
- `SECURITY_ANDROID_GID_CAPABILITIES` (Halium's commits from its halium-7.1 branch of this kernel): with paranoid
  networking off, net_raw/net_admin members got no CAP_NET_RAW/CAP_NET_ADMIN (rmt_storage, pm-service).
- `halium_bullhead_defconfig`: that option, CHECKPOINT_RESTORE (kcmp), FANOTIFY, CGROUP_PERF, BLK_CGROUP,
  NET_CLS_CGROUP. MEMCG_KMEM, which both other ports set, stays off (experimental in 3.10).

## Build findings (bitbake)

- The 3.10 arm64 Makefile links `libgcc.a`: `DEPENDS += "libgcc"` and `TOOLCHAIN_OPTIONS` on `KERNEL_CC`/`KERNEL_LD`,
  as linux-huawei-angler.
- `headers_install` leaves the Android staging uapi headers under `/usr/src/usr`: removed in do_install.
- The page size 4096 needs `ANDROID_BOOTIMG_EXTRA_ABOOTIMG_ARGS = "-c pagesize=4096"` (abootimg ignores
  `ANDROID_BOOTIMG_PAGESIZE`).

## The vendor image

The plain GSI goes with it, so nothing of the old image is added to the GSI. What the vendor opens under
`/system/etc` is in `/vendor/etc` instead, and the paths that name it are changed in the old image before the
conversion (`bullhead_vendor_etc_paths` in the recipe, in the files' own blocks, so owner, mode and SELinux label
stay): the three `data/*_config.xml` that netmgrd, qmuxd and libdsi_netctrl.so spell as `/system/etc/data/`
(`/vendor` is as long as `/system`), and init.bullhead.rc's copy of `qcril.db`. `irsc_util` gets
`/vendor/etc/sec_config` through `zz-bullhead-irsc.rc`. To make a new image: `HALIUM_LEGACY_VENDOR_SHA256:pn-
android-system-image-bullhead-halium = ""` in local.conf and the device tarball (a local file, the LineageOS
system.img packed unchanged; not published).

Unresolved libraries the converter reports (framework copies pulled in by name, IMS, keystore.default):
libandroidicu, libicu, libdl_android, libnativehelper, libart, libart-compiler, libkeystore_binder,
libsoftkeymaster, and libmmcamera2_is.so (missing from the LineageOS build too).

## Install

Bootloader unlocked; `fastboot boot` the official TWRP 3.7.0_9-0 (it links `/dev/block/bootdevice`, which the
package's updater-script writes `boot` through, and its mke2fs makes an ext4 3.10 mounts); Format Data;
install `luneos-dev-package-bullhead-halium-*.zip`. The package's busybox-static (glibc, 4.9) does not run on
TWRP's 3.10 kernel; `webos_deploy.sh` falls back to TWRP's busybox.

## Open, expected next problems

- Wi-Fi: qcacld-2.0 is built in and starts only when "sta" is written to its fwpath parameter (round 8).
- Bluetooth, sensors, vibrator: passthrough-only in this vendor (`-impl`, no service binaries); bluebinder and
  sensorfw want hwbinder services.
- GPS: gps.conf, izat.conf, sap.conf, flp.conf, lowi.conf are opened as `/etc/<file>` and are not in place.
- Camera: 32-bit HAL with LineageOS linker shims (`TARGET_LD_SHIM_LIBS`) not reproduced.
- An Android 14 vendor on the Android 16 GSI has not run anywhere.
- Four cores only: the vendor's cpuset writes for CPUs 4-5 (`0-2,4-5`, `0-5`) and lpm_levels/system/a57 fail.
  Checked for CPUs missing from the possible mask: msm-core, BCL, msm_thermal, jtagv8, coresight (not built),
  lpm-levels (fixed); the drivers before lpm-levels in the initcall order got through on the phone.
- On LuneOS' first start android-kernel-bootimg writes the rootfs' /boot/boot.img to the boot partition:
  a new kernel needs a reinstall of the package, not only `fastboot flash boot`.
- umediaserver needs `waitid(P_PIDFD)`, which no 3.x kernel has (as on the other legacy ports).
