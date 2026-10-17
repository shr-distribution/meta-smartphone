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

## First tester logs (10 Oct 2026)

From a tester's `collect-logs.sh` run in the LineageOS recovery (`~/Desktop/sm-t113/logs-20261010-082836/`):

- The install ran to the end and LuneOS ran afterwards: `/data/luneos` is complete, `.firstboot_done` (12:50),
  `android-data` with nine directories (12:51) and `luneos-data/var` (12:56) were written after the install
  finished (12:49). What the screen showed is not reported.
- Samsung's sboot ignores the boot image's command line (see the kernel recipe); the kernel's built-in
  `CONFIG_CMDLINE` is the only place for arguments. The recovery's kernel is 3.10.17 (2019 build).
- `last_kmsg` held the install session of the recovery, not the LuneOS boot, so the LuneOS kernel's log is not
  preserved across the reset (cause not found: `sec_log` is built in and the `sec_log=` argument is passed). The
  persistent journal is the record; the tester's copy came out empty because Git Bash rewrote the `/data/...`
  arguments of the first `collect-logs.sh` (fixed with `MSYS_NO_PATHCONV`, and tar over `adb exec-out`).
- Needed next: the journal (`/data/luneos-data/var/log/journal`) and what was on the screen.

## Second tester logs (10 Oct 2026, 09:50 UTC: persistent journal of a LuneOS boot)

The journal of the run (`/data/luneos-data/var/log/journal`, one boot of about 10 minutes) says:

- Kernel, initramfs, systemd, the LuneOS services and the Android system image mount all work: Mali driver,
  `mmcblk0p25` as /data, `android-rootfs.img` and the vendor image mounted, the APEXes (`com.android.vndk.v28`)
  mounted by mount-android.sh, adb gadget up.
- **The Android container does not run.** `lxc-android.log`: `Exec'ing "/init"`, `Started "/init" with pid`,
  100 ms later `Child <pid> ended on error (127)` (a real exit status; lxc maps nothing to 127 itself). Android's
  init wrote nothing to the kernel log in that time (no `init:` line at all), so it ended before or without
  its kernel logging. Consequences downstream: `could not set property` from every start-android-hals call,
  `composer HAL never registered`, `android-system.service` start-post timeout after 300 s, codename `unknown`
  in luneos-device-config (no `ro.product.vendor.device` to read, so no goyavewifi adaptation: pixel ratio 2.28
  derived, no hardware EGL name), no gst-droid, no GNSS.
- Missing: `androidboot.selinux=permissive` is not on the kernel command line (sboot drops the header string);
  the SM-T520 recipe has it in CONFIG_CMDLINE, the A3 in its header. Now in CMDLINE. Not shown to be the cause.
- The kernel had no `getrandom`, `memfd_create` or the `seccomp` syscall number wired (table ended at
  renameat2, 382). The Nexus 4 (3.4.113) has seccomp (383) and getrandom (384) and `sys_ni_syscall` at 385, so
  `memfd_create` is not what stops the container. Local commit 82f74cfc on the kernel branch wires seccomp (the
  code was already in kernel/seccomp.c) and adds getrandom (hand port, the N4 commits do not apply to this
  random.c); `__NR_syscalls` is 388. Pushed to shr-distribution/linux, recipe SRCREV bumped to it, built with
  bitbake and the table entries checked in vmlinux; not booted. Not shown to be the cause of the exit.
- `last_kmsg` again held a recovery session (the boot before the collection was a recovery boot).
- Not read from the logs: what the screen showed.
- The initramfs in the deploy directory is newer than the one in the first kit (adds `luneos-charger`, changed
  init/halium-boot.sh); the second-round boot image reuses the first kit's ramdisk so that the command line is
  the only change. Rebuild `luneos-dev-package` before handing a fresh kit to someone else.
- Third tester logs (10 Oct 2026, v3 kernel with seccomp/getrandom): the container's init now runs and starts its
  services (the exit 127 is gone); `servicemanager` and `hwservicemanager` abort with "Binder driver protocol(7)
  does not match user space protocol(8)". Cause: ANDROID_BINDER_IPC_32BIT was promptless and on. Fixed on the
  kernel branch by 7ee697e8 (prompt) + luneos.cfg (off) + aa78a54a (get_user() of 8 byte types, which binder
  needs once the option is off); recipe SRCREV is aa78a54a. Built; the v4 boot images are in the kit; not booted.
  The vendor side is unchecked: the device BoardConfig does not set TARGET_USES_64_BIT_BINDER.
- Fourth tester logs (10 Oct 2026, v4 kernel, binder protocol 8): servicemanager starts on /dev/binder. Then
  apexd-bootstrap fails on every loop device ("Failed to flush buffers on the loop device: Permission denied",
  EACCES from BLKFLSBUF without CAP_SYS_ADMIN) with `reboot_on_failure`, vold fails the same way, and Android's
  init runs its shutdown, which restarts the whole tablet about 25 s after the container started. The init log
  also says "capabilities requested but the kernel does not support ambient capabilities" for every service with
  a capabilities line (tenderloin's log has none). Kernel commit 23ff2c18 adds ambient capabilities (hand port of
  the N4's 4fc98e65869a); recipe SRCREV is 23ff2c18; the v5 boot images are in the kit. Not booted. That the
  missing capability set is the cause of the loop-device errors is an inference.

- Fifth tester logs (10 Oct 2026, v5 kernel with ambient capabilities; from the recovery's collect-logs.sh, journal
  of the LuneOS boot): the init warning about ambient capabilities is gone and `apexd-bootstrap` exits 0, so the
  runtime APEXes are in place; logd, servicemanager, hwservicemanager, vndservicemanager, vold, keymaster HAL and
  apexd start. vold then aborts about 5 s later: `binder: <pid>:<tid> ioctl c0306201 returned -14` (BINDER_WRITE_READ,
  protocol 8, EFAULT) on its main thread and two pool threads, libbinder calls abort() ("getAndExecuteCommand
  returned unexpected error -14"), `reboot_on_failure` makes Android's init shut down (`reboot,vold-failed`). vdc's
  `cryptfs init_user0` is the call that arrives at vold at that moment. The container's init then writes
  sysrq 'e' ("SysRq : Terminate All Tasks"), which SIGTERMs the host's processes too (journald and the LuneOS services
  restart; no device reboot). lxc reports "Container will be stopped instead of rebooted" because post-stop.sh
  exits 32 (`umount: /mnt/vendor/persist: no mount point specified`). Cause of the EFAULT: not known. Everything in
  uaccess.h/getuser.S/putuser.S for 8 byte types reads right; the working theory (unverified) is a kernel write
  into a user page the page fault handler refuses to resolve (fork/COW in vold, or a wrong preempt count).
  v6 boot images carry the recipe patch 0001-android-binder-debug-report-failed-user-accesses.patch (debug only,
  remove again) which prints pid, function, address, VMA and flags and the preempt count of every failed
  access in binder.c. Not booted.
- Other non-fatal things in the v5 journal: no cgroup v2 (/sys/fs/cgroup/system: createProcessGroup fails and
  init continues), `lmkd` and `tombstoned` abort/exit 1, `ro.zygote` unset (no zygote, expected for Halium),
  vold cannot parse fstab.sc8830 ("FDE is no longer supported; 'encryptable'"), init.sc8830.rc line 384 `check_fs`
  invalid keyword, no /metadata, no /productinfo or /preload mount points (read-only GSI).
- v5 journal, the "other" items (11 Oct 2026):
  - lmkd aborts in the static constructor of libmemevents.so (it opens /sys/fs/bpf/memevents/*; 3.10 has no BPF)
    and is `critical` in lmkd.rc, so init would reboot after four aborts. Halium's init.disabled.rc (`override`,
    exec lmkd_HYBRIS_DISABLED) is parsed before lmkd.rc and does not win. Fix: stubbed-services-goyavewifi
    (/system/bin/lmkd, sleep stub; picked by ro.product.vendor.device) + bbappend section; the kit has
    stub-lmkd.sh to put the same line on an installed tablet. Not booted.
  - tombstoned exits with status 1 about 1.2 s after start (no message anywhere). Cause not known, not critical;
    it is what writes /data/tombstones. Look again once vold stays up.
  - fstab.sc8830 (device/samsung/scx30g-common/rootdir): `encryptable=footer` on the /data line is FDE, which
    Android 16's libfstab rejects, so vold loads no fstab at all and has no volumes. Fix in the device tree: drop
    it (the extSdCard line's `encryptable=userdata` is the adoptable form and stays). Needs a vendor image
    rebuild; not done.
  - init.sc8830.rc line 384 `check_fs` is no keyword in Android 16 (the line is skipped); the persdata block
    (lines 386-391) names /dev/block/platform/sdio_emmc/..., a device that does not exist here, and its `wait`
    costs init five seconds. Remove the block in the device tree; not done.
  - /metadata: not needed. This tablet has no such partition; init's mkdir /metadata/* fail on the read-only
    GSI and are ignored (checkpoint calls of vdc still return 0). It would only matter for bootstat, staged APEX
    installs and OTA, none used here.
- Vendor rebuild (11 Oct 2026): device tree edits (uncommitted in the shared 9.0 tree): scx30g-common
  rootdir/fstab.sc8830 (no `encryptable=footer` on /data), scx35-common rootdir/init.sc8830.rc (persdata
  wait/check_fs/mount lines removed). `m systemimage` (56 s), tools/assemble-device.py -> out-device-16/
  halium-luneos-9.0-20261011-1-goyavewifi.tar.bz2 (sha256 c69c7b1f...); android-system-image-goyavewifi.bb points at
  it and clears HALIUM_LEGACY_VENDOR_SHA256 (the published goyavewifi-vendor.img is from the 20261008-2 tarball).
  The derived vendor.img differs from the published one in exactly those two files (diff -rq). Kit:
  sm-t113-vendor-v6.zip (update-vendor.sh, recovery only). Not booted.
- Sixth tester logs (11 Oct 2026, v6 kernel = v5 + binder debug patch; vendor and stub list not applied): no binder
  EFAULT in any of the boots (the debug patch printed nothing), vold lives, the container runs for the whole
  200 s capture: keymaster/audio/bluetooth/configstore/usb/wifi HALs and surface-manager start. Whether the
  v5 EFAULT was a race or the patch changed code generation is not known (only 1 of 3 boots on the v5 kernel
  had it). Still failing every 5 s: lmkd (37 aborts / 200 s), gpsd and memtrack HAL (exit 1), tombstoned
  (main returns 1 0.2-2.6 s after start; libevent dispatch failure suspected). The vendor has no composer@2.1 or
  allocator@2.0 *service* (only -impl), so wait-for-android waited ~140 s and the compositor got no composer.
  v7: scx35-common hidl.mk adds both -service packages (CRLF file), vendor tarball 20261011-2 (sha 78c6fa23...),
  stub list + gpsd + memtrack, recipe patch 0002-epoll-debug-report-failing-epoll-calls.patch. Kit:
  sm-t113-boot-v7.zip + sm-t113-vendor-v7.zip. Not booted.
- Seventh tester logs (11 Oct 2026, v7 kernel _etext c0a6bf14; vendor update NOT applied): our update-vendor.sh and
  stub-lmkd.sh used /data/luneos-data/var/lib/lxc/android; the installed rootfs is /data/luneos
  (/data/halium-rootfs -> luneos), luneos-data is only the persistent /var. So the old 2018 vendor.img and an empty
  stubbed-services.d were in use (the FDE message is still in the log). epoll-dbg and binder-dbg printed nothing
  in 316 s, so tombstoned's exit 1 is not an epoll or binder uaccess error. New in the longer log: media.swcodec
  aborts every 5-6 s ("gralloc-mapper is missing", 20 times); init runs flags_health_check for
  sys.init.updatable_crashing and says "Reboot already performed in last 24hrs because of crash" (it has a
  crash-reboot of its own; not shown to have fired in these logs). composer/allocator services still absent
  (old vendor). v8: fixed paths, 50-stub-services handles /apex/* on the host, stub list adds mediaswcodec,
  collect-live.sh drops `timeout` (not on the tablet) and straces tombstoned by attach, collect-logs.sh writes
  vendor-state.txt. Kit: sm-t113-vendor-v8.zip (no new boot image).
- Kernel audit against the sibling ports (11 Oct 2026, after the O_TMPFILE finding; the question was why each round
  turns up another missing 3.11-3.17 feature). Method, repeatable: (1) every syscall number that the arm32 libc.so of
  the GSI's runtime APEX issues (parse `movw/mov r7,#N ... svc`), checked against each kernel's calls.S;
  (2) a feature matrix of the kernel trees in tmp/work-shared (a3-2015-halium 3.10.108, sm-t520 3.4.113,
  hammerhead-halium 3.4) for the interfaces the Android 16 userspace has been seen to use. Result for goyavewifi at
  the time: gaps that a sibling already has: memfd_create (385: a3-2015, sm-t520), sched_setattr/getattr (380/381:
  a3-2015), O_TMPFILE (a3-2015, hammerhead), BINDER_GET_NODE_INFO_FOR_REF (a3-2015). Absent in all siblings (bionic
  falls back): bpf, statx, pidfd_*, close_range, mlock2, preadv2/pwritev2, copy_file_range, process_madvise,
  epoll_pwait2, userfaultfd, cgroup2, PSI, schedtune/uclamp, binderfs, loop LOOP_CONFIGURE.
  Done: O_TMPFILE + ext3/ext4 ->tmpfile (found by strace of tombstoned: openat(O_TMPFILE) = EISDIR),
  memfd_create + sealing + i_mmap_writable. Not done: sched_setattr/getattr (needs the deadline class; kernel/sched
  differs by 1900 lines), NODE_INFO_FOR_REF (informational message only), anything in drivers/ or the f2fs, sdcardfs,
  lowmemorykiller, zsmalloc, vmscan, wakelock differences of a3-2015 (SoC or Android-version specific; no failure
  traced to them).
- Eighth tester logs (11 Oct 2026; vendor update landed, v7 kernel): lmkd/gpsd/memtrack no longer fail (stubbed).
  strace of tombstoned: `openat(6, ".", O_WRONLY|O_APPEND|O_TMPFILE, 0660) = -1 EISDIR` then "failed to create
  temporary tombstone in /data/tombstones" and exit 1: 3.10 has no O_TMPFILE. vendor.hwcomposer-2-1 and
  vendor.gralloc-2-0 (the new composer/allocator services) exit 1 at every start, no message (reason not known;
  library NEEDED lists are all satisfiable from vendor + GSI + VNDK 28 APEX); media.swcodec still aborts (its
  APEX is mounted by the container's own apexd, so the host-side /apex stub of 50-stub-services cannot reach it).
  "wait-for-android" now sees hwcomposer "running" after 79 s instead of 200 s. v9: kernel commits (local, NOT
  pushed, recipe SRCREV unchanged) fb7a80f9.. O_TMPFILE, a941b994 memfd_create/sealing; vendor tarball
  20261011-3 with `stop media.swcodec` in init.sc8830.rc; collect-live.sh straces tombstoned, the allocator and the
  composer by attach. Kit: sm-t113-boot-v9.zip + sm-t113-vendor-v9.zip. Not booted.
- Kernel branch pushed (11 Oct 2026): goyavewifi/3.10/hybris-14.1 at e292d3fc = O_TMPFILE (e9a1f204) and
  memfd_create/sealing (e292d3fc) on top of da7b6d3a, nine Bluetooth L2CAP commits that another session had pushed
  to the same branch at 13:45 CEST (my two commits were rebased onto them, no force). Recipe SRCREV is e292d3fc.
  The v9 boot images in the kit were built from a941b994 (the same two commits without the Bluetooth ones), so a
  rebuild from the recipe now differs from them by net/bluetooth only.
- Ninth tester logs (11 Oct 2026, v9: O_TMPFILE + memfd kernel, vendor 20261011-3): tombstoned, lmkd, gpsd,
  memtrack, media.swcodec all quiet; the tablet runs 155 s of capture with nothing crash-looping except
  vendor.hwcomposer-2-1 and vendor.gralloc-2-0 (exit 1 about 0.1-0.2 s after start, 28 times each; init counts
  them as "process with updatable components ... exited 4 times before boot completed", which feeds its
  updatable-crash reboot logic). hwcomposer "running" is seen at 78 s, surface-manager starts at 83 s (was 200 s).
  The attach strace caught the attach script's own shell (pattern without anchor), the HAL log text is not in the
  kernel log. collect-live.sh now anchors the match and saves logcat -d via lxc-attach (sm-t113-collect-v10.zip).
- Tenth tester logs (12 Oct 2026, logcat via lxc-attach): ROOT CAUSE of the allocator/composer exit 1:
  `vndksupport: Could not load /vendor/lib/hw/gralloc.sc8830.so from default namespace: dlopen failed: library
  "libft2.so" not found: needed by /vendor/lib/libpdfium.so` (gralloc.sc8830.so -> libdither.so ->
  libandroid_runtime.so -> libpdfium.so -> libft2.so). Same trap the SM-T520, A3 (2015) and tenderloin recipes
  already document (the GSI's LLNDK list names libft2.so, the converter counts it as provided, the vendor
  namespace does not hand it over): HALIUM_LEGACY_EXTRA_LIBS = "libft2.so", plus HALIUM_LEGACY_SHIM_TARGETS for
  the framework copies (PermissionCache, AppOpsManager, IProcessInfoService undefined everywhere visible to
  the vendor). Vendor image rebuilt (72 MB); NEEDED closure of gralloc, hwcomposer and the three graphics impl
  libraries checked against vendor + VNDK 28 + bionic + LLNDK: nothing missing. Lesson: the recipes of the
  sibling ports had this line and I did not read them. Kit: sm-t113-vendor-v11.zip (no new boot image).
  collect-live.sh: the strace attach is removed (it waited for ever on a service that stays up).
- Eleventh tester logs (12 Oct 2026, vendor with libft2): libft2 fixed; next dlopen error of gralloc.sc8830.so:
  `cannot locate symbol "gMallocLeakZygoteChild" referenced by libandroid_runtime.so` (Android 9 libc variable,
  absent from Android 16 bionic entirely, not even LIBC_PRIVATE). Defined in halium_legacy_shim.c. Full
  NEEDED+symbol closure of gralloc, hwcomposer and the graphics impl/service binaries checked afterwards against
  vendor+VNDK28+bionic+GSI /system/lib: only noise inside GSI libraries remains. Kit: sm-t113-vendor-v12.zip.
  Converter now pulls libft2.so itself (halium-legacy-vendor rev 32). Other logcat errors: cutils-trace
  permission denied (harmless), CameraService cannot reach ActivityManager (no framework).
- Twelfth tester logs (12 Oct 2026): gMallocLeakZygoteChild fixed; next: `cannot locate symbol "android_dlwarning"
  referenced by libandroid_runtime.so`. My symbol checker had a bug: readelf prints versioned symbols as
  `name@VER (n)` and my regex required the name at end of line, so EVERY versioned undefined symbol was silently
  skipped in the earlier "nothing missing" checks. Fixed; a full scan of all vendor libs/binaries with bionic
  limited to libc/libm/libdl (libdl_android is not in the vendor namespace) found the real set: android_dlwarning,
  android_create_namespace, android_get_exported_namespace, android_init_anonymous_namespace,
  android_link_namespaces, android_set_application_target_sdk_version (all now stubs in the shim). Remaining
  unresolved names are IFUNC string functions (resolved by the linker). Kit: sm-t113-vendor-v13.zip.
- Thirteenth tester logs (12 Oct 2026): android_dlwarning fixed; next `cannot locate symbol "get_malloc_leak_info"`
  (libc exports it only as LIBC_DEPRECATED in Android 16; the importer asks for LIBC_PRIVATE, so the version
  does not match). Scan made version-aware; shim now also defines get/free_malloc_leak_info, malloc_iterate,
  malloc_disable/enable/backtrace, __system_properties_init; SHIM_TARGETS extended with libnativeloader,
  libgraphicsenv, libC, libmemunreachable, libpdx_default_transport. Kit: sm-t113-vendor-v14.zip.
  Remaining unresolved names in the scan: IFUNC string functions and __page_size (camera only).

## v14 result and v15 (12 Oct 2026)
- v14 logs: gralloc and the composer load and stay up, surface-manager runs (`-platform hwcomposer`). No UI.
- The composer (pid 157) calls getService(IAllocator@2.0) at 29.414; the allocator registers at 29.427. libhidl
  then waits for a registration callback ("Waited one second ... Waiting another") that needs a hwbinder thread,
  and the composer has not started its pool yet (still inside the HWC init). Composer never registers; surface-manager
  loops on it. Inferred from timing, not yet confirmed by a backtrace.
- Also logged: "SPRDHWComposer: hwcomposer can't find GSP lib / open GSP device failed" (possibly harmless).
- v15: `hwc-order` oneshot in scx35-common/rootdir/init.sc8830.rc restarts the composer once, 5 s after it first runs.
  collect-live.sh adds hal-state.txt (lshal, task wchan, debuggerd -b). Kit sm-t113-vendor-v15.zip, SHA-256 1b868f5a...43f4a.
