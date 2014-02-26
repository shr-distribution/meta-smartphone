FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

volatiles = ""
dirs755 += "${localstatedir}/cache \
    ${localstatedir}/log \
    ${localstatedir}/lock \
    ${localstatedir}/lock/subsys \
    ${localstatedir}/tmp \
    ${localstatedir}/volatile/tmp \
    /run \
"
