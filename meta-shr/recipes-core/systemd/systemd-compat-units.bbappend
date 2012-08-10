PRINC := "${@int(PRINC) + 10}"

SYSTEMD_DISABLED_SYSV_SERVICES = " \
  busybox-udhcpc \
  dnsmasq \
  hwclock \
  syslog \
  syslog.busybox \
  shr-splash \
  crond \
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
