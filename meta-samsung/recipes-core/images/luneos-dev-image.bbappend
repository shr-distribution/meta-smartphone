# SM-T520 bring-up: keep the tablet alive when a Samsung vendor driver oopses.
#
# The bootloader puts oops=panic on the kernel command line and the header cmdline is
# ignored, so a driver bug reached from user space (the camera ISP and the MFC codec
# were both opened by GStreamer's plugin scanner) used to reset the tablet every time.
# Turn the panics off at run time. Development image only: a release image wants them.
sm_t520_dev_nopanic() {
    install -d ${IMAGE_ROOTFS}${sysconfdir}/sysctl.d
    cat > ${IMAGE_ROOTFS}${sysconfdir}/sysctl.d/90-sm-t520-nopanic.conf <<'SYSCTL'
# SM-T520 development image: an oops in a vendor driver must not reboot the tablet
kernel.panic_on_oops = 0
kernel.panic = 0
kernel.hung_task_panic = 0
SYSCTL
}

ROOTFS_POSTPROCESS_COMMAND:append:sm-t520 = " sm_t520_dev_nopanic;"
