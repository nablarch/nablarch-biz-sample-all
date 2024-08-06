import { Box, Button, Container, Heading, Stack, Text } from '@chakra-ui/react';
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MsalProvider, useIsAuthenticated, useMsal } from '@azure/msal-react';
import { PublicClientApplication } from '@azure/msal-browser';
import TokenClaimsTable from './TokenClaimsTable';
import BackendSignInButton from './BackendSignInButton';

// claimsの型定義はCognito側の実装に合わせておく
type IdToken = {
  jwt: string;
  claims: { [id: string]: any };
};

// 環境情報の設定項目についてはガイドを参照する
//   https://github.com/AzureAD/microsoft-authentication-library-for-js/blob/dev/lib/msal-browser/docs/initialization.md
// Viteでは import.meta.env で環境変数にアクセスできる
//   https://ja.vitejs.dev/guide/env-and-mode.html
const msalConfig = {
  auth: {
    clientId: import.meta.env.VITE_ADB2C_APPLICATION_ID,
    authority: `https://${import.meta.env.VITE_ADB2C_TENANT}.b2clogin.com/${import.meta.env.VITE_ADB2C_TENANT}.onmicrosoft.com/${import.meta.env.VITE_ADB2C_SIGNIN_POLICY}`,
    knownAuthorities: [`${import.meta.env.VITE_ADB2C_TENANT}.b2clogin.com`],
    redirectUri: import.meta.env.VITE_ADB2C_REDIRECT_URL,
  },
  cache: {
    cacheLocation: "sessionStorage",
    storeAuthStateInCookie: false
  },
};

const loginRequest = {
  scopes: ['openid']
}

function Adb2cPage() {
  const msalInstance = new PublicClientApplication(msalConfig);
  // MsalProvider配下では認証状態コンテキストにアクセスできるため、主コンテンツでアクセスできるように別コンポーネントに分けておく
  return (
    <MsalProvider instance={msalInstance}>
      <Content />
    </MsalProvider>
  )
}

function Content() {
  const { instance, accounts } = useMsal();
  const isAuthenticated = useIsAuthenticated();
  const navigate = useNavigate();
  const [idToken, setIdToken] = useState<IdToken | null>(null);
  const [signedInBackend, setSignedInBackend] = useState(false);

  useEffect(() => {
    if (isAuthenticated) {
      // userMsalフックで取得するaccountsにもトークンが入っているため、以下のようにしてもトークンは取得できる。
      //   setIdToken({ jwt: accounts[0].idToken!, claims: accounts[0].idTokenClaims! });
      // ただ、Azure側の更新は即時反映されないため、acquireTokenSilentで最新トークンを取得するようにしておく。
      //   https://github.com/AzureAD/microsoft-authentication-library-for-js/blob/dev/lib/msal-react/docs/hooks.md#usemsal-hook
      instance.acquireTokenSilent({ account: accounts[0], ...loginRequest })
        .then((response) => {
          setIdToken({ jwt: response.idToken, claims: response.idTokenClaims });
        })
    } else {
      setIdToken(null);
    }
  }, [isAuthenticated]);

  const signIn = () => {
    // ポップアップ表示するための関数もあるが、Cognitog泡に合わせてリダイレクトにしておく
    instance.loginRedirect(loginRequest);
  }

  const signOut = () => {
    instance.logoutRedirect();
  }

  const goBack = () => {
    navigate('/');
  };

  const onSignedInBackend = () => {
    // 表示時にバックエンドの認証状態をチェックしたりする方がいいが、検証できれば十分なため毎回リクエストを送信するようにしておく
    setSignedInBackend(true);
  }

  const onFailedSignInBackend = (message: string) => {
    console.error(message);
  }

  return (
    <Container maxWidth="4xl" px={16} py={16}>
      <Heading textAlign="center">Azure AD B2CでID連携</Heading>
      <Stack mt="8" spacing={4}>
        <Button onClick={goBack}>サービス選択へ戻る</Button>
        {isAuthenticated ? <Button onClick={signOut}>サインアウト</Button> : <Button onClick={signIn}>サインイン</Button>}
        {isAuthenticated && idToken && <BackendSignInButton idToken={idToken.jwt}
                                                            service="adb2c"
                                                            onSuccess={onSignedInBackend}
                                                            onFailure={onFailedSignInBackend} />}
        <Box borderWidth='1px' borderRadius="lg" p={4}>
          <Stack spacing={8}>
            <Stack spacing={4}>
              <Text as="b" color="gray.500">Azure AD B2Cの認証状態：</Text>
              <Text>{isAuthenticated ? '認証済み' : '未認証'}</Text>
            </Stack>
            <Stack spacing={4}>
              <Text as="b" color="gray.500">バックエンドの認証状態：</Text>
              <Text>{signedInBackend ? '認証済み' : '未認証'}</Text>
            </Stack>
            {isAuthenticated && idToken &&
              <>
                <Stack spacing={4}>
                  <Text as="b" color="gray.500">IDトークン：</Text>
                  <Text>{idToken.jwt}</Text>
                </Stack>
                <Stack spacing={4}>
                  <Text as="b" color="gray.500">IDトークンのクレーム：</Text>
                  <TokenClaimsTable claims={idToken.claims} />
                </Stack>
              </>
            }
          </Stack>
        </Box>
      </Stack>
    </Container>
  )
}

export default Adb2cPage;
