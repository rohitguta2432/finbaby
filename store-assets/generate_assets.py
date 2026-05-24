"""Generate Play Store assets for Jama: app icon, feature graphic, phone screenshots."""
from PIL import Image, ImageDraw, ImageFont
import os

OUT = os.path.dirname(os.path.abspath(__file__))

# Palette from finbaby-logo.svg (Mindful Ledger design system)
TEAL_DARK = (13, 148, 136)    # #0D9488
TEAL_MID  = (20, 184, 166)    # #14B8A6
TEAL_LITE = (45, 212, 191)    # #2DD4BF
AMBER     = (251, 191, 36)    # #FBBF24
INK       = (15, 23, 42)      # #0F172A
CREAM     = (250, 248, 243)   # #FAF8F3
MUTED     = (109, 122, 119)   # #6D7A77

def load_font(size, bold=False):
    candidates_bold = [
        "/System/Library/Fonts/Helvetica.ttc",
        "/System/Library/Fonts/Avenir Next.ttc",
        "/Library/Fonts/Arial Bold.ttf",
    ]
    candidates_regular = [
        "/System/Library/Fonts/Helvetica.ttc",
        "/System/Library/Fonts/SFNS.ttf",
        "/Library/Fonts/Arial.ttf",
    ]
    for p in (candidates_bold if bold else candidates_regular):
        if os.path.exists(p):
            try:
                return ImageFont.truetype(p, size, index=1 if bold and p.endswith(".ttc") else 0)
            except Exception:
                pass
    return ImageFont.load_default()

def text_size(draw, text, font):
    bbox = draw.textbbox((0, 0), text, font=font)
    return bbox[2] - bbox[0], bbox[3] - bbox[1]

# ---------------- ICON (512x512) ----------------
def make_icon():
    img = Image.new("RGBA", (512, 512), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Squircle background
    d.rounded_rectangle((24, 24, 488, 488), radius=112, fill=TEAL_DARK)
    # Three stacked bars (mirroring the logo)
    bar_h = 56
    bars = [
        (96, 320, 416, 320 + bar_h, TEAL_LITE),
        (112, 232, 400, 232 + bar_h, TEAL_MID),
        (128, 144, 384, 144 + bar_h, (255, 255, 255)),
    ]
    for x0, y0, x1, y1, color in bars:
        d.rounded_rectangle((x0, y0, x1, y1), radius=bar_h // 2, fill=color)
    # Amber sparkle in upper-right
    cx, cy, r = 392, 120, 18
    pts = [
        (cx, cy - r), (cx + 6, cy - 6),
        (cx + r, cy), (cx + 6, cy + 6),
        (cx, cy + r), (cx - 6, cy + 6),
        (cx - r, cy), (cx - 6, cy - 6),
    ]
    d.polygon(pts, fill=AMBER)
    img.save(os.path.join(OUT, "icon-512.png"), "PNG")
    print("wrote icon-512.png")

# ---------------- FEATURE GRAPHIC (1024x500) ----------------
def make_feature_graphic():
    img = Image.new("RGB", (1024, 500), CREAM)
    d = ImageDraw.Draw(img)
    # Teal panel on left two-thirds with stepped bars
    d.rectangle((0, 0, 640, 500), fill=TEAL_DARK)
    # Diagonal accent
    d.polygon([(640, 0), (760, 0), (640, 500), (520, 500)], fill=TEAL_MID)
    d.polygon([(760, 0), (820, 0), (700, 500), (640, 500)], fill=TEAL_LITE)
    # Stacked bars icon (left)
    bar_h = 30
    bars = [
        (80, 320, 240, 320 + bar_h, TEAL_LITE),
        (90, 280, 250, 280 + bar_h, (255, 255, 255)),
        (100, 240, 260, 240 + bar_h, AMBER),
    ]
    for x0, y0, x1, y1, color in bars:
        d.rounded_rectangle((x0, y0, x1, y1), radius=bar_h // 2, fill=color)
    # Wordmark
    f_title  = load_font(96, bold=True)
    f_tag    = load_font(28, bold=True)
    d.text((80, 100), "Jama", fill=(255, 255, 255), font=f_title)
    d.text((84, 200), "Mindful expense tracking", fill=AMBER, font=f_tag)
    # Right-side feature callouts (on cream panel)
    f_feat = load_font(32, bold=True)
    f_sub  = load_font(20)
    features = [
        ("50/30/20 budgets", "Needs - Wants - Savings"),
        ("Smart tips",        "Personalised saving advice"),
        ("100% on-device",    "No accounts. No tracking."),
    ]
    y = 70
    for title, sub in features:
        d.text((860 - 320, y), title, fill=INK, font=f_feat)
        d.text((860 - 320, y + 42), sub, fill=MUTED, font=f_sub)
        y += 110
    img.save(os.path.join(OUT, "feature-1024x500.png"), "PNG")
    print("wrote feature-1024x500.png")

# ---------------- PHONE SCREENSHOTS (1080x1920) ----------------
def make_phone_screenshot(filename, headline, sub, mock_lines):
    img = Image.new("RGB", (1080, 1920), CREAM)
    d = ImageDraw.Draw(img)
    # Top banner
    d.rectangle((0, 0, 1080, 320), fill=TEAL_DARK)
    f_title = load_font(64, bold=True)
    f_sub   = load_font(36)
    d.text((80, 90), headline, fill=(255, 255, 255), font=f_title)
    d.text((80, 180), sub, fill=AMBER, font=f_sub)
    # Phone-frame mock area
    d.rounded_rectangle((100, 400, 980, 1720), radius=56, fill=(255, 255, 255), outline=(220, 220, 220), width=2)
    # Mock content lines
    y = 480
    f_h = load_font(46, bold=True)
    f_b = load_font(34)
    f_m = load_font(28)
    for i, (line_kind, text) in enumerate(mock_lines):
        if line_kind == "h":
            d.text((160, y), text, fill=INK, font=f_h)
            y += 80
        elif line_kind == "b":
            d.text((160, y), text, fill=MUTED, font=f_b)
            y += 60
        elif line_kind == "card":
            d.rounded_rectangle((140, y, 940, y + 130), radius=24, fill=(248, 250, 252))
            d.rounded_rectangle((160, y + 30, 230, y + 100), radius=18, fill=TEAL_LITE)
            d.text((260, y + 30), text[0], fill=INK, font=f_b)
            d.text((260, y + 75), text[1], fill=MUTED, font=f_m)
            d.text((940 - 250, y + 50), text[2], fill=TEAL_DARK, font=f_b)
            y += 160
    # Footer disclaimer
    d.text((80, 1830), "Placeholder screenshot - replace before public launch", fill=MUTED, font=load_font(20))
    img.save(os.path.join(OUT, filename), "PNG")
    print(f"wrote {filename}")

make_icon()
make_feature_graphic()
make_phone_screenshot(
    "screenshot-1-home.png",
    "Home",
    "Today's spend, at a glance",
    [
        ("h", "March"),
        ("b", "Spent so far: Rs. 24,580"),
        ("card", ("Coffee", "Cafe Coffee Day", "Rs. 280")),
        ("card", ("Metro card top-up", "BMRCL", "Rs. 500")),
        ("card", ("Groceries", "Big Basket", "Rs. 1,840")),
        ("card", ("Rent", "Monthly", "Rs. 18,000")),
    ],
)
make_phone_screenshot(
    "screenshot-2-budget.png",
    "Budget",
    "50 / 30 / 20 made simple",
    [
        ("h", "March budget"),
        ("b", "Salary: Rs. 60,000"),
        ("card", ("Needs (50%)", "Rs. 22,800 of 30,000", "76%")),
        ("card", ("Wants (30%)", "Rs. 9,400 of 18,000", "52%")),
        ("card", ("Savings (20%)", "Rs. 12,000 of 12,000", "100%")),
    ],
)
make_phone_screenshot(
    "screenshot-3-reports.png",
    "Reports",
    "See where your money goes",
    [
        ("h", "Top categories"),
        ("b", "This month"),
        ("card", ("Food & Dining", "32 transactions", "Rs. 8,420")),
        ("card", ("Transport", "18 transactions", "Rs. 3,240")),
        ("card", ("Groceries", "12 transactions", "Rs. 5,860")),
        ("card", ("Bills", "6 transactions", "Rs. 7,060")),
    ],
)
print("done")
