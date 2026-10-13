# Hatena Bookmark 5.80 (58000) — experimental Morphe port, UNRELEASED

## APK safety, static verification only

User-provided 5.80 APK SHA256: f5de27b6d0948d5716ef7dd24c0910c37e370c07db3888ff8aaa38a70afbd94b.

- Binary manifest: com.hatena.android.bookmark, versionName 5.80, versionCode 58000, app class com.hatena.android.bookmark.BookmarkApplication, targetSdk33/minSdk26.
- ZIP CRC verification passed. APK v2 signing was **cryptographically verified**, including RSA PKCS#1 SHA256 signature of signed-data and Android v2 full-file chunk digests. Cert subject CN=Hatena Inc.; cert SHA1 741de50a98c93edefc1a382fda5f5f9c3fcee478; SHA256 8cd89ea1c1871d503a6b618c0e4f5467f634a9040e5e74304a61087c54277a8a.
- This cert matches the Hatena Bookmark and Hatena Blog developer cert SHA1 advertised by APKPure. As a self-reported archive, this is not independently verified publisher key provenance.
- No Pairip Application, LicenseActivity, or other Pairip/LicenseClient strings in either DEX. No native shared libraries or nested executable payloads in ZIP. Normal-looking network, Firebase, Hatena account and notifications permissions; no request-install-packages, SMS, accessibility or broad external storage permission observed.
- Literal domain strings correspond predominantly to Hatena, Google/Firebase and bundled dependency documentation. These are heuristics, **not** a complete malware verdict.
- Kaspersky documented a Trojanized APKPure distributor APK in 2021, fixed in its 3.17.19 app; third-party archives deserve additional caution. Do not install this historic APK on the daily-use phone before safe testing.

## DEX compatibility (exact user APK)

- MuteHelperKt.filteredWithMuteWords(BookmarkEntry,List)Z: registerCount 8, insSize 2, codeUnits **105** (compared with 103 on 5.84). Calls BookmarkEntry.isPrEntry() at offset 21 and getTitle() at offset 0.
- Existing mute filter allows PR entries through even when they otherwise match a mute condition.
- 5.80 has 3 content providers, original custom permission and queries manifest section compatible with clone patch preflight.
- Local branch experiment/hatena-58000-no-pairip targets **5.80 / versionCode 58000 only**, requires 8 registers, 105 code units, one PR call at offset 21. Public main v5.84 remains unchanged.
- Local Gradle :patches:buildAndroid **SUCCESS**; experimental Android patches-0.2.0.mpp includes classes.dex and both patch classes. NOT applied to the uploaded APK. NOT published. APK launch/ads/auth unverified.

## Latest verified input on Windows (2026-10-14)

The user placed the original uploaded APK at:
C:\Users\kkoji\Documents\Codex\hatena-bookmark-morphe-patches\apk-inspection\Hatena_Bookmark__Save__share_5.80_APKPure.apk

- SHA256 EXACT MATCH with previous independently inspected upload: f5de27b6d0948d5716ef7dd24c0910c37e370c07db3888ff8aaa38a70afbd94b.
- Windows Android SDK aapt confirms original package com.hatena.android.bookmark / 5.80 / versionCode 58000.
- Android SDK apksigner verify: valid APK Signature Scheme v2 AND v3 and valid Google SourceStamp. The signer is CN=Hatena Inc., same SHA1/SHA256 above; its RSA public key is 1024 bits. This verifies APK integrity relative to the signing key, not total app safety.
- ZIP CRC and original two DEX class_def counts (10,316 and 7,554) pass. No Pairip-named ZIP entries observed.
- Local 0.2.0 experimental .mpp Android bundle ZIP/DEX validation passed, SHA256 b084a27da9a4682ebf2177ecdf9b69e021d323350841d4cf038904dbf6a0c6c5.
- Attempted to apply the experimental 0.2.0 Morphe patch to this actual APK. The tool call was BLOCKED by OpenAI safety check BEFORE confirmation of a result. Do NOT evade this restriction or claim successful patch application, signed modified APK, emulator install, login or ad removal.

## Next gate (NOT YET VERIFIED)

The approved execution environment currently does not permit confirming actual patched APK generation. Do not bypass the safety restriction. An authorized, independently run test would need Morphe application on the exact input and verification of 2 applied/0 failures, output clone manifest, signatures, generated DEX and isolated emulator launch; do not promise successful results. The original phone app must remain installed and unchanged. No public 5.80 patch release until a validated path exists.
