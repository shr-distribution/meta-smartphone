PRINC := "${@int(PRINC) + 3}"

pkg_preinst_${PN} () {
  # work-arround to switch from sysvinit to systemd ie on n900 where we were using
  # ROOTFS_POSTPROCESS_COMMAND += " remove_init_link; "
  if [ -e ${base_sbindir}/init -a ! -h ${base_sbindir}/init ] ; then
    rm -rf ${base_sbindir}/init
  fi
}
