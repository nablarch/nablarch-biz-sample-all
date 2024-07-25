# bouncycastleを使用した電子署名つきメールの送信サンプル

## テストの実施方法

[SMIMESignedMailSenderTest](src/test/java/please/change/me/common/mail/smime/SMIMESignedMailSenderTest.java) は実際にメールを送信してテストを行っている。
その際に内臓サーバーを利用していないため、送信に利用するメールサーバーがローカル環境に設定されていることが前提となる。

GreenMailのDockerコンテナを利用してテストを行う場合は、以下の手順で実行する。

### 1. GreenMailのDockerコンテナを起動する

    $ docker compose up -d

### 2. テストを実行する

    $ mvn test