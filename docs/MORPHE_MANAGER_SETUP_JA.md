# Morphe Manager での追加方法（公開後）

**このリポジトリは現在、公開前のローカル開発状態です。** GitHubへ公開し、v0.1.1 Releaseに `patches-0.1.1.mpp` を添付してから、以下の手順を使います。

1. Morphe Manager を起動し、パッチソース管理画面で「リモートから追加」を選ぶ。
2. 以下の URL を追加する。

```text
https://github.com/rikunode/hatena-bookmark-hide-pr-patches
```

3. パッチ一覧に **2件** 表示されることを確認する。v0.1.1 / 2 patches / エラーなしが合格。
4. 入力APKは **はてなブックマーク5.84 (58400)** に限定。
5. `Hide native PR entries from article feeds` が広告非表示本体。
6. `Clone Hatena Bookmark for safe side-by-side testing` は元アプリを残したい場合のクローンパッチ。現在のバンドルでは両方デフォルト有効。
7. Morpheで対象APKを選び、両方選択してパッチ適用・署名する。
8. できあがったAPKは元アプリをアンインストールせずに共存インストールする。別パッケージ `com.hatena.android.bookmark.morphe` になるため元アプリのアカウント・データは移行されない。

## 重要な注意

- 入力APKのSHA256検証と、パッチ適用成功はWindowsで確認済み。パッチ後のAPKはエミュレーターにインストールでき、`MainActivity` まで進んだ。
- その後 **Pairip LicenseActivity** に切り替わった。Playライセンス保護を無効化したり迂回したりはしない。元アプリが存在するスマホではどうなるか未確認。
- 元アプリとの共存はパッケージ名、独自権限、3つのContentProviderオーソリティの衝突を避ける設計。非root。
- `isPrEntry()` に該当するネイティブ広告はミュート対象フィルタから除去する。広告の実表示、全タブの網羅率、ログインやブックマーク保存は実機で要確認。
- Play Protectやライセンスエラーが発生しても、保護機能を無効化せずにスクリーンショット・ログを確認する。
- 通常のはてなブックマークは元の状態のまま残すこと。アップデートできなくてもアンインストールしない。
