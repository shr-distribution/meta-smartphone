FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# Init scripts for the Nexus 5X's container, under the per-codename directory 65-extra-init
# reads (ro.product.vendor.device is "bullhead"). PACKAGE_ARCH is MACHINE_ARCH, so only the
# bullhead-halium package changes.
SRC_URI:append:bullhead-halium = " file://zz-luneos-bullhead-bringup.rc"

do_install:append:bullhead-halium() {
    install -d ${D}${localstatedir}/lib/lxc/android/extra-init.d/bullhead
    install -m 0644 ${UNPACKDIR}/zz-luneos-bullhead-bringup.rc \
        ${D}${localstatedir}/lib/lxc/android/extra-init.d/bullhead/
}
