import React from 'react'
import ReactDOM from 'react-dom/client'
import Top from './Top'
import { ChakraProvider } from '@chakra-ui/react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import CognitoPage from './CognitoPage';
import Adb2cPage from './Adb2cPage';

// React Router v6ではobject-based routeも選択可能になったので使ってみる
const router = createBrowserRouter([
  { path: "/", element: <Top /> },
  { path: "/cognito", element: <CognitoPage /> },
  { path: "/adb2c", element: <Adb2cPage /> },
]);

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <ChakraProvider>
      <RouterProvider router={router} />
    </ChakraProvider>
  </React.StrictMode>,
)
