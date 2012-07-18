DESCRIPTION = "SHR Lite Image Feed"
PR = "r53"
PV = "2.0"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit task

PACKAGE_ARCH = "${MACHINE_ARCH}"

def get_rdepends(bb, d):
    enabled = bb.data.getVar("ENABLE_BINARY_LOCALE_GENERATION", d, 1)

    # If locale is disabled, bail out
    if not enabled or not int(enabled):
        return ""

    locales = bb.data.getVar("GLIBC_GENERATE_LOCALES", d, 1)
    if not locales or locales == "all":
        locales = bb.data.getVar("IMAGE_LINGUAS", d, 1);

    libc = bb.data.getVar('TCLIBC', d, 1)
    import re

    rdepends = ""
    if not locales or locales == "all":
        # if locales aren't specified, or user has written "all"
        import os
        ipkdir = bb.data.getVar('DEPLOY_DIR_IPK', d, 1)

        regexp1 = re.compile(libc+"-binary-localedata-.*") # search pattern
        regexp2 = re.compile("_.*") # we want to remove all version info and file extension

        for root, subFolders, files in os.walk(ipkdir):
            for file in files:
                if regexp1.search(file):
                    file = regexp2.sub("", file)
                    rdepends = "%s %s" % (rdepends, file)

    else:
        # if locales are specified
        regexp1 = re.compile("\\..*") # We want to turn en_US.UTF-8 into en_US
        regexp2 = re.compile("_")     # We want to turn en_US into en-US


        for locale in locales.split(" "):
            locale = regexp1.sub("", locale)
            locale = regexp2.sub("-", locale)
            locale = str.lower(locale)
            rdepends = "%s %s-binary-localedata-%s" % (rdepends, libc, locale)
    return rdepends




PACKAGES += "\
	${PN}-base \
	${PN}-cli \
	${PN}-fso \
	${PN}-audio \
	${PN}-x \
	${PN}-apps \
	${PN}-gtk \
"



RDEPENDS_${PN}-base = "\
  ${MACHINE_TASK_PROVIDER} \
  task-base \
  task-boot \
  glibc-utils \
  glibc-charmap-utf-8 \
  netbase \
  sysfsutils \
  modutils-initscripts \
  module-init-tools-depmod \
  fbset \
  fbset-modes \
  cronie \
  logrotate\
  util-linux-fdisk \
  shr-splash \
  task-shr-systemd \
"

RDEPENDS_${PN}-cli = "\
  nano \
  mtd-utils \
"

RDEPENDS_${PN}-fso = "\
  fsoraw \
  opimd-utils-cli \
  python-codecs \
  python-gst \
"


#FIXME: libcanberra-alsa should be pulled in by fsodeviced but isn't
#  libcanberra-alsa

RDEPENDS_${PN}-audio = "\
  alsa-utils-alsactl \
  alsa-utils-alsamixer \
  alsa-utils-aplay \
  alsa-utils-amixer \
"

RDEPENDS_${PN}-x = "\
  task-x11-illume \
  task-fonts-truetype-core \
  shr-wizard \
  shr-theme-gry \
  xcursor-transparent-theme \
  xinput-calibrator \
  libx11-locale \
  ${@get_rdepends(bb, d)} \
"

RDEPENDS_${PN}-apps = "\
  task-fso2-compliance \
  phoneui-apps-messages \
  phoneui-apps-contacts \
  phoneui-apps-dialer \
  phoneui-apps-quick-settings \
  phonefsod \
  phoneuid \
  libphone-ui \
  libphone-ui-shr \
  ffalarms \
  shr-settings \
  shr-theme \
  ecalc \
  iliwi \
  ca-certificates \
"

# hicolor-icon-theme to make gdk-pixbuf-loader postinst happy

RDEPENDS_${PN}-gtk = "\
  gtk-theme-e17lookalike \
  hicolor-icon-theme \
  vala-terminal \
  ffphonelog \
  matchbox-keyboard-im \
  gtk-immodule-xim \
"

