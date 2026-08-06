
export abstract class User {
     userId!: number;
     userLastname!: string;
     userFirstname!: string
     password!: string;
     userEmail!: string;
     userAddress!: string;
     userPhone!: string;
     role!:any


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
