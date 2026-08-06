export class CustomMenuItem {
    constructor() {
        this.Label = "null";
        this.Icon = "null";
        this.RouterLink = "null";
        this.Childs = [];
        this.IsChildVisible = false;
    }
    Label: string;
    Icon?: string;
    RouterLink: string;
    Childs?: any;
    IsChildVisible: boolean;
}
