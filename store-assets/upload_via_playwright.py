"""
Upload Play Store listing graphics for Jama via Playwright.

Why this exists:
  Play Console's Angular `<simple-uploader>` component validates files
  through an event path that ignores DOM-level `input.files = ...`
  injection. The MCP-style `javascript_tool` can't drive it.
  Playwright's `set_input_files()` uses Chrome DevTools Protocol under
  the hood, which dispatches a trusted file-input event Angular accepts.

Usage:
  python3 upload_via_playwright.py
    # opens a persistent Chromium window with a saved profile in
    # ~/Library/Application Support/Google/Chrome/Playwright-Profile-Jama
    # - first run: you'll need to log into Google in that window
    # - subsequent runs: cookies persist, no login needed

  python3 upload_via_playwright.py --headless
    # only works after the profile has cookies; useful for re-runs

  python3 upload_via_playwright.py --dry
    # opens the page but skips actual uploads (for selector debugging)

Files uploaded (must exist):
  - ./icon-512.png             -> App icon slot
  - ./feature-1024x500.png     -> Feature graphic slot
  - ./screenshot-1-home.png    -> Phone screenshots slot
  - ./screenshot-2-budget.png  -> Phone screenshots slot
  - ./screenshot-3-reports.png -> Phone screenshots slot
"""
from __future__ import annotations

import argparse
import sys
from pathlib import Path

from playwright.sync_api import sync_playwright, Page, expect, TimeoutError as PWTimeout


HERE = Path(__file__).resolve().parent
ASSET_ICON       = HERE / "icon-512.png"
ASSET_FEATURE    = HERE / "feature-1024x500.png"
ASSET_SCREENS    = [
    HERE / "screenshot-1-home.png",
    HERE / "screenshot-2-budget.png",
    HERE / "screenshot-3-reports.png",
]

# Jama-specific. For other apps, change these.
LISTING_URL = (
    "https://play.google.com/console/u/0/developers/"
    "7974256350151127084/app/4972206024482393782/main-store-listing"
)
PROFILE_DIR = Path(
    "~/Library/Application Support/Google/Chrome/Playwright-Profile-Jama"
).expanduser()


def wait_for_save_toast(page: Page, timeout_ms: int = 15000) -> None:
    """Wait for Play's `Change saved` toast (or accept a graceful no-op)."""
    try:
        page.get_by_text("Change saved", exact=False).wait_for(timeout=timeout_ms)
    except PWTimeout:
        print("    (no save toast observed; continuing)")


def upload_to_slot(
    page: Page,
    slot_label: str,
    files: list[Path],
    add_button_index: int,
) -> None:
    """Open the asset side-panel for one slot and upload files into it.

    `add_button_index` is the position of the slot's "Add assets" button
    in document order: 0 = App icon, 1 = Feature graphic, 2 = Phone screenshots.
    """
    print(f"  uploading {len(files)} file(s) to '{slot_label}' (button #{add_button_index})")
    add_buttons = page.locator('button.add-button:has-text("Add assets")')
    add_buttons.nth(add_button_index).click()

    # The side panel mounts <simple-uploader> once visible
    uploader = page.locator("simple-uploader").first
    uploader.wait_for(state="attached", timeout=10000)
    file_input = uploader.locator('input[type="file"]')

    # Playwright fires a CDP-level file selection that Angular accepts
    file_input.set_input_files([str(f) for f in files])

    # Wait for the side panel to either auto-attach the asset (fast) or for
    # the asset library entry to appear
    page.wait_for_timeout(2500)

    # Close the side panel
    try:
        page.locator('[debug-id="collapse-button"]').first.click(timeout=3000)
    except PWTimeout:
        # Some flows auto-close; ignore
        pass
    page.wait_for_timeout(500)


def run(headless: bool, dry: bool) -> int:
    # Verify all files exist before launching a browser
    missing = [
        str(p) for p in [ASSET_ICON, ASSET_FEATURE, *ASSET_SCREENS] if not p.exists()
    ]
    if missing:
        print("ERROR: missing files:")
        for m in missing:
            print(f"  - {m}")
        print("Run generate_assets.py first.")
        return 1

    PROFILE_DIR.mkdir(parents=True, exist_ok=True)
    print(f"profile dir: {PROFILE_DIR}")

    with sync_playwright() as p:
        context = p.chromium.launch_persistent_context(
            user_data_dir=str(PROFILE_DIR),
            headless=headless,
            viewport={"width": 1500, "height": 900},
            # Mac-default-ish user agent so Play Console renders the
            # full UI rather than mobile fallback
            user_agent=(
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                "AppleWebKit/537.36 (KHTML, like Gecko) "
                "Chrome/131.0.0.0 Safari/537.36"
            ),
        )

        page = context.pages[0] if context.pages else context.new_page()
        print(f"navigating: {LISTING_URL}")
        page.goto(LISTING_URL, wait_until="domcontentloaded", timeout=60000)

        # If not yet logged in we'll bounce to accounts.google.com — pause for user.
        if "play.google.com/console" not in page.url:
            print(
                "\n  ===> Not logged in. Log into Google in the browser window,\n"
                "       then navigate back to the Store Listing page. Press Enter\n"
                "       here when you're ready.\n"
            )
            input("press Enter to continue...")
            page.goto(LISTING_URL, wait_until="domcontentloaded", timeout=60000)

        # The Graphics section is below the fold; scroll to it
        page.locator('text=Graphics').first.scroll_into_view_if_needed(timeout=10000)
        page.wait_for_timeout(1000)

        if dry:
            print("DRY RUN — page loaded, skipping uploads")
            input("press Enter to close browser...")
            context.close()
            return 0

        # The three slots, in document order
        upload_to_slot(page, "App icon",          [ASSET_ICON],     add_button_index=0)
        upload_to_slot(page, "Feature graphic",   [ASSET_FEATURE],  add_button_index=1)
        upload_to_slot(page, "Phone screenshots", ASSET_SCREENS,    add_button_index=2)

        # Click Save
        print("clicking Save...")
        save_btn = page.locator('button:has-text("Save")').last
        save_btn.click()
        wait_for_save_toast(page)

        print("done. closing browser in 5s (Ctrl-C to keep open)...")
        try:
            page.wait_for_timeout(5000)
        except KeyboardInterrupt:
            print("staying open; close browser window manually when done")
            input()
        context.close()
        return 0


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--headless", action="store_true")
    parser.add_argument("--dry", action="store_true", help="don't upload, just open the page")
    args = parser.parse_args()
    return run(headless=args.headless, dry=args.dry)


if __name__ == "__main__":
    sys.exit(main())
