require recipes-graphics/libgles-android/libgles-android.inc

LIC_FILES_CHKSUM += "file://${WORKDIR}/LICENSE.nvidia;md5=a7760e2d42bec19a59a14fa084df0d39"

COMPATIBLE_MACHINE = "grouper"

PV = "4.1.2+gitr${SRCPV}"
PR = "${INC_PR}.0"

ANDROID_VERSION = "grouper-4.1.2_r1"

SRC_URI += " \
  https://dl.google.com/dl/android/aosp/nvidia-grouper-jzo54k-56de148f.tgz;name=nvidia \
  git://github.com/webOS-ports/android-binaries;tag=${ANDROID_VERSION};protocol=git;destsuffix=android-binaries"

SRCREV_FORMAT = "hybris"

SRC_URI[nvidia.md5sum] = "c2fc0baa77ee1f9a71096459950651ba"
SRC_URI[nvidia.sha256sum] = "41c2fc49bbdd8956710fd451984403ca61bef41e2b3e80bce46e8a640ecd3957"

unpack_nvidia_license() {
    cd ${WORKDIR}
    head -n 233 extract-nvidia-grouper.sh | tail -n 218 > LICENSE.nvidia
}

python do_unpack() {
    bb.build.exec_func('base_do_unpack', d)
    bb.build.exec_func('unpack_nvidia_license', d)
}

do_compile_append() {
    cd ${WORKDIR}
    tail -n +269 extract-nvidia-grouper.sh | tar zxv
}

VENDOR_BINARY_PATH = "${WORKDIR}/vendor/nvidia/grouper/proprietary"

BINARIES_SYSTEM_LIB  = "libcgdrv.so libnvmm_video.so libnvddk_2d.so libnvodm_dtvtuner.so"
BINARIES_SYSTEM_LIB += "libnvrm.so libnvparser.so libnvodm_hdmi.so libnvmm_image.so"
BINARIES_SYSTEM_LIB += "libnvmm_utils.so libnvasfparserhal.so libnvmmlite.so libnvmmlite_audio.so"
BINARIES_SYSTEM_LIB += "libardrv_dynamic.so libnvomx.so libnvmm_misc.so libnvmm_contentpipe.so"
BINARIES_SYSTEM_LIB += "libnvapputil.so libnvmmlite_image.so libnvwsi.so libnvdispmgr_d.so"
BINARIES_SYSTEM_LIB += "libnvodm_imager.so libnvwinsys.so libnvtvmr.so libnvmm_service.so"
BINARIES_SYSTEM_LIB += "libnvmm.so libnvmm_audio.so libnvrm_graphics.so libnvmm_manager.so"
BINARIES_SYSTEM_LIB += "libnvmmlite_video.so libnvmm_writer.so libnvaviparserhal.so"
BINARIES_SYSTEM_LIB += "libtf_crypto_sst.so libnvmmlite_utils.so libnvcamerahdr.so"
BINARIES_SYSTEM_LIB += "libnvodm_query.so libnvmm_parser.so libnvodm_misc.so"
BINARIES_SYSTEM_LIB += "libnvomxilclient.so libnvsm.so libnvos.so libnvmm_camera.so"
BINARIES_SYSTEM_LIB += "libnvavp.so libnvddk_2d_v2.so"

BINARIES_SYSTEM_LIB_EGL = "libEGL_tegra.so libGLESv2_tegra.so libGLESv1_CM_tegra.so"
BINARIES_SYSTEM_LIB_HW  = "gralloc.tegra3.so hwcomposer.tegra3.so"

BINARIES_SYSTEM_ETC = "nvcamera.conf nvram.txt"
BINARIES_SYSTEM_ETC_FIRMWARE  = "nvavp_os_e0000000.bin nvavp_os_eff00000.bin nvavp_vid_ucode_alt.bin"
BINARIES_SYSTEM_ETC_FIRMWARE += "nvavp_os_0ff00000.bin nvavp_os_00001000.bin"

do_install_append() {
    install -d ${D}/system/lib/hw
    install -d ${D}/system/lib/egl
    install -d ${D}/system/etc/firmware

    cp -rav ${WORKDIR}/android-binaries/binaries/${MACHINE}/system ${D}

    for f in ${BINARIES_SYSTEM_LIB} ; do
        cp ${VENDOR_BINARY_PATH}/$f ${D}/system/lib/
    done

    for f in ${BINARIES_SYSTEM_LIB_HW} ; do
        cp ${VENDOR_BINARY_PATH}/$f ${D}/system/lib/hw/
    done

    for f in ${BINARIES_SYSTEM_LIB_EGL} ; do
        cp ${VENDOR_BINARY_PATH}/$f ${D}/system/lib/egl/
    done

    for f in ${BINARIES_SYSTEM_ETC} ; do
        cp ${VENDOR_BINARY_PATH}/$f ${D}/system/etc/
    done

    for f in ${BINARIES_SYSTEM_ETC_FIRMWARE} ; do
        cp ${VENDOR_BINARY_PATH}/$f ${D}/system/etc/firmware/
    done
}

FILES_${PN} += "/system"
FILES_${PN}-nonfree = " \
    /system/etc/nvram.txt \
    /system/etc/firmware/nvavp_os_e0000000.bin \
    /system/etc/firmware/nvavp_os_eff00000.bin \
    /system/etc/firmware/nvavp_vid_ucode_alt.bin \
    /system/etc/firmware/nvavp_os_0ff00000.bin \
    /system/etc/firmware/nvavp_os_00001000.bin \
    /system/etc/nvcamera.conf \
    /system/lib/libnvodm_imager.so \
    /system/lib/libnvwinsys.so \
    /system/lib/libnvtvmr.so \
    /system/lib/libnvmm_service.so \
    /system/lib/libnvmm.so \
    /system/lib/libnvmm_audio.so \
    /system/lib/libnvrm_graphics.so \
    /system/lib/libnvmm_manager.so \
    /system/lib/libnvmmlite_video.so \
    /system/lib/libnvmm_writer.so \
    /system/lib/libnvaviparserhal.so \
    /system/lib/libtf_crypto_sst.so \
    /system/lib/libnvmmlite_utils.so \
    /system/lib/libnvcamerahdr.so \
    /system/lib/libnvodm_query.so \
    /system/lib/libnvmm_parser.so \
    /system/lib/libnvodm_misc.so \
    /system/lib/libnvomxilclient.so \
    /system/lib/libnvsm.so \
    /system/lib/libnvos.so \
    /system/lib/libnvmm_camera.so \
    /system/lib/libnvavp.so \
    /system/lib/libnvddk_2d_v2.so \
    /system/lib/hw/gralloc.tegra3.so \
    /system/lib/hw/hwcomposer.tegra3.so \
    /system/lib/libcgdrv.so \
    /system/lib/libnvmm_video.so \
    /system/lib/libnvddk_2d.so \
    /system/lib/libnvodm_dtvtuner.so \
    /system/lib/libnvrm.so \
    /system/lib/libnvparser.so \
    /system/lib/libnvodm_hdmi.so \
    /system/lib/libnvmm_image.so \
    /system/lib/libnvmm_utils.so \
    /system/lib/libnvasfparserhal.so \
    /system/lib/libnvmmlite.so \
    /system/lib/libnvmmlite_audio.so \
    /system/lib/libardrv_dynamic.so \
    /system/lib/libnvomx.so \
    /system/lib/libnvmm_misc.so \
    /system/lib/libnvmm_contentpipe.so \
    /system/lib/libnvapputil.so \
    /system/lib/libnvmmlite_image.so \
    /system/lib/libnvwsi.so \
    /system/lib/libnvdispmgr_d.so \
    /system/lib/egl/egl.cfg \
    /system/lib/egl/libGLES_android.so \
    /system/lib/egl/libEGL_tegra.so \
    /system/lib/egl/libGLESv2_tegra.so \
    /system/lib/egl/libGLESv1_CM_tegra.so \
"
