# Rapport - bechir-jee-spring-project

## Architecture implémentée
### Structure en couches

- **Entités JPA**: `Beer`, `Brewery`, `Cart`, `CartItem`.
- **Repositories**: interfaces `CrudRepository` (ex: `BeerRepository`).
- **Services**: logique "minimale" de persistance (save/find/delete) (ex: `BeerServiceImpl`).
- **Controllers REST**: endpoints HTTP + validations + mapping des erreurs en codes HTTP.

### Modèle de données (JPA)

- `Brewery` (fabricant)
  - `@OneToMany(mappedBy = "brewery")` vers `Beer`
- `Beer` (bière)
  - attributs: `name`, `price`, `stock`
  - `@ManyToOne` vers `Brewery` (obligatoire via `nullable=false`)
- `Cart` (panier)
  - `@OneToMany(mappedBy = "cart", cascade = ALL, orphanRemoval = true)` vers `CartItem`
  - total calculé: `getTotalPrice()` = somme `(beer.price * quantity)`
- `CartItem` (ligne de panier)
  - `@ManyToOne` vers `Cart` (obligatoire)
  - `@ManyToOne` vers `Beer` (obligatoire)
  - attribut: `quantity`

Impact de `cascade=ALL` + `orphanRemoval=true` sur `Cart.items`:
- supprimer un `Cart` supprime automatiquement ses `CartItem`
- remplacer/vider la liste des items puis sauvegarder supprime les items orphelins

### Données de démo

- `DataInitializer` insère:
  - des `Brewery` et des `Beer` avec prix/stock
  - des `Cart` et `CartItem` d’exemple

## Fonctionnalités et API

### Consulter le catalogue (utilisateur)

Objectif: consulter bières, prix, stock.

Endpoints (publics):
- `GET /beers` → liste des bières
- `GET /beers/{id}` → détail d’une bière
- `GET /breweries` → liste des fabricants (avec ses bières)
- `GET /breweries/{id}` → détail d’un fabricant (avec ses bières)

### Créer un panier et obtenir le total (utilisateur)

Objectif: créer un panier, y mettre des bières, voir les lignes et le total.

Endpoints:
- `POST /carts` → crée un panier (avec ou sans items)
- `PUT /carts/{id}` → remplace le contenu du panier (stratégie "replace-all")
- `GET /carts/{id}` → récupère le panier (items inclus)
- `GET /carts/{id}/total` → retourne le total calculé
- `DELETE /carts/{id}` → supprime le panier

Gestion des lignes de panier (CRUD séparé):
- `POST /cart_items` → crée une ligne (requiert `cart.id`, `beer.id`, `quantity > 0`)
- `PUT /cart_items/{id}` → met à jour une ligne
- `GET /cart_items` / `GET /cart_items/{id}`
- `DELETE /cart_items/{id}`

Remarque importante:
- Le **total** est recalculé à la demande via `Cart.getTotalPrice()`.
- Le projet **n’applique pas** de décrément automatique du stock lors d’un ajout au panier (stock = information métier portée par `Beer`). 
  La décrémentation serait plus logique après l'achat du panier.

### Opération autres que lecture sur fabricants + bières + stock/prix (administrateur)

Objectif: créer/modifier/supprimer fabricants et bières (prix/stock).

Différenciation admin/user:
- Les routes d’écriture côté admin exigent un header: `X-ADMIN-KEY`.
- Validation centralisée dans `AdminUtils.checkAdminKey(...)`.
- Clé actuelle: `secret123` (constante dans `AdminUtils`).

Endpoints admin (requièrent `X-ADMIN-KEY`):
- `POST /breweries`, `PUT /breweries/{id}`, `DELETE /breweries/{id}`
- `POST /beers`, `PUT /beers/{id}`, `DELETE /beers/{id}`

Exemple de requête admin (création bière):

```bash
curl -X POST http://localhost:8080/beers \
  -H 'Content-Type: application/json' \
  -H 'X-ADMIN-KEY: secret123' \
  -d '{"name":"Test IPA","price":4.2,"stock":10,"brewery":{"id":1}}'
```

## Problèmes, résolutions, choix

### Sécurité / distinction admin-user

- **Choix**: pas de Spring Security / OAuth; les routes d’administration exigent un header `X-ADMIN-KEY`.
- **Pourquoi**: approche volontairement légère, adaptée au contexte pédagogique.
- **Implémentation**: contrôle centralisé dans `AdminUtils.checkAdminKey(...)`.
- **Conséquence**: clé **en dur** (`secret123`) → OK pour démo, insuffisant pour prod.
- **Amélioration possible**: Spring Security + rôles (ADMIN/USER), configuration externe (env), rotation de secrets, audit.

### Gestion JPA: attacher des entités "managées"

Dans `BeerController` et `CartItemController`, les entités référencées (brewery/cart/beer) sont rechargées depuis la base avant la sauvegarde.

- **Problème évité**: relations vers un id inexistant ou entité transiente.
- **Résolution**: `attachManagedBrewery(...)` et `attachManagedEntities(...)` valident les IDs et remplacent par des instances managées.
- **Bénéfice**: erreurs plus claires (`400`/`404`) et cohérence des relations.

### Suppressions / intégrité référentielle

- **Panier**: `Cart.items` est en `cascade=ALL` + `orphanRemoval=true`.
  - supprimer un `Cart` supprime ses `CartItem`.
  - remplacer la liste (via `PUT /carts/{id}`) supprime les anciennes lignes devenues orphelines.
- **Bière**: suppression bloquée si référencée par des `CartItem`.
  - `BeerController.delete(...)` traduit une `DataIntegrityViolationException` en `409 CONFLICT`.

Note "client":
- pas d’entité `Client`/`User` dans ce projet; la problématique "supprimer un client avec panier" ne s’applique pas directement.

### Validations métier

- `CartItemController`: `quantity` doit être strictement > 0.
- Amélioration: ajouter validation sur `price`/`stock` (>= 0), idéalement via Bean Validation.

### Gestion des erreurs HTTP

- `404 NOT_FOUND`: ressource non trouvée.
- `400 BAD_REQUEST`: données invalides (IDs requis manquants, quantité <= 0).
- `403 FORBIDDEN`: `X-ADMIN-KEY` manquant/invalide.
- `409 CONFLICT`: suppression d’une bière impossible (référencée).

### Planning initial (exemple)

- S1: mise en place Spring Boot + H2 + JPA
- S2: modèle `Beer`/`Brewery` + endpoints catalogue
- S3: panier `Cart`/`CartItem` + calcul du total
- S4: endpoints admin + validations + tests

### Planning effectif (observé)

- Base technique en place: Spring Boot + Spring Web + Spring Data JPA + H2
- Catalogue bières/fabricants: lecture publique + CRUD admin
- Panier + lignes + total calculé
- Initialisation de données via `DataInitializer` (profil `h2`)

Écarts notables (vs “prod”):
- pas d’authentification utilisateur, pas d’entité client
- pas de décrément de stock transactionnel lors d’un "checkout"

## Bilan

### Points positifs

- Architecture en couches claire (controller/service/repository/entities).
- Relations JPA cohérentes; cascade/orphanRemoval utile pour le panier.
- API simple à tester (curl) et erreurs HTTP explicites.

### Limites / améliorations

- Remplacer `X-ADMIN-KEY` hardcodé par une vraie auth (Spring Security) + configuration externe.
- Ajouter validation de champs (`price`, `stock`) et éventuellement DTOs dédiés.
- Implémenter un endpoint "checkout" (décrément de stock transactionnel, contrôles de stock).
- Harmoniser le comportement lors de suppressions invalides (ex: supprimer un `Brewery` référencé).

