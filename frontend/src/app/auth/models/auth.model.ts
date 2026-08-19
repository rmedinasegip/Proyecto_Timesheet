export type UserRole = 'CON' | 'AUT';

export interface CurrentUser {
  code: number;
  email: string;
  name: string;
  role: UserRole;
  roleName: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}
