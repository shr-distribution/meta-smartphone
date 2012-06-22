PRINC := "${@int(PRINC) + 2}"
# enable alsa here even without alsa in DISTRO_FEATURES
PACKAGECONFIG += "alsa"
do_configure_prepend() {
  grep "Enable=Socket" ${S}/audio/audio.conf || sed -i 's/#Disable/Enable=Socket\n#Disable/g' ${S}/audio/audio.conf
}
