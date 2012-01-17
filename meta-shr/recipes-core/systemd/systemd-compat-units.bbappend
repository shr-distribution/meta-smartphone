PRINC := "${@int(PRINC) + 5}"

pkg_postinst_${PN} () {
cd $D${sysconfdir}/init.d

echo -n "Disabling the following sysv scripts: "

for i in busybox-udhcpc dnsmasq hwclock.sh syslog syslog.busybox shr-splash crond keymap fbsetup; do
	if [ -e $i -a ! -e $D${base_libdir}/systemd/system/$i.service ] ; then
		echo -n "$i " ; ln -s /dev/null $D${base_libdir}/systemd/system/$i.service
	fi
done ; echo
}
