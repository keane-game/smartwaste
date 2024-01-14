
export abstract class User {
     id: number | undefined;
     username: string | undefined;
     password: string | undefined;
     email: string | undefined;
     nom: string | undefined;
     prenom: string | undefined;
     avatar: Blob | undefined;
     archive: boolean | undefined;
     address?: string;
     birthDate: Date | undefined;
     phone: string | undefined;

    constructor(user: User) {
      this.id        = user.id;
      this.username  = user.username;
      this.password  = user.password;
      this.email     = user.email;
      this.nom = user.nom;
      this.prenom  = user.prenom;
      this.avatar     = user.avatar;
      this.archive   = user.archive;
      this.address   = user.address;
     }

}
