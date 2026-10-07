#!/bin/sh
# See mtk-audio-param-fixup.service for the full rationale. Idempotent; safe to
# re-run. Every instruction word is checked before anything is written, so a
# vendor image with a different libaudio_param_parser is left exactly as it is.
set -e

SRC=/android/vendor/lib64/libaudio_param_parser-vnd.so
DIR=/run/mtk-audio-param-fixup
DST=$DIR/libaudio_param_parser-vnd.so

# android-system mounts /android/vendor; wait for the library to show up.
i=0
while [ ! -e "$SRC" ] && [ "$i" -lt 30 ]; do
    i=$((i + 1))
    sleep 1
done
[ -e "$SRC" ] || { echo "mtk-audio-param-fixup: $SRC not found, nothing to do"; exit 0; }

# Already fixed up on an earlier run of this service.
if grep -q " $SRC " /proc/self/mountinfo; then
    echo "mtk-audio-param-fixup: $SRC is already bind-mounted, nothing to do"
    exit 0
fi

# Offsets equal virtual addresses in this library (single RX segment at 0).
# appGetXmlDirFromProperty() starts at 0x2e9a8. The words below are, in order:
#   0x2e9c0  mov  w0, #0x200
#   0x2e9c4  ldr  x8, [x21, #40]        (stack protector: load canary)
#   0x2e9c8  stur x8, [x29, #-8]        (stack protector: store canary)
#   0x2e9cc  bl   malloc@plt
#   0x2eb9c  b.ne __stack_chk_fail      (stack protector: check canary)
#
# Bytes are written as octal escapes because the shell printf here has no \x.
expect() {  # offset, 4 octal-escaped bytes
    printf "$2" > "$DIR/want"
    dd if="$SRC" bs=1 skip="$1" count=4 2>/dev/null > "$DIR/have"
    cmp -s "$DIR/want" "$DIR/have"
}

mkdir -p "$DIR"

if expect 190916 '\041\000\200\122' && expect 190924 '\005\063\000\224'; then
    echo "mtk-audio-param-fixup: library already carries the fix, nothing to do"
    exit 0
fi

if ! { expect 190912 '\000\100\200\122' &&
       expect 190916 '\250\026\100\371' &&
       expect 190920 '\250\203\037\370' &&
       expect 190924 '\125\061\000\224' &&
       expect 191388 '\041\010\000\124'; }; then
    echo "mtk-audio-param-fixup: $SRC is not the library this fix was written for, leaving it alone" >&2
    exit 0
fi

# Rebuild the library from the original with the four words replaced. This
# BusyBox dd has no conv=notrunc, so patching in place is not possible; every
# offset is 4-byte aligned, which lets the copy run in 4-byte blocks.
#   words 47729..47731 (offsets 190916..190924) and word 47847 (offset 191388)
{
    dd if="$SRC" bs=4 count=47729 2>/dev/null
    printf '\041\000\200\122'   # mov  w1, #1
    printf '\037\040\003\325'   # nop
    printf '\005\063\000\224'   # bl   calloc@plt  =>  calloc(512, 1)
    dd if="$SRC" bs=4 skip=47732 count=115 2>/dev/null
    printf '\037\040\003\325'   # nop (the canary is no longer stored here)
    dd if="$SRC" bs=4 skip=47848 2>/dev/null
} > "$DST"

if [ "$(wc -c < "$DST")" != "$(wc -c < "$SRC")" ]; then
    echo "mtk-audio-param-fixup: patched copy has the wrong size, not using it" >&2
    rm -f "$DST"
    exit 0
fi
chmod 0644 "$DST"

mount --bind "$DST" "$SRC"
echo "mtk-audio-param-fixup: appGetXmlDirFromProperty() now zero-initialises its buffer"
