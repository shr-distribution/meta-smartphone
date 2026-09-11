require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mido-halium"

# Which system.img to put at /android: "device" is the mido-specific Halium 9
# build, "gsi" is the device-agnostic halium_arm64 one.
#
# The GSI is the point of this recipe. It puts mido on the same Android
# userspace, libhybris branch and android-headers version as tissot and sargo.
# mido and tissot are both msm8953 with an Android 9 vendor, so the whole
# Treble-gap treatment below is the same on both - see
# android-system-image-tissot.bb, which this follows.
MIDO_ANDROID_SYSTEM ?= "gsi"

# Which vendor - and as on tissot, "none" is not available here.
#
# sargo can drop vendor.img entirely because mount_device_vendor() in
# initramfs-scripts-halium mounts the phone's own /vendor partition. mido has no
# such partition: it launched on Android 6
# (`ro.vendor.build.fingerprint=xiaomi/mido/mido:7.0/NRD90M/...`), so Treble
# never applied and the stock ROM's GPT has no vendor, no super, no product. Its
# HALs live only in the vendor.img built alongside the device system image,
# which therefore has to keep shipping in the rootfs whichever system.img is
# used.
#
# That is also why the device tarball is fetched unconditionally below.
MIDO_ANDROID_VENDOR ?= "device"

# Whether to copy the support libraries the vendor needs out of the A9
# system.img into vendor.img (step 4 in mido_patch_vendor_for_treble).
#
# Unlike tissot this is a short list - one library, both arches - because mido's
# fingerprint stack is already complete inside its own vendor (see the comment
# on step 4). Kept as a switch purely so a bring-up can A/B it.
MIDO_VENDOR_EXTRA_LIBS ?= "1"

# Device-specific Halium 9 build. Still the source of vendor.img, and of
# system.img when MIDO_ANDROID_SYSTEM is flipped back to "device" to A/B a
# regression against the GSI.
#
# This is the original 2021 release - the only mido image webOS-ports has ever
# published. tissot got a fresh Halium 9 rebuild (20260901) because its Android
# tree is checked out locally; mido's is not. Nothing here needs a rebuild: the
# vendor blobs are the phone's own and do not age, and everything the A16 GSI
# additionally wants is injected below.
#
# This release used the old halium-images tagging scheme, where the tag is the
# asset's own filename, so the two are the same string.
MIDO_DEVICE_PV ?= "20210506-2"
MIDO_DEVICE_TARBALL ?= "halium-luneos-9.0-${MIDO_DEVICE_PV}-mido.tar.bz2"
MIDO_DEVICE_RELEASE ?= "${MIDO_DEVICE_TARBALL}"

# Device-agnostic halium_arm64 build.
#
# This is the 16.0 GSI, not a 9.0 one, and the vendor it has to serve is Android
# 9 (VNDK 28). That works because the GSI ships VNDK snapshots 27 through 34 -
# see PRODUCT_EXTRA_VNDK_VERSIONS in device/halium/halium_arm64/device.mk - so
# /apex/com.android.vndk.v28 is present for mido's vendor to link against.
# Verified against this exact image: of everything mido's vendor pulls in, only
# libstdc++ is outside VNDK-28, and step 4 injects it.
#
# Newer halium-images releases are tagged by date and carry several artefacts
# per release, so the tag and the asset are named separately.
PV = "20260910-1"
MIDO_GSI_RELEASE ?= "halium-luneos-20260910"
MIDO_GSI_TARBALL ?= "halium-luneos-16.0-${PV}-halium_arm64.tar.bz2"
MIDO_GSI_SHA256 ?= "583ea441a11671ebdbe0b17e220a98a68ba28d227235e7274cc6df69f84bab18"

SRC_URI = "\
    https://github.com/webOS-ports/halium-images/releases/download/${MIDO_GSI_RELEASE}/${MIDO_GSI_TARBALL};name=gsi;subdir=gsi \
    https://github.com/webOS-ports/halium-images/releases/download/${MIDO_DEVICE_RELEASE}/${MIDO_DEVICE_TARBALL};name=device;subdir=device \
"
SRC_URI[gsi.sha256sum] = "${MIDO_GSI_SHA256}"
SRC_URI[device.sha256sum] = "af9deead686663ceab1717fe3d76ade19207e91191761eb23778bee7107de994"

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
    # The 2021 mido tarball ships ANDROID SPARSE images, where the GSI and
    # tissot's 2026 device build both ship raw ext4. Every patching step below
    # drives debugfs, which cannot read a sparse image at all, so the device
    # pair has to be expanded first. The .inc does the same expansion later for
    # whatever it finds at ${UNPACKDIR}/system.img and vendor.img, but that runs
    # after this function, which is too late to help us.
    mido_ensure_raw ${UNPACKDIR}/device/system.img
    mido_ensure_raw ${UNPACKDIR}/device/vendor.img
    mido_ensure_raw ${UNPACKDIR}/gsi/system.img

    cp ${UNPACKDIR}/${MIDO_ANDROID_SYSTEM}/system.img ${UNPACKDIR}/system.img
    if [ "${MIDO_ANDROID_SYSTEM}" = "gsi" ]; then
        mido_patch_gsi_for_legacy_vendor ${UNPACKDIR}/system.img
    fi
    # Remove first so a re-run cannot leave a stale vendor.img behind.
    rm -f ${UNPACKDIR}/vendor.img
    if [ "${MIDO_ANDROID_VENDOR}" = "device" ]; then
        cp ${UNPACKDIR}/device/vendor.img ${UNPACKDIR}/vendor.img
        mido_patch_vendor_for_treble ${UNPACKDIR}/vendor.img ${UNPACKDIR}/device/system.img
    fi
}

# Expand an Android sparse image to raw ext4, in place and idempotently: a
# second run sees a raw image and does nothing. No resize here - vendor.img is
# still to be written to, and shrinking it to its minimum first would leave no
# room for the libraries step 4 injects. mido_patch_vendor_for_treble shrinks it
# at the end instead.
mido_ensure_raw() {
    img="$1"
    [ -e "$img" ] || return 0
    if file "$img" | grep -q "Android sparse image"; then
        simg2img "$img" "$img.raw"
        mv "$img.raw" "$img"
    fi
}

# mido's vendor was built vndk_lite, so it is missing metadata a real Treble
# vendor always has. All of it is fatal against a modern GSI and all of it is
# pure metadata or a single library, so it is injected here rather than by
# rebuilding the Android side.
#
# debugfs "rm" unlinks without freeing the block, which leaves a block bitmap
# difference, so e2fsck -fy afterwards is required, not optional. It exits 1
# when it corrects something, which is the expected path here.
mido_patch_vendor_for_treble() {
    img="$1"
    sysimg="$2"
    workdir=`mktemp -d`

    # 1. /vendor/etc/selinux/plat_sepolicy_vers.txt
    #
    #    Android 16's init calls SelinuxGetVendorAndroidVersion() from
    #    PropertyInit() and reads this file unconditionally; without it init
    #    takes InitFatalReboot(signal 6) in SecondStageMain and the container
    #    reboot-loops. Unlike tissot, mido's vendor DOES ship it, already
    #    reading 28.0 - so this is a guarded no-op kept only so a differently
    #    built vendor.img cannot silently lose it. 28.0 is the honest value:
    #    the vendor is Android 9.
    #
    #    It is load-bearing beyond init: HYBRIS_PREFER_VNDK (set for this
    #    machine by 50-hybris-prefer-vndk.conf) reads this file to decide which
    #    VNDK APEX to resolve /vendor callers out of.
    debugfs -R "dump /etc/selinux/plat_sepolicy_vers.txt $workdir/psv.txt" $img 2>/dev/null || true
    if [ ! -s $workdir/psv.txt ]; then
        bbwarn "vendor.img has no plat_sepolicy_vers.txt; writing 28.0"
        printf '28.0\n' > $workdir/plat_sepolicy_vers.txt
        debugfs -w -R "write $workdir/plat_sepolicy_vers.txt /etc/selinux/plat_sepolicy_vers.txt" $img
    fi

    debugfs -R "dump /build.prop $workdir/build.prop" $img
    changed=0

    # 2. ro.vndk.version
    #
    #    Not set at all on a vndk_lite build. linkerconfig keys the vendor
    #    namespace off it, so without it no VNDK path is wired up and every
    #    vendor HAL dies at exec with "CANNOT LINK EXECUTABLE ... library
    #    libbinder.so not found" (likewise libhidlbase, libhardware, libcutils).
    #    The libraries are present - the GSI mounts /apex/com.android.vndk.v28 -
    #    the vendor just never asks for them.
    if ! grep -q '^ro.vndk.version=' $workdir/build.prop; then
        echo 'ro.vndk.version=28' >> $workdir/build.prop
        changed=1
    fi

    # 3. ro.hardware.egl
    #
    #    Never set by this vendor, and on Halium 9 nothing needed it. The A16
    #    EGL loader tries persist.graphics.egl, then ro.hardware.egl, then
    #    ro.board.platform, and opens libEGL_<value>.so. Only ro.board.platform
    #    is set, to msm8953, but the drivers here are /vendor/lib{,64}/egl/
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
    #     mediacodec), same as tissot. Under the A16 GSI, vendor processes link
    #     the A16 bionic, which calls gettid()/getpid() directly - syscalls the
    #     2017-era configstore@1.1.policy never allowed - so
    #     vendor.configstore-hal takes SIGSYS inside registerAsService() ~0.9s
    #     after every start, forever.
    #
    #     Nothing on the A16 host uses configstore, so LuneOS itself is fine and
    #     this goes unnoticed. But Waydroid (HALIUM_9) resolves
    #     ISurfaceFlingerConfigs through the HOST hwservicemanager via
    #     /dev/host_hwbinder, the host VINTF manifest declares configstore@1.1,
    #     and libhidl then waits for it forever -> Waydroid's SurfaceFlinger
    #     never starts -> sys.boot_completed is never set -> no Waydroid UI.
    #
    #     gettid/getpid are the proven killers on tissot (strace:
    #     si_syscall=__NR_gettid); the rest are cheap insurance for the same
    #     A16-bionic drift. Every name must exist in the vendor's old libminijail
    #     seccomp_policy table - an unknown name is fatal at policy parse - so do
    #     NOT add e.g. rseq, which that table predates.
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
    #    the VNDK-28 APEX and nothing else. libstdc++ is in neither, so it has to
    #    travel with the vendor.
    #
    #    This list is short because it was derived rather than copied: taking the
    #    DT_NEEDED closure of every ELF in mido's vendor.img, subtracting what
    #    vendor.img itself provides and what /apex/com.android.vndk.v28 provides,
    #    leaves only LLNDK libraries (libc, libdl, libm, liblog, libEGL,
    #    libGLESv*, libnativewindow, libsync, libandroid*), which the vendor
    #    namespace always gets, plus:
    #
    #      libstdc++.so   the bionic forwarder, dropped from AOSP long ago. Both
    #                     arches: /vendor/lib64/hw/fingerprint.goodix.so and
    #                     gxfingerprint.default.so need the 64-bit one, and
    #                     /vendor/lib/libts_face_beautify_hal.so - reached from
    #                     camera.msm8953.so - needs the 32-bit one, so without it
    #                     the camera provider fails HIDL_FETCH exactly as on
    #                     tissot.
    #
    #    Three other names come out of that subtraction and are deliberately NOT
    #    injected:
    #      libgcc.so, libFastRPC_UTF_Forward_skel.so
    #                     only referenced by /vendor/lib/rfsa/adsp/* - Hexagon
    #                     DSP images, never loaded by the ARM linker.
    #      libmmosal_proprietary.so (64-bit), libllvd_smore.so
    #                     wanted by the Miracast stack and by one camera
    #                     low-light module; absent from the device system.img
    #                     too, so they were already missing under Halium 9 and
    #                     are not a GSI regression.
    #
    #    mido needs none of tissot's Goodix fingerprint injection: its HAL is the
    #    device-specific @2.1-service.xiaomi_mido, and both modules it can select
    #    (fingerprint.goodix.so, fingerprint.searchf.so) plus their private deps
    #    (libfp_client.so, libfpnav.so, libQSEEComAPI.so) are already in this
    #    vendor.img.
    # Skipping the libraries must NOT skip the fsck and shrink below: the
    # property and seccomp writes above have already happened, and an expanded
    # sparse image is still at its full partition size.
    if [ "${MIDO_VENDOR_EXTRA_LIBS}" != "1" ]; then
        bbnote "MIDO_VENDOR_EXTRA_LIBS is not 1, skipping the support libraries"
    else
        for l in lib/libstdc++.so \
                 lib64/libstdc++.so; do
            staged=$workdir/`echo $l | tr / _`
            debugfs -R "dump /system/$l $staged" $sysimg
            if [ ! -s $staged ]; then
                bbfatal "could not extract /system/$l from the device system.img"
            fi
            debugfs -w -R "rm /$l" $img 2>/dev/null || true
            debugfs -w -R "write $staged /$l" $img
        done
    fi

    set +e
    e2fsck -fy $img > $workdir/fsck.log 2>&1
    rc=$?
    set -e
    if [ $rc -gt 1 ]; then
        bbfatal "e2fsck on the patched vendor.img failed with $rc:`cat $workdir/fsck.log`"
    fi

    # Expanding the sparse image inflated it from 290 MB to the full 872 MB
    # partition size, nearly all of it zeroes. The .inc only calls resize2fs on
    # images it expanded itself, and by now this one is already raw, so it would
    # ship those ~570 MB of padding inside the rootfs. Shrink to the minimum -
    # after the injections above, so there is room for them.
    resize2fs -M $img >> $workdir/fsck.log 2>&1

    rm -rf $workdir
}

# The GSI is a generic Android userspace; mido's vendor is an Android 9 one that
# expects the legacy top-level symlinks the device's own system.img provided.
#
# The A9 system.img had /firmware, /dsp and /persist pointing into the vendor.
# /vendor/lib64/hw/fingerprint.searchf.so and gxfingerprint.default.so both carry
# the literal string "/firmware/image" - that is where QSEECom loads the
# fingerprint trustlet from - so without the symlink the trustlet never loads and
# enrolment fails the same way it did on tissot. The targets below are read off
# mido's own device system.img, and are the same three tissot uses.
#
# Note the host already gets these from android-system, which symlinks them to
# /android/$i; that does nothing for code running INSIDE the container, whose
# root is this image.
#
# Unlike tissot, nothing else has to be grafted in: tissot additionally needed
# /system/etc/camera, because its camera_config.xml and chromatix XMLs lived in
# the device system.img. mido's libmmcamera2_sensor_modules.so reads
# /vendor/etc/camera/ instead, and that directory - camera_config.xml plus 19
# chromatix files - is already inside mido's vendor.img.
mido_patch_gsi_for_legacy_vendor() {
    gsi="$1"
    workdir=`mktemp -d`

    debugfs -w -R "symlink /firmware /vendor/firmware_mnt" $gsi
    debugfs -w -R "symlink /dsp /vendor/dsp" $gsi
    debugfs -w -R "symlink /persist /mnt/vendor/persist" $gsi

    set +e
    e2fsck -fy $gsi > $workdir/fsck.log 2>&1
    rc=$?
    set -e
    if [ $rc -gt 1 ]; then
        bbfatal "e2fsck on the patched GSI failed with $rc:`cat $workdir/fsck.log`"
    fi

    rm -rf $workdir
}
