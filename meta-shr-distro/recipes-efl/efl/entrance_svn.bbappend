FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_append_shr = " file://entrance.conf"

# replace default entrance.conf with autologin version
do_install_append_shr() {
        cp -R --no-dereference --preserve=mode,links -v ${WORKDIR}/entrance.conf ${D}/${sysconfdir}/entrance.conf
}
