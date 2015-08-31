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

# just to prevent update-rc.d.bbclass pulling initscripts dependency for
# initscripts-functions we don't care about, see:
# http://lists.openembedded.org/pipermail/openembedded-core/2015-August/109735.html
PROVIDES += "initscripts"
