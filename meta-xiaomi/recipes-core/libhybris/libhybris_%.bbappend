# we have android-headers-mido
COMPATIBLE_MACHINE_mido = "(^mido$)"

CFLAGS_append_mido = " -DQTI_BSP -DQCOM_HARDWARE"
CXXFLAGS_append_mido = " -DQTI_BSP -DQCOM_HARDWARE"
