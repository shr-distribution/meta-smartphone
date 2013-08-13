FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

PRINC := "${@int(PRINC) + 3}"

volatiles = ""
dirs755 += "${localstatedir}/cache \
    ${localstatedir}/run \
    ${localstatedir}/log \
    ${localstatedir}/lock \
    ${localstatedir}/lock/subsys \
    ${localstatedir}/tmp \
    ${localstatedir}/volatile/tmp \
    /run \
"
