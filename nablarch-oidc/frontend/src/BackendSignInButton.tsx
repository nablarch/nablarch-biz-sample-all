import { Button } from '@chakra-ui/react';
import React from 'react';

type Props = {
  idToken: string;
  service: 'cognito' | 'adb2c';
  onSuccess: () => void;
  onFailure: (message: string) => void;
}

function BackendSignInButton({ idToken, service, onSuccess, onFailure }: Props) {
  const signInBackend = async () => {
    try {
      // CSRFトークンを取得する
      // 本来はキャッシュする方が望ましいが、ここでは簡易的に実装するため毎回取得しにいく
      const csrfTokenResponse = await fetch("/api/csrf_token", {
        method: 'GET'
      });
      if (!csrfTokenResponse.ok) {
        onFailure('Get CSRF token failed...');
        return;
      }
      const csrfToken = await csrfTokenResponse.json();

      // CSRFトークンを設定してログインする
      const loginResponse = await fetch(`/api/${service}/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          [csrfToken.csrfTokenHeaderName]:  csrfToken.csrfTokenValue,
        },
        body: JSON.stringify({ idToken })
      });
      if (!loginResponse.ok) {
        onFailure('Login failed...');
        return;
      }
      onSuccess();
    } catch (e) {
      console.error(e);
      onFailure('Fetch failed...');
    }
  };

  return (
    <Button onClick={signInBackend}>バックエンドにサインイン</Button>
  )
}

export default BackendSignInButton;
