# Rapport - Projet JEE-Spring

## Introduction

Ce projet met en œuvre une API REST de gestion de bières, de fabricants et de paniers. L’objectif principal est de répondre aux fonctionnalités minimales demandées : consulter un catalogue de bières, gérer un panier et permettre à un administrateur de gérer fabricants, bières, prix et stock. Le projet inclut aussi un mécanisme de checkout avec décrément de stock et journalisation des ventes (vente en gros) via un journal dédié.

Le rapport est structuré autour des sections demandées : architecture implémentée, problèmes/résolutions/choix, planning initial vs effectif, puis bilan. Les informations ci‑dessous sont basées sur la lecture complète du code actuel.

## 1. Architecture implémentée

### 1.1 Structure en couches

L’application suit une architecture en couches classique :

- **Entités JPA** : `Beer`, `Brewery`, `Cart`, `CartItem`, `SalesLog`, `SalesLogItem`.
- **Repositories** : interfaces `CrudRepository` pour l’accès aux données.
- **Services** : logique de persistance minimale (save/find/delete) avec implémentations simples.
- **Controllers REST** : exposition HTTP, validations métier, gestion des codes d’erreur.

Cette séparation facilite la lisibilité et l’évolution du projet : les entités restent concentrées sur les données, les services sur la persistance, et les contrôleurs sur les règles métier et l’API.

### 1.2 Modèle de données (JPA)

**Brewery (fabricant)**
- Champs principaux : `name`, `country`.
- Relation : `@OneToMany(mappedBy = "brewery")` vers `Beer`.

**Beer (bière)**
- Champs principaux : `name`, `price`, `stock`.
- Relation : `@ManyToOne` vers `Brewery` (obligatoire, `nullable = false`).

**Cart (panier)**
- Contient la liste des lignes `CartItem`.
- Relation : `@OneToMany(mappedBy = "cart", cascade = ALL, orphanRemoval = true)`.
- Méthode métier : `getTotalPrice()` calcule le total en additionnant `price * quantity`.

**CartItem (ligne de panier)**
- Champs principaux : `quantity`.
- Relations : `@ManyToOne` vers `Cart` et vers `Beer`.

**SalesLog (journal de vente)**
- Champs principaux : `cartId`, `total`, `createdAt`.
- Relation : `@OneToMany(mappedBy = "salesLog", cascade = ALL, orphanRemoval = true)` vers `SalesLogItem`.

**SalesLogItem (ligne de journal)**
- Champs principaux : `beerId`, `beerName`, `unitPrice`, `quantity`, `lineTotal`.
- Relation : `@ManyToOne` vers `SalesLog`.

**Conséquences techniques**
- Le couple `cascade = ALL` + `orphanRemoval = true` sur `Cart.items` implique qu’un panier supprimé efface automatiquement ses lignes, et qu’un remplacement d’items supprime les anciens.

### 1.3 Initialisation des données

Le composant `DataInitializer` (profil `h2`) insère un jeu de données minimal : fabricants, bières, paniers et lignes. Cela permet de tester rapidement l’API sans chargement manuel préalable.

## 2. Fonctionnalités minimales (exigences)

### 2.1 Consultation du catalogue (utilisateur)

**User story** : *En tant qu’utilisateur, je veux pouvoir consulter le catalogue des bières, leurs prix et les stocks restants.*

Endpoints publics :
- `GET /beers` → liste des bières (nom, prix, stock, fabricant).
- `GET /beers/{id}` → détail d’une bière.
- `GET /breweries` → liste des fabricants (avec leurs bières).
- `GET /breweries/{id}` → détail d’un fabricant.

La consultation est ouverte sans authentification et fournit les informations nécessaires (prix/stock) à la préparation d’une commande.

### 2.2 Panier et prix total (utilisateur)

**User story** : *En tant qu’utilisateur, je veux pouvoir créer un panier contenant des bières, et obtenir les éléments commandés ainsi que le prix total.*

Endpoints principaux :
- `POST /carts` → crée un panier (avec ou sans items).
- `PUT /carts/{id}` → remplace le contenu du panier (stratégie « replace‑all »).
- `GET /carts/{id}` → récupère le panier avec ses lignes.
- `GET /carts/{id}/total` → retourne le total calculé.
- `POST /carts/{id}/checkout` → valide l’achat : décrémente le stock, supprime le panier, journalise la vente.
- `DELETE /carts/{id}` → supprime le panier.

Gestion des lignes de panier (CRUD dédié) :
- `POST /cart_items` → ajoute une ligne (requiert `cart.id`, `beer.id`, `quantity > 0`).
- `PUT /cart_items/{id}` → met à jour une ligne.
- `GET /cart_items` / `GET /cart_items/{id}`.
- `DELETE /cart_items/{id}`.

**Remarques métier**
- Le total est **recalculé à la demande** via `Cart.getTotalPrice()`.
- Le stock est **décrémenté uniquement lors du checkout** pour éviter des réservations incomplètes.

### 2.3 Administration (fabricants, bières, prix, stock)

**User story** : *En tant qu’administrateur, je veux pouvoir créer des fabricants, des bières liées et définir le stock et le prix.*

Accès admin :
- Les routes d’écriture utilisent le header `X-ADMIN-KEY`.
- Validation centralisée via `AdminUtils.checkAdminKey(...)`.
- Clé actuelle : `secret123` (en dur, suffisante pour la démonstration).

Endpoints admin :
- `POST /breweries`, `PUT /breweries/{id}`, `DELETE /breweries/{id}`.
- `POST /beers`, `PUT /beers/{id}`, `DELETE /beers/{id}`.

Les contrôleurs s’assurent que les entités liées sont bien « managées » (ex : une `Beer` doit référencer une `Brewery` existante) et que les champs métier essentiels sont valides.

### 2.4 Fonctionnalités supplémentaires (au‑delà du minimum)

En plus des objectifs minimaux, le projet implémente :

- **Checkout transactionnel** : validation d’achat, décrément du stock, suppression du panier.
- **Journalisation des ventes** : création d’un `SalesLog` avec ses lignes `SalesLogItem` lors du checkout.
- **CRUD séparé des lignes de panier** : endpoints dédiés `/cart_items` pour créer, modifier et supprimer des lignes.
- **Jeu de données de démonstration** : insertion automatique via `DataInitializer` (profil `h2`).

## 3. Problèmes, résolutions, choix

### 3.1 Sécurité admin/user

- **Problème** : distinguer accès public et opérations administratives sans implémenter une authentification complète.
- **Choix** : utilisation d’une clé simple `X-ADMIN-KEY`.
- **Résolution** : contrôle centralisé dans `AdminUtils`.
- **Conséquence** : méthode volontairement légère, adaptée au cadre du cours, mais insuffisante pour la production.

### 3.2 Relations JPA et entités managées

- **Problème** : éviter la création de relations vers des entités inexistantes.
- **Résolution** : rechargement des entités référencées via les services (`attachManagedBrewery`, `attachManagedEntities`).
- **Bénéfice** : erreurs explicites (`400`/`404`) et cohérence relationnelle.

### 3.3 Gestion du stock au checkout

- **Problème** : garantir que le stock est suffisant au moment de l’achat.
- **Résolution** : l’endpoint `POST /carts/{id}/checkout` vérifie les quantités, décrémente le stock, puis supprime le panier.
- **Choix** : transaction simple et contrôle de quantité, sans stratégie de verrouillage avancée.

### 3.4 Journalisation des ventes (vente en gros)

- **Problème** : tracer les ventes réalisées après checkout.
- **Résolution** : création d’un `SalesLog` et de ses `SalesLogItem` lors du checkout.
- **Choix** : journalisation côté serveur, sans API de lecture/export pour l’instant.

### 3.5 Validation des données

- **CartItem** : `quantity` doit être strictement > 0 et ne pas dépasser le stock.
- **Beer** : `price >= 0` et `stock >= 0` (validation manuelle dans le contrôleur).
- **Erreur HTTP** : `400` si données invalides, `409` si stock insuffisant ou suppression impossible (entité référencée).

## 4. Planning initial vs effectif

### 4.1 Planning initial

- **S1** : mise en place Spring Boot + H2 + JPA.
- **S2** : modèle `Beer`/`Brewery` et endpoints catalogue.
- **S3** : panier `Cart`/`CartItem` + calcul du total.
- **S4** : endpoints admin + validations + tests.

### 4.2 Planning effectif

- Base technique (Spring Boot, Spring Web, Spring Data JPA, H2) mise en place.
- Catalogue public bières/fabricants opérationnel.
- Panier + lignes de panier + total calculé.
- Checkout avec décrément de stock et suppression du panier.
- Journalisation des ventes (SalesLog).
- Initialisation de données via `DataInitializer`.

**Écarts notables**
- Authentification limitée à une clé admin en dur.
- Pas d’API de lecture/export du journal des ventes.

## 5. Bilan

### 5.1 Points positifs

- Architecture en couches claire et cohérente.
- Modèle JPA simple, relations bien définies.
- API REST lisible avec gestion explicite des erreurs HTTP.
- Fonctionnalités minimales entièrement couvertes.

### 5.2 Limites et améliorations possibles

- Remplacer `X-ADMIN-KEY` par une authentification complète (Spring Security).
- Ajouter Bean Validation (`@Min`, `@NotBlank`) pour homogénéiser la validation.
- Exposer des endpoints de consultation/export des `SalesLog`.
- Mettre en place un contrôle transactionnel plus fin au checkout (optimistic locking).
- Harmoniser le comportement lors des suppressions référentielles (ex. suppression d’un `Brewery` lié).

## Conclusion

Le projet respecte les exigences minimales : catalogue de bières avec stock et prix, panier avec total, et administration des fabricants/bières/stock/prix. L’architecture et les choix techniques restent adaptés au cadre du cours, tout en offrant une base solide pour des évolutions futures (authentification, audit des ventes, validations déclaratives).
