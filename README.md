# FreeMyMusicPlayer

Android向けのシンプルな音楽プレイヤーアプリです。ローカルの音楽ファイルを快適に再生できます。

## 主な機能

- 📁 フォルダベースの音楽管理
- 🎵 高品質な音楽再生（Media3 ExoPlayer）
- 🔀 シャッフル・リピート再生
- 📱 バックグラウンド再生対応
- 🎨 12色のテーマカラー対応
- 📊 再生回数の記録・表示
- ⚙️ 表示設定のカスタマイズ

## 色設定・カスタマイズ機能

### ColorConfig クラス
アプリ全体の色設定を統一的に管理するための設定クラスです。UI要素の色を一元管理し、テーマ変更時の色調整を容易にします。

#### 設定可能な色テーマ
```kotlin
val THEME_COLORS = arrayOf(
    "#FF69B4", // ピンク
    "#FF6B6B", // 赤
    "#4ECDC4", // ターコイズ
    "#45B7D1", // 青
    "#000000", // 黒
    "#FFFFFF", // 白
    "#DDA0DD", // プラム
    "#F39C12", // オレンジ
    "#8E44AD", // 紫
    "#2ECC71", // エメラルド
    "#E74C3C", // 深紅
    "#95A5A6"  // グレー
)
```

#### 色調整パラメータ
以下の定数により、各UI要素の色を細かく調整できます：

| パラメータ | 用途 | デフォルト値 | 説明 |
|-----------|------|-------------|------|
| `LIGHTER_COLOR_BLEND_RATIO` | 薄いテーマカラー作成 | 0.7f | 白とのブレンド比率 |
| `BACKGROUND_COLOR_BLEND_RATIO` | 背景色作成 | 0.9f | 白とのブレンド比率 |
| `LIST_BACKGROUND_BLEND_RATIO` | リスト背景色作成 | 0.05f | 黒とのブレンド比率（暗化） |
| `ICON_BLACK_THEME_BLEND_RATIO` | 黒テーマアイコン背景 | 0.3f | 白とのブレンド比率（薄化） |
| `ICON_OTHER_THEME_BLEND_RATIO` | その他テーマアイコン背景 | 0.2f | 黒とのブレンド比率（濃化） |
| `TAB_INDICATOR_BLEND_RATIO` | タブインジケーター色 | 0.3f | テーマカラー調整比率 |

#### 提供される色計算メソッド
- `createLighterColor()`: 薄いテーマカラーを作成
- `createBackgroundColor()`: 背景色を作成
- `createListBackgroundColor()`: リスト背景色を作成（少し暗く）
- `createIconBackgroundColor()`: アイコン背景色を作成
- `isWhiteTheme()`: 白テーマかどうかを判定
- `getWhiteThemeBarColor()`: 白テーマ用のActionBar/タブ背景色を取得
- `getBarTextColor()`: ActionBar/タブのテキスト色を取得
- `getTabIndicatorColor()`: タブインジケーター色を取得
- `getContrastTextColor()`: コントラストテキスト色を計算

#### 色設定の適用範囲
- ActionBarとステータスバーの背景色・文字色
- タブレイアウトの背景色・文字色・インジケーター色
- リスト背景色とアイコン背景色
- 各種ボタンとテキストの色
- 白テーマ時の特別な色調整

#### 使用方法
```kotlin
// テーマカラーからアイコン背景色を作成
val iconBgColor = ColorConfig.createIconBackgroundColor(themeColor)

// 白テーマ用のActionBar色を取得
val actionBarColor = if (ColorConfig.isWhiteTheme(themeColor)) {
    ColorConfig.getWhiteThemeBarColor(backgroundColor)
} else {
    themeColor
}
```

#### 色のカスタマイズ方法
1. `app/src/main/java/com/enzo/freemymusicplayer/ColorConfig.kt` を開く
2. 上記のパラメータ定数を変更
3. アプリを再ビルドして変更を反映

例：アイコン背景をもう少し濃くしたい場合
```kotlin
// 変更前
const val ICON_OTHER_THEME_BLEND_RATIO = 0.2f

// 変更後（より濃く）
const val ICON_OTHER_THEME_BLEND_RATIO = 0.3f
```

## 技術仕様

### 開発環境
- **言語**: Kotlin
- **プラットフォーム**: Android
- **最小SDK**: API 26 (Android 8.0)
- **アーキテクチャ**: MVVM

### 使用ライブラリ
- **Media3**: 音楽再生エンジン
- **ExoPlayer**: 高性能メディアプレイヤー
- **ViewBinding**: UIバインディング
- **Material Design Components**: UI コンポーネント

### 権限
- `READ_EXTERNAL_STORAGE`: 音楽ファイルアクセス用（Android 12以下）
- `READ_MEDIA_AUDIO`: メディアアクセス用（Android 13以上）

## インストール・ビルド

1. プロジェクトをクローン
2. Android Studioで開く
3. 必要な権限を許可
4. アプリをビルド・実行

## 使い方

1. アプリを起動
2. 音楽ファイルへのアクセス権限を許可
3. フォルダから音楽を選択して再生
4. 設定画面でテーマカラーや表示設定をカスタマイズ
5. 再生回数画面で統計情報を確認

## ライセンス

このプロジェクトはMITライセンスの下で公開されています。