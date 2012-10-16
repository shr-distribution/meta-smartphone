PRINC := "${@int(PRINC) + 3}"
do_configure_prepend() {
  grep "Enable=Socket" ${S}/audio/audio.conf || sed -i 's/#Disable/Enable=Socket\n#Disable/g' ${S}/audio/audio.conf
}
