require recipes-core/android-system-image/android-system-image-legacy-gsi.inc
require recipes-core/android-system-image/android-system-image-legacy-a9-services.inc

COMPATIBLE_MACHINE = "mako-halium"

# The vendor image published with the GSI (mako-halium-vendor.img in its release), made by the settings below
# against that GSI. See android-system-image-legacy-gsi.inc for how to make a new one.
HALIUM_LEGACY_VENDOR_SHA256 = "64644b6cdcfa9956b2005ff9c4cdf2302c18d1297a293a4bed1bb7338f321aff"

HALIUM_LEGACY_DEVICE_URL = "https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-9.0-20240313-1-mako.tar.bz2/halium-luneos-9.0-20240313-1-mako.tar.bz2"
HALIUM_LEGACY_DEVICE_SHA256 = "9f258461ba71306231400a45fce45539800ad8823e4d50f7c5f9f17d4427d467"

HALIUM_LEGACY_INIT_FILES = "init.mako.rc init.mako.power.rc ueventd.mako.rc"

# The GSI is the plain halium_arm one, which has /persist and /firmware itself: nothing of the old image
# goes into it (what the vendor names under /system/etc, DxHDCP.cfg, is the HDCP DRM blob).
HALIUM_LEGACY_GSI_AUTODETECT = "0"

# The Nexus 4 has everything the Nexus 5 does that needs a service of its own under the GSI (see
# android-system-image-legacy-a9-services.inc): a camera HAL (camera.mako.so), a Qualcomm modem
# (libril-qc-qmi-1.so) and the vendor OMX codecs (libstagefrighthw.so). Its Halium 9 build keeps the vendor's
# libraries and configuration under /system/vendor already, so unlike the hammerhead's none of them has to be
# fetched from /system/lib or /system/etc by name.
HALIUM_LEGACY_EXTRA_FILES += " \
    ${HALIUM_LEGACY_A9_CAMERA} \
    ${HALIUM_LEGACY_A9_RIL} \
    ${HALIUM_LEGACY_A9_OMX} \
"
HALIUM_LEGACY_BINDERIZED_HALS = "android.hardware.graphics.composer android.hardware.graphics.allocator \
    android.hardware.camera.provider"
