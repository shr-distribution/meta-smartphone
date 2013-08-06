PRINC := "${@int(PRINC) + 11}"

SYSTEMD_DISABLED_SYSV_SERVICES = " \
  busybox-udhcpc \
  dnsmasq \
  hwclock \
  syslog \
  syslog.busybox \
  shr-splash \
  keymap \
  fbsetup \
  populate-volatile \
  mountall \
  bootmisc \
  mountnfs \
  checkroot \
  modutils \
  fso-gpsd-sysv \
  gpsd-sysv \
"
