import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { CustomMenuItem } from '../models/menu-item.model';

@Injectable({
    providedIn: 'root',
})
/**
 * menu data service
 */
export class MenuDataService {

    public toggleMenuBar: BehaviorSubject<any> = new BehaviorSubject<any>(null);

    getMenuList(): CustomMenuItem[] {
        return [
            {
                Label: 'Briefs', Icon: 'fa-file', RouterLink: '/admin/briefs', Childs: null, IsChildVisible: false
            },
            {
                Label: 'Rendus', Icon: 'fa-download', RouterLink: '/admin/rendus', Childs: null, IsChildVisible: false
            },
            {
                Label: 'Explorer', Icon: 'fa-compass', RouterLink: '/admin/explorer', Childs: null, IsChildVisible: false
            },
            {
                Label: 'Forum', Icon: 'fa-comments', RouterLink: '/admin/forum', Childs: null, IsChildVisible: false
            },
            {
                Label: 'Paramétes', Icon: 'fa-cog', RouterLink: '', Childs: [

                    { Label: 'Promos', RouterLink: '/admin/users/promos', Childs: null, IsChildVisible: false },
                    { Label: 'Référentiels', RouterLink: '/admin/users/referenciel', Childs: null, IsChildVisible: false },
                    { Label: 'Groupe competences', RouterLink: '/admin/users/grp-competence', Childs: null, IsChildVisible: false },
                    { Label: 'Compétences', RouterLink: '/admin/users/comptence', Childs: null, IsChildVisible: false },
                    { Label: 'Groupe tags', RouterLink: '/admin/users/grp-tags', Childs: null, IsChildVisible: false },
                    { Label: 'Profils sorties', RouterLink: '/admin/users/profil-sortis', Childs: null, IsChildVisible: false },
                ], IsChildVisible: false
            },
            {
                Label: 'Users', Icon: 'fa-user', RouterLink: '', Childs: [
                    { Label: 'Utilisateurs', RouterLink: '/admin/users/user-list', Childs: null, IsChildVisible: false },
                    {Label: 'Profils', RouterLink: '/admin/users/list-profil', IsChildVisible: false, Childs: null }
                ], IsChildVisible: false
            },
            {
                Label: 'Historique des promos', Icon: 'fa-history', RouterLink: '/historique', Childs: null, IsChildVisible: false
            },
        ];
    }
}
