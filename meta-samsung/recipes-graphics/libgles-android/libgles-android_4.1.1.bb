DESCRIPTION = "Properitary OpenGL ES implementation from Android based on libhybris"
LICENSE = "Properitary & Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

COMPATIBLE_MACHINE = "tuna"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PR = "r6"
PROVIDES += "virtual/libgles1 virtual/libgles2 virtual/egl"

RREPLACES_${PN} = "libhybris"
RREPLACES_${PN}-dev = "libhybris-dev"

SRC_URI = " \
  git://github.com/webOS-ports/android-binaries.git;protocol=git;tag=4.1.1_r6;destsuffix=android-binaries \
  git://github.com/morphis/libhybris;branch=master;protocol=git;branch=master;destsuffix=libhybris;name=libhybris \
  file://pvrinit.sh"
S = "${WORKDIR}"

SRCREV_android-binaries = ""
SRCREV_libhybris = "a5c99f375bd9c10674b897efe2fabeded199a376"

do_compile() {
    cd ${WORKDIR}/libhybris
    export PARALLEL_MAKE=""
    oe_runmake ARCH=arm
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/libhybris/test_egl ${D}${bindir}
    install -m 0755 ${WORKDIR}/libhybris/test_glesv2 ${D}${bindir}
    install -m 0755 ${WORKDIR}/libhybris/test_gingerbread ${D}${bindir}

    install -d ${D}${libdir}
    oe_libinstall -C ${WORKDIR}/libhybris -so libEGL ${D}${libdir}
    oe_libinstall -C ${WORKDIR}/libhybris -so libGLESv2 ${D}${libdir}
    oe_libinstall -C ${WORKDIR}/libhybris -so libhybris_gingerbread ${D}${libdir}

    install -d ${D}${includedir}
    cp -ra ${WORKDIR}/libhybris/include/* ${D}${includedir}/

    mkdir -p ${D}${libdir}/android-binaries
    cp ${WORKDIR}/android-binaries/binaries/libc.so ${D}${libdir}/android-binaries/

    install -d ${D}${sysconfdir}/init.d
    install -d ${D}${sysconfdir}/rcS.d
    install -m 0755 ${WORKDIR}/pvrinit.sh ${D}${sysconfdir}/init.d
    ln -sf ../init.d/pvrinit.sh ${D}${sysconfdir}/rcS.d/S90pvrinit.sh
}

SYSTEM_LIB_FILES = "libcorkscrew.so libcrypto.so libcutils.so libgccdemangle.so libhardware.so"
SYSTEM_LIB_FILES += "liblog.so libm.so libstdc++.so libstlport.so libui.so libutils.so libz.so"
SYSTEM_LIB_FILES += "libEGL.so libGLESv2.so libGLES_trace.so libGLESv1_CM.so"

SYSTEM_LIB_EGL_FILES = "libEGL_POWERVR_SGX540_120.so libGLESv1_CM_POWERVR_SGX540_120.so libGLESv2_POWERVR_SGX540_120.so"

SYSTEM_LIB_PROPERITARY_FILES = "libglslcompiler.so libIMGegl.so libpvr2d.so"
SYSTEM_LIB_PROPERITARY_FILES += "libpvrANDROID_WSEGL.so libPVRScopeServices.so libsrv_init.so libsrv_um.so libusc.so"

SOURCE_DIR = "/tmp/android-rootfs"
TARGET_DIR = "/system"
SYSTEM_BLKDEV = "/dev/mmcblk0p10"

pkg_postinst_${PN}() {
  #!/bin/sh -e
  BLKDEV="/dev/mmcblk0p10"
  if [ x"$D" = "x" ]; then
    mkdir -p ${SOURCE_DIR}
    mount ${BLKDEV} ${SOURCE_DIR}

    mkdir -p ${TARGET_DIR}/lib/hw
    mkdir -p ${TARGET_DIR}/lib/egl
    mkdir -p ${TARGET_DIR}/bin
    mkdir -p ${TARGET_DIR}/vendor/lib/egl

    cp ${libdir}/android-binaries/libc.so ${TARGET_DIR}/lib

    for f in ${SYSTEM_LIB_FILES}; do
      cp ${SOURCE_DIR}/lib/$f ${TARGET_DIR}/lib/
    done

    cp ${SOURCE_DIR}/build.prop ${TARGET_DIR}
    cp ${SOURCE_DIR}/bin/linker ${TARGET_DIR}/bin/
    cp -r ${SOURCE_DIR}/lib/egl/* ${TARGET_DIR}/lib/egl/
    cp ${SOURCE_DIR}/vendor/bin/pvrsrvctl ${TARGET_DIR}/bin
    cp ${SOURCE_DIR}/vendor/lib/hw/gralloc.omap4.so ${TARGET_DIR}/lib/hw/

    for f in ${SYSTEM_LIB_EGL_FILES}; do
      cp ${SOURCE_DIR}/vendor/lib/egl/$f ${TARGET_DIR}/vendor/lib/egl/
      cp ${SOURCE_DIR}/vendor/lib/egl/$f ${TARGET_DIR}/lib/egl/
    done

    for f in ${SYSTEM_LIB_PROPERITARY_FILES}; do
        cp ${SOURCE_DIR}/vendor/lib/$f ${TARGET_DIR}/lib/
    done

    umount ${SOURCE_DIR}
  else
    exit 1
  fi
}

pkg_postrm_${PN}() {
  #!/bin/sh -e
  if [ -d /system ] ; then
    rm -rf /system
  fi
}

FILES_${PN} += "${libdir}/android-binaries/"
