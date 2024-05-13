require linux-mainline-8953.inc

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "mido"

SRC_URI += " \
    file://defconfig \
"
