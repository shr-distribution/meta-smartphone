inherit linux-mainline-8953

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "mido"

SRC_URI += " \
    file://defconfig \
"
