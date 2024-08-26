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

モックサーバ、アプリケーションの両方が起動したら、ブラウザで http://localhost:8080/logbook/get にアクセスします。

## REST API一覧

アプリケーションでは、以下のREST APIを定義しています。

- GET /logbook/get
  - デフォルト設定のLogbookで、GETリクエストのログを出力します
- GET /logbook/get/mask
  - マスクを設定したLogbookで、GETリクエストのログを出力します
- POST /logbook/post
  - デフォルト設定のLogbookで、POSTリクエストのログを出力します
- POST /logbook/post/mask
  - マスクを設定したLogbookで、POSTリクエストのログを出力します


