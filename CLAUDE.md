# chunklimitcounter

## Ce projet fait partie de Toolkit

- Emplacement : `projets/chunklimitcounter/`, à l'intérieur du dossier Toolkit
  (la racine de Toolkit est donc `../..` depuis ce dossier).
- Ce projet peut être modifié librement depuis n'importe quel éditeur (VS Code, Android
  Studio, Notepad++…), sans passer par Toolkit.
- Sa fiche `projet.json` décrit le projet pour Toolkit (nom, type, point d'entrée,
  actions…). **Garde-la à jour** si tu changes le point d'entrée, ajoutes ou modifies une
  action (bouton), etc. Le format est documenté dans `../../app/docs/projet-json.md`.
- Explique ce que tu fais en français, simplement.
- Git : fais des **commits locaux** réguliers avec des messages clairs en français.
  **Ne fais jamais de push** : l'utilisateur pousse lui-même depuis Toolkit.
  Ne versionne jamais de secrets (clés, mots de passe, keystores, `.env`, `local.properties`).

## Rôle du projet

Mod client Fabric pour Minecraft **26.2** qui affiche en temps réel le nombre de blocs, block-entities et entités limités par chunk (par exemple par le plugin serveur [Insights](https://www.spigotmc.org/resources/insights.55561/)), sans avoir à poser/casser pour vérifier.

## Technologies

Projet Java (Gradle).

## Lancer ou compiler

Compiler avec le wrapper Gradle du projet : `gradlew build` ; le .jar est produit dans `build/libs/`.

## Tests

- `gradlew build` lance aussi les tests unitaires (`src/test`) : couleurs et position du HUD, liste des limites
  (chaque identifiant doit exister dans le jeu : à relancer après un changement de version de Minecraft),
  lecture de la config, contenu de l'export JSON.
- `gradlew runClientGameTest` : test dans le vrai jeu (`src/gametest`), ouvre une fenêtre Minecraft (prévenir
  l'utilisateur). Il pose des blocs dans le chunk du joueur et vérifie les comptes. Regarder les captures dans
  `build/run/clientGameTest/screenshots/`.
