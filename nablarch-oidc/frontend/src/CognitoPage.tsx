import { Amplify } from 'aws-amplify';
import { fetchAuthSession, signInWithRedirect, signOut as amplifySignOut } from 'aws-amplify/auth';
import type { JWT } from 'aws-amplify/auth';
import { Hub } from 'aws-amplify/utils';
import { useEffect, useState } from 'react';
import { Box, Button, Container, Heading, Stack, Text } from '@chakra-ui/react';
import TokenClaimsTable from './TokenClaimsTable';
import { useNavigate } from 'react-router-dom';
import BackendSignInButton from './BackendSignInButton';

// Amplify v6 を使用する（v5(Gen1)は2027-05にEOLとなるため）
// v6では Auth.configure は廃止され、Amplify.configure にネスト構造で設定する
// 既存のCognitoリソースを利用する場合の設定項目についてはガイドを参照する
//   https://docs.amplify.aws/react/build-a-backend/auth/use-existing-cognito-resources/
// region の指定は不要（userPoolId から導出される）
// redirectSignIn / redirectSignOut は v6 では配列で指定する
// Viteでは import.meta.env で環境変数にアクセスできる
//   https://ja.vitejs.dev/guide/env-and-mode.html
Amplify.configure({
  Auth: {
    Cognito: {
      userPoolId: import.meta.env.VITE_COGNITO_USERPOOL_ID,
      userPoolClientId: import.meta.env.VITE_COGNITO_CLIENT_ID,
      loginWith: {
        oauth: {
          domain: import.meta.env.VITE_COGNITO_DOMAIN,
          scopes: ['openid'],
          redirectSignIn: [import.meta.env.VITE_COGNITO_REDIRECT_URL],
          redirectSignOut: [import.meta.env.VITE_COGNITO_REDIRECT_URL],
          responseType: 'code',
        },
      },
    },
  },
});

function CognitoPage() {
  const [idToken, setIdToken] = useState<JWT | null>(null);
  const [signedInBackend, setSignedInBackend] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    // v6の fetchAuthSession は未認証でも例外にならず tokens が undefined になるため、
    // トークンの有無で認証状態を判定する
    fetchAuthSession()
      .then(({ tokens }) => setIdToken(tokens?.idToken ?? null))
      .catch(() => setIdToken(null));
  }, []);

  useEffect(() => {
    // 認証イベントにコールバックを設定できるので、操作に応じた処理を登録しておく。
    // v6ではHosted UIサインインの失敗イベント(signInWithRedirect_failure)が用意されて
    // いるため、失敗時のハンドリングも登録する。
    // なお、認証イベントの一覧については以下のガイドに記載されている。
    //   https://docs.amplify.aws/react/build-a-backend/auth/connect-your-frontend/listen-to-auth-events/
    // Hub.listen は購読解除関数を返すため、クリーンアップで解除する
    const unsubscribe = Hub.listen('auth', async ({ payload }) => {
      switch (payload.event) {
        case 'signInWithRedirect': {
          // Hosted UIからのリダイレクト後、認可コードとトークンの交換が完了した時点で
          // 発火するイベントのため、この時点で fetchAuthSession を呼べばトークンを取得できる
          const { tokens } = await fetchAuthSession();
          setIdToken(tokens?.idToken ?? null);
          break;
        }
        case 'signInWithRedirect_failure':
          console.error('Hosted UIでのサインインに失敗しました');
          setIdToken(null);
          break;
        case 'signedOut':
          setIdToken(null);
          break;
      }
    });
    return unsubscribe;
  }, [])

  const signIn = () => {
    // ユーザープールの Hosted UI を使用している想定のため、signInWithRedirect を使う
    // (v5 の federatedSignIn に相当する)
    //   https://docs.amplify.aws/react/build-a-backend/auth/connect-your-frontend/sign-in/#sign-in-with-redirect
    signInWithRedirect();
  }

  const signOut = () => {
    amplifySignOut();
  }

  const goBack = () => {
    navigate('/');
  };

  const onSignedInBackend = () => {
    setSignedInBackend(true);
  }

  const onFailedSignInBackend = (message: string) => {
    console.error(message);
  }

  return (
    <Container maxWidth="4xl" px={16} py={16}>
      <Heading textAlign="center">Amazon CognitoでID連携</Heading>
      <Stack mt="8" spacing={4}>
        <Button onClick={goBack}>サービス選択へ戻る</Button>
        {idToken ? <Button onClick={signOut}>サインアウト</Button> : <Button onClick={signIn}>サインイン</Button>}
        {idToken && <BackendSignInButton idToken={idToken.toString()}
                                         service="cognito"
                                         onSuccess={onSignedInBackend}
                                         onFailure={onFailedSignInBackend} />}
        <Box borderWidth='1px' borderRadius="lg" p={4}>
          <Stack spacing={8}>
            <Stack spacing={4}>
              <Text as="b" color="gray.500">Cognitoの認証状態：</Text>
              <Text>{idToken ? '認証済み' : '未認証'}</Text>
            </Stack>
            <Stack spacing={4}>
              <Text as="b" color="gray.500">バックエンドの認証状態：</Text>
              <Text>{signedInBackend ? '認証済み' : '未認証'}</Text>
            </Stack>
            {idToken &&
              <>
                <Stack spacing={4}>
                  <Text as="b" color="gray.500">IDトークン：</Text>
                  <Text>{idToken.toString()}</Text>
                </Stack>
                <Stack spacing={4}>
                  <Text as="b" color="gray.500">IDトークンのクレーム：</Text>
                  <TokenClaimsTable claims={idToken.payload} />
                </Stack>
              </>
            }
          </Stack>
        </Box>
      </Stack>
    </Container>
  )
}

export default CognitoPage;
