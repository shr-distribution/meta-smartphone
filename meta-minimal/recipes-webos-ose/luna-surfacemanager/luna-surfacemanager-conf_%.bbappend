# Ship the MP01's compositor environment. surface-manager.sh sources
# ${sysconfdir}/surface-manager.d/product.env when it exists; nothing else in
# the tree provides one, so this is additive.
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:mp01 = " file://mp01-product.env"

do_install:append:mp01() {
    install -d ${D}${sysconfdir}/surface-manager.d
    install -m 0644 ${UNPACKDIR}/mp01-product.env \
        ${D}${sysconfdir}/surface-manager.d/product.env
}
