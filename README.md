# Hatena Bookmark native PR filter for Morphe

**Published experimental prerelease: [v0.2.0](https://github.com/rikunode/hatena-bookmark-hide-pr-patches/releases/tag/v0.2.0) — Hatena Bookmark 5.80 (versionCode 58000) only.** Public asset downloaded and SHA256 verified; GitHub Actions clean build PASS. A fresh Morphe Desktop invocation using the GitHub repo URL and its `--prerelease` flag downloaded v0.2.0 and applied both patches with zero failures. Actual Android Morphe Manager UI and phone feed still require user verification.

Morphe Manager patch source:

```text
https://github.com/rikunode/hatena-bookmark-hide-pr-patches
```

> **Experimental prerelease, not a verified ad-free application.** We confirmed Morphe APK patching, signature, bytecode and Android emulator installation/launch. We have **NOT confirmed live feed rendering, native ads disappearing in the actual UI, login, or bookmarking**. Both Android 36 emulator setups became unresponsive in the system UI while attempting screenshot/hierarchy capture on a constrained Windows host. Keep the original app installed and do not disable Play Protect.

## Included patches

- **Hide native PR entries from article feeds**: the app's normal filtering method `MuteHelperKt.filteredWithMuteWords(BookmarkEntry,List)` contains an unconditional "keep PR" branch; this Morphe bytecode patch checks `BookmarkEntry.isPrEntry()` first and returns `false` (exclude from list) for PR entries only. Non-PR items follow original mute logic. Does not merely hide the small ad label.
- **Clone Hatena Bookmark for safe side-by-side testing**: changes installed package to `com.hatena.android.bookmark.morphe` and rewrites one custom permission plus three provider authorities, allowing the original app to remain installed. Both patches default to enabled. Login and settings do **not** transfer to the clone.

**Compatibility:** original APK package `com.hatena.android.bookmark`, versionName `5.80`, versionCode `58000`. Source-side guard checks exact 5.80 mute-filter bytecode (8 registers, 105 code units, one original PR check at code-unit offset 21). Changed unknown builds fail closed.

## 5.80 verification

- Historical user-supplied input SHA256 `f5de27b6d0948d5716ef7dd24c0910c37e370c07db3888ff8aaa38a70afbd94b`, APK v2/v3 signature verified with subject `CN=Hatena Inc.`, Google SourceStamp verified. This demonstrates integrity against this signing key, **not comprehensive protection against malware** or independent proof of publisher key ownership.
- Unlike 5.84, no Pairip/Play license activity identified in 5.80.
- Morphe Desktop applied **2 patches, 0 failures**. Rebuilding and APK v2 signing passed. APK keeps the source's `classes2.dex` unchanged and adds a `classes3.dex` that Android SDK `dexdump` independently verified checks PR first, returns `false` and falls through to unchanged original logic for ordinary articles.
- An isolated Android 36 emulator installed the new clone and reached `MainActivity` without observed Pairip interception. Article rendering and actual PR removal remain **UNVERIFIED** due System UI ANR/device offline. A second emulator image repeated the System UI issue.
- In local release preflight, **23 tests passed**. GitHub Actions compiles its own Android bundle and runs regression tests, skipping local-only APK integration checks if the user APK is absent.

**Security warning:** the old 2024 APK came from a third-party archive. Its signature is valid but this is **not an assurance that the archive is malware-free**. The GitHub repository and releases **do not distribute any APK**, only patch source and compiled Morphe `.mpp`. Do not uninstall your working 5.84 to try this.

See [Japanese Morphe installation notes](docs/MORPHE_MANAGER_SETUP_JA.md), [5.80 security/test evidence](docs/EXPERIMENT_58000_SECURITY_AND_PORT.md), and [v0.2.0 prerelease notes](docs/RELEASE_NOTES_v0.2.0.md).

## History: v0.1.1 for 5.84 (known broken)

The previous v0.1.1 targeted Hatena Bookmark 5.84/versionCode58400 and passed APK patching tests, but **actual user device testing found Google Pairip license rejection** after modification. Installing original+clone together and claiming Play installer provenance did not repair it. Keep this release flagged prerelease for reproducibility only. **Do not use 5.84 v0.1.1 for day-to-day operation.**

## Development

Build: `./gradlew.bat :patches:generatePatchesList --offline` on Windows (or `bash ./gradlew :patches:generatePatchesList` on Linux).

Run metadata/tests: `python scripts/validate_morphe_release.py` and `python -m unittest discover -s tests -v`.

Morphe bundle: `patches/build/libs/patches-0.2.0.mpp`. Public source metadata: `patches-bundle.json`, `patches-list.json`. Submodules are pinned to reviewed Morphe patcher and Gradle plugin versions.
