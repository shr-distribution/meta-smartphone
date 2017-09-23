# we have android-headers-tenderloin
COMPATIBLE_MACHINE_tenderloin = "(^tenderloin$)"

CFLAGS_append_tenderloin = " -DQCOM_DIRECTTRACK -DQCOM_BSP"
CXXFLAGS_append_tenderloin = " -DQCOM_DIRECTTRACK -DQCOM_BSP"
