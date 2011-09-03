DESCRIPTION = "Linux Kernel"
SECTION = "kernel"
LICENSE = "GPLv2"

LIC_FILES_CHKSUM = "file://COPYING;md5=d7810fab7487fb0aad327b76f1be7cd7"

inherit kernel siteinfo

# Quirk for udev greater or equal 141
UDEV_GE_141 ?= "0"

# Set the verbosity of kernel messages during runtime
# You can define CMDLINE_DEBUG in your local.conf or distro.conf to override this behaviour
CMDLINE_DEBUG ?= '${@base_conditional("DISTRO_TYPE", "release", "quiet", "debug", d)}'

do_configure_prepend() {
  # Rename getline in ./scripts/unifdef.c
  # Kernels up to 2.6.29 are currently failing to build unifdef.c,
  # clashing with exposed getline() from <stdio.h>
  # see https://patchwork.kernel.org/patch/11166/
  # committed in 2.6.29 (commit d15bd1067b1fcb2b7250d22bc0c7c7fea0b759f7)

  if [ -e ${S}/scripts/unifdef.c ]; then
    sed -i -e 's/getline/parseline/g' ${S}/scripts/unifdef.c
  fi

  echo "" > ${S}/.config

  # When enabling thumb for userspace we also need thumb support in the kernel
  if [ "${ARM_INSTRUCTION_SET}" = "thumb" ] ; then
    sed -i -e /CONFIG_ARM_THUMB/d ${WORKDIR}/defconfig 
    echo "CONFIG_ARM_THUMB=y" >> ${S}/.config
  fi

  #
  # endian support
  #
  if [ "${SITEINFO_ENDIANNESS}" = "be" ]; then
    echo "CONFIG_CPU_BIG_ENDIAN=y" >> ${S}/.config
  fi

  echo "CONFIG_CMDLINE=\"${CMDLINE} ${CMDLINE_DEBUG}\"" >> ${S}/.config

  cat '${WORKDIR}/defconfig' >>'${S}/.config'

  #
  # Udev quirks
  #

  # Newer versions of udev mandate that sysfs doesn't have deprecated entries
  if [ "${UDEV_GE_141}" = "1" ] ; then
    sed -e /CONFIG_SYSFS_DEPRECATED/d \
        -e /CONFIG_SYSFS_DEPRECATED_V2/d \
        -e /CONFIG_HOTPLUG/d \
        -e /CONFIG_UEVENT_HELPER_PATH/d \
        -e /CONFIG_UNIX/d \
        -e /CONFIG_SYSFS/d \
        -e /CONFIG_PROC_FS/d \
        -e /CONFIG_TMPFS/d \
        -e /CONFIG_INOTIFY_USER/d \
        -e /CONFIG_SIGNALFD/d \
        -e /CONFIG_TMPFS_POSIX_ACL/d \
        -e /CONFIG_BLK_DEV_BSG/d \
        -i '${S}/.config'

    echo '# CONFIG_SYSFS_DEPRECATED is not set' >> ${S}/.config
    echo '# CONFIG_SYSFS_DEPRECATED_V2 is not set' >> ${S}/.config
    echo 'CONFIG_HOTPLUG=y' >> ${S}/.config
    echo 'CONFIG_UEVENT_HELPER_PATH=""' >> ${S}/.config
    echo 'CONFIG_UNIX=y' >> ${S}/.config
    echo 'CONFIG_SYSFS=y' >> ${S}/.config
    echo 'CONFIG_PROC_FS=y' >> ${S}/.config
    echo 'CONFIG_TMPFS=y' >> ${S}/.config
    echo 'CONFIG_INOTIFY_USER=y' >> ${S}/.config
    echo 'CONFIG_SIGNALFD=y' >> ${S}/.config
    echo 'CONFIG_TMPFS_POSIX_ACL=y' >> ${S}/.config
    echo 'CONFIG_BLK_DEV_BSG=y' >> ${S}/.config
    echo 'CONFIG_DEVTMPFS=y' >> ${S}/.config
    echo 'CONFIG_DEVTMPFS_MOUNT=y' >> ${S}/.config
  fi

  # Newer inits like systemd need cgroup support
  if [ "${KERNEL_ENABLE_CGROUPS}" = "1" ] ; then
    sed -e /CONFIG_CGROUP_SCHED/d \
        -e /CONFIG_CGROUPS/d \
        -i '${S}/.config'

    echo 'CONFIG_CGROUP_SCHED=y' >> ${S}/.config
    echo 'CONFIG_CGROUPS=y' >> ${S}/.config
    echo 'CONFIG_CGROUP_NS=y' >> ${S}/.config
    echo 'CONFIG_CGROUP_FREEZER=y' >> ${S}/.config
    echo 'CONFIG_CGROUP_DEVICE=y' >> ${S}/.config
    echo 'CONFIG_CPUSETS=y' >> ${S}/.config
    echo 'CONFIG_PROC_PID_CPUSET=y' >> ${S}/.config
    echo 'CONFIG_CGROUP_CPUACCT=y' >> ${S}/.config
    echo 'CONFIG_RESOURCE_COUNTERS=y' >> ${S}/.config
  fi

  # Let build system check the new generated config file
  yes '' | oe_runmake oldconfig
}

do_configure_append() {
  if test -e scripts/Makefile.fwinst ; then
    sed -i -e "s:-m0644:-m 0644:g" scripts/Makefile.fwinst
  fi
}

do_install_append() {
  oe_runmake headers_install INSTALL_HDR_PATH=${D}${exec_prefix}/src/linux-${KERNEL_VERSION} ARCH=$ARCH
}

PACKAGES =+ "kernel-headers"
FILES_kernel-headers = "${exec_prefix}/src/linux*"
