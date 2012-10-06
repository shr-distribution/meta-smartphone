DESCRIPTION = "Properitary OpenGL ES implementation from Android"
LICENSE = "Properitary & Apache-2.0"
LIC_FILES_CHKSUM = " \
  file://extract-imgtec-maguro.sh;beginline=16;endline=233;md5=b341de387d90c3d3dccce77c1acae986 \
  file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

COMPATIBLE_MACHINE = "tuna"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PR = "r5"
PROVIDES += "virtual/libgles1 virtual/libgles2 virtual/egl"

RREPLACES_${PN} = "libhybris"
RREPLACES_${PN}-dev = "libhybris-dev"

SRC_URI = " \
  http://oss.reflected.net/jenkins/9090/cm-10-20120930-NIGHTLY-maguro.zip;name=cm \
  https://dl.google.com/dl/android/aosp/imgtec-maguro-jro03r-c7f638f1.tgz;name=imgtec \
  git://github.com/webOS-ports/android-binaries.git;protocol=git;tag=4.1.1_r6;destsuffix=android-binaries \
  git://github.com/morphis/libhybris;branch=master;protocol=git;branch=master;destsuffix=libhybris;name=libhybris \
  file://pvrinit.sh"
S = "${WORKDIR}"

SRCREV_libhybris = "a5c99f375bd9c10674b897efe2fabeded199a376"

SRC_URI[cm.md5sum] = "df73b7121a958f60fad74ed257eb4a83"
SRC_URI[cm.sha256sum] = "8d651f3d5408cb52954413db56355384e86816b75508593c940b668636ff1171"

SRC_URI[imgtec.md5sum] = "ead0e48c86717abcce76b72265797495"
SRC_URI[imgtec.sha256sum] = "3dc31ab2bd34e090d3b2244a67f9199760426ac7e354062d7c2f1d5481727a99"

do_compile() {
    tail -n +269 extract-imgtec-maguro.sh | tar zxv

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

    install -d ${D}/system/lib
    install -d ${D}/system/lib/hw
    install -d ${D}/system/lib/egl
    install -d ${D}/system/bin

    cp ${WORKDIR}/android-binaries/binaries/libc.so ${D}/system/lib/

    cp ${WORKDIR}/system/build.prop ${D}/system/
    install -m 0755 ${WORKDIR}/system/bin/linker ${D}/system/bin/

    for lib in libcorkscrew.so libcrypto.so libcutils.so libgccdemangle.so libhardware.so liblog.so libm.so libstdc++.so libstlport.so libui.so libutils.so libz.so libEGL.so libGLESv2.so libGLES_trace.so libGLESv1_CM.so ; do
        cp ${WORKDIR}/system/lib/$lib ${D}/system/lib/
    done

    cp -r ${WORKDIR}/system/lib/egl/* ${D}/system/lib/egl/

    install -d ${D}/system/vendor/lib
    install -d ${D}/system/vendor/lib/egl

    install -m 0755 ${WORKDIR}/vendor/imgtec/maguro/proprietary/pvrsrvctl ${D}/system/bin
    cp ${WORKDIR}/vendor/imgtec/maguro/proprietary/gralloc.omap4.so ${D}/system/lib/hw/

    for f in libEGL_POWERVR_SGX540_120.so libGLESv1_CM_POWERVR_SGX540_120.so libGLESv2_POWERVR_SGX540_120.so ; do
        cp ${WORKDIR}/vendor/imgtec/maguro/proprietary/$f ${D}/system/vendor/lib/egl/
        cp ${WORKDIR}/vendor/imgtec/maguro/proprietary/$f ${D}/system/lib/egl/
    done

    for f in libglslcompiler.so libIMGegl.so libpvr2d.so libpvrANDROID_WSEGL.so libPVRScopeServices.so libsrv_init.so libsrv_um.so libusc.so; do
        cp ${WORKDIR}/vendor/imgtec/maguro/proprietary/$f ${D}/system/lib/
    done

    install -d ${D}${sysconfdir}/init.d
    install -d ${D}${sysconfdir}/rcS.d
    install -m 0755 ${WORKDIR}/pvrinit.sh ${D}${sysconfdir}/init.d
    ln -sf ../init.d/pvrinit.sh ${D}${sysconfdir}/rcS.d/S60pvrinit.sh
}

FILES_${PN} += "/system"
