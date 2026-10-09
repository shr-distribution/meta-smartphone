# goyavewifi: Waydroid needs a kernel of 3.18 or newer (binder, ashmem and the namespaces its LineageOS
# container uses) and this is a 3.10 kernel; the checksums of waydroid-data are the arm64 builds too, so its
# fetch would not work on a 32-bit machine. The ":halium" line of the packagegroup would otherwise pull it in.
# The same as the Halium machines on 3.4 kernels (hammerhead, mako, tenderloin, sm-t520).
RDEPENDS:${PN}:remove:goyavewifi = "waydroid waydroid-sensors"
