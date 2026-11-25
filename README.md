# IncidentDesk - Backend Spring Boot

API REST de declaration et suivi d'incidents informatiques / cybersecurite.
Authentification JWT + Spring Security, roles EMPLOYEE / TECHNICIAN / ADMIN.

Le frontend React est dans un projet a part (`incident-frontend`).

## Technologies

- Java 17, Spring Boot 3.4
- Spring Security (stateless) + JWT (jjwt 0.12)
- Spring Data JPA, H2 (par defaut) ou MySQL
- Bean Validation, Lombok

## Lancer le projet

### Backend

```bash
mvn spring-boot:run
```

ou lancer `IncidentBackendApplication` depuis IntelliJ.

- API : http://localhost:8080
- Console H2 : http://localhost:8080/h2-console (JDBC URL : `jdbc:h2:mem:incidentsdb`, user `sa`, pas de mot de passe)

Pour utiliser MySQL : decommenter les lignes MySQL dans `application.properties` (et commenter celles de H2).

### Frontend

```bash
cd incident-frontend
npm install
npm run dev
```

-> http://localhost:5173 (CORS deja autorise cote backend pour cette origine)

## Ports

| Application | Port |
|-------------|------|
| Backend Spring Boot | 8080 |
| Frontend React (Vite) | 5173 |

## Comptes de test

Crees automatiquement au demarrage par `DataInitializer` (mot de passe : **123456** pour tous) :

| Email | Role |
|-------|------|
| employee1@test.com | EMPLOYEE |
| employee2@test.com | EMPLOYEE |
| tech@test.com | TECHNICIAN |
| tech2@test.com | TECHNICIAN |
| admin@test.com | ADMIN |

## Roles

- **EMPLOYEE** : declare des incidents, consulte uniquement les siens, gere son profil
- **TECHNICIAN** : consulte les incidents qui lui sont affectes, change leur statut, ajoute des notes
- **ADMIN** : voit tous les incidents et utilisateurs, affecte les incidents, active/desactive les comptes, statistiques

A l'inscription le role est **toujours EMPLOYEE** (il n'y a pas de champ role dans `RegisterRequest`).

## Endpoints

### Auth (public)

| Methode | URL | Description |
|---------|-----|-------------|
| POST | /auth/register | creer un compte |
| POST | /auth/login | connexion -> `token`, `refreshToken`, `role`, `fullName` |
| POST | /auth/refresh | nouveau access token a partir du refresh token |
| POST | /auth/logout | supprime le refresh token |

### Profil (authentifie)

| Methode | URL |
|---------|-----|
| GET | /api/profile |
| PUT | /api/profile |
| PUT | /api/profile/password |

### Incidents

| Methode | URL | Acces |
|---------|-----|-------|
| POST | /api/incidents | EMPLOYEE |
| GET | /api/incidents/my | EMPLOYEE |
| GET | /api/incidents/{id} | proprietaire, technicien affecte ou ADMIN |
| GET | /api/incidents/assigned | TECHNICIAN |
| PUT | /api/incidents/{id}/status | TECHNICIAN (affecte) ou ADMIN |
| PUT | /api/incidents/{id}/assign/{technicianId} | ADMIN |
| GET | /api/incidents | ADMIN |
| GET | /api/incidents/{id}/history | comme le detail |
| GET | /api/incidents/{id}/comments | comme le detail |
| POST | /api/incidents/{id}/comments | TECHNICIAN (affecte) |

### Administration (ADMIN)

| Methode | URL |
|---------|-----|
| GET | /api/admin/users |
| PUT | /api/admin/users/{id}/enable |
| PUT | /api/admin/users/{id}/disable |
| GET | /api/admin/statistics |

## Fonctionnalites realisees

- inscription / connexion avec JWT (email, role, date de creation et d'expiration dans le token)
- mots de passe encodes avec BCrypt
- access token de 5 minutes + refresh token (7 jours, stocke en base, supprime au logout)
- routes protegees : `/auth/**` et `/h2-console/**` publiques, `/api/**` authentifiees, `/api/admin/**` ADMIN
- controle des roles avec `hasRole()` dans la config et `@PreAuthorize` sur les controllers (`@EnableMethodSecurity`)
- controle du proprietaire cote backend : un employe ne peut pas lire l'incident d'un autre meme en tapant l'URL dans Postman
- un technicien ne peut modifier que les incidents qui lui sont affectes
- un incident CLOSED ne peut plus changer de statut
- un utilisateur desactive ne peut plus se connecter (et son token ne marche plus)
- historique des changements (`IncidentHistory`) : creation, affectation, changements de statut
- notes de traitement (`IncidentComment`)
- validation des donnees (`@NotBlank`, `@Email`, `@Size`, `@Valid`)
- gestion des erreurs avec `@RestControllerAdvice` -> reponses JSON `{ "status": 404, "message": "Incident introuvable" }`
- 401 si pas de token / token invalide / expire, 403 si mauvais role

## Codes d'erreur

| Code | Cas |
|------|-----|
| 400 | donnees invalides, regle metier non respectee (incident ferme...) |
| 401 | pas authentifie (token absent, invalide, expire), mauvais mot de passe, compte desactive |
| 403 | authentifie mais pas le droit (mauvais role, incident d'un autre utilisateur) |
| 404 | incident / utilisateur introuvable |
| 409 | email deja utilise |

## Choix JPA (relations)

- `Incident -> User` (createdBy, assignedTo) en `@ManyToOne(fetch = LAZY)` : plusieurs incidents pour un utilisateur. Par defaut `@ManyToOne` est EAGER, on le met en LAZY pour ne pas charger les utilisateurs a chaque requete sur les incidents. Les DTO sont construits dans les services `@Transactional`, donc pas de `LazyInitializationException`.
- Pas de `@OneToMany` dans `User` : on n'a jamais besoin de "tous les incidents d'un user" depuis l'entite, on passe par le repository (`findByCreatedBy`). Ca evite aussi les boucles infinies en JSON.
- `IncidentHistory` et `IncidentComment` -> `@ManyToOne(LAZY)` vers `Incident`.

## Tests Postman

La collection `postman/incidents-security-tests.postman_collection.json` contient les 9 tests de securite demandes (sans JWT, JWT invalide, JWT expire, employee sur route admin, etc.). Lancer d'abord le dossier "0 - Connexions" qui recupere les tokens.

## Structure

```
src/main/java/com/example/incidents
  config/       DataInitializer (comptes de test)
  controller/   AuthController, ProfileController, IncidentController, AdminController
  dto/          requetes / reponses (on ne renvoie jamais les entites directement)
  entity/       User, Incident, IncidentHistory, IncidentComment, RefreshToken + enums
  exception/    exceptions + GlobalExceptionHandler
  repository/   interfaces Spring Data JPA
  security/     SecurityConfig, JwtService, JwtFilter, CustomUserDetailsService, gestion 401/403
  service/      logique metier
```
