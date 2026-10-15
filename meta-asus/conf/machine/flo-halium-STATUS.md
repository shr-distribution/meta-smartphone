# flo-halium port status

Google/Asus Nexus 7 (2013) Wi-Fi (`flo`, product `razor`, APQ8064), 32-bit,
3.4 kernel. Approach: Halium with the 32-bit 16.0 GSI, as sm-t520,
hammerhead-halium, mako-halium and tenderloin-halium. The vendor is that of
LineageOS 18.1 (Android 11, VNDK 30) for the tablet, made Treble-shaped by
`halium-legacy-vendor`. No Android build of our own: the input is the OTA zip
the flox maintainer publishes.

State: kernel, boot image and luneos-dev-image build (15 Oct 2026); not booted. Nothing in this
machine has run on the tablet yet.

## Sources

- `lineage-18.1-20260923-UNOFFICIAL-flox.zip` from github.com/ods-releases/flox
  (listed at updater-api.oddsolutions.us/flox; sha256 44f9c595...179c). Its
  `system.new.dat.br` and `vendor.new.dat.br` are the vendor's input, its
  `boot.img` the source of the boot image layout.
- LineageOS `android_device_asus_flox` and `android_kernel_google_msm`, branch
  `lineage-18.1`.
- Scratch tooling, not in the layer: `~/claude-scratch/flox/` on the build host
  (`assemble-old-system.sh` turns the zip into the device tarball,
  `convert.py` runs the converter outside bitbake, `check-cfg.sh` resolves the
  kernel config, `check-deps.py` lists what a vendor tree links that the GSI
  does not provide).

## Why flox and not flo

LineageOS 18.1 moved the tablet to the `flox` codename, the "Repartitioned"
layout with a vendor partition of its own. LuneOS does not need that partition:
the vendor image is shipped inside the rootfs as on every other GSI machine, so
the tablet does not have to be repartitioned. The vendor still reports
`ro.product.vendor.device=flox`, which is why the deviceinfo directory is
`flox`.

## Vendor

The flox vendor has its own composer, allocator and OMX hwbinder services, but
it is not self-contained: 24 libraries it links (libqdutils, libmemalloc,
libstdc++, keymaster, the vendor.lineage.* interfaces, ...) live in the Lineage
`/system/lib`. So it goes through the converter like sm-t520's, with the board
init scripts from the root of the system-as-root image
(`HALIUM_LEGACY_INIT_FILES`). The camera HAL has only a passthrough provider;
the A9 service set's camera provider is added and binderized.

Left unresolved by the converter (none expected to matter):

- `libdl_android.so` (libandroid_runtime, libgraphicsenv, libstagefright): the
  same as on sm-t520, which works.
- `libLLVM.so` (libbccQTI.so): RenderScript only.
- `libmm-abl-oem.so` (libmm-abl.so): not in the zip either, so missing on
  LineageOS too.

`HALIUM_LEGACY_VENDOR_SHA256` is empty: the vendor is derived at build time
until `flo-halium-vendor.img` is published with the GSI.

## Kernel

`linux-asus-flo-halium`, branch `flo/3.4/lineage-18.1` of
shr-distribution/linux: Lineage's 0da5107137f9 with the commits listed in the
recipe. Additions specific to this tree:

- the pid namespace setns() backport (`pidns_operations` was declared and listed
  in /proc/<pid>/ns but never defined, so PID_NS did not link);
- a port of the in-tree `hci_smd` driver to the backports 4.2 Bluetooth core
  (`BACKPORT_BT_HCISMD`), because the backports stack had no driver for the
  WCNSS controller. Its send() leaves the skb to the core on failure, as the 4.2
  core frees it then itself.
- `shmem_tmpfile()` calling `simple_acl_create()`: the tree's backport of "fs:
  remove generic_acl" missed it, so the kernel did not build with
  TMPFS_POSIX_ACL, which meta-oe's linux.inc forces on;
- the backports compat `idr_alloc()` left out when the tree has its own (from the
  V4L2 forward-port), as on the sm-t520 branch.

`luneos.cfg` turns on module support for v4l2loopback; every option that the
defconfig builds in and that module support would have turned into a module is
pinned back to `y` (found by resolving the config both ways).

## Not verified (to do on the tablet)

- That it boots at all: boot image layout and command line are copied from the
  flox boot.img, the boot partition name from fstab.flox.
- Display orientation and touch (`deviceinfo_compositor_geometry`,
  `deviceinfo_touch_rotate`), and the web app pixel ratio (2.0).
- Wi-Fi: `flo-prima-wlan.service` writes `sta` to prima's fwpath once WCNSS is
  up, as on the Galaxy A3 (2015).
- Bluetooth: `flo-hci-smd.service` writes 1 to `hcismd_set`. The ported driver
  has only been compiled. The controller's address is whatever the WCNSS NV
  gives it; LineageOS sets it from /persist/bluetooth/.bdaddr with
  hci_qcomm_init, which nothing does here yet.
- Audio (default jb2q droid modules, sink `sink.primary_output` from the vendor's
  "primary output" mix port), camera provider, sensors, GPS, NFC.
- Battery: current in mA and capacity 3448 mAh from power_profile.xml, read from
  the driver source, not from the tablet.
