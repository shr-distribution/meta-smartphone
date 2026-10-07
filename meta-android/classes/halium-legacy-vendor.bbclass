# SPDX-License-Identifier: MIT
#
# Run a GSI on a device whose Halium 9 build predates Treble.
#
# Such a device has no /vendor partition and its Halium 9 build is one monolithic
# system.img, so there is no vendor.img to pair with a GSI. This class makes one
# from the old system.img (see lib/halium/legacy_vendor.py for what that means)
# and leaves ${UNPACKDIR}/system.img as the GSI, ready for
# android-system-image.inc's do_install, which installs both unchanged.
#
# The recipe has to unpack two tarballs, the device's own Halium 9 build into
# ${UNPACKDIR}/device and the GSI into ${UNPACKDIR}/gsi:
#
#   SRC_URI = "<gsi tarball>;name=gsi;subdir=gsi <device tarball>;name=device;subdir=device"
#
# tissot-halium does the same job with a vendor.img that `m vendorimage` made;
# these devices have to derive it.

# The VNDK snapshot the old vendor links against: 28 for an Android 9 vendor.
# The GSI has to ship com.android.vndk.v${HALIUM_LEGACY_VNDK} or the build fails.
HALIUM_LEGACY_VNDK ?= "28"

# Device init scripts and ueventd rules from the root of the old image, as file
# names. They move to /vendor/etc/init/hw and /vendor/etc/ueventd.rc. An `import`
# of anything not listed here is dropped, so leave out init.<device>.usb.rc: the
# container's init must not write the shared USB gadget the host's adbd owns.
HALIUM_LEGACY_INIT_FILES ?= ""

# Services to drop from those scripts: nothing in a container wants the charger
# or a bugreport.
HALIUM_LEGACY_SKIP_SERVICES ?= "charger bugreport"

# Commands of the device init scripts to drop, as regular expressions (no spaces: use \\s) matched against
# each command line of an `on` section. For what the host already does and a container must not do again:
# mounting a partition the host has mounted, activating volume groups the host has activated.
HALIUM_LEGACY_SKIP_COMMANDS ?= ""

# Libraries to pull from the old /system/lib even though no ELF's DT_NEEDED
# names them: dlopen()ed ones.
HALIUM_LEGACY_EXTRA_LIBS ?= ""

# HIDL HALs the container runs as a service, which the vendor manifest has to call
# hwbinder. A monolithic Halium 9 build declares them all passthrough, and a libhidl
# client on the host then loads the HAL itself instead of using the container's
# service. The composer matters because loaded twice it fails, the allocator because a
# process in an APEX namespace (media.swcodec) cannot load /vendor/lib/hw and aborts in
# GraphicBufferAllocator.
HALIUM_LEGACY_BINDERIZED_HALS ?= "android.hardware.graphics.composer android.hardware.graphics.allocator"

# Files of the old image to copy into the vendor image, as <old path>:<vendor path>.
HALIUM_LEGACY_COPY_FILES ?= ""

# Files the recipe supplies itself, as <file under ${UNPACKDIR}>:<path in /vendor>:<octal mode>. For what a
# monolithic Halium 9 build never had to ship: HIDL service binaries built from the Android 9 tree and the
# init scripts or wrapper scripts that start them. The path in /vendor starts with a slash, /bin/hw/foo.
HALIUM_LEGACY_EXTRA_FILES ?= ""

# Files under ${UNPACKDIR} whose rules are appended to the vendor's ueventd.rc (the one made from the
# HALIUM_LEGACY_INIT_FILES ueventd.<board>.rc), for device nodes the old build's ueventd.rc does not place
# where the vendor code opens them.
HALIUM_LEGACY_UEVENTD_EXTRA ?= ""

# Extra /system/etc files of the old image to put in the GSI at the same path, for vendor code that
# opens them through /etc (a link to /system/etc), so no binary spells the /system/etc path.
HALIUM_LEGACY_GSI_FILES ?= ""

# Copy the /system/etc files the vendor binaries name into the GSI (1), or leave the GSI alone
# and take only HALIUM_LEGACY_GSI_FILES (0). A port whose vendor needs nothing of the kind sets
# it to 0 and ships the plain GSI.
HALIUM_LEGACY_GSI_AUTODETECT ?= "1"

# Name prefixes of the old /system/build.prop properties to carry into the vendor build.prop. The old
# image keeps the device's tuning there (PRODUCT_PROPERTY_OVERRIDES), the GSI's build.prop does not.
HALIUM_LEGACY_SYSTEM_PROP_PREFIXES ?= "ro.qti. ro.vendor.qti. ro.telephony. telephony. ro.ril. \
    persist.radio. persist.audio. persist.debug. debug.qualcomm. persist.hwc. persist.vendor. \
    persist.service. persist.bt. af. wifi. drm. media. ro.config. ro.input. net.tethering"

# Symlinks to create in the GSI, as <link>:<target>. A link is one inode, like a file.
HALIUM_LEGACY_GSI_LINKS ?= ""

# Directories to create in the GSI for the container to mount partitions on: whatever the vendor's
# fstab mounts at a path the GSI does not have (/efs and /persist on a vendor that still uses the
# legacy locations). mount-android cannot create them on the read-only image and skips the mount.
HALIUM_LEGACY_GSI_DIRS ?= ""

# Files to delete from the GSI to free inodes, when the directories above (or anything else a device
# needs in the GSI) do not fit in the few spare ones the image is packed with. None by default: the
# GSI is left as it is.
HALIUM_LEGACY_GSI_PRUNE ?= ""

# Revision of lib/halium/legacy_vendor.py. Bitbake does not track imported
# modules, so a change there would reuse the old task output; bump this with it.
HALIUM_LEGACY_REV ?= "31"

# Shims for the Android 9/11 framework copies a legacy vendor brings along.
#
# HALIUM_LEGACY_SHIM_TARGETS: libraries of the vendor image that get libhalium_legacy_shim.so (the
# halium-legacy-shim recipe) as a dependency. The framework libraries a HAL pulls in (libgui, libmedia,
# libmediautils, libsensor, libandroid_runtime) want symbols of the full libbinder and libmedia that the
# VNDK snapshot of the GSI leaves out, and the library does not load without them. Empty by default:
# a device names the libraries its vendor has.
HALIUM_LEGACY_SHIM_TARGETS ?= ""
# HALIUM_LEGACY_LD_SHIMS: "<library>|<shim library>" pairs, what LineageOS calls TARGET_LD_SHIM_LIBS.
# Its linker loads the shim with the library; the Android 16 one does not, so the shim becomes a
# dependency of the library itself.
HALIUM_LEGACY_LD_SHIMS ?= ""
HALIUM_LEGACY_SHIM_LIB ?= "${STAGING_LIBDIR}/halium-legacy/libhalium_legacy_shim.so"
DEPENDS += "${@'halium-legacy-shim patchelf-native' if (d.getVar('HALIUM_LEGACY_SHIM_TARGETS') or d.getVar('HALIUM_LEGACY_LD_SHIMS')) else ''}"

# Free space left in vendor.img.
HALIUM_LEGACY_SLACK_MB ?= "24"

python do_halium_legacy_vendor() {
    import os
    import shutil

    from halium import legacy_vendor as lv

    unpack = d.getVar("UNPACKDIR")
    device = os.path.join(unpack, "device", "system.img")
    gsi = os.path.join(unpack, "gsi", "system.img")
    for img in (device, gsi):
        if not os.path.exists(img):
            bb.fatal("halium-legacy-vendor: %s is missing; SRC_URI needs the device and "
                     "the GSI tarball unpacked to device/ and gsi/" % img)

    bb.note("halium-legacy-vendor revision %s" % d.getVar("HALIUM_LEGACY_REV"))
    work = os.path.join(d.getVar("WORKDIR"), "halium-legacy-vendor")
    shutil.rmtree(work, ignore_errors=True)
    os.makedirs(work)

    # Patch a copy, so a re-run starts from the pristine GSI again.
    target = os.path.join(unpack, "system.img")
    shutil.copyfile(lv.raw_image(gsi, work), target)

    plan = lv.build(
        device, target, os.path.join(unpack, "vendor.img"),
        vndk=d.getVar("HALIUM_LEGACY_VNDK"),
        init_files=d.getVar("HALIUM_LEGACY_INIT_FILES").split(),
        skip_services=d.getVar("HALIUM_LEGACY_SKIP_SERVICES").split(),
        extra_libs=d.getVar("HALIUM_LEGACY_EXTRA_LIBS").split(),
        binderized_hals=d.getVar("HALIUM_LEGACY_BINDERIZED_HALS").split(),
        copy_files=[tuple(x.split(":", 1)) for x in d.getVar("HALIUM_LEGACY_COPY_FILES").split()],
        extra_files=[(os.path.join(unpack, f[0]), f[1], int(f[2], 8))
                     for f in (x.split(":") for x in d.getVar("HALIUM_LEGACY_EXTRA_FILES").split())],
        gsi_files=d.getVar("HALIUM_LEGACY_GSI_FILES").split(),
        system_prop_prefixes=d.getVar("HALIUM_LEGACY_SYSTEM_PROP_PREFIXES").split(),
        gsi_links=[tuple(x.split(":", 1)) for x in d.getVar("HALIUM_LEGACY_GSI_LINKS").split()],
        gsi_dirs=d.getVar("HALIUM_LEGACY_GSI_DIRS").split(),
        shim_lib=d.getVar("HALIUM_LEGACY_SHIM_LIB"),
        shim_targets=d.getVar("HALIUM_LEGACY_SHIM_TARGETS").split(),
        ld_shims=d.getVar("HALIUM_LEGACY_LD_SHIMS").split(),
        gsi_autodetect=d.getVar("HALIUM_LEGACY_GSI_AUTODETECT") != "0",
        skip_commands=d.getVar("HALIUM_LEGACY_SKIP_COMMANDS").split(),
        ueventd_extra=[os.path.join(unpack, f) for f in d.getVar("HALIUM_LEGACY_UEVENTD_EXTRA").split()],
        slack_mb=int(d.getVar("HALIUM_LEGACY_SLACK_MB")),
        workdir=os.path.join(work, "build"),
        log=bb.note)
    try:
        lv.patch_gsi(target, plan.gsi_files, device, work,
                     symlinks=plan.gsi_symlinks, dirs=plan.gsi_dirs,
                     prune=d.getVar("HALIUM_LEGACY_GSI_PRUNE").split(), log=bb.note)
    except RuntimeError as e:
        # These are config files the vendor code opens by absolute path. Nothing
        # here proves a boot needs them, so warn instead of failing the image.
        bb.warn("halium-legacy-vendor: GSI left unpatched: %s" % e)

    # Left open on purpose, not guessed at: nothing provides these, so whatever
    # links them will fail to load. Usually RenderScript and RIL, which LuneOS
    # does not use; read the list before assuming that.
    for lib, users in sorted(plan.unresolved.items()):
        bb.warn("halium-legacy-vendor: nothing provides %s (needed by %s)"
                % (lib, ", ".join(sorted(users)[:4])))
    shutil.rmtree(work, ignore_errors=True)
}
addtask halium_legacy_vendor after do_unpack do_prepare_recipe_sysroot before do_install
