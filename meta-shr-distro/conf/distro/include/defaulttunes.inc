# This is not included by distro config by default
# If you want more optimized feeds, dd this to local.conf:
# require meta-smartphone/meta-shr/conf/distro/include/defaulttunes.inc
# build image and reflash or at least rebuild opkg-config-base distro-feed-configs
# and opkg -force-reinstall opkg-config-base distro-feed-configs
# on target to get new /etc/opkg/arch.conf

DEFAULTTUNE_om-gta01 = "arm920t"
DEFAULTTUNE_om-gta02 = "arm920t"

DEFAULTTUNE_spitz    = "xscale"

DEFAULTTUNE_om-gta04 = "cortexa8t-neon"
DEFAULTTUNE_nokia900 = "cortexa8t-neon"
DEFAULTTUNE_crespo   = "cortexa8t-neon"
DEFAULTTUNE_tuna     = "cortexa8t-neon"

# or if you don't care about sharing binary feed between armv7a machines, 
# add this to local.conf after that require line
# DEFAULTTUNE_tuna = "cortexa9t-neon"

# For meta-shr-distro/recipes-core/meta/distro-feed-configs.bbappend:
# this is just to prevent opkg installing something from e.g. armv4t feed on armv7a,
# just because some upgrade was available in armv4t feed sooner and when it gets
# available for armv7a (same version) it's not reinstalled to get it from feed with higher priority
SUPPORTED_EXTRA_ARCHS_om-gta01 ?= "arm920tt"
SUPPORTED_EXTRA_ARCHS_om-gta02 ?= "arm920tt"

SUPPORTED_EXTRA_ARCHS_spitz    ?= "xscalete"

SUPPORTED_EXTRA_ARCHS_om-gta04 ?= "cortexa8t2-vfp-neon"
SUPPORTED_EXTRA_ARCHS_nokia900 ?= "cortexa8t2-vfp-neon"
SUPPORTED_EXTRA_ARCHS_crespo   ?= "cortexa8t2-vfp-neon"
SUPPORTED_EXTRA_ARCHS_tuna     ?= "cortexa8t2-vfp-neon"
