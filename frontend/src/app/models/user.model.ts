
/** Authority telle que renvoyee par /v1/authorities (voir backend AuthorityEntity). */
export interface UserAuthority {
  authorityId: string;
  name: string;
  description?: string;
}

export abstract class User {
     /** UUID v7 depuis la migration ADR-0012 (2026-08-07) — plus un number depuis longtemps. */
     userId!: string;
     userLastname!: string;
     userFirstname!: string
     password!: string;
     userEmail!: string;
     userAddress!: string;
     userPhone!: string;
     role!: UserAuthority | null;


    constructor(user: User) {
      this.userId        = user.userId;
      this.userLastname  = user.userLastname;
      this.userFirstname  = user.userFirstname;
      this.password  = user.password;
      this.userEmail     = user.userEmail;
      this.userPhone   = user.userPhone;
      this.userAddress   = user.userAddress;
      this.role = user.role
     }

}
