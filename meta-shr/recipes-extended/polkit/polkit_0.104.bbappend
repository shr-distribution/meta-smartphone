PRINC := "${@int(PRINC) + 1}"
# Until we switch to systemd globally we cannot allow this package to pull systemd package to image!
EXTRA_OECONF += " --disable-systemd"
