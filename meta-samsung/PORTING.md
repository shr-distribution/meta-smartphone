# Samsung Galaxy A3 (2015) — LuneOS mainline port (`a3-2015`)

A mainline-kernel port in the same shape as `tissot` and `rosy`: no Halium, no
Android container, mesa/freedreno for graphics, lk2nd providing fastboot.

## Hardware

| | |
|---|---|
| SoC | Qualcomm MSM8916 (Snapdragon 410), 4× Cortex-A53 @ 1.2 GHz |
| GPU | Adreno 306 (freedreno) |
| Display | 4.5" 540×960 AMOLED, Samsung AMS452EF01 / S6E88A0 |
| RAM | **1 GB** (1.5 GB on SM-A300FU) |
| Storage | **8 GB** (16 GB on SM-A300FU) |
| Touch | Zinitix bt541, plus tm2-touchkey capacitive back/recents |
| Sensors | bmc150 accel + magn. **No gyro. No ALS/proximity** (cm36652 has no mainline driver) |
| WiFi/BT | WCN3620 (wcn36xx / btqcomsmd) |
| NFC | Samsung S3FWRN5 — driver exists, LuneOS stack does not reach it (see below) |

Variants a3lte / a3ulte / a33g all use one device tree,
`qcom/msm8916-samsung-a3u-eur.dtb`, the same call postmarketOS made when it
collapsed them into a single `device-samsung-a3` package.

> **Read this before building.** 1 GB of RAM is well under what LuneOS normally
> runs in — the smallest device in the tree today has 3.5 GB and the tissot
> triage still recorded an OOM reboot on it. Expect to have to fight for it
> (bigger zram, fewer preloaded services) and treat the 1.5 GB A300FU as the
> realistic target.

## What the port consists of

### meta-smartphone

| File | Purpose |
|---|---|
| `meta-samsung/conf/machine/a3-2015.conf` | the machine |
| `meta-samsung/recipes-kernel/linux/linux-samsung-a3-2015_git.bb` | kernel + boot.img geometry |
| `meta-samsung/recipes-kernel/linux/linux-samsung-a3-2015/defconfig` | full config, see below |
| `meta-samsung/recipes-kernel/firmware/firmware-samsung-a3-2015.bb` | recovers `WCNSS_qcom_wlan_nv.bin` |
| `meta-samsung/recipes-core/initrdscripts/.../a3-2015/machine.conf` | which partition the initramfs mounts |
| `meta-mainline/classes/linux-mainline-8916.bbclass` | the msm8916-mainline kernel tree |
| `meta-android/recipes-bsp/lk2nd/lk2nd-msm8916.bb` | upstream lk2nd |

### meta-webos-ports

`mesa-gl.bbappend` (mesa provides `virtual/mesa`), `qtbase_git.bbappend`
(`kms gbm`), `packagegroup-luneos-extended.bb` (`rmtfs qrtr rpmsgexport …`),
`nyx-modules-machines.inc` (no ALS module), `sensorfw/sensord-a3-2015.conf`,
`alsa-ucm-conf_%.bbappend` (msm8916 UCM), `luneos-package.inc`
(`LK2ND_BOOT_IMAGE_OFFSET`), and the Tier 1 adaptation
`luneos-device-config/.../adaptations/a3u-eur/deviceinfo`.

The adaptation directory is **`a3u-eur`, not `a3-2015`**: with no Android
container every `ro.product.*` is empty, so `luneos-device-config` falls
through to `/proc/device-tree/compatible` and takes the part after the comma
in `samsung,a3u-eur`.

## The kernel defconfig

Generated, not hand-written:

```sh
git clone -b wip/msm8916/7.3-rc2 https://github.com/msm8916-mainline/linux
cd linux
cp <layer>/recipes-kernel/linux/linux-samsung-a3-2015/luneos.config.fragment \
   arch/arm64/configs/luneos.config
make ARCH=arm64 msm8916_defconfig pmos.config luneos.config
cp .config <layer>/recipes-kernel/linux/linux-samsung-a3-2015/defconfig
```

`msm8916_defconfig` + `pmos.config` is exactly what postmarketOS ships, which
is the same thing tissot and rosy do with the msm8953 pair.
`luneos.config.fragment` is the LuneOS delta and is kept next to the defconfig
purely as documentation — bitbake does not read it. It currently carries three
things:

* `CONFIG_LOCALVERSION=""` — the suffix comes from `LINUX_VERSION_EXTENSION`
  instead, otherwise the release string carries both.
* `USB_LIBCOMPOSITE`, `USB_CONFIGFS` and `USB_CONFIGFS_ECM` **built in**. The
  LuneOS initramfs brings USB networking up at 172.16.42.2 by writing a CDC ECM
  gadget through `/config/usb_gadget` before any rootfs module can load;
  postmarketOS configures its gadget from the rootfs, so `pmos.config` leaves
  these as modules/disabled. Without this there is no way into a device that
  does not reach the UI.
* `NFC_S3FWRN5{,_I2C}` for the controller this board actually has.

Redo this whenever the kernel branch is bumped — the branch is rebased onto each
new upstream `-rc`, so the SRCREV in the bbclass and the branch name move
together and the old SRCREV stops being reachable.

## Flashing

Samsung has no fastboot, so lk2nd is what provides it.

**Flash lk2nd and the kernel together the first time.** lk2nd on its own is not
bootable: Samsung's aboot hands off to it (printing "SET WARRANTY BIT: kernel",
which is normal and just means an unsigned kernel was flashed), lk2nd then looks
for a boot image 512 KiB into the same partition, finds nothing, and the device
loops. `fastboot flash boot` cannot rescue that, because reaching lk2nd's
fastboot is the thing that is failing. Concatenate them, exactly as
`luneos-package.inc` does for the TWRP zip:

```sh
# 0. Download mode: Home + Volume Down + Power, then Volume Up to confirm.
heimdall detect                       # "Device detected"
heimdall print-pit --no-reboot        # confirm the name is BOOT (it is)

# 1. lk2nd padded to its 512 KiB offset, with the boot image behind it
cp lk2nd-a3-2015.img lk2nd-boot-a3-2015.img
truncate -s 512K lk2nd-boot-a3-2015.img
cat Image.gz-a3-2015.fastboot >> lk2nd-boot-a3-2015.img
heimdall flash --BOOT lk2nd-boot-a3-2015.img

# 2. Afterwards, with lk2nd running, the kernel alone can be replaced from its
#    fastboot (Volume Down while booting) - it writes at the 512 KiB offset.
fastboot flash boot Image.gz-a3-2015.fastboot
```

The combined image is 12994560 bytes against a 13631488-byte BOOT partition.

Or flash `luneos-dev-package-a3-2015.zip` from TWRP, which does both of those
plus the rootfs.

In download mode the device enumerates as `04e8:685d`, which
`/usr/lib/udev/rules.d/60-heimdall-flash.rules` already tags `uaccess`, so no
root and no extra rules are needed. It identifies itself as manufacturer
"Sasmsung", product "MSM8960" - both are Samsung's own strings and neither is
wrong for an A3.

If heimdall says "Failed to detect compatible download-mode device", check the
kernel log before touching anything else. A cable with no data lines produces no
USB event at all; a marginal one produces an attach that never completes:

```
usb 1-11: new high-speed USB device number 54 using xhci_hcd
usb 1-11: Device not responding to setup address.
usb 1-11: device not accepting address 54, error -71
```

That is the cable (or the port), not heimdall. A different cable fixed it here.


**The 512 KiB offset matters.** Upstream lk2nd (`msm8916-mainline`) loads the
real Android boot image from 512 KiB into the boot partition; the
`msm8953-mainline` fork that tissot, rosy and mido use moved that to 1 MiB, and
`luneos-package.inc` was hard-coded to the fork's value. That is now
`LK2ND_BOOT_IMAGE_OFFSET`, defaulting to `1M`, and this machine sets `512K`.
Get it wrong and lk2nd reads past its own image and nothing boots.

## Where the rootfs lives

On the **userdata** partition, as a plain directory `/data/luneos`, which is
where `webos_deploy.sh` puts it and where `mount_root_partition` looks third.

Userdata is 4.46 GiB on the 8 GB variants (measured from the PIT below) and
~9.8 GB on the 16 GB A300FU. The stock SYSTEM partition is 2.40 GiB, so a
LuneOS rootfs would probably fit there too - but `/data/luneos` is what
`webos_deploy.sh` writes and what the initramfs looks for, so there is no reason
to fight it.

`machine.conf` names the partition rather than numbering it —
`sdcard_partition="userdata"`, resolved through the
`/dev/disk/by-partlabel/` symlinks `mdev-partlabel.sh` creates during coldplug.
Samsung's PIT differs between the 8 GB and 16 GB variants, so an `mmcblk0pNN`
that is right on one is wrong on the other.

## The partition table

Read off the unit with `heimdall print-pit --no-reboot` (31 entries, CPU tag
MSM8916). The ones that matter, sizes in MiB:

| PIT name | start (512 B) | size |
|---|---:|---:|
| APNHLOS | 8192 | 15 |
| MODEM | 38912 | 57 |
| BOOT | 253952 | **13** |
| RECOVERY | 280576 | 15 |
| PERSIST | 342016 | 8 |
| SYSTEM | 376832 | 2456 |
| CACHE | 5406720 | 200 |
| HIDDEN | 5816320 | 50 |
| USERDATA | 5918720 | rest (4.46 GiB to the SGPT at 15269855) |

USERDATA reaching only 4.46 GiB puts the eMMC at ~7.8 GB, i.e. **this is an
8 GB unit, so 1 GB of RAM** - the tighter of the two variants. The 16 GB A300FU
is the 1.5 GB one.

**Case matters, and the two tables disagree.** heimdall addresses partitions by
their *PIT* names, which are upper case, so it is `--BOOT`. The kernel builds
`/dev/disk/by-partlabel/*` from the *GPT* labels, which on these Samsungs are
lower case - which is why the initramfs `machine.conf` says
`sdcard_partition="userdata"`, and why the LineageOS fstab says
`by-name/userdata`. Both are right; they are different tables.

## The boot image has to fit in 13 MiB

This is the binding constraint on the whole port, and it is not obvious until
the PIT is read: BOOT is 13.00 MiB, lk2nd takes the first 512 KiB, so the
Android boot image has **12.50 MiB**. The first build produced 16.28 MiB.

|  | gz, as built for every other machine | after the trim | shipped |
|---|---:|---:|---:|
| kernel + dtb | 10447307 | 10447307 | 10447307 |
| ramdisk | 6618062 | 2955065 | **2018332** |
| boot.img | 17070080 | 13406208 | **12470272** |
| vs 13107200 budget | +3962880 | +299008 | **-636928** |

Two changes got it there, both scoped to this machine:

1. **`IMAGE_INSTALL:remove:a3-2015 = "android-tools bash"`**. 5.51 MiB of the
   uncompressed tree was `libcrypto.so.3`, pulled in by `RDEPENDS:android-tools`
   for adbd - in a ramdisk whose whole job is to mount a partition and
   `switch_root`, and which already brings up CDC ECM on 172.16.42.2. bash went
   with it; everything in the initramfs is `#!/bin/sh` and busybox ash runs it
   (better, per the `mdev-partlabel.sh` note about bash 5.3 and sysfs).
2. **xz instead of gzip** for the ramdisk. Measured on the trimmed tree:
   `gzip -9` 2936251, `xz` 2246800, `zstd -19` 2295341, `lz4 -9` 3392560. The
   kernel has every `RD_*` decompressor from pmos.config, and the ramdisk is
   decompressed by the kernel rather than by lk2nd, so nothing in the bootloader
   path cares.

Both have sharp edges. `initramfs-android-image.bb` pins `IMAGE_FSTYPES` with
`:forcevariable`, which outranks a plain machine override, so the bbappend has
to assign at that same strength and gate on `MACHINE` by hand - and it must stay
in step with `INITRAMFS_NAME`, which `kernel_android.bbclass` assigns with `=`
and which therefore has to be set in the *kernel recipe*, after the inherit.
Disagree on the extension and `do_deploy` fails looking for a file nothing
produced. Verified after the change that tissot, rosy and sargo still resolve to
`cpio.gz` with `android-tools bash` present.

**0.61 MiB of headroom is not much.** Any real kernel growth puts this back over
the line. The durable fix is cutting `msm8916_defconfig` - a config for every
msm8909/8916/8939 device postmarketOS supports, 1618 `=y` symbols, `Image`
25.61 MiB before compression - down to what the A3 actually has.

## Firmware

`msm-firmware-loader` covers the modem, WCNSS and venus blobs off the stock
apnhlos/modem/persist partitions, which stay in place. Two things it does not
cover:

* Adreno 306 microcode — `linux-firmware-qcom-adreno-a3xx`, in the machine's
  `MACHINE_EXTRA_RDEPENDS`.
* `WCNSS_qcom_wlan_nv.bin`, the per-device WiFi calibration. It is on the stock
  `/system`, not on a firmware partition, it is not in linux-firmware, and no
  LineageOS vendor tree for this device carries it — postmarketOS resorted to
  pasting one unit's copy on a paste site. `firmware-samsung-a3-2015` reads the
  device's own copy off the stock system partition at first boot instead, which
  is both more correct (it is *this* unit's calibration) and redistributes
  nothing. **If you wipe or repurpose the stock system partition, WiFi stops
  working** and you will need to supply the file yourself.

## Expected state, from postmarketOS

Working there: display, touch, 3D, audio, WiFi, Bluetooth, GPS, calls, SMS,
mobile data, accel, magnetometer, hall sensor, USB OTG, battery/charging.
Not working: camera (MSM8916 CAMSS is unsupported SoC-wide), ambient light,
proximity. Display is marked *partial*.

## Known open items on the LuneOS side

1. **Not booted.** The kernel, lk2nd and initramfs all build clean and the boot
   image verifies (see below), but nothing has run on hardware. Everything
   device-specific is still derived from the device tree, pmaports and the
   tissot/rosy ports.
2. **Touchkeys.** The adaptation declares `tm2-touchkey` among the key devices so
   the two capacitive keys do something. Drop it from
   `deviceinfo_key_devices_by_name` if they fire spuriously.
3. **NFC.** The chip and the mainline driver are both there, but LuneOS's NFC
   stack is `nfcd` + `nfcd-binder-plugin` against an Android HAL. Wiring the
   kernel NFC netlink interface up instead would make this the first LuneOS
   device with working mainline NFC.
4. **Memory.** See the warning above. Consider a larger zram than the 350 MB
   tissot ships.

## Build state

`bitbake linux-samsung-a3-2015` completes with no warnings or errors, and
`bitbake -n luneos-dev-package` resolves the full 11813-task graph.

Two things the first build settled:

* **`KMETA_AUDIT = ""` is required.** `do_kernel_configcheck` runs
  `symbol_why.py`, whose bundled kconfiglib cannot parse
  `depends on USB if !USB_GADGET` in `drivers/usb/cdns3/Kconfig` — valid Kconfig
  that the kernel's own `scripts/kconfig` accepts. The audit cannot run at all on
  a 7.x tree and fails the build. `linux-megi.inc` hits the identical line on 7.2
  and turns it off the same way. The kernel-cache branch is `yocto-7.2` to match
  the kernel, not the `yocto-6.6` the 8953 class uses.
* **lk2nd needs no GCC patch.** `lk2nd-msm8953.bb` carries one downgrading
  GCC 14's new default errors to warnings; upstream lk2nd's makefile is cleaner
  (`-fno-common -Wstrict-prototypes`, no `-fcommon` hack) and builds clean with
  `gcc-arm-none-eabi-native` as it stands.

Verified on the deployed `Image.gz-a3-2015.fastboot`:

```
header v0, page size 2048
kernel  10447307 @ 0x80080000   = Image.gz (10388010) + msm8916-samsung-a3u-eur.dtb (59297), appended, in that order
ramdisk  6618062 @ 0x82000000
second         0 @ 0x80f00000
tags             @ 0x81e00000
cmdline "earlycon console=ttyMSM0,115200"
```

`lk2nd-a3-2015.img` is 423952 bytes, which leaves ~98 KiB of headroom under the
512 KiB offset the boot image is padded to. Watch that margin when bumping
lk2nd: if the image ever exceeds 512 KiB, `luneos-package.inc` will truncate it
and the device will not boot.

## First bring-up (2026-09-29)

It boots. lk2nd -> kernel -> initramfs -> rootfs on userdata -> DRM -> Adreno ->
LuneOS UI on the panel. `uname` reports `7.3.0-rc2-luneos` on
`Samsung Galaxy A3U (EUR)`, root is
`/dev/disk/by-partlabel/userdata[/luneos] ext4 rw`, and **`free` shows 1362 MB
total - this is a 1.5 GB unit**, not the 1 GB one the 8 GB eMMC implied.

Four things had to be fixed to get there, and three are still open.

### 1. lk2nd: the 23.1 tag and the 23.1 release binary disagree (FIXED, untested)

`lk2nd-msm8916.bb` builds cleanly and its boot image header matches the official
23.1 release field for field - same load addresses, page size, `lk2nd` cmdline,
only 2672 bytes of size difference - but the device resets after 2-3 s with it.
Swapping in the official `lk2nd-msm8916.img` from the GitHub release, with the
same kernel behind it, boots. lk2nd itself runs in both cases: it chain-loads the
stock RECOVERY partition on Volume Up either way.

The cause is not the build. It is that **the published 23.1 image was not built
from the 23.1 source tag**. For msm8916 the tag lists one appended DTB:

    ADTBS += $(LOCAL_DIR)/msm8939-qrd-skuk.dtb

while the release binary carries two. Walking back from the end of each payload,
where the appended DTBs sit contiguously:

    ours (tag 23.1)   payload 288196   appended [1408]         = msm8939-qrd-skuk
    official 23.1     payload 290868   appended [2688, 1408]   = + msm8916-qrd-9

and our own build log agreed with the source it was given - "generating image
with 1 appended DTBs". So a build of the tag appends only an **msm8939** QRD
device tree, on an **msm8916** phone, and the appended DTB is exactly what the
stock Samsung aboot matches against. `main` has both entries; SRCREV now points
there (8b46487c) rather than at the tag, and a rebuild gives

    generating image with 2 appended DTBs
    generating QCDT image with 45 DTBs
    payload 290956   appended [2688, 1408]   - the same structure as the release

Not yet flashed, so the causal link is strong but unproven. If it still resets,
`fastboot oem log && fastboot get_staged /dev/stdout` from lk2nd's own fastboot
is the way to get lk2nd's own account rather than inferring.

Things that looked like the cause and were not: the GCC 14 `-Wno-error` patch
from `lk2nd-msm8953.bb` (nothing miscompiles - the build is clean), a parallel
make race, and the QCDT (byte-different between the two builds because of dtc
versions, but structurally identical - 118 entries each, `msm8916-samsung.dtb`
present in both). The msm8953 fork is no help by analogy: it builds no
`lk.bin-dtb` and no `qcdt.img` at all, which is itself what pointed at
`lk.bin-dtb` as the thing to measure.

### 2. ZSTD module compression breaks everything (FIXED)

`pmos.config` selects `CONFIG_MODULE_COMPRESS_ZSTD`. LuneOS' kmod is built
without it - `kmod version 34.2 / -ZSTD +XZ +ZLIB` - and so is the depmod that
runs at image time. The result is silent and total:

    modules.dep    0 bytes          (depmod exits 0 and writes nothing)
    modules.alias  45 bytes
    modprobe msm   "Module msm not found in directory"
    lsmod          0 modules

680 modules shipped, none loadable. No DRM, so no `/dev/dri/card0`, so
`surface-manager` fails and the screen is black - and equally no touch, no wifi,
no bluetooth, no audio. Nothing in the build warns. The fragment now selects
`CONFIG_MODULE_COMPRESS_GZIP`, which is what tissot and rosy ship and what
kmod's `+ZLIB` handles.

Recovering the running device without a rebuild: decompress the `.ko.zst` on the
host, `depmod -b`, and push the tree over - the device has no `zstd` binary, and
its own `depmod -a` writes a zero-byte file for the same reason.

### 3. The kernel searches /lib/firmware; LuneOS puts firmware in /usr/lib (OPEN)

    /usr/lib/firmware/qcom/a300_pm4.fw     exists
    /lib/firmware                           does not exist
    firmware_class.path = /lib/firmware/msm-firmware-loader/target

`/lib` here is a real directory containing only `modules`, not a usrmerge symlink,
so nothing under `/lib/firmware` resolves. The Adreno microcode therefore never
loads and the GPU retries forever:

    [drm:adreno_request_fw] *ERROR* failed to load a300_pm4.fw

`msm-firmware-loader.sh` has the same problem from the other side: it hardcodes
`/lib/firmware/msm-firmware-loader` while its recipe installs to
`${libdir}/firmware`, and its log shows it failing on a read-only path.

Proven fix at runtime:

    echo -n /usr/lib/firmware > /sys/module/firmware_class/parameters/path

after which both blobs load ("loaded qcom/a300_pm4.fw from new location") and the
UI comes up. The durable form is to make `/lib/firmware` resolve - linking each
entry across works, and cannot replace the directory outright because
msm-firmware-loader owns it as a mountpoint:

    for e in /usr/lib/firmware/*; do n=$(basename "$e")
        [ -e "/lib/firmware/$n" ] || ln -s "$e" "/lib/firmware/$n"; done

This also unblocked msm-firmware-loader itself, which had failed at boot with
"Read-only file system": it runs DefaultDependencies=no before the rootfs is
writable and before anything has created /lib/firmware, so its tmpfs mount never
took. Restarted by hand it mounts apnhlos/modem/persist and links every blob -
wcnss, venus, modem, the TZ images - into its target. With that in place:

    echo start > /sys/class/remoteproc/remoteproc1/state   # a204000, the WCNSS PIL
    -> Booting fw image wcnss.mdt, size 37980
    -> remote processor a204000.remoteproc is now up
    -> qcom_wcnss_ctrl: WCNSS Version 1.5 1.2
    -> wcn36xx: mac address: 02:00:35:73:d3:0d      => wlan0

So WiFi works; it is purely an early-boot ordering problem. Whatever the fix, it
has to leave /lib/firmware populated *and* let msm-firmware-loader mount its
tmpfs, and it very likely applies to tissot and rosy too.

Note the WCNSS calibration itself came from firmware-samsung-a3-2015 exactly as
designed - "installed /lib/firmware/wlan/prima/WCNSS_qcom_wlan_nv.bin from
/etc/firmware/wlan/prima/WCNSS_qcom_wlan_nv.bin", 29816 bytes off this unit's own
stock system partition, no paste site involved.

### 4. luneos-device-config derives nothing when it runs (OPEN)

Because of (2) it ran against a machine with no drivers loaded at all:

    luneos-device-config: codename=a3u-eur api= density= panel=x touch= keys=

No panel geometry, no density, no touchscreen, no keys - hence the UI coming up
at the wrong size, and the warnings that none of `GPIO Buttons;pm8941_pwrkey;
pm8941_resin;tm2-touchkey` resolved. Re-running it with `msm` loaded derives
`panel=540x960` correctly, so the generator is fine; it is an ordering problem
that (2) made total. **The adaptation's key names are all correct** - once modules
load, the kernel exposes exactly `pm8941_pwrkey`, `pm8941_resin`, `GPIO Buttons`,
`GPIO Hall Effect Sensor`, `tm2-touchkey`, `pwm-vibrator` and
`Zinitix Capacitive TouchScreen`. Worth re-checking once a GZIP-module image boots, before
concluding anything about the adaptation's key names.

Note the empty Android `density=` is expected and not the cause: the DPI order is
`deviceinfo_display_dpi` -> the shipped `luna-platform.conf` value -> Android
density -> `LAST_RESORT_DPI=320`, and the Pine adaptations likewise declare only
`deviceinfo_name`, taking DPI and GridUnit from `luna-sysmgr-conf` as this
machine does.

### Installing without a recovery

There is no TWRP for this device (`dl.twrp.me` 404s for a3ulte/a3lte/a32015;
twrp.me lists A3 2016 onward), and stock Samsung recovery has no adbd - so the
`luneos-dev-package` zip cannot be run at all. What works instead:

    # rootfs as an ext4 image with the tree under luneos/, which is what
    # mount_root_partition looks for as $SDCARD_DIR/luneos
    mkdir -p root/luneos
    fakeroot -s fk.env -- tar --numeric-owner -xzf luneos-dev-image-a3-2015.rootfs.tar.gz -C root/luneos
    fakeroot -i fk.env -- mke2fs -t ext4 -L data -b 4096 -d root userdata.img 1048576
    fastboot flash userdata userdata.img

fakeroot matters: extracting as a normal user gives every file uid 1000 and the
rootfs will not boot. Verify before the transfer with
`debugfs -R "stat /luneos/sbin/init" userdata.img` - it must say `User: 0`.

### Getting in with no screen

The initramfs raises a CDC ECM gadget (`18d1:d001`, "LuneOS device",
`fa:75:7f:bb:f4:e6`) before it looks for a rootfs, and the rootfs runs sshd. With
no IPv4 configured, find the device over IPv6 link-local:

    ping6 -c3 ff02::1%usb0          # device answers from its own fe80::
    ssh root@fe80::<addr>%usb0      # no password

That gadget is also the most reliable progress signal on a device with no
display: it appearing means the kernel reached the initramfs, and
`NETDEV WATCHDOG: transmit queue timed out` in the host's log means the CPU
stopped servicing USB, i.e. it panicked.
