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

# Pin the module signing key rather than letting the kernel's own certs/
# machinery generate a fresh one on every build.
#
# CONFIG_MODULE_SIG_FORCE=y is on for this device. Out-of-tree kernel modules
# built with module.bbclass are otherwise a well-trodden, low-risk path here -
# this is the one piece that trips that up. With no pinned key, every
# from-scratch kernel build generates a brand new self-signed key and bakes
# its public half in as the kernel's own trusted key - so a module built and
# signed against *this* build's STAGING_KERNEL_BUILDDIR (shared across
# recipes that depend on virtual/kernel, e.g. wireguard-module) will only
# ever load on a kernel that was built in that exact same session. Rebuild
# the kernel later, even with no source change at all, and it's "Required
# key not available" the moment you try to load any out-of-tree module
# again.
#
# certs/Makefile only auto-(re)generates the key when CONFIG_MODULE_SIG_KEY
# is exactly the literal string "certs/signing_key.pem" - that path's rule
# depends on certs/x509.genkey, which is rewritten with a fresh mtime on
# every single build, so a key merely *copied* to that exact path before
# do_compile still gets regenerated (confirmed: still a fresh key after
# adding a do_compile:prepend copy there). Pointing CONFIG_MODULE_SIG_KEY at
# a different filename entirely sidesteps that rule altogether - kbuild then
# just uses whatever is there, no regeneration logic in play at all.
#
# Committing one fixed key here makes both sides of that relationship
# (kernel's trusted key, module's signature) deterministic across rebuilds -
# necessary for this to actually be reusable for other pre-5.6 devices
# (tissot, ...) built later, not just a one-off fix for tonight's kernel
# build.
SRC_URI += "file://module-signing/luneos-module-signing-key.pem"

do_configure:append() {
    sed -i '/CONFIG_MODULE_SIG_KEY/d' ${B}/.config
    echo 'CONFIG_MODULE_SIG_KEY="certs/luneos-module-signing-key.pem"' >> ${B}/.config
}

do_compile:prepend() {
    mkdir -p ${B}/certs
    cp ${UNPACKDIR}/module-signing/luneos-module-signing-key.pem ${B}/certs/luneos-module-signing-key.pem
}
