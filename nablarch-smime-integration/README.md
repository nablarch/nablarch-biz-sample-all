# bouncycastleを使用した電子署名つきメールの送信サンプル

## 前提

動作確認は以下の環境で行っています。

* Java Version : 17
* Maven : 3.9.6
* Docker : 24.0.7

## テストの実施方法

[SMIMESignedMailSenderTest](src/test/java/please/change/me/common/mail/smime/SMIMESignedMailSenderTest.java) は実際にメールを送信してテストを行っています。
その際に内臓サーバーを利用していないため、送信に利用するメールサーバーがローカル環境に設定されていることが前提となります。

GreenMailのDockerコンテナを利用してテストを行う場合は、以下の手順で実行してください。

### 1. GreenMailのDockerコンテナを起動

    $ docker compose up -d

### 2. テストを実行

    $ mvn test