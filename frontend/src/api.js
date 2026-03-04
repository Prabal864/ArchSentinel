// API configuration
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const api = {
  baseURL: API_URL,

  // Register token endpoint
  registerToken: async (token, repo) => {
    const response = await fetch(`${API_URL}/api/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify({ token, repo }),
    });

    if (!response.ok) {
      throw new Error(`API Error: ${response.status} ${response.statusText}`);
    }

    return response.json();
  },

  // Get registered tokens
  getTokens: async () => {
    const response = await fetch(`${API_URL}/api/tokens`, {
      method: 'GET',
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error(`API Error: ${response.status} ${response.statusText}`);
    }

    return response.json();
  },
};

export default api;

