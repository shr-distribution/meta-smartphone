DESCRIPTION = "freesmartphone.org API glib wrapper"
LICENSE = "GPL-2.0"
LIC_FILES_CHKSUM = "file://COPYING;md5=801f80980d171dd6425610833a22dbe6"
SECTION = "devel"
DEPENDS = "dbus-glib"
SRCREV = "d71036545435ea2e72a48fb93fd1d38fc6d6c0a7"
PV = "0.0.1+gitr${SRCPV}"
PE = "1"

SRC_URI = "${FREESMARTPHONE_GIT}/libframeworkd-glib.git;protocol=git;branch=master"
S = "${WORKDIR}/git"

inherit autotools pkgconfig

# | ../i586-oe-linux-libtool  --tag=CC   --mode=compile i586-oe-linux-gcc  -m32 -march=i586 --sysroot=/home/jenkins/oe/world/shr-core/tmp-glibc/sysroots/qemux86 -DHAVE_CONFIG_H -I. -I../../git/src -I..  -DDATADIR=\"/usr/share\" -DPKGDATADIR=\"/usr/share/libframeworkd-glib\" -DG_LOG_DOMAIN=\"libframeworkd-glib\"  -Wall -Wextra -Werror -I/home/jenkins/oe/world/shr-core/tmp-glibc/sysroots/qemux86/usr/include/dbus-1.0 -I/home/jenkins/oe/world/shr-core/tmp-glibc/sysroots/qemux86/usr/lib/dbus-1.0/include -I/home/jenkins/oe/world/shr-core/tmp-glibc/sysroots/qemux86/usr/include/glib-2.0 -I/home/jenkins/oe/world/shr-core/tmp-glibc/sysroots/qemux86/usr/lib/glib-2.0/include -O2 -pipe -g -feliminate-unused-debug-types -fdebug-prefix-map=/home/jenkins/oe/world/shr-core/tmp-glibc/work/i586-oe-linux/libframeworkd-glib/1_0.0.1+gitrAUTOINC+d710365454-r0=/usr/src/debug/libframeworkd-glib/1_0.0.1+gitrAUTOINC+d710365454-r0 -fdebug-prefix-map=/home/jenkins/oe/world/shr-core/tmp-glibc/sysroots/x86_64-linux= -fdebug-prefix-map=/home/jenkins/oe/world/shr-core/tmp-glibc/sysroots/qemux86=  -c -o frameworkd-glib-opimd-sources.lo `test -f 'opimd/frameworkd-glib-opimd-sources.c' || echo '../../git/src/'`opimd/frameworkd-glib-opimd-sources.c
# | In file included from ../../git/src/ogsmd/frameworkd-glib-ogsmd-call.c:24:0:
# | ../../git/src/ogsmd/dbus/call.h:425:1: error: 'org_freesmartphone_GSM_Call_send_dtmf' defined but not used [-Werror=unused-function]
PNBLACKLIST[libframeworkd-glib] ?= "BROKEN: many errors when building with gcc-6 - the recipe will be removed on 2017-09-01 unless the issue is fixed"
