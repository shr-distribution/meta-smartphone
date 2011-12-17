# until it's fixed properly in opkg
pkg_postinst_dbus_append() {
  chown messagebus:messagebus $D${localstatedir}/run/dbus $D${localstatedir}/lib/dbus
  chown root:messagebus $D${libexecdir}/dbus-daemon-launch-helper
  chmod 4754 $D${libexecdir}/dbus-daemon-launch-helper
}

PRINC := "${@int(PRINC) + 1}"
