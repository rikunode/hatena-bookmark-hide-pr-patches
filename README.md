# Hatena Bookmark 5.84 native PR-entry filter — Morphe

**Status: Android bundle compiled and statically inspected. Not yet tested on the uploaded APK or on a real Android device. Not published to GitHub.**

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
- Screenshot shows an inline PR article in the `総合` tab. The direct dex code path for this type of list is confirmed; device visual proof is still pending.
- The input APK declares `com.pairip.application.Application` for app protection. Morphe must sign modified APKs, which may trigger anti-tamper checks. **Morphe bundle build PASS does not imply patched APK will launch or preserve sign-in.**
- A new signing certificate cannot update an installed app signed with the original cert. Do not uninstall the current app until backups/restore and an isolated install plan are validated.
- No provider calls, credential changes, or third-party SDK behavior changes are intended.
- No server-side or username-based ad targeting bypass is attempted.

## Local files and tests

- Morphe patch source: `patches/src/main/kotlin/dev/rikunode/hatenabookmark/HideNativePrEntriesPatch.kt`.
- Patch archive: `patches/build/libs/patches-0.1.0.mpp`.
- Local build: `./gradlew.bat :patches:generatePatchesList --offline`.
- Static validation: `python -m unittest discover -s tests -v`.

**Next gate:** Transfer the user's exact uploaded APK to this project's `apk-inspection` folder, then apply through Morphe Desktop, compare patches applied/errors, validate output signing, investigate Pairip protection, and test on user device separately. Do not announce the patch as functional before this gate.
