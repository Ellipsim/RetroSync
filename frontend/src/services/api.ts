
import type { AuthCredentials, User, Save } from '../types';

const API_URL = '/api';

async function request<T>(
  endpoint: string,
  options: RequestInit = {},
): Promise<T> {
  const response = await fetch(`${API_URL}${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });

  if (!response.ok) {
    throw new Error(
      response.status === 401
        ? 'Invalid email or password'
        : response.status === 409
          ? 'This email is already registered'
          : `Request failed (${response.status})`,
    );
  }

  return response.json() as Promise<T>;
}

export function register(credentials: AuthCredentials): Promise<User> {
  return request<User>('/users/register', {
    method: 'POST',
    body: JSON.stringify(credentials),
  });
}

export function login(credentials: AuthCredentials): Promise<User> {
  return request<User>('/users/login', {
    method: 'POST',
    body: JSON.stringify(credentials),
  });
}

// Save management

const SAVES_URL = '/saves';

export async function getSaves(userId: string): Promise<Save[]> {
  const response = await fetch(`${API_URL}${SAVES_URL}/${userId}`);

  if (!response.ok) {
    throw new Error(`Failed to load saves (${response.status})`);
  }

  return response.json();
}

export async function uploadSave(
  userId: string,
  file: File,
): Promise<Save> {
  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch(`${API_URL}${SAVES_URL}/${userId}`, {
    method: 'POST',
    body: formData,
  });

  if (!response.ok) {
    throw new Error(`Failed to upload save (${response.status})`);
  }

  return response.json();
}

export async function downloadSave(
  userId: string,
  saveId: string,
): Promise<Blob> {
  const response = await fetch(
    `${API_URL}${SAVES_URL}/${userId}/${saveId}/download`,
  );

  if (!response.ok) {
    throw new Error(`Failed to download save (${response.status})`);
  }

  return response.blob();
}

export async function deleteSave(
  userId: string,
  saveId: string,
): Promise<void> {
  const response = await fetch(
    `${API_URL}${SAVES_URL}/${userId}/${saveId}`,
    { method: 'DELETE' },
  );

  if (!response.ok) {
    throw new Error(`Failed to delete save (${response.status})`);
  }
}
