import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable()
export class AuthGuardService implements CanActivate {
  private savedUrl: string;
  private failedAuth = false;
  constructor(private authService: AuthService, private router: Router) { }
  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    return this.authService.getIsAuth().then((result) => {
      const isAuth = result;
      if (this.failedAuth && isAuth) {
        this.failedAuth = false;
        if (this.savedUrl) {
          this.router.navigate([this.savedUrl]);
        }
      }
      this.savedUrl = state.url;
      if (!isAuth) {
        this.failedAuth = true;
        this.router.navigate(['./login']);
      }
      return result;
    }, (error) => {
      console.log(error);
      return error;
    });
  }
}
