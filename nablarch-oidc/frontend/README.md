# OIDCを用いて認証するサンプルアプリケーションのフロントエンド

## 動作確認の環境

* Node : 20.15.1
* npm : 10.7.0

## 主な技術スタック

- [React](https://react.dev/)
- [Chakra UI](https://chakra-ui.com)
- [React Router](https://reactrouter.com/en/main)
- [Vite](https://ja.vitejs.dev/)
- [amplify-js](https://github.com/aws-amplify/amplify-js)
- [msal-react](https://github.com/AzureAD/microsoft-authentication-library-for-js/tree/dev/lib/msal-react)

## 実行方法

### 環境変数の設定

フロントエンドのアプリケーションでは以下の環境変数を使用します。

| 名前                        | 説明                             |
|---------------------------|--------------------------------|
| VITE_COGNITO_REGION       | Cognitoのリージョンコード               |
| VITE_COGNITO_USERPOOL_ID  | CognitoのユーザープールID              |
| VITE_COGNITO_CLIENT_ID    | CognitoのアプリケーションクライアントID       |
| VITE_COGNITO_DOMAIN       | Cognitoドメイン                    |
| VITE_COGNITO_REDIRECT_URL | Cognitoでアプリケーションに許可したコールバックURL |
| VITE_ADB2C_APPLICATION_ID | B2Cのアプリケーション（クライアント）ID         |
| VITE_ADB2C_TENANT         | B2Cテナント名(`onmicrosoft.com`は除く) |
| VITE_ADB2C_SIGNIN_POLICY  | B2Cのサインイン用のユーザーフローの名前          |
| VITE_ADB2C_REDIRECT_URL   | B2Cでアプリケーションに許可したリダイレクトURI     |

環境変数は任意の方法で設定してください。Viteの機能で`.env`系のファイルを使用することも可能です。（[参考](https://ja.vitejs.dev/guide/env-and-mode.html#env-files)）

`VITE_COGNITO_XXX`系の環境変数に設定する値は、AWSマネジメントコンソールのCognitoから確認できます。なお、Cognito用の認証ページを使用しない場合は設定不要です。

`VITE_ADB2C_XXX`系の環境変数に設定する値は、Azure PortalのB2CテナントおよびAzure AD B2Cから確認できます。なお、Azure AD B2C用の認証ページを使用しない場合は設定不要です。

### ローカルサーバの起動

Viteの標準のままであるため、devスクリプトでローカルサーバが起動します。

`npm run dev`

起動後に http://localhost:5173 にアクセスすることでトップページが表示されます。
