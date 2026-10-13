# Hatena Bookmark — native PR article filter for Morphe

**Current experimental fix: v0.2.1** — compatible with Hatena Bookmark **5.80 / versionCode 58000 only**.

Morphe Manager source:

```text
https://github.com/rikunode/hatena-bookmark-hide-pr-patches
```

## Why the fix was necessary

An actual Android user confirmed v0.2.0 could install, launch and load article feeds, but the **`総合` feed still showed an article labeled `広告`**. The old patch hooked only `MuteHelperKt.filteredWithMuteWords()`, which is never called if the user is logged out or has zero mute words.

**v0.2.1** additionally filters `CategoryHotEntryPagingSource.withDate(List<BookmarkEntry>, ZonedDateTime)` before it converts hot-feed entries into date-tagged list items. This is invoked on the paged hot-entry path irrespective of mute configuration. It calls the original `filterByMuteWords(entries, emptyList(), null)` with the patched PR-flag guard, keeping normal entries and removing `isPrEntry()` entries. The original mute-matching logic continues to run when the user has configured rules.

### Two included Morphe patches

- **Hide native PR entries from article feeds** — native PR entries excluded at the model/list level, not just the visible advertisement label. V0.2.1 includes the unconditional hot-feed fix.
- **Clone Hatena Bookmark for safe side-by-side testing** — changes package to `com.hatena.android.bookmark.morphe`, one custom receiver permission and three ContentProvider authorities so the working original 5.84 can stay installed. Both patches enabled by default.

The code checks fixed 5.80 bytecode fingerprints (the original mute method and hot-feed `withDate`) and refuses unknown layouts.

## Verified v0.2.1

The source 5.80 APK has SHA256 `f5de27b6d0948d5716ef7dd24c0910c37e370c07db3888ff8aaa38a70afbd94b` and a valid v2/v3 signature under a certificate labeled `CN=Hatena Inc.`. This does not guarantee the third-party archive is malware-free.

Morphe Desktop applied **2/2 patches, 0 failures**, rebuilt and signed an output APK. Android SDK `apksigner` verified the v2 signature; `dexdump` confirmed the unconditional `withDate` filter call and the early `isPrEntry()` guard in the executable output DEX; the original `classes2.dex` remained identical. v0.2.1 specific tests verify these properties.

**Still not verified: real-device removal of the previously visible `広告` entry with v0.2.1, other tabs, login and bookmarking.** The user is expected to apply the update and visually verify the `総合` feed. Do not claim full ad-free operation without that screenshot.

## Risks and earlier versions

- Historical 5.80 APK is dated 2024 and originates from third-party APK archives. Same hashes across APKPure and Uptodown and valid APK signatures help establish identity but do not establish absence of malware or old vulnerabilities.
- Public GitHub source distributes **only Morphe patches, no APK files or credentials**. Never uninstall your working 5.84 merely to test the clone, and never disable Play Protect.
- Previous **v0.2.0** for 5.80: patch applied and app rendered live feed, but PR ad remained due missing unconditional hot-feed filter.
- Earlier **v0.1.1** for 5.84: modifying APK triggered a Pairip Play license failure on actual phone; still marked as known-broken prerelease.

Documentation: [Morphe Manager guide (JA)](docs/MORPHE_MANAGER_SETUP_JA.md), [v0.2.1 release notes](docs/RELEASE_NOTES_v0.2.1.md), [5.80 safety/integration evidence](docs/EXPERIMENT_58000_SECURITY_AND_PORT.md).

## Developer verification

```bash
./gradlew.bat :patches:generatePatchesList --offline
python scripts/validate_morphe_release.py
python -m unittest discover -s tests -v
```

Output `patches/build/libs/patches-0.2.1.mpp`. Source metadata `patches-bundle.json`, `patches-list.json`. GitHub Actions compiles Android DEX and checks both on clean checkout; local actual-APK regression tests are skipped there because the user's APK is not distributed.
