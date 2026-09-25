SUMMARY = "MediaTek combo (WMT/connac) connectivity bring-up for Halium"
DESCRIPTION = "Brings up the MediaTek combo chip from the glibc side, both ways \
it can be built: force-loading the vendor combo modules against the LuneOS \
kernel where they are modules, and sending CONNINFRA_IOCTL_DO_MODULE_INIT where \
the driver is built into the kernel and waits for userspace to do it. Then \
powers on Wi-Fi and fixes up Bluetooth (nvram MAC + container HAL device \
owner). Ships on every Halium rootfs; the units are guarded by the presence of \
the combo driver, so they are inert on non-MediaTek devices."
SECTION = "base"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

# libhybris-based container, and the modules only exist there
COMPATIBLE_MACHINE = "^halium$"

PV = "1.0"

# Nothing but local files, so nothing ever lands in the default
# S = "${UNPACKDIR}/${BP}" and do_unpack warns about it.
S = "${UNPACKDIR}"

SRC_URI = " \
    file://mtk-conninfra-init.c \
    file://mtk-wifi-nvram.c \
    file://mtk-load-modules.sh \
    file://mtk-bt-address.sh \
    file://mtk-bt-bringup.sh \
    file://mtk-fuelgauged.sh \
    file://mtk-conninfra-init.service \
    file://mtk-connectivity-modules.service \
    file://mtk-connectivity-wifi.service \
    file://mtk-connectivity-bt.service \
    file://mtk-fuelgauged.service \
"

inherit systemd

# One translation unit, no libraries beyond libc - see mtk-conninfra-init.c for
# what it does and why it cannot be a shell script (it is an ioctl, and the
# combo driver takes no other trigger).
do_compile() {
    ${CC} ${CFLAGS} ${LDFLAGS} -o ${B}/mtk-conninfra-init ${UNPACKDIR}/mtk-conninfra-init.c
    ${CC} ${CFLAGS} ${LDFLAGS} -o ${B}/mtk-wifi-nvram     ${UNPACKDIR}/mtk-wifi-nvram.c
}

do_install() {
    install -d ${D}${sbindir} ${D}${bindir}
    install -m 0755 ${B}/mtk-conninfra-init          ${D}${sbindir}/mtk-conninfra-init
    install -m 0755 ${B}/mtk-wifi-nvram              ${D}${sbindir}/mtk-wifi-nvram
    install -m 0755 ${UNPACKDIR}/mtk-load-modules.sh ${D}${sbindir}/mtk-load-modules.sh
    install -m 0755 ${UNPACKDIR}/mtk-fuelgauged.sh   ${D}${sbindir}/mtk-fuelgauged.sh
    install -m 0755 ${UNPACKDIR}/mtk-bt-address.sh   ${D}${bindir}/mtk-bt-address.sh
    install -m 0755 ${UNPACKDIR}/mtk-bt-bringup.sh   ${D}${bindir}/mtk-bt-bringup.sh

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/mtk-conninfra-init.service       ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/mtk-connectivity-modules.service ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/mtk-connectivity-wifi.service    ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/mtk-connectivity-bt.service      ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/mtk-fuelgauged.service           ${D}${systemd_system_unitdir}
}

SYSTEMD_SERVICE:${PN} = " \
    mtk-conninfra-init.service \
    mtk-connectivity-modules.service \
    mtk-connectivity-wifi.service \
    mtk-connectivity-bt.service \
    mtk-fuelgauged.service \
"

# modprobe/depmod, rfkill, hexdump, the container tool, and the kernel's
# force-load support (the modules are stock vendor .ko with mismatched CRCs).
RDEPENDS:${PN} = "kmod util-linux-hexdump util-linux-nsenter lxc"
RRECOMMENDS:${PN} = "rfkill"

FILES:${PN} = "${sbindir}/mtk-conninfra-init ${sbindir}/mtk-wifi-nvram ${sbindir}/mtk-load-modules.sh ${bindir}/mtk-bt-address.sh ${bindir}/mtk-bt-bringup.sh ${sbindir}/mtk-fuelgauged.sh"
