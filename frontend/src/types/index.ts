export interface User {
  id: string;
  email: string;
  createdAt: string;
}

export interface AuthCredentials {
  email: string;
  password: string;
}

export interface Save {
  id: string;
  userId: string;
  fileName: string;
  fileSize: number;
  createdAt: string;
}