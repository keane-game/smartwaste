import { Injectable } from '@angular/core';
const TOKEN_KEY = 'auth-token';
const USER_KEY = 'auth-user';
@Injectable({
    providedIn: 'root',
})
/**
 * Session storage service
 * Provides methods to get, set, remove, clear session storage items.
 */
export class SessionService {


    
    /**
     * set session storage item
     * @param key 
     * @param value 
     */
    setItem(key: string, value: any) {
        sessionStorage.setItem(key, JSON.stringify(value));
    }

    /**
     * get session storage item
     * @param key 
     */
    getItem(key: string): any {
        var value = sessionStorage.getItem(key);
        return JSON.parse(value || '');
    }

    /**
     * remove session storage item
     * @param key
     */
    removeItem(key: string) {
        sessionStorage.removeItem(key);
    }

    /**
     * remove all session storage items
     */
    clear() {
        sessionStorage.clear();
    }


  signOut(): void {
    window.sessionStorage.clear();
  }

  public saveToken(token: string): void {
    window.sessionStorage.removeItem(TOKEN_KEY);
    window.sessionStorage.setItem(TOKEN_KEY, token);
  }

  public getToken(): string {
    return sessionStorage.getItem(TOKEN_KEY ) || '{}' ;
  }

  public saveUser(user: any): void {
    window.sessionStorage.removeItem(USER_KEY);
    window.sessionStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  public getUser(): any {
    return JSON.parse(sessionStorage.getItem(USER_KEY)|| '{}');
  }
}