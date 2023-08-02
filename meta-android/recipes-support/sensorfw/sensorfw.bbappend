FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"

# Devices specific configuration and options for sensorfw go here

do_install:append:halium() {
    install -d ${D}${sysconfdir}/sensorfw/
    install -m 0644 ${S}/config/sensord-hybris.conf ${D}${sysconfdir}/sensorfw/
}

EXTRA_QMAKEVARS_PRE:append:halium = "CONFIG+=autohybris "

# Halium-9.0 devices use binder to communicate with sensors
EXTRA_QMAKEVARS_PRE:append:hammerhead-halium = "CONFIG+=binder "
EXTRA_QMAKEVARS_PRE:append:mako = "CONFIG+=binder "
EXTRA_QMAKEVARS_PRE:append:mido = "CONFIG+=binder "
EXTRA_QMAKEVARS_PRE:append:rosy = "CONFIG+=binder "
EXTRA_QMAKEVARS_PRE:append:sagit = "CONFIG+=binder "
EXTRA_QMAKEVARS_PRE:append:tissot = "CONFIG+=binder "
EXTRA_QMAKEVARS_PRE:append:yggdrasil = "CONFIG+=binder "

# Tenderloin here is an exception: sensorfw doesn't need to use Halium for the sensor
EXTRA_QMAKEVARS_PRE:remove:tenderloin = "CONFIG+=autohybris "

DEPENDS:append:halium = " libhybris virtual/android-headers libgbinder libglibutil "

SRC_URI:append:tenderloin = " \
    file://sensord-tenderloin.conf \
"

SRC_URI:append:hammerhead = " \
    file://sensord-hammerhead.conf \
"
