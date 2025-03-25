import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private http: HttpClient) {}

  login(user: { email: string, password: string }): Observable<{ token: string, message: string }> {
    return this.http.post<{ token: string, message: string }>('/api/auth/login', user).pipe(
      tap(response => {
        localStorage.setItem('authToken', response.token);
        localStorage.setItem('userEmail', user.email);
      })
    );
  }

  register(user: { email: string, password: string }): Observable<{ message: string }> {
    return this.http.post<{ message: string }>('/api/auth/register', user);
  }

  logout(): Observable<{ message: string }> {
    const token = localStorage.getItem('authToken');
    localStorage.removeItem('authToken');
    localStorage.removeItem('userEmail');
    return this.http.post<{ message: string }>('/api/auth/logout', {}, {
      headers: { Authorization: `Bearer ${token}` }
    });
  }

  isLoggedIn(): boolean {return !!localStorage.getItem('authToken');}
  getUserEmail(): string | null {return localStorage.getItem('userEmail');}
  getToken(): string | null {return localStorage.getItem('authToken');}
}