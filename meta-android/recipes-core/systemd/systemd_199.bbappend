# Android has it's firmware binaries in different directories so we're adding them as
# search paths for the systemd firmware loader too
EXTRA_OECONF += " \
    --with-firmware-path=/lib/firmware/updates:/lib/firmware:/system/etc/firmware:/etc/firmware:/vendor/firmware:/firmware/image"
