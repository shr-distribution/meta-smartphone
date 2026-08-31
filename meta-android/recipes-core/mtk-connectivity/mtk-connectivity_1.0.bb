SUMMARY = "MediaTek combo (WMT/connac) connectivity bring-up for Halium"
DESCRIPTION = "Force-loads the MediaTek vendor combo modules against the LuneOS \
kernel, powers on Wi-Fi, and fixes up Bluetooth (nvram MAC + container HAL \
device owner). Ships on every Halium rootfs; the units are guarded by the \
presence of the vendor combo driver, so they are inert on non-MediaTek devices."
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
    file://mtk-load-modules.sh \
    file://mtk-bt-address.sh \
    file://mtk-connectivity-modules.service \
    file://mtk-connectivity-wifi.service \
    file://mtk-connectivity-bt.service \
"

inherit systemd

do_install() {
    install -d ${D}${sbindir} ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/mtk-load-modules.sh ${D}${sbindir}/mtk-load-modules.sh
    install -m 0755 ${UNPACKDIR}/mtk-bt-address.sh   ${D}${bindir}/mtk-bt-address.sh

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/mtk-connectivity-modules.service ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/mtk-connectivity-wifi.service    ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/mtk-connectivity-bt.service      ${D}${systemd_system_unitdir}
}

SYSTEMD_SERVICE:${PN} = " \
    mtk-connectivity-modules.service \
    mtk-connectivity-wifi.service \
    mtk-connectivity-bt.service \
"

# modprobe/depmod, rfkill, hexdump, the container tool, and the kernel's
# force-load support (the modules are stock vendor .ko with mismatched CRCs).
RDEPENDS:${PN} = "kmod util-linux-hexdump lxc"
RRECOMMENDS:${PN} = "rfkill"

FILES:${PN} = "${sbindir}/mtk-load-modules.sh ${bindir}/mtk-bt-address.sh"
