# MythicRPG

大規模RPGプラグイン for Minecraft (Paper 1.20+)

[![Java](https://img.shields.io/badge/Java-17+-orange)](https://adoptium.net/)
[![Paper](https://img.shields.io/badge/Paper-1.20-brightgreen)](https://papermc.io/)
[![MythicMobs](https://img.shields.io/badge/MythicMobs-5.x-blue)](https://mythiccraft.io/)

---

## 概要

MythicRPGは、MinecraftサーバーをフルスケールのRPGに変えるプラグインです。  
MythicMobs 5.x と連携し、カスタム装備・スキル・ジョブ・ダンジョン・ペット・PvP等を提供します。

---

## 機能一覧

### ⚔ 戦闘・属性
- **属性システム** (fire / water / wind / earth / light / dark) — 相性倍率・耐性管理
- **RPGダメージ式** — 攻撃力・防御力・会心率を考慮した独自計算
- **コンボシステム** — 連続ヒットで最大×3倍のダメージ倍率
- **バフ/デバフ** — YAML定義のバフ・ポーションとの連動

### 🧑‍💼 キャラクター
- **ジョブシステム** — 戦士 / 魔法使い / 弓使い
- **レベル・経験値** — MythicMobs討伐・クエスト報酬で成長
- **ステータスポイント** — STR / VIT / INT / AGI を自由に振り分け
- **称号システム** — 討伐数・レベル・ジョブ達成で解放

### 🗡 装備
- **RPG装備スロット** — WEAPON / HELMET / CHESTPLATE / RING×2 / NECKLACE / RELIC
- **レアリティ** — COMMON → UNCOMMON → RARE → EPIC → LEGENDARY → MYTHIC
- **強化** (+1〜+10) / **精錬** (ステータス%アップ)
- **ソケット** — ルビー・サファイア等の宝石を挿入
- **セット効果** — ドラゴン / 賢者 / 影セット等
- **鑑定システム** — 未鑑定装備を鑑定書で開封
- **エンチャント** — POWER / FORTIFY / PRECISION / ARCANE 等10種
- **ステータス継承** — 高レアから低レアへの引き継ぎ
- **ランダム生成** — ドロップ・鍛冶でランダム装備を生成

### 🏰 ダンジョン
- **マルチフロア進行** — 最大10層・複数人対応
- **制限時間タイマー** — タイムアップで自動失敗
- **MythicMobs連携** — フロアごとにMobをスポーン
- **クリア報酬** — ランダム装備 + 経験値ボーナス

### 🐾 ペット
- **MythicMobs召喚** — ウルフ・炎霊・石像ゴーレム等
- **ペット成長** — オーナーがMobを倒すとペットに経験値
- **ジョブ制限** — 特定ジョブ専用ペット

### ⚡ PvP
- **ゾーン管理** — 矩形ゾーンでPvP許可エリアを定義
- **RPGダメージ適用** — ゾーン内はステータス計算でダメージ
- **ELOレーティング** — キル/デスでレーティング変動
- **ランキング** — /mrpg pvp rank で上位5名表示

### 🎯 その他
- **パーティーシステム** — 最大6名・経験値シェア
- **アーティファクト** — セット装備のボーナス効果
- **クエスト連携** — QuestPlugin (softdepend) と統合
- **スコアボード / アクションバー** — リアルタイムHP/MP表示
- **MySQL永続化** — HikariCPによる非同期DB保存

---

## インストール

### 動作要件
| 項目 | バージョン |
|------|-----------|
| Java | 17以上 |
| Paper | 1.20.x |
| MythicMobs | 5.x (必須) |
| QuestPlugin | 任意 (softdepend) |

### 手順
1. `MythicRPG-x.x.x.jar` を `plugins/` フォルダに配置
2. サーバーを起動して `plugins/MythicRPG/config.yml` を編集
3. データベース設定を記入してサーバーを再起動

---

## 設定 (config.yml)

```yaml
database:
  host: localhost
  port: 3306
  name: mythicrpg
  user: root
  password: password

level:
  base-exp: 100
  exp-rate: 1.25

mythicmobs:
  exp-multiplier: 1.0
```

---

## コマンド

| コマンド | 説明 |
|---------|------|
| `/mrpg` | ヘルプ表示 |
| `/mrpg skills` | スキル一覧GUI |
| `/mrpg job` | ジョブ選択GUI |
| `/mrpg stats [gui]` | ステータス確認 |
| `/mrpg buff` | バフ一覧 |
| `/mrpg title` | 称号GUI |
| `/mrpg combo` | コンボ統計 |
| `/mrpg party ...` | パーティー操作 |
| `/mrpg artifact` | アーティファクト確認 |
| `/mrpg profile` | プロフィールGUI |
| `/mrpg dungeon` | ダンジョン選択GUI |
| `/mrpg pet ...` | ペット操作 |
| `/mrpg pvp rank` | PvPランキング |
| `/mrpg element` | 属性耐性GUI |
| `/mrpg reload` | 設定リロード (OP) |

---

## パーミッション

| パーミッション | デフォルト | 説明 |
|--------------|---------|------|
| `mythicrpg.use` | 全員 | 基本コマンド使用 |
| `mythicrpg.admin` | OP | 管理者コマンド |

---

## パッケージ構成

```
com.woxloi.mythicrpg
├── artifact/      アーティファクトセット効果
├── buff/          バフ・デバフシステム
├── combat/        ダメージ計算・Mobキル処理
├── combo/         コンボシステム
├── command/       コマンド処理
├── core/          起動・ロガー
├── db/            MySQL・HikariCP・マイグレーション
├── dungeon/       ダンジョン進行管理
├── element/       属性システム
├── equipment/     装備全般
│   ├── drop/      ドロップテーブル
│   ├── enchant/   エンチャント
│   ├── forge/     鍛冶
│   ├── identify/  鑑定
│   ├── model/     データモデル
│   ├── random/    ランダム生成
│   ├── refine/    精錬
│   ├── set/       セット効果
│   ├── socket/    ソケット・宝石
│   └── transfer/  ステータス継承
├── job/           ジョブシステム
├── level/         レベル・経験値
├── party/         パーティー
├── pet/           ペット召喚・成長
├── player/        プレイヤーデータ管理
├── pvp/           PvPゾーン・ランキング
├── quest/         QuestPlugin連携
├── skill/         スキルシステム
├── stats/         ステータスポイント
├── title/         称号
└── ui/            UI部品（スコアボード・GUI）
    ├── stats/     詳細ステータスGUI
    └── title/     称号詳細GUI
```

---

## 開発

```bash
# ビルド
./gradlew build

# 出力先
build/libs/MythicRPG-x.x.x.jar
```

---

## ライセンス

MIT License — © woxloi
