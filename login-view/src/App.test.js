import React from 'react';
import { render } from '@testing-library/react';
import App from './App';

test('renders sign in', () => {
  const { getByText } = render(<App />);
  const linkElement = getByText(/Remember me/i);
  expect(linkElement).toBeInTheDocument();
});
