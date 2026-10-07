# module-droid-hidl spawns /usr/libexec/audiosystem-passthrough/audiosystem-passthrough
# as its helper process, but the recipe only has a build-time DEPENDS on it, so the
# image carried the module without its helper ("module-droid-hidl.c: helper
# disappeared" in the PulseAudio log). Scoped to sm-t220 rather than changing the
# shared recipe.
RDEPENDS:${PN}:append:sm-t220 = " audiosystem-passthrough"
