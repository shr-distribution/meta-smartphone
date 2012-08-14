FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}"
PRINC := "${@int(PRINC) + 2}"

#for some reason the automatic MACHINE_ARCH based on paths didn't work.
PACKAGE_ARCH_crespo = "${MACHINE_ARCH}"
PACKAGE_ARCH_tuna = "${MACHINE_ARCH}"
