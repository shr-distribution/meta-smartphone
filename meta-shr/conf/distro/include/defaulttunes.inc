# This is not included by distro config by default
# If you want more optimized feeds, dd this to local.conf:
# require meta-smartphone/meta-shr/conf/distro/include/defaulttunes.inc
# build image and reflash or at least rebuild opkg-config-base distro-feed-configs
# and opkg -force-reinstall opkg-config-base distro-feed-configs
# on target to get new /etc/opkg/arch.conf

DEFAULTTUNE_om-gta01 = "arm920t"
DEFAULTTUNE_om-gta02 = "arm920t"
DEFAULTTUNE_om-gta04 = "cortexa8t-neon"
DEFAULTTUNE_nokia900 = "cortexa8t-neon"
DEFAULTTUNE_crespo = "cortexa8t-neon"
DEFAULTTUNE_tuna = "cortexa8t-neon"

# or if you don't care about sharing binary feed between armv7a machines, 
# add this to local.conf after that require line
# DEFAULTTUNE_tuna = "cortexa9t-neon"
