
export abstract class User {
     /**
      * UUID v7 depuis la migration DDD du contexte « Identité & Accès » (ADR-0013) : le backend
      * ne renvoie plus un entier. Aucun code ne fait d'arithmétique dessus — l'identifiant ne
      * sert qu'à composer les URL — mais le type doit refléter ce que l'API renvoie réellement.
      */
     userId!: string;
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
