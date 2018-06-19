# we have android-headers-mido
COMPATIBLE_MACHINE_mido = "(^mido$)"

CFLAGS_append_mido = " -DQTI_BSP -DQCOM_HARDWARE"
CXXFLAGS_append_mido = " -DQTI_BSP -DQCOM_HARDWARE"

# we have android-headers-tissot
COMPATIBLE_MACHINE_tissot = "(^tissot$)"

CFLAGS_append_tissot = " -DQTI_BSP -DQCOM_HARDWARE"
CXXFLAGS_append_tissot = " -DQTI_BSP -DQCOM_HARDWARE"
