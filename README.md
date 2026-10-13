# Hatena Bookmark 5.84 native PR-entry filter — Morphe

**Morphe Manager source (public):** `https://github.com/rikunode/hatena-bookmark-hide-pr-patches` — [v0.1.1 release](https://github.com/rikunode/hatena-bookmark-hide-pr-patches/releases/tag/v0.1.1). Add this repository URL in Morphe Manager. See [Japanese setup guide](docs/MORPHE_MANAGER_SETUP_JA.md).

**Published-release preflight:** `python scripts/validate_morphe_release.py` checks Android `classes.dex`, offset-free Kotlin LocalDateTime in `patches-bundle.json`, exact public Release URL, two expected patches and Hatena Bookmark 5.84/versionCode58400. CI workflow runs the same tests with pinned Morphe patcher/plugin. No secrets, raw user APK or generated modified APK should be committed.


**Status: v0.1.1 compiled with both PR-filter and separate-install clone patches; applied successfully to exact Hatena 5.84 APK and installed/launched in read-only Android 36 emulator. Runtime reached MainActivity but then Pairip LicenseActivity took focus, so app usability and ad removal are NOT verified. Existing phone app and data untouched. GitHub public source and v0.1.1 patch bundle released; device ad removal still unverified.**

## What the patch does

The official Hatena Bookmark 5.84 (versionCode 58400; package `com.hatena.android.bookmark`) app has a native PR-ad flag `BookmarkEntry.isPrEntry()`. In its existing `MuteHelperKt.filteredWithMuteWords(BookmarkEntry,List)` method, PR items return `true` immediately and therefore bypass normal mute filtering.

This patch uses Morphe bytecode transformation to insert an early check: PR items return `false` (excluded from list); non-PR items execute the original method exactly as before. It does **not** merely erase the visible AD badge, nor block arbitrary network domains. The patch fails closed if the expected 5.84 bytecode changes.

The original attached Antisplit APK hash was inspected without credentials or personal data:

- APK SHA-256: `0e2c6727e92a8a4ecbb05a4fdcd9059d80c11552ff6f4155d94c874247384684`
- Package `com.hatena.android.bookmark` / versionName `5.84` / versionCode `58400`
- Methods: `MuteHelperKt.filteredWithMuteWords(...)` is 103 DEX code units, 8 registers, PR check at offset 21.
- Actual patch eligibility: only 5.84/58400.

## Known scope/limitations

- Call sites confirmed in `GetCategoryHotEntryList`, `CategoryHotEntryPagingSource`, and `CategoryNewEntryPagingSource`. Other lists such as some personalized views may not run through this method.
- Screenshot shows an inline PR article in the `総合` tab. The relevant popular/new-article path is confirmed; actual device visual proof, and exhaustive per-tab coverage, remain pending.
- Input and patched APK both declare `com.pairip.application.Application` and `com.pairip.licensecheck.LicenseActivity`. Morphe re-signs output, which can trigger Pairip license / anti-tamper checks. **Successful patching and APK signature verification do not demonstrate app launch or sign-in.**
- Input APK certificate: Android test/debug key; patched APK certificate: `CN=Morphe` (v2 verified, SHA256 `1d2ce7241e0555208cda714edaeb2ff28118f6475409d79db2df8f5bbecf035f`). Android won't update a different-signed installed package in place. Do not uninstall the current app until backups/restore and an isolated install plan are validated.
- No provider calls, credential changes, or third-party SDK behavior changes are intended.
- No server-side or username-based ad targeting bypass is attempted.

## Local files and tests

- Morphe patch source: `patches/src/main/kotlin/dev/rikunode/hatenabookmark/HideNativePrEntriesPatch.kt`.
- Patch archive: `patches/build/libs/patches-0.1.1.mpp` (both PR-filter and clone enabled).
- Local build: `./gradlew.bat :patches:generatePatchesList --offline`.
- Static validation: `python -m unittest discover -s tests -v`.

## Real APK application (2026-10-13)

The user's exact 20,916,502-byte antisplit APK was copied to `apk-inspection/hatena-bookmark-v5.84-58400.apk`; SHA-256 equals the pinned value above. `python scripts/patch_user_apk.py apk-inspection/hatena-bookmark-v5.84-58400.apk` applied **2 patches, 0 failures** (PR removal and safe side-by-side clone), and Morphe Desktop reported PATCHING/REBUILDING/SIGNING all PASS.

- Current side-by-side test output: `apk-inspection/HatenaBookmark-5.84-PR-hidden-clone-test.apk`
- Current clone APK SHA-256: `79bad0c793fbb0962d0291dcac82ab4c41bba821fbcf15d5913b3ed0af75fff1`
- `apksigner verify --verbose`: V2 `true`, single signer `CN=Morphe`.
- Clone package `com.hatena.android.bookmark.morphe`; versionName `5.84`, versionCode `58400`. All 3 provider authorities and the custom receiver permission are also rewritten to avoid coexistence conflicts.
- Original `classes2.dex` unchanged byte-for-byte; output adds a small `classes3.dex` carrying the modified class `MuteHelperKt` as part of Morphe STRIP_FAST.
- Android SDK `dexdump -d` on the output's `classes3.dex` shows the first instructions in `filteredWithMuteWords` check `BookmarkEntry.isPrEntry()`, return `false` for PR, and fall through to original article filtering otherwise.

## Isolated Android 36 emulator test

- Started an existing Android 36 / Google Play emulator in **read-only** mode, without saving snapshots.
- `adb install -r` of the **cloned** APK returned `Success`, with `pm path` confirming package `com.hatena.android.bookmark.morphe`. No original package installation was changed, and no physical device was attached.
- `am start` launched original `RootMenuActivity` under the new package and navigated to `MainActivity`. The app process remained alive after 18 seconds; there was no observed `FATAL EXCEPTION`.
- Later the foreground window became `com.pairip.licensecheck.LicenseActivity`. Pairip logged `NameNotFoundException: com.hatena.android.bookmark` while trying to inspect the original package, which had **not** been installed in the emulator. This confirms a **licensing blocker in the isolated environment**, not successful feed rendering. The original app is present on the user's phone, so device outcome is uncertain.
- A second emulator attempt also installed/launched the clone, but timed out before an original+clone combination test. **Do not claim Pairip licensing passed or that native ads disappeared.**
- All **12 local unit/actual APK/clone manifest and signature tests PASS**. These confirm build/apply/DEX logic and package coexistence metadata, not licensing or visual behavior.

**Next gate:** On an actual phone where the official Hatena app remains installed, install only the cloned APK as a separate app; check whether Pairip licensing permits normal navigation, then compare the `総合` feed's native PR rows. Do not uninstall or clear the original app. Preserve any account/bookmark data before broader testing. The generated/test patched APK remains local and is not distributed; only the .mpp patch bundle and source are published on GitHub.
