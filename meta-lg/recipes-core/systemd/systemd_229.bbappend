FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_append_mako = " \
    file://0001-fd_fdinfo_mnt_id_disablefdinfostat.patch \
"
