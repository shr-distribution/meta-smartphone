FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
PRINC := "${@int(PRINC) + 1}"

COMPATIBLE_MACHINE_tuna = "tuna"
COMPATIBLE_MACHINE_crespo = "crespo"
