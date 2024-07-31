# NablarchでLogbookを使用する実装例のアプリケーション

## 動作確認方法

REST APIへのリクエスト送信時に、Logbookによるアクセスログを出力しています。
外部のREST APIサーバを構築するため、[mockoon](https://mockoon.com/)を使用してモックサーバを構築します。

mockoonはDockerコンテナイメージでも提供されているため、以下のコマンドで起動できます。

```
$ cd mock
$ docker compose up -d
```

アプリケーションを起動します。

```
$ mvn compile jetty:run
```

モックサーバ、アプリケーションの両方が起動したら、ブラウザで http://localhost:8080/logbook/get にアクセスする。

アプリケーションでは、以下のREST APIを定義している。

- GET /logbook/get
  - デフォルト設定のLogbookで、GETリクエストのログを出力する
- GET /logbook/get/mask
  - マスクを設定したLogbookで、GETリクエストのログを出力する
- POST /logbook/post
  - デフォルト設定のLogbookで、POSTリクエストのログを出力する
- POST /logbook/post/mask
  - マスクを設定したLogbookで、POSTリクエストのログを出力する


