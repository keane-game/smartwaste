/**
 * Configuration technique transverse (sécurité, OpenAPI, CORS, ordonnancement…).
 *
 * <p>Ce package <b>n'est pas un contexte borné</b> : il ne porte donc pas d'{@code @ApplicationModule}
 * et n'a pas vocation à contenir de règle métier. Il n'existe que pour héberger le câblage Spring
 * qui, par nature, doit connaître plusieurs modules à la fois — le concentrer ici évite que ce
 * câblage ne crée des dépendances croisées entre les modules eux-mêmes.
 */
package sn.smartwaste.collect.config;
