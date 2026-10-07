# The 16.0 GSI on a device whose Halium 9 build predates Treble.
#
# hammerhead, mako and tenderloin never had a /vendor partition, and their
# Halium 9 builds are one monolithic system.img with the vendor payload under
# /system/vendor. The GSI wants a Treble vendor next to it, so this recipe
# derives one from that old image at build time - see halium-legacy-vendor.bbclass
# and lib/halium/legacy_vendor.py for what that involves - and ships it beside the
# GSI the way android-system-image-tissot does with a vendor.img that
# `m vendorimage` produced.
#
# Opt-in per machine through <DEVICE>_ANDROID_SYSTEM = "gsi" in its machine
# configuration; the default machines still ship the device's own system.img.
#
# What a recipe including this sets:
#   HALIUM_LEGACY_DEVICE_URL, HALIUM_LEGACY_DEVICE_SHA256   the device's Halium 9 tarball
#   HALIUM_LEGACY_INIT_FILES                                  its init and ueventd scripts
#   HALIUM_LEGACY_VENDOR_SHA256                               the sha256 of its published vendor image, if any
require recipes-core/android-system-image/android-system-image.inc
require recipes-core/android-system-image/halium-luneos-gsi-16.inc

inherit halium-legacy-vendor

# The 32-bit GSI is its own build on its own cadence, like mindphone's. It has to
# carry com.android.vndk.v28 for an Android 9 vendor, which the 20260830 build
# does not. 20261005-1 is the first with a /persist directory and a /firmware link
# of its own, so a device no longer needs them patched in; 20261005-2 adds /efs for
# sm-t520's Samsung vendor. 20261007-1 lets the sphal namespace link libbinder.so and
# libui.so from the VNDK 28 APEX: the TouchPad's gralloc.msm8660.so needs them, and without
# them media.swcodec aborts and every libdroidmedia (gst-droid) client waits for ever.
HALIUM_LUNEOS_GSI16_ARCH = "halium_arm"
HALIUM_LUNEOS_GSI16_PV = "20261007-1"
HALIUM_LUNEOS_GSI16_SHA256 = "3f62631989cc03148725e22be84d3c60ed68f16cc8f5aef56157ee612fae5ae4"

PV = "${HALIUM_LUNEOS_GSI16_PV}"

# A device whose vendor image is published next to the GSI, as <machine>-vendor.img in the same
# release, sets HALIUM_LEGACY_VENDOR_SHA256: the image is then fetched and installed as it is, with the
# plain GSI, and nothing is derived. The HALIUM_LEGACY_* settings of its recipe stay as the record of how
# that image was made; to make a new one, set HALIUM_LEGACY_VENDOR_SHA256:pn-<recipe> = "" in local.conf
# (a plain assignment there loses to the recipe's), build, and publish UNPACKDIR/vendor.img. Such a device must leave the GSI alone
# (HALIUM_LEGACY_GSI_AUTODETECT = "0", no GSI files, links, directories or pruning), or the GSI it gets
# is not the one the image was made against.
HALIUM_LEGACY_VENDOR_SHA256 ?= ""
HALIUM_LEGACY_VENDOR_URL ?= "https://github.com/webOS-ports/halium-images/releases/download/${HALIUM_LUNEOS_GSI16_RELEASE}/${MACHINE}-vendor.img"
HALIUM_LEGACY_PREBUILT = "${@'1' if d.getVar('HALIUM_LEGACY_VENDOR_SHA256') else '0'}"

# The release asset name carries no version, so the download is named after the GSI it was made against;
# otherwise the next release's image would collide with this one in DL_DIR.
SRC_URI = " \
    ${HALIUM_LUNEOS_GSI16_URL};name=gsi;subdir=gsi \
    ${@'${HALIUM_LEGACY_VENDOR_URL};name=vendor;subdir=prebuilt;downloadfilename=${MACHINE}-vendor-${HALIUM_LUNEOS_GSI16_PV}.img' \
       if d.getVar('HALIUM_LEGACY_PREBUILT') == '1' else \
       '${HALIUM_LEGACY_DEVICE_URL};name=device;subdir=device'} \
"
SRC_URI[gsi.sha256sum] = "${HALIUM_LUNEOS_GSI16_SHA256}"
SRC_URI[device.sha256sum] = "${HALIUM_LEGACY_DEVICE_SHA256}"
SRC_URI[vendor.sha256sum] = "${HALIUM_LEGACY_VENDOR_SHA256}"

python () {
    if d.getVar("HALIUM_LEGACY_PREBUILT") != "1":
        return
    for var in ("HALIUM_LEGACY_GSI_FILES", "HALIUM_LEGACY_GSI_LINKS", "HALIUM_LEGACY_GSI_DIRS",
                "HALIUM_LEGACY_GSI_PRUNE"):
        if d.getVar(var).strip():
            bb.fatal("%s is set, but a published vendor image (HALIUM_LEGACY_VENDOR_SHA256) goes with "
                     "the plain GSI" % var)
    if d.getVar("HALIUM_LEGACY_GSI_AUTODETECT") != "0":
        bb.fatal("a published vendor image (HALIUM_LEGACY_VENDOR_SHA256) goes with the plain GSI: "
                 "set HALIUM_LEGACY_GSI_AUTODETECT = \"0\"")
    bb.build.deltask("do_halium_legacy_vendor", d)
    bb.build.addtask("do_halium_legacy_prebuilt", "do_install", "do_unpack", d)
}

# Puts the fetched images where do_install of android-system-image.inc takes them from.
do_halium_legacy_prebuilt() {
    cp ${UNPACKDIR}/gsi/system.img ${UNPACKDIR}/system.img
    cp ${UNPACKDIR}/prebuilt/${MACHINE}-vendor-${HALIUM_LUNEOS_GSI16_PV}.img ${UNPACKDIR}/vendor.img
}

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"

# The host's /etc/wifi normally points into the GSI's /system/etc/wifi. This GSI has no
# free inode to hold the old image's Wi-Fi files, so halium-legacy-vendor puts them in the
# vendor image (HALIUM_LEGACY_COPY_FILES) and the link follows them.
do_install:append() {
    ln -sf /vendor/etc/wifi ${D}${sysconfdir}/wifi
    # libacdbloader opens /etc/acdbdata/<board>/*.acdb by absolute path
    ln -sf /vendor/etc/acdbdata ${D}${sysconfdir}/acdbdata
}
