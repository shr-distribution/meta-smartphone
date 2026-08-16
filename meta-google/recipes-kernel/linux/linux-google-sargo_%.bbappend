# scripts/unifdef.c declares "static bool constexpr;" and assigns to it.
# C23 made constexpr a keyword, and this host GCC (15.x on Ubuntu 26.04) defaults
# to -std=gnu23, so building the kernel host tools fails:
#
#   scripts/unifdef.c:206:1: error: 'constexpr' in empty declaration
#   scripts/unifdef.c:880:27: error: expected identifier or '(' before '=' token
#
# Upstream renamed the variable in 6.7; this kernel predates that. Only the host
# tools are affected - the kernel itself is built with OE's cross toolchain -
# and kernel.bbclass passes HOSTCFLAGS="${BUILD_CFLAGS}", so pinning the host
# C standard here is enough.
#
# Same fix as linux-xiaomi-tissot-halium: both are 4.9 trees from
# shr-distribution/linux, so both hit this the moment the host compiler moved on.
BUILD_CFLAGS:append = " -std=gnu17"
