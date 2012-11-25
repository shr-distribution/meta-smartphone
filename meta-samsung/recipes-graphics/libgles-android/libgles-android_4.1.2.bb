DESCRIPTION = "Properietary OpenGL ES implementation from Android based on libhybris"
LICENSE = "Apache-2.0 & Proprietary"
LIC_FILES_CHKSUM = " \
  file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10 \
  file://${WORKDIR}/LICENSE.imgtec;md5=b341de387d90c3d3dccce77c1acae986 \
"

COMPATIBLE_MACHINE = "tuna"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PV = "4.1.2+gitr${SRCPV}"
PR = "r0"
PROVIDES += "virtual/libgles1 virtual/libgles2 virtual/egl"

RREPLACES_${PN} = "libhybris"
RREPLACES_${PN}-dev = "libhybris-dev"

# We need the nonfree parts to be able to do anything but we still want them separated
# from the free parts for easier tracking.
RDEPENDS_${PN} = "${PN}-nonfree"

ANDROID_VERSION = "4.1.2_r1"

SRC_URI = " \
  git://github.com/morphis/libhybris;protocol=git;branch=master-next;name=hybris \
  https://dl.google.com/dl/android/aosp/imgtec-maguro-jzo54k-0911a9b5.tgz;name=imgtec \
  https://github.com/webOS-ports/android-binaries/archive/${ANDROID_VERSION}.zip;name=binaries;downloadfilename=android-binaries-${ANDROID_VERSION}.zip \
  file://pvrinit.sh"
S = "${WORKDIR}/git"

SRCREV_hybris = "f96e1101d9d8d5f824e880c8d7a6c8fb6bc22297"
SRC_URI[imgtec.md5sum] = "bb6a93e9839da21d3687f87edb84635e"
SRC_URI[imgtec.sha256sum] = "e297f1756121fdf417305108772c435167e9e652eb49242431c9213f236f2092"
SRC_URI[binaries.md5sum] = "3cf9606cbb472a001bbeefe7edbe02fe"
SRC_URI[binaries.sha256sum] = "0db1f57d5d82523492606057567efe1c16fd08c82144d6ede1e55ec966c28f14"

inherit autotools

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
    # Install header files until automake does it
    install -d ${D}${includedir}
    cp -ra ${WORKDIR}/git/include/KHR ${D}${includedir}/
    cp -ra ${WORKDIR}/git/include/CL ${D}${includedir}/
    cp -ra ${WORKDIR}/git/include/VG ${D}${includedir}/

    install -d ${D}${sysconfdir}/init.d
    install -d ${D}${sysconfdir}/rcS.d
    install -m 0755 ${WORKDIR}/pvrinit.sh ${D}${sysconfdir}/init.d
    ln -sf ../init.d/pvrinit.sh ${D}${sysconfdir}/rcS.d/S90pvrinit.sh

    cp -rav ${WORKDIR}/android-binaries-${ANDROID_VERSION}/binaries/system ${D}

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

PACKAGES =+ "${PN}-nonfree"
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
