DESCRIPTION = "SHR Feed"
PR = "r116"
PV = "1.0"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PACKAGE_ARCH = "${MACHINE_ARCH}"
inherit packagegroup

RPROVIDES_${PN} += "task-shr-feed"
RREPLACES_${PN} += "task-shr-feed"
RCONFLICTS_${PN} += "task-shr-feed"

MISSING = "\
        di \
        dictator \
        dosbox \
        enotes \
        libyaml-perl \
        pythm \
        python-wifi \
        openmoko-agpsui \
        pyphonelog \
        wireshark \
        pingus \
        openmoocow \
        python-pygame \
        obexpush \
        obexftp \
        mtpaint \
        fltk-chess \
        remoko \
        openbmap-logger \
        accelges \
        ebrainy \
        sms-sentry \
        cellhunter \
        dillo2 \
        pyefl-sudoku \
        python-pyid3lib \
        vpnc \
        mg \
        mcabber \
        callrec \
        kbdd \
        iotop \
        xev \
        zsh \
        microcom \
        joe \
        ntpclient \
        tor \
        vnc \
        wxwidgets \
        cacao \
        openjdk-6-jre \
        xournal \
        ruby \
        synergy \
        irssi \
        cu \
        debianutils \
        shr-installer \
        numptyphysics \
        libnet-dbus-perl \
        tcptraceroute \
        packagegroup-proper-tools \
        netkit-telnet \
        font-adobe-100dpi \
        font-adobe-75dpi \
        font-adobe-utopia-100dpi \
        font-adobe-utopia-75dpi \
        font-arabic-misc \
        font-bh-100dpi \
        font-bh-75dpi \
        font-bh-lucidatypewriter-100dpi \
        font-bh-lucidatypewriter-75dpi \
        font-bitstream-100dpi \
        font-bitstream-75dpi \
        font-bitstream-speedo \
        font-cronyx-cyrillic \
        font-cursor-misc \
        font-daewoo-misc \
        font-dec-misc \
        font-isas-misc \
        font-jis-misc \
        font-micro-misc \
        font-misc-cyrillic \
        font-misc-misc \
        font-mutt-misc \
        font-schumacher-misc \
        font-screen-cyrillic \
        font-sony-misc \
        font-sun-misc \
        font-winitzki-cyrillic \
        qwo \
        python-xlib \
        xcompmgr \
        ipython \
        phoneme-advanced-foundation \
        python-pybluez \
        pyring \
        bt-configure \
        bt-gps \
        bonnie++ \
        reiserfsprogs \
        blipomoko \
        xboard \
        sox \
        zile \
        podpooch \
        fltkclock \
        fltkhackdiet \
        fltkwwpointcal \
        transmission \
        fltkcocktailbar \
        fltkcurrency \
        fltkspacetrader \
        mokoeightball \
        wpa-gui \
        gabriel \
        stopwatch \
        qgpslog \
        litephone \
        linphone \
        unixbench \
        xfsprogs \
        xfsdump \
        supertux-qvga \
        wesnoth \
        mokosuite2 \
        mokowm-imf-ecore \
        mokohome \
        mokopanel \
        mokophone \
        galculator \
        xterm \
        inkspill \
        dns2tcp \
        sflphone-common \
        rtmom \
\
        gpe-calendar \
        gpe-todo \
        gpe-sketchbook \
        gpe-filemanager \
        gpe-gallery \
        gpe-timesheet \
        gpe-contacts \
        shr-theme-neo \
        mupdf \
        omnewrotate \
"

BROWSER = ""
# webkit-efl is currently broken
#BROWSER = "elbow"
#BROWSER_armv4 = ""
#BROWSER_armv5 = ""

BROKEN_BY_VALA_UPGRADE = "\
    iliwi \
    ffalarms \
    valacompass \
    sensmon \
"
# mplayer is currently blacklisted
# intone depends on blacklisted mplayer2
# emacs is currently blacklisted
# xchat is currently blacklisted
# abiword is currently blacklisted
# omgps is currently blacklisted
# sylpheed is currently blacklisted
# mcnavi is currently blacklisted
# gwaterpas is currently blacklisted
# aceofpenguins is currently blacklisted
# pisi, podboy, navit blacklisted because of bluez4 dependency
# elmdentica is blacklisted because of packagekit dependency
# fbreader is blacklisted because of gcc-6
# oh-puzzles replaced by puzzles
RDEPENDS_${PN} += "\
        ${BROWSER} \
        vagalume \
        bootchart \
        grep \
        chroneo \
        rfkill \
        estardict \
        orrery \
        gpm \
        links-x11 \
        gnuplot \
        php \
        mariadb \
        enjoy \
        babiloo-efl \
        gpe-scap \
        gtkmm \
        guitartune \
        mc \
        elfe \
        omhacks \
        python-elementary \
        openvpn \
        ffphonelog \
        shr-theme-o2 \
        shr-theme-niebiee \
        shr-theme-sixteen \
        shr-splash \
        shr-splash-theme-simple \
        shr-splash-theme-dontpanic \
        shr-splash-theme-handy \
        shr-splash-theme-tux \
        shr-splash-theme-logo \
        shr-splash-theme-openmoko \
        x11vnc \
        telepathy-python \
        opkg-utils \
        libnotify \
        dates \
        vim \
        gdb \
        tar \
        e-wm-illume-dict-pl \
        udev \
        ppp \
        kexec \
        mc \
        xprop \
        xwininfo \
        tcpdump \
        thone \
        lsof \
        gzip \
        zip \
        minicom \
        picocom \
        aspell \
        enchant \
        nano \
        ntp \
        x11vnc \
        dbus-x11 \
        git \
        net-tools \
        iproute2 \
        iputils \
        i2c-tools \
        psmisc \
        wmiconfig \
        bind-utils \
        ttf-droid-sans \
        ttf-droid-sans-mono \
        ttf-droid-sans-fallback \
        ttf-droid-sans-japanese \
        ttf-droid-serif \
        fso-apm \
        fsousaged \
        fsodeviced \
        fsonetworkd \
        mdbus \
        mickeyterm \
        opimd-utils \
        e-tasks \
        eject \
        illume-keyboard-arabic \
        illume-keyboard-browse \
        illume-keyboard-danish \
        illume-keyboard-default-alt \
        illume-keyboard-dutch \
        illume-keyboard-dvorak \
        illume-keyboard-finnish \
        illume-keyboard-french \
        illume-keyboard-german \
        illume-keyboard-hebrew \
        illume-keyboard-numeric-alt \
        illume-keyboard-default-numeric \
        illume-keyboard-russian \
        illume-keyboard-russian-terminal \
        man \
        man-pages \
        om-neon \
        x11perf \
        imagemagick \
        speex \
        xf86-input-tslib \
        xf86-video-fbdev \
        babiloo-efl \
        make \
        dnsmasq \
        neolight \
        spojegui \
        dbus-daemon-proxy \
        numberx \
        neomis \
        mokojeweled \
        atrack \
        minneo \
        ca-certificates \
        setxkbmap \
        shr-theme-efenniht \
        samba \
        bison \
        automake \
        linux-firmware \
        joe \
        openmoocow \
        mokomaze \
        mokoeightball \
"

# armv4 is not goog enough for firefox ASM optimalizations
EXTRA_MOZILLA_armv4 = ""
# firefox fails to build with gcc-6
# EXTRA_MOZILLA = "firefox"
EXTRA_MOZILLA = ""

RDEPENDS_${PN} += "${EXTRA_MOZILLA}"

# this is only usefull on gta02 and on other devices it's trying to pull mesa-dri (not respecting DEFAULT_PROVIDER for virtual/libgl)
#RDEPENDS_${PN}_append_om-gta02 = "glamo-dri-tests \
#"
