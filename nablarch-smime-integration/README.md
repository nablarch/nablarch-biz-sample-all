# bouncycastleを使用した電子署名つきメールの送信サンプル

## 前提

動作確認は以下の環境で行っています。

* Java : 17
* Maven : 3.9.6
* Docker : 24.0.7

## テストの実施方法

テストでは実際にメールを送信するため、メールサーバが必要になります。
メールサーバにはGreenMailを使用しているため、以下の手順で実行してください。

### 1. GreenMailのDockerコンテナを起動

    $ docker compose up -d

### 2. テストを実行

    $ mvn test