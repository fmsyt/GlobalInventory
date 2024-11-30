# CHANGELOG

## [0.5.0] - 2024-11-30

### 追加

- 保存しているアイテムに対して採番し、それをキーとしてアイテムを引き出すコマンドを追加
    - `gs pull <管理番号> [<個数>]`

- プラグインの管理コマンドを追加 (Operatorのみ)
    - `gs manage backup` : 現時点のバックアップを作成
    - `gs manage config max_pull_count` : 保存可能なアイテムの最大数を表示
    - `gs manage config max_pull_count <個数>` : 保存可能なアイテムの最大数を設定
    - `gs manage config backup_interval` : バックアップの間隔を表示
    - `gs manage config backup_interval <tick>` : バックアップの間隔を設定

- バックアップの自動作成機能を追加
    - バックアップの間隔はデフォルトで2時間
