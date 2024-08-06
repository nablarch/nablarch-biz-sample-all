# OIDCを用いて認証するサンプルアプリケーションのバックエンド

## 動作確認の環境

* Java : 17
* Maven : 3.9.8

## 主な技術スタック

- [Nablarch](https://nablarch.github.io/docs/LATEST/doc/)

## 実行方法

### プロパティの設定

バックエンドのアプリケーションでは以下のプロパティを使用します。

| 名前                        | 説明                             |
|---------------------------|--------------------------------|
| aws.cognito.region       | Cognitoのリージョンコード               |
| aws.cognito.userPool.id  | CognitoのユーザープールID              |
| aws.cognito.userPool.clientId    | CognitoのアプリケーションクライアントID       |
| azure.adb2c.tenant       | B2Cテナント名(`onmicrosoft.com`は除く) |
| azure.adb2c.signInPolicy | B2Cのサインイン用のユーザーフローの名前          |
| azure.adb2c.applicationId | B2Cのアプリケーション（クライアント）ID         |
| azure.adb2c.directoryId         | B2CのディレクトリID                   |

設定方法については、Nablarchの解説書を参照してください。

- [システムプロパティを使って環境依存値を上書きする](https://nablarch.github.io/docs/LATEST/doc/application_framework/application_framework/libraries/repository.html#repository-overwrite-environment-configuration)
- [OS環境変数を使って環境依存値を上書きする](https://nablarch.github.io/docs/LATEST/doc/application_framework/application_framework/libraries/repository.html#os)

`aws.cognito.XXX`系のプロパティに設定する値は、AWSマネジメントコンソールのCognitoから確認できます。なお、Cognito用の認証ページを使用しない場合は設定不要です。

`azure.adb2c.XXX`系のプロパティに設定する値は、Azure PortalのB2CテナントおよびAzure AD B2Cから確認できます。なお、Azure AD B2C用の認証ページを使用しない場合は設定不要です。

### ローカルサーバの起動

Nablarchのブランクプロジェクトに設定済みの[waitt-maven-plugin](https://github.com/kawasima/waitt)で起動します。

```
mvn waitt:run
```
