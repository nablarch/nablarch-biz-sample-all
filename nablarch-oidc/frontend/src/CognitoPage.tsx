import { Auth, Hub } from 'aws-amplify';
import { useEffect, useState } from 'react';
import { Box, Button, Container, Heading, Stack, Text } from '@chakra-ui/react';
import { CognitoIdToken } from 'amazon-cognito-identity-js';
import TokenClaimsTable from './TokenClaimsTable';
import { useNavigate } from 'react-router-dom';
import BackendSignInButton from './BackendSignInButton';

// Amplifyは Gen2 がリリースされているが、ここでは Gen1 v5 は使用する
// 環境情報の設定項目についてはガイドを参照する
//   https://docs.amplify.aws/gen1/react/prev/tools/libraries/configure-categories/#scoped-configuration
// Auth関連しか使わないので、 Amplify.configure ではなく Auth.configure を使っている
//   https://github.com/aws-amplify/amplify-js/issues/3635#issuecomment-686530160
// Viteでは import.meta.env で環境変数にアクセスできる
//   https://ja.vitejs.dev/guide/env-and-mode.html
Auth.configure({
  region: import.meta.env.VITE_COGNITO_REGION,
  userPoolId: import.meta.env.VITE_COGNITO_USERPOOL_ID,
  userPoolWebClientId: import.meta.env.VITE_COGNITO_CLIENT_ID,
  mandatorySignIn: true,
  oauth: {
    domain: import.meta.env.VITE_COGNITO_DOMAIN,
    scope: ['openid'],
    redirectSignIn: import.meta.env.VITE_COGNITO_REDIRECT_URL,
    redirectSignOut: import.meta.env.VITE_COGNITO_REDIRECT_URL,
    responseType: 'code',
  },
});

function CognitoPage() {
  const [idToken, setIdToken] = useState<CognitoIdToken | null>(null);
  const [signedInBackend, setSignedInBackend] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    Auth.currentSession()
      .then((session) => setIdToken(session.getIdToken()))
      .catch(() => setIdToken(null));
  }, []);

  useEffect(() => {
    // 認証イベントにコールバックを設定できるので、操作に応じた処理を登録しておく。
    // 認証失敗など細かいハンドリングを本来はすべきだが、ここでは簡易的に成功した場合のみ実装しておく。
    // なお、認証イベントの一覧については以下のガイドに記載されている。
    //   https://docs.amplify.aws/gen1/javascript/prev/build-a-backend/auth/auth-events/
    Hub.listen('auth', async (data) => {
      switch (data.payload.event) {
        case 'cognitoHostedUI':
          const session = await Auth.currentSession();
          setIdToken(session.getIdToken());
          break;
        case 'signOut':
          setIdToken(null);
          break;
      }
    });
  }, [])

  const signIn = () => {
    // ユーザープールの Hosted UI を使用している想定のため、federatedSignIn の方を使う
    //   https://docs.amplify.aws/gen1/javascript/prev/build-a-backend/auth/advanced-workflows/#identity-pool-federation
    Auth.federatedSignIn();
  }

  const signOut = () => {
    Auth.signOut();
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
        {idToken && <BackendSignInButton idToken={idToken.getJwtToken()}
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
                  <Text>{idToken.getJwtToken()}</Text>
                </Stack>
                <Stack spacing={4}>
                  <Text as="b" color="gray.500">IDトークンのクレーム：</Text>
                  <TokenClaimsTable claims={idToken.decodePayload()} />
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
