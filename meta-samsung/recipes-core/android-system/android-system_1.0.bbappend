# Ship the SM-T220 Android property overrides. The android-system lxc-config
# bind-mounts /usr/lib/droid-system-overlay/etc/prop.halium over the GSI's empty
# /system/etc/prop.halium when it exists. android-system is already
# MACHINE_ARCH, so a per-machine file does not leak into other machines'
# packages. The file carries the device in its name for the same reason the
# stubbed-services files do: two layers adding file://prop.halium would resolve
# to whichever layer is searched first.
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:sm-t220 = " file://prop.halium-sm-t220"

do_install:append:sm-t220() {
    install -d ${D}${libdir}/droid-system-overlay/etc
    install -m 0644 ${UNPACKDIR}/prop.halium-sm-t220 \
        ${D}${libdir}/droid-system-overlay/etc/prop.halium
}

FILES:${PN}:append:sm-t220 = " ${libdir}/droid-system-overlay"

# SM-T520: the list of vendor services to stub out (see the file for why). 50-stub-services picks it by the
# codename in the vendor build.prop, n2awifi, from stubbed-services.d.
SRC_URI:append:sm-t520 = " file://stubbed-services-n2awifi"

do_install:append:sm-t520() {
    install -d ${D}${localstatedir}/lib/lxc/android/stubbed-services.d
    install -m 0644 ${UNPACKDIR}/stubbed-services-n2awifi \
        ${D}${localstatedir}/lib/lxc/android/stubbed-services.d/n2awifi
}

FILES:${PN}:append:sm-t520 = " ${localstatedir}/lib/lxc/android/stubbed-services.d/n2awifi"
