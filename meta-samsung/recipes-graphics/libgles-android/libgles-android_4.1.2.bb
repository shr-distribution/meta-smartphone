require recipes-graphics/libgles-android/libgles-android.inc

LIC_FILES_CHKSUM += "file://${WORKDIR}/LICENSE.imgtec;md5=b341de387d90c3d3dccce77c1acae986"

COMPATIBLE_MACHINE = "tuna"

PV = "4.1.2+gitr${SRCPV}"
PR = "${INC_PR}.0"

ANDROID_VERSION = "tuna-4.1.2_r1"

SRC_URI += " \
  https://dl.google.com/dl/android/aosp/imgtec-maguro-jzo54k-0911a9b5.tgz;name=imgtec \
  git://github.com/webOS-ports/android-binaries;tag=${ANDROID_VERSION};protocol=git;destsuffix=android-binaries \
  file://pvrinit.sh"

SRCREV_FORMAT = "hybris"

SRC_URI[imgtec.md5sum] = "bb6a93e9839da21d3687f87edb84635e"
SRC_URI[imgtec.sha256sum] = "e297f1756121fdf417305108772c435167e9e652eb49242431c9213f236f2092"

unpack_imgtec_license() {
    cd ${WORKDIR}
    head -n 233 extract-imgtec-maguro.sh | tail -n 218 > LICENSE.imgtec
}

python do_unpack() {
    bb.build.exec_func('base_do_unpack', d)
    bb.build.exec_func('unpack_imgtec_license', d)
}

do_compile_append() {
    cd ${WORKDIR}
    tail -n +269 extract-imgtec-maguro.sh | tar zxv
}

VENDOR_BINARY_PATH = "${WORKDIR}/vendor/imgtec/maguro/proprietary"

BINARIES_SYSTEM_LIB  = "libIMGegl.so libPVRScopeServices.so libglslcompiler.so"
BINARIES_SYSTEM_LIB += "libpvr2d.so libpvrANDROID_WSEGL.so libsrv_init.so"
BINARIES_SYSTEM_LIB += "libsrv_um.so libusc.so"

do_install_append() {
    install -d ${D}${sysconfdir}/init.d
    install -d ${D}${sysconfdir}/rcS.d
    install -m 0755 ${WORKDIR}/pvrinit.sh ${D}${sysconfdir}/init.d
    ln -sf ../init.d/pvrinit.sh ${D}${sysconfdir}/rcS.d/S90pvrinit.sh

    cp -rav ${WORKDIR}/android-binaries/binaries/${MACHINE}/system ${D}

    mkdir -p ${D}/system/lib/egl
    mkdir -p ${D}/system/lib/hw
    mkdir -p ${D}/system/vendor/lib/egl

    install -m 0755 ${VENDOR_BINARY_PATH}/pvrsrvctl ${D}/system/bin
    cp ${VENDOR_BINARY_PATH}/libEGL_POWERVR_SGX540_120.so ${D}/system/lib/egl
    cp ${VENDOR_BINARY_PATH}/libGLESv1_CM_POWERVR_SGX540_120.so ${D}/system/lib/egl
    cp ${VENDOR_BINARY_PATH}/libGLESv2_POWERVR_SGX540_120.so ${D}/system/lib/egl
    cp ${VENDOR_BINARY_PATH}/gralloc.omap4.so ${D}/system/lib/hw

    for f in ${BINARIES_SYSTEM_LIB} ; do
        cp ${VENDOR_BINARY_PATH}/$f ${D}/system/lib/
    done

    cp ${VENDOR_BINARY_PATH}/libEGL_POWERVR_SGX540_120.so ${D}/system/vendor/lib/egl/
    cp ${VENDOR_BINARY_PATH}/libGLESv2_POWERVR_SGX540_120.so ${D}/system/vendor/lib/egl/
    cp ${VENDOR_BINARY_PATH}/libGLESv1_CM_POWERVR_SGX540_120.so ${D}/system/vendor/lib/egl/
}

FILES_${PN} += "/system"
FILES_${PN}-nonfree = " \
    /system/bin/pvrsrvctl \
    /system/lib/egl/libEGL_POWERVR_SGX540_120.so \
    /system/lib/egl/libGLESv1_CM_POWERVR_SGX540_120.so \
    /system/lib/egl/libGLESv2_POWERVR_SGX540_120.so \
    /system/lib/hw/gralloc.omap4.so \
    /system/lib/libIMGegl.so \
    /system/lib/libPVRScopeServices.so \
    /system/lib/libglslcompiler.so \
    /system/lib/libpvr2d.so \
    /system/lib/libpvrANDROID_WSEGL.so \
    /system/lib/libsrv_init.so \
    /system/lib/libsrv_um.so \
    /system/lib/libusc.so \
    /system/vendor/lib/egl/libEGL_POWERVR_SGX540_120.so \
    /system/vendor/lib/egl/libGLESv1_CM_POWERVR_SGX540_120.so \
    /system/vendor/lib/egl/libGLESv2_POWERVR_SGX540_120.so \
"
