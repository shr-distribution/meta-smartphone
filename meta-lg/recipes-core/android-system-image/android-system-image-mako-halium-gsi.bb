require recipes-core/android-system-image/android-system-image-legacy-gsi.inc
require recipes-core/android-system-image/android-system-image-legacy-a9-services.inc

COMPATIBLE_MACHINE = "mako-halium"

# The vendor image published with the GSI (mako-halium-vendor.img in its release), made by the settings below
# against that GSI. See android-system-image-legacy-gsi.inc for how to make a new one. This one (17 Oct 2026:
# libft2.so for the composer, the camera daemon launcher and nodes, the HSIC modem check in rild-wait.sh, the
# Bluetooth address and transport) is published under its own name, next to the earlier ones.
HALIUM_LEGACY_VENDOR_URL = "https://github.com/webOS-ports/halium-images/releases/download/${HALIUM_LUNEOS_GSI16_RELEASE}/mako-halium-vendor-20261017.img"
HALIUM_LEGACY_VENDOR_SHA256 = "e04af5f130a05d05587501963cf8e9d42824eae54cfc5e45452456fe28782d06"

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

# The camera daemon. The blob, mm-qcamera-daemon, cannot be linked by the Android 16 linker: liboemcamera.so
# needs libmmjpeg.so, which has text relocations, and an executable targets API 23 or newer, so the linker
# refuses it and init restarts qcamerasvr every 5 s for ever (468 times in one boot). The blob's main() does
# nothing but call qcamsvr_start() from liboemcamera.so; mm-qcamera-daemon-launcher (qcamsvr_launcher.c,
# built by build-qcamsvr-launcher.sh) lowers the target SDK to 22 first and then does the same through
# dlopen(). mako-qcamerasvr.rc is init.mako.rc's service with the launcher. ueventd-msm-camera.rc puts the
# camera's config nodes where the daemon opens them.
SRC_URI += " \
    file://mm-qcamera-daemon-launcher \
    file://mako-qcamerasvr.rc \
    file://ueventd-msm-camera.rc \
"
HALIUM_LEGACY_SKIP_SERVICES += "qcamerasvr"
HALIUM_LEGACY_EXTRA_FILES += " \
    mm-qcamera-daemon-launcher:/bin/mm-qcamera-daemon-launcher:755 \
    mako-qcamerasvr.rc:/etc/init/mako-qcamerasvr.rc:644 \
"
HALIUM_LEGACY_UEVENTD_EXTRA = "ueventd-msm-camera.rc"

# Bluetooth. The HAL (libbt-vendor) starts init.mako.rc's hciattach, init.mako.bt.sh, which runs
# hci_qcomm_init to set the WCNSS core up. hci_qcomm_init only talks to it over SMD when
# ro.qualcomm.bt.hci_transport=smd, which the old /system/build.prop had and the GSI's does not; without it
# it fails ("QSoC firmware download failed") and the controller reports 00:00:00:00:5A:AD. init.mako.bt.sh
# also gets the address from bdAddrLoader, which is in neither image, so even with the transport set the
# controller came up with an address of its own (00:A0:C6:AA:9C:B7). mako-bt-init.sh
# reads /persist/bluetooth/.bdaddr itself; mako-hciattach.rc starts it under the old service's name. The
# bdAddrLoader service only fails, so it goes too.
SRC_URI += " \
    file://mako-bt-init.sh \
    file://mako-hciattach.rc \
"
HALIUM_LEGACY_SYSTEM_PROP_PREFIXES += "ro.qualcomm.bt. ro.bluetooth."
HALIUM_LEGACY_SKIP_SERVICES += "hciattach bdAddrLoader"
HALIUM_LEGACY_EXTRA_FILES += " \
    mako-bt-init.sh:/bin/mako-bt-init.sh:755 \
    mako-hciattach.rc:/etc/init/mako-hciattach.rc:644 \
"
