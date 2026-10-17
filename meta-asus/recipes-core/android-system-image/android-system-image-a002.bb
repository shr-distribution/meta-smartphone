require recipes-core/android-system-image/android-system-image.inc
require recipes-core/android-system-image/halium-luneos-gsi-16.inc

COMPATIBLE_MACHINE = "a002"

# The Zenfone AR runs the device-agnostic arm64 16.0 GSI as its Android system,
# paired with a vendor.img built from the stock Nougat /vendor. Unlike tissot
# there is no device (own Halium 9) build: the stock ROM is Android 7.0 only and
# there is no LineageOS tree for this phone, so the GSI is the only system.img
# and the vendor is the only source of HALs.
#
# The vendor.img was built by `m vendorimage` in a Halium 9 Android tree against
# a LineageOS 16.0 msm8996 device tree (device/asus/{A002,msm8996-common}) that
# packs the stock /vendor (2170 files: gralloc.msm8996, gatekeeper, vulkan,
# fingerprint.qcom, keystore, sensors, nfc_nci, sound_trigger, camera, adreno,
# ril ...). It is a raw ext4 image published next to the GSI, not derived here
# from a monolithic system.img the way bullhead's is.

# The arm64 GSI (halium_arm64), from halium-luneos-gsi-16.inc.
PV = "${HALIUM_LUNEOS_GSI16_PV}"
A002_GSI_RELEASE ?= "${HALIUM_LUNEOS_GSI16_RELEASE}"
A002_GSI_TARBALL ?= "${HALIUM_LUNEOS_GSI16_TARBALL}"
A002_GSI_SHA256  ?= "${HALIUM_LUNEOS_GSI16_SHA256}"

# The A002 vendor.img, published at webOS-ports/halium-images. Raw ext4 (not a
# sparse Android image), so the build.prop injection below can edit it in place
# with debugfs and the base recipe installs it without simg2img.
A002_VENDOR_RELEASE ?= "halium-luneos-20261010"
A002_VENDOR_IMG     ?= "A002-vendor-20261010.img"
A002_VENDOR_SHA256  ?= "68909ac79f9fdcbb1ddb8dccb3b9749cd5778391fbc0f80a307da5bed022dc0f"

SRC_URI = "\
    https://github.com/webOS-ports/halium-images/releases/download/${A002_GSI_RELEASE}/${A002_GSI_TARBALL};name=gsi;subdir=gsi \
    https://github.com/webOS-ports/halium-images/releases/download/${A002_VENDOR_RELEASE}/${A002_VENDOR_IMG};name=vendor \
"
SRC_URI[gsi.sha256sum] = "${A002_GSI_SHA256}"
SRC_URI[vendor.sha256sum] = "${A002_VENDOR_SHA256}"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"

# Stage the GSI system.img and the vendor.img where the base .inc do_install
# expects them (${UNPACKDIR}/system.img and ${UNPACKDIR}/vendor.img), then
# inject into the vendor the two build.prop keys a vndk_lite / non-Treble vendor
# is missing. Done in do_install:prepend, before the base body, so a re-run is
# idempotent (the base body consumes the files in place).
do_install:prepend() {
    cp ${UNPACKDIR}/gsi/system.img ${UNPACKDIR}/system.img

    cp ${UNPACKDIR}/${A002_VENDOR_IMG} ${UNPACKDIR}/vendor.img
    a002_patch_vendor_build_prop ${UNPACKDIR}/vendor.img
}

# ro.vndk.version and ro.hardware.egl are both ABSENT from this vendor's
# /build.prop (read with debugfs from the built image: it has
# ro.board.platform=msm8996 and /etc/selinux/plat_sepolicy_vers.txt=28.0, but
# neither of these). Both are fatal against the A16 GSI and both are pure
# metadata, so inject them here rather than rebuild the Android side - the same
# fix as tissot_patch_vendor_for_treble, trimmed to what A002 actually lacks:
#
#   ro.vndk.version=28   linkerconfig keys the vendor namespace off it; without
#                        it no VNDK path is wired up and every vendor HAL dies at
#                        exec with "library libbinder.so not found" (the GSI
#                        mounts /apex/com.android.vndk.v28, the vendor just never
#                        asks for it). The vendor is Android 9-class VNDK on an
#                        msm8996 Nougat base; 28 matches plat_sepolicy_vers.txt.
#   ro.hardware.egl=adreno  the A16 EGL loader tries persist.graphics.egl, then
#                        ro.hardware.egl, then ro.board.platform, opening
#                        libEGL_<value>.so. Only ro.board.platform is set
#                        (msm8996) but the drivers are /vendor/lib64/egl/
#                        libEGL_adreno.so, so without this surface-manager aborts
#                        "couldn't find an OpenGL ES implementation".
#
# plat_sepolicy_vers.txt is already present (28.0), so unlike tissot it is not
# written here. debugfs "rm" leaves a bitmap difference, so e2fsck -fy after is
# required; it exits 1 when it corrects something, the expected path.
a002_patch_vendor_build_prop() {
    img="$1"
    workdir=`mktemp -d`

    if file $img | grep -q "Android sparse image"; then
        bbfatal "$img is a sparse Android image; publish the raw ext4 vendor.img (simg2img it first)"
    fi

    debugfs -R "dump /build.prop $workdir/build.prop" $img
    [ -s $workdir/build.prop ] || bbfatal "no /build.prop in the A002 vendor.img"
    changed=0
    if ! grep -q '^ro.vndk.version=' $workdir/build.prop; then
        echo 'ro.vndk.version=28' >> $workdir/build.prop
        changed=1
    fi
    if ! grep -q '^ro.hardware.egl=' $workdir/build.prop; then
        echo 'ro.hardware.egl=adreno' >> $workdir/build.prop
        changed=1
    fi
    if [ $changed = 1 ]; then
        android_image_write "$workdir/build.prop" "$img" "/build.prop"
    fi

    set +e
    e2fsck -fy $img > $workdir/fsck.log 2>&1
    rc=$?
    set -e
    if [ $rc -gt 1 ]; then
        bbfatal "e2fsck on the patched A002 vendor.img failed with $rc:`cat $workdir/fsck.log`"
    fi

    rm -rf $workdir
}
