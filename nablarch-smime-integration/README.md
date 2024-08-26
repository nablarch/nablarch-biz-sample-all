# bouncycastleを使用した電子署名つきメールの送信サンプル

## 動作確認方法

テストでは実際にメールを送信するため、メールサーバとして[GreenMail](https://greenmail-mail-test.github.io/greenmail/)を使用します。

GreenMailのDockerコンテナを起動するため、以下のコマンドを実行します。

```
$ docker compose up -d
```

テストコードで動作確認するため、以下のコマンドを実行します。

```
$ mvn test
```
