require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tissot-halium"

# Which system.img to put at /android: "device" is the tissot-specific Halium 9
# build, "gsi" is the device-agnostic halium_arm64 one.
#
# The GSI is the point of this recipe. It deletes the per-device `m systemimage`
# and puts tissot on the same Android userspace, libhybris branch and
# android-headers version as sargo.
TISSOT_ANDROID_SYSTEM ?= "gsi"

# Which vendor - and unlike sargo, "none" is not available here.
#
# sargo can drop vendor.img entirely because mount_device_vendor() in
# initramfs-scripts-halium mounts the phone's own /vendor partition. tissot has
# no such partition: it launched on Android 7.1
# (`ro.product.first_api_level=25`), so Treble never applied, and the stock Pie
# fastboot ROM's GPT has system_a/system_b, boot_a/boot_b, userdata, persist,
# modem_a/modem_b and dsp - no vendor, no super, no product. Its HALs live only
# in the vendor.img built alongside the device system image, which therefore has
# to keep shipping in the rootfs no matter which system.img is used.
#
# That is also why the device tarball is fetched unconditionally below.
TISSOT_ANDROID_VENDOR ?= "device"

# Whether to copy the camera/fingerprint support libraries out of the A9
# system.img into vendor.img (step 4 in tissot_patch_vendor_for_treble).
#
# Set to "0" for a vendor carrying only the three property/selinux stubs. Those
# are proven on hardware: 29 vendor HALs, UI, audio, BT, WiFi, sensors, RIL.
# The libraries are what camera and fingerprint additionally need and are NOT
# yet proven - a hand-applied equivalent, patched on-device with the phone's own
# debugfs, took out the property service: init logged "sys_prop: invalid command
# 2", every getprop returned empty, so ro.vndk.version was unset and all 29 HALs
# died with "libcutils.so not found". Reverting that image restored everything.
#
# Images from this recipe are not known to share that fault - the injected files
# verify byte-identical to their sources and e2fsck is clean - so the on-device
# tooling may simply have written them badly. Until a flashed image settles it,
# this is the bisect switch: flash with "1", and if the HALs do not come up,
# rebuild with "0" to return to the proven set.
TISSOT_VENDOR_EXTRA_LIBS ?= "1"

# Device-specific Halium 9 build. Still the source of vendor.img, and of
# system.img when TISSOT_ANDROID_SYSTEM is flipped back to "device" to A/B a
# regression against the GSI.
TISSOT_DEVICE_PV ?= "20260901-1"
TISSOT_DEVICE_RELEASE ?= "halium-luneos-20260901"
TISSOT_DEVICE_TARBALL ?= "halium-luneos-9.0-${TISSOT_DEVICE_PV}-tissot.tar.bz2"

# Device-agnostic halium_arm64 build.
#
# This is the 16.0 GSI, not a 9.0 one, and the vendor it has to serve is Android
# 9 (VNDK 28, vendor manifest target-level="2"). That works because the GSI now
# ships VNDK snapshots 27 through 34 - see PRODUCT_EXTRA_VNDK_VERSIONS in
# device/halium/halium_arm64/device.mk - so /apex/com.android.vndk.v28 is
# present for tissot's vendor to link against.
#
# The framework compatibility matrix floor is not an obstacle even though this
# GSI ships only matrices 5-8: sargo runs the same image against a vendor whose
# manifest is also target-level="3", because nothing in the Halium boot path
# runs checkvintf.
#
# webOS-ports/halium-images tags newer releases by date and carries several
# artefacts per release, so the tag and the asset are named separately.
PV = "20260910-1"
TISSOT_GSI_RELEASE ?= "halium-luneos-20260910"
TISSOT_GSI_TARBALL ?= "halium-luneos-16.0-${PV}-halium_arm64.tar.bz2"
TISSOT_GSI_SHA256 ?= "583ea441a11671ebdbe0b17e220a98a68ba28d227235e7274cc6df69f84bab18"

SRC_URI = "\
    https://github.com/webOS-ports/halium-images/releases/download/${TISSOT_GSI_RELEASE}/${TISSOT_GSI_TARBALL};name=gsi;subdir=gsi \
    https://github.com/webOS-ports/halium-images/releases/download/${TISSOT_DEVICE_RELEASE}/${TISSOT_DEVICE_TARBALL};name=device;subdir=device \
"
SRC_URI[gsi.sha256sum] = "${TISSOT_GSI_SHA256}"
SRC_URI[device.sha256sum] = "53ff8c6ed8fc46f50be001f3eefb20990b4e1004a8d5d95ac14001724f686026"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"

# Stage the selected pair where the .inc expects them. Done in do_install rather
# than after do_unpack because the .inc consumes ${UNPACKDIR}/system.img in place
# (mv to .sparse, simg2img, rm), so re-copying here keeps a re-run idempotent.
#
# Neither tarball carries a vndservicemanager, and neither needs to: the GSI's
# init.halium.rc redefines the service with
# `setenv LD_PRELOAD libselinux_stubs.so`, which is what lets the vendor's own
# binary come up in a container with no selinuxfs.
do_install:prepend() {
    cp ${UNPACKDIR}/${TISSOT_ANDROID_SYSTEM}/system.img ${UNPACKDIR}/system.img
    if [ "${TISSOT_ANDROID_SYSTEM}" = "gsi" ]; then
        tissot_patch_gsi_for_legacy_vendor ${UNPACKDIR}/system.img ${UNPACKDIR}/device/system.img
    fi
    # Remove first so a re-run cannot leave a stale vendor.img behind.
    rm -f ${UNPACKDIR}/vendor.img
    if [ "${TISSOT_ANDROID_VENDOR}" = "device" ]; then
        cp ${UNPACKDIR}/device/vendor.img ${UNPACKDIR}/vendor.img
        tissot_patch_vendor_for_treble ${UNPACKDIR}/vendor.img ${UNPACKDIR}/device/system.img
    fi
}

# tissot's vendor was built PRODUCT_FULL_TREBLE=false / vndk_lite, so it is
# missing two things a real Treble vendor always has. Both are fatal against a
# modern GSI and both are pure metadata, so they are injected here rather than
# by rebuilding the Android side.
#
#   1. /vendor/etc/selinux/plat_sepolicy_vers.txt
#      Android 16's init calls SelinuxGetVendorAndroidVersion() from
#      PropertyInit() and reads this file unconditionally. Without it init logs
#      "Could not read vendor SELinux version" and takes InitFatalReboot(signal
#      6) in SecondStageMain, so the container reboot-loops with only pid 1 ever
#      alive. 28.0 is the honest value - the vendor is Android 9. Note the GSI
#      only ships sepolicy mappings for 29.0 and up, which does not matter here
#      because the container has no selinuxfs and never compiles policy; init
#      just needs a parseable version.
#
#   2. ro.vndk.version in /vendor/build.prop
#      Not set at all on a vndk_lite build. linkerconfig keys the vendor
#      namespace off it, so without it no VNDK path is wired up and every vendor
#      HAL dies at exec with "CANNOT LINK EXECUTABLE ... library libbinder.so
#      not found" (likewise libhidlbase, libhardware, libcutils). The libraries
#      are present - the GSI mounts /apex/com.android.vndk.v28 - the vendor just
#      never asks for them.
#
# debugfs "rm" unlinks without freeing the block, which leaves a block bitmap
# difference, so e2fsck -fy afterwards is required, not optional. It exits 1
# when it corrects something, which is the expected path here.
tissot_patch_vendor_for_treble() {
    img="$1"
    sysimg="$2"
    workdir=`mktemp -d`

    printf '28.0\n' > $workdir/plat_sepolicy_vers.txt
    debugfs -w -R "write $workdir/plat_sepolicy_vers.txt /etc/selinux/plat_sepolicy_vers.txt" $img

    debugfs -R "dump /build.prop $workdir/build.prop" $img
    changed=0
    if ! grep -q '^ro.vndk.version=' $workdir/build.prop; then
        echo 'ro.vndk.version=28' >> $workdir/build.prop
        changed=1
    fi
    # 3. ro.hardware.egl
    #    Never set by this vendor, and on Halium 9 nothing needed it. The A16
    #    EGL loader tries persist.graphics.egl, then ro.hardware.egl, then
    #    ro.board.platform, and opens libEGL_<value>.so. Only ro.board.platform
    #    is set, to msm8953, but the drivers here are /vendor/lib64/egl/
    #    libEGL_adreno.so - so the lookup misses and surface-manager aborts with
    #    "couldn't find an OpenGL ES implementation", taking the whole UI down.
    if ! grep -q '^ro.hardware.egl=' $workdir/build.prop; then
        echo 'ro.hardware.egl=adreno' >> $workdir/build.prop
        changed=1
    fi
    if [ $changed = 1 ]; then
        debugfs -w -R "rm /build.prop" $img
        debugfs -w -R "write $workdir/build.prop /build.prop" $img
    fi

    # 3b. Extend the configstore seccomp policy so Waydroid can boot.
    #
    #     Only two vendor services carry a seccomp policy (configstore@1.1 and
    #     mediacodec). Under the A16 GSI, vendor processes link the A16 bionic,
    #     which calls gettid()/getpid() directly - syscalls the 2017-era
    #     configstore@1.1.policy never allowed - so vendor.configstore-hal takes
    #     SIGSYS inside registerAsService() ~0.9s after every start, forever.
    #
    #     Nothing on the A16 host uses configstore, so LuneOS itself is fine and
    #     this went unnoticed. But Waydroid (HALIUM_9) resolves
    #     ISurfaceFlingerConfigs through the HOST hwservicemanager via
    #     /dev/host_hwbinder, the host VINTF manifest declares configstore@1.1,
    #     and libhidl then waits for it forever -> Waydroid's SurfaceFlinger
    #     never starts -> sys.boot_completed is never set -> no Waydroid UI.
    #
    #     gettid/getpid are the proven killers (strace: si_syscall=__NR_gettid);
    #     the rest are cheap insurance for the same A16-bionic drift. Every name
    #     must exist in the vendor's old libminijail seccomp_policy table - an
    #     unknown name is fatal at policy parse - so do NOT add e.g. rseq, which
    #     that table predates.
    seccomp=/etc/seccomp_policy/configstore@1.1.policy
    debugfs -R "dump $seccomp $workdir/configstore.policy" $img 2>/dev/null || true
    if [ -s $workdir/configstore.policy ] && \
       ! grep -q '^gettid:' $workdir/configstore.policy; then
        for sc in gettid getpid getrandom newfstatat membarrier \
                  sched_yield ppoll dup; do
            echo "$sc: 1" >> $workdir/configstore.policy
        done
        debugfs -w -R "rm $seccomp" $img
        debugfs -w -R "write $workdir/configstore.policy $seccomp" $img
    fi

    # 4. Libraries the vendor needs that live on the old device system.img.
    #
    #    Setting ro.vndk.version above is what makes the vendor namespace real,
    #    and a real namespace is also a restricted one: it reaches /vendor plus
    #    the VNDK-28 APEX and nothing else. These libraries are in neither, so
    #    they have to travel with the vendor.
    #
    #      libstdc++.so       the bionic forwarder, dropped from AOSP long ago.
    #                         /vendor/lib/libts_face_beautify_hal.so still needs
    #                         it, so camera.msm8953.so cannot be dlopened and
    #                         the camera provider fails HIDL_FETCH.
    #      libgf_*, gf_fingerprint.default.so, libgoodixfingerprintd_binder.so
    #                         the Goodix fingerprint stack, for the Mi A1 units
    #                         that carry a Goodix sensor. The prebuilt
    #                         fingerprint@2.1-service is hardwired to
    #                         hw_get_module("gf_fingerprint"), so it always loads
    #                         gf_fingerprint.default.so. That module also needs
    #                         libbacktrace.so and libunwind.so (below), or it
    #                         fails to dlopen; then openHal() leaves the device
    #                         pointer NULL and the service SIGSEGVs in preEnroll/
    #                         setActiveGroup. FPC units (the fpc1020 majority)
    #                         instead need the FPC module fingerprint.default.so,
    #                         which is already in vendor.img - fingerprint-hal-
    #                         fixup.service (systemd-machine-units) binds it over
    #                         the gf_ path at boot when it detects an FPC sensor,
    #                         and fixes the container /dev/uinput the HAL opens.
    #      libsoftkeymaster.so, android.hidl.base@1.0.so
    #                         pulled in by the Goodix libraries above.
    #
    #    Both arches of libstdc++ are taken: the camera stack here is 32-bit.
    if [ "${TISSOT_VENDOR_EXTRA_LIBS}" != "1" ]; then
        bbnote "TISSOT_VENDOR_EXTRA_LIBS is not 1, skipping camera/fingerprint libraries"
        rm -rf $workdir
        return 0
    fi

    for l in lib/libstdc++.so \
             lib64/libstdc++.so \
             lib64/libgf_algo.so \
             lib64/libgf_ca.so \
             lib64/libgf_hal.so \
             lib64/libgoodixfingerprintd_binder.so \
             lib64/libsoftkeymaster.so \
             lib64/android.hidl.base@1.0.so \
             lib64/hw/gf_fingerprint.default.so; do
        staged=$workdir/`echo $l | tr / _`
        debugfs -R "dump /system/$l $staged" $sysimg
        if [ ! -s $staged ]; then
            bbfatal "could not extract /system/$l from the device system.img"
        fi
        debugfs -w -R "rm /$l" $img 2>/dev/null || true
        debugfs -w -R "write $staged /$l" $img
    done

    # gf_fingerprint.default.so's own dependencies, needed on Goodix units for it
    # to dlopen at all (the load error is `library "libbacktrace.so" not found`).
    # Best-effort: FPC units never load the Goodix module (the fixup service
    # binds the FPC one over it), so a device system.img that lacks these must
    # not fail the build - warn and continue.
    for l in lib64/libbacktrace.so lib64/libunwind.so; do
        staged=$workdir/`echo $l | tr / _`
        debugfs -R "dump /system/$l $staged" $sysimg 2>/dev/null || true
        if [ ! -s $staged ]; then
            bbwarn "no /system/$l in the device system.img; Goodix fingerprint units will not work until it is provided"
            continue
        fi
        debugfs -w -R "rm /$l" $img 2>/dev/null || true
        debugfs -w -R "write $staged /$l" $img
    done

    set +e
    e2fsck -fy $img > $workdir/fsck.log 2>&1
    rc=$?
    set -e
    if [ $rc -gt 1 ]; then
        bbfatal "e2fsck on the patched vendor.img failed with $rc:`cat $workdir/fsck.log`"
    fi

    rm -rf $workdir
}

# The GSI is a generic Android userspace; tissot's vendor is an Android 9 one
# that expects two things the device's own system.img used to provide. Both were
# found by diffing the old device system.img against the GSI after fingerprint
# and camera failed on an otherwise healthy image.
#
#   1. Legacy top-level symlinks. The A9 system.img had /firmware, /dsp and
#      /persist pointing into the vendor. fingerprint.default.so has the literal
#      string "/firmware/image" - that is where QSEECom loads the FPC trustlet
#      from - so without the symlink the trustlet never loads, the HAL gives up,
#      and biomd reports "Failed to get pre-enroll challenge". With it:
#      "QSEECOM: App (fpctzapp) now loaded".
#
#      Note the host already gets these from android-system, which symlinks them
#      to /android/$i; that does nothing for code running INSIDE the container,
#      whose root is this image.
#
#   2. /system/etc/camera. libmmcamera2_sensor_modules.so reads its sensor list
#      from there, and camera_config.xml plus the per-sensor chromatix XMLs are
#      device-specific files that lived in the device system.img, not in vendor.
#      Without them the daemon loads all 43 of its other modules, opens
#      msm_sensor_init, and then probes nothing: "Number of camera devices: 0"
#      and gst-droid "cannot find camera 0". With them: 3 devices and a live
#      preview. ~108 KB.
#
# Both are taken from the device tarball this recipe already fetches, so the
# files are the ones this vendor was built alongside.
tissot_patch_gsi_for_legacy_vendor() {
    gsi="$1"
    devimg="$2"
    workdir=`mktemp -d`

    debugfs -w -R "symlink /firmware /vendor/firmware_mnt" $gsi
    debugfs -w -R "symlink /dsp /vendor/dsp" $gsi
    debugfs -w -R "symlink /persist /mnt/vendor/persist" $gsi

    # rdump creates the "camera" directory itself and fails if it already exists.
    debugfs -R "rdump /system/etc/camera $workdir" $devimg
    if [ ! -e $workdir/camera/camera_config.xml ]; then
        bbfatal "no /system/etc/camera/camera_config.xml in the device system.img"
    fi
    debugfs -w -R "mkdir /system/etc/camera" $gsi
    for f in $workdir/camera/*; do
        debugfs -w -R "write $f /system/etc/camera/`basename $f`" $gsi
    done

    set +e
    e2fsck -fy $gsi > $workdir/fsck.log 2>&1
    rc=$?
    set -e
    if [ $rc -gt 1 ]; then
        bbfatal "e2fsck on the patched GSI failed with $rc:`cat $workdir/fsck.log`"
    fi

    rm -rf $workdir
}
