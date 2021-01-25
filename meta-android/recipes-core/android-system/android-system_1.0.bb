DESCRIPTION = "System configuration and startup scripts for the Android compatibility layer"
LICENSE = "GPL-3.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-3.0;md5=c79ff39f19dfec6d293b95dea7b07891"

PR = "r4"

PACKAGE_ARCH = "${MACHINE_ARCH}"

# For some of the operations inside our setup script we need "real" sed!
RDEPENDS_${PN} = "sed"

# For running the container we're using lxc (>= 1.0 required)
# we use this with lxc from meta-luneos:
# https://github.com/webOS-ports/meta-webos-ports/tree/master/meta-luneos/recipes-containers/lxc
# because we don't want to include whole meta-virtualization
# https://github.com/webOS-ports/meta-webos-ports/commit/000ba06e2075e14ce9939f38e8dae9f4eea5824b
# But the one form meta-virtualization should work as well.
#
# Not adding the runtime dependency here to keep yocto-check-layer happy
# without this layer depending on whole meta-virtualization
# RDEPENDS_${PN} += "lxc"

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
    file://10-boot-marker \
    file://30-mount-nothing \
    file://40-rootfs-rw \
"

# Create additional android users we need (need to have same UIDs as in android)
USERADD_PACKAGES = "${PN}"
USERADD_PARAM_${PN} = " \
                       -u 1000 -M system; \
                       -u 1001 -M radio; \
                       -u 1002 -M bluetooth; \
                       -u 1003 -M graphics; \
                       -u 1006 -M camera; \
                       -u 1007 -M log; \
                       -u 1008 -M compass; \
                       -u 1010 -M wifi; \
                       -u 1011 -M adb; \
                       -u 1012 -M install; \
                       -u 1013 -M media; \
                       -u 1014 -M dhcp; \
                       -u 1015 -M sdcard_rw; \
                       -u 1016 -M vpn; \
                       -u 1017 -M keystore; \
                       -u 1018 -M usb; \
                       -u 1019 -M drm; \
                       -u 1020 -M mdnsr; \
                       -u 1021 -M gps; \
                       -u 1023 -M media_rw; \
                       -u 1024 -M mtp; \
                       -u 1026 -M drmrpc; \
                       -u 1027 -M nfc; \
                       -u 1028 -M sdcard_r; \
                       -u 1029 -M clat; \
                       -u 1030 -M loop_radio; \
                       -u 1031 -M mediadrm; \
                       -u 1032 -M package_info; \
                       -u 1033 -M sdcard_pics; \
                       -u 1034 -M sdcard_av; \
                       -u 1035 -M sdcard_all; \
                       -u 1036 -M logd; \
                       -u 1037 -M shared_relro; \
                       -u 1038 -M dbus; \
                       -u 1039 -M tlsdate; \
                       -u 1040 -M mediaex; \
                       -u 1041 -M audioserver; \
                       -u 1042 -M metrics_coll; \
                       -u 1043 -M metricsd; \
                       -u 1044 -M webserv; \
                       -u 1045 -M debuggerd; \
                       -u 1046 -M mediacodec; \
                       -u 1047 -M cameraserver; \
                       -u 1048 -M firewall; \
                       -u 1049 -M trunks; \
                       -u 1050 -M nvram; \
                       -u 1051 -M dns; \
                       -u 1052 -M dns_tether; \
                       -u 1053 -M webview_zygote; \
                       -u 1054 -M vehicle_network; \
                       -u 1055 -M media_audio; \
                       -u 1056 -M media_video; \
                       -u 1057 -M media_image; \
                       -u 1058 -M tombstoned; \
                       -u 1059 -M media_obb; \
                       -u 1060 -M ese; \
                       -u 1061 -M ota_update; \
                       -u 1062 -M automotive_evs; \
                       -u 1063 -M lowpan; \
                       -u 1064 -M hsm; \
                       -u 1065 -M reserved_disk; \
                       -u 1066 -M statsd; \
                       -u 1067 -M incidentd; \
                       -u 1068 -M secure_element; \
                       -u 1069 -M lmkd; \
                       -u 1070 -M llkd; \
                       -u 1071 -M iorapd; \
                       -u 1072 -M gpu_service; \
                       -u 1073 -M network_stack; \
                       -u 1074 -M gsid; \
                       -u 2000 -M shell; \
                       -u 2001 -M cache; \
                       -u 2002 -M diag; \
                       -u 2901 -M vendor_qti_diag; \
                       -u 2902 -M vendor_qdss; \
                       -u 2903 -M vendor_rfs; \
                       -u 2904 -M vendor_rfs_shared; \
                       -u 2905 -M vendor_adpl_odl; \
                       -u 2906 -M vendor_qrtr; \
                       -u 3001 -M net_bt_admin; \
                       -u 3002 -M net_bt; \
                       -u 3003 -M inet; \
                       -u 3004 -M net_raw; \
                       -u 3005 -M net_admin; \
                       -u 3006 -M net_bw_stats; \
                       -u 3007 -M net_bw_acct; \
                       -u 3008 -M net_bt_stack; \
                       -u 3009 -M readproc; \
                       -u 3010 -M wakelock; \
                       -u 3011 -M uhid; \
                       -u 9997 -M everybody; \
                       -u 9998 -M misc; \
                       -u 9999 -M nobody; "

do_install() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/android-system.service ${D}${systemd_unitdir}/system

    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/wait-for-android.sh ${D}${bindir}

    install -d ${D}${localstatedir}/lib/lxc/android
    install -m 0644 ${WORKDIR}/lxc-config ${D}${localstatedir}/lib/lxc/android/config
    install -m 0755 ${WORKDIR}/pre-start.sh ${D}${localstatedir}/lib/lxc/android/
    install -m 0755 ${WORKDIR}/post-stop.sh ${D}${localstatedir}/lib/lxc/android/

    install -d ${D}${localstatedir}/lib/lxc/android/pre-start.d
    install -m 0755 ${WORKDIR}/10-boot-marker ${D}${localstatedir}/lib/lxc/android/pre-start.d/
    install -m 0755 ${WORKDIR}/30-mount-nothing ${D}${localstatedir}/lib/lxc/android/pre-start.d/
    install -m 0755 ${WORKDIR}/40-rootfs-rw ${D}${localstatedir}/lib/lxc/android/pre-start.d/

    install -d ${D}${localstatedir}/lib/lxc/android/rootfs

    # Somehow during post installation the link for android-system.service isn't created
    install -d ${D}${sysconfdir}/systemd/system/basic.target.requires
    ln -sf ${systemd_unitdir}/system/android-system.service \
        ${D}${sysconfdir}/systemd/system/basic.target.requires/android-system.service
}

SYSTEMD_SERVICE_${PN} = "android-system.service"
