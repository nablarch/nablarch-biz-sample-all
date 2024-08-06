import { Button, Container, Heading, Stack, Text } from '@chakra-ui/react';
import { useNavigate } from 'react-router-dom';

function Top() {
  const navigate = useNavigate();

  return (
    <Container maxWidth="3xl" px={16} py={16}>
      <Stack spacing={8}>
        <Heading textAlign="center">ID連携に使用するサービスを選択</Heading>
        <Stack spacing={4}>
            <Button onClick={() => navigate('/cognito')}>Amazon Cognito</Button>
          <Button onClick={() => navigate('/adb2c')}>Azure AD B2C</Button>
          </Stack>
      </Stack>
    </Container>
  )
}

export default Top
