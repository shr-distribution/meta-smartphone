DESCRIPTION = "System configuration and startup scripts for the Android compatibility layer"
LICENSE = "GPL-3.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-3.0;md5=c79ff39f19dfec6d293b95dea7b07891"

PR = "r3"

PACKAGE_ARCH = "${MACHINE_ARCH}"

# For some of the operations inside our setup script we need "real" sed!
RDEPENDS_${PN} = "sed"

# For running the container we're using lxc
RDEPENDS_${PN} += "lxc"

# We're the former android-initscripts package
RPROVIDES_${PN} = "android-initscripts"
RREPLACES_${PN} = "android-initscripts"
RCONFLICTS_${PN} = "android-initscripts"

inherit systemd useradd

SRC_URI = " \
    file://android-system.service \
    file://wait-for-android.sh \
    file://lxc-config \
    file://pre-start.sh \
    file://post-stop.sh \
    file://android-chroot \
    file://20-remove-services \
    file://30-mount-nothing \
    file://40-rootfs-rw \
"

# Create additional android users we need (need to have same UIDs as in android)
USERADD_PACKAGES = "${PN}"
USERADD_PARAM_${PN} = "-u 1000 -M system; -u 1001 -M radio"

do_install() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/android-system.service ${D}${systemd_unitdir}/system

    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/android-chroot ${D}${bindir}
    install -m 0755 ${WORKDIR}/wait-for-android.sh ${D}${bindir}

    install -d ${D}${localstatedir}/lib/lxc/android
    install -m 0644 ${WORKDIR}/lxc-config ${D}${localstatedir}/lib/lxc/android/config
    install -m 0755 ${WORKDIR}/pre-start.sh ${D}${localstatedir}/lib/lxc/android/
    install -m 0755 ${WORKDIR}/post-stop.sh ${D}${localstatedir}/lib/lxc/android/

    install -d ${D}${localstatedir}/lib/lxc/android/pre-start.d
    install -m 0755 ${WORKDIR}/20-remove-services ${D}${localstatedir}/lib/lxc/android/pre-start.d/
    install -m 0755 ${WORKDIR}/30-mount-nothing ${D}${localstatedir}/lib/lxc/android/pre-start.d/
    install -m 0755 ${WORKDIR}/40-rootfs-rw ${D}${localstatedir}/lib/lxc/android/pre-start.d/
}

SYSTEMD_SERVICE_${PN} = "android-system.service"
