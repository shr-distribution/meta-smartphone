# lagos: keep the debug initramfs inside the 8 MiB init_boot partition.
#
# With INITRAMFS_DEBUG_SHELL=1 the image pulls in android-tools for adbd, and
# with it 7zip and android-libboringssl: 10,350,592 B of init_boot, against
# 8,388,608 B of partition. It brings nothing here. Since android-tools 35.0.2
# adbd is split into android-tools-adbd (so android-tools alone has no adbd),
# and lagos's machine.conf offers no "adb" gadget function anyway: its debug
# shell is telnet over ECM and a getty on ACM, both busybox.
IMAGE_INSTALL:remove:lagos = "android-tools"
