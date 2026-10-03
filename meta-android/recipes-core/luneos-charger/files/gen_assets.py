#!/usr/bin/env python3
"""Regenerate luneos-charger's artwork: logo.png, text.png and text.idx.

Not run at build time - meta-android depends on nothing that ships Pillow -
so the outputs are committed next to this script. Rerun it after changing the
logo, the font or the strings, and commit all three files.

  logo.png   the LuneOS logo, RGBA, at the size it is drawn
  text.png   every string and digit the charger draws, as 8-bit coverage,
             stacked into one atlas; the charger tints and blends them
  text.idx   one line per atlas entry: name x y w h

Digits and '%' are rendered in cells of the font's full ascent + descent and
their advance width, so they line up on a common baseline when laid side by
side.

Usage: gen_assets.py <logo.png> <font.ttf> <outdir>

The committed outputs come from the LuneOS logo and Noto Sans Regular
(SIL Open Font License 1.1).
"""
import os
import sys

from PIL import Image, ImageDraw, ImageFont

LOGO_SIZE = 300
DIGIT_PT = 150
LABEL_PT = 56
HINT_PT = 40

STRINGS = [
    ('charging', 'Charging', LABEL_PT),
    ('charged', 'Charged', LABEL_PT),
    ('hint', 'Hold the power key to start LuneOS', HINT_PT),
]


def text_cov(text, font):
    left, top, right, bottom = font.getbbox(text)
    im = Image.new('L', (right - left + 4, bottom - top + 4), 0)
    ImageDraw.Draw(im).text((2 - left, 2 - top), text, font=font, fill=255)
    return im


def main():
    logo_path, font_path, outdir = sys.argv[1:4]

    Image.open(logo_path).convert('RGBA').resize(
        (LOGO_SIZE, LOGO_SIZE), Image.LANCZOS).save(
        os.path.join(outdir, 'logo.png'), optimize=True)

    entries = []
    font = ImageFont.truetype(font_path, DIGIT_PT)
    ascent, descent = font.getmetrics()
    for ch, name in [(c, c) for c in '0123456789'] + [('%', 'pct')]:
        im = Image.new('L', (int(round(font.getlength(ch))), ascent + descent), 0)
        ImageDraw.Draw(im).text((0, 0), ch, font=font, fill=255)
        entries.append(('glyph_' + name, im))
    for name, text, pt in STRINGS:
        entries.append((name, text_cov(text, ImageFont.truetype(font_path, pt))))

    width = max(im.width for _, im in entries)
    atlas = Image.new('L', (width, sum(im.height for _, im in entries)), 0)
    lines, y = [], 0
    for name, im in entries:
        atlas.paste(im, (0, y))
        lines.append('%s 0 %d %d %d' % (name, y, im.width, im.height))
        y += im.height
    atlas.save(os.path.join(outdir, 'text.png'), optimize=True)
    with open(os.path.join(outdir, 'text.idx'), 'w') as f:
        f.write('\n'.join(lines) + '\n')


if __name__ == '__main__':
    main()
