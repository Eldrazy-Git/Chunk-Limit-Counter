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

Mod client Fabric pour Minecraft **26.3** qui affiche en temps réel le nombre de blocs, block-entities et entités limités par chunk (par exemple par le plugin serveur [Insights](https://www.spigotmc.org/resources/insights.55561/)), sans avoir à poser/casser pour vérifier.

## Technologies

- Java **25**, Gradle **9.8.2** (wrapper). Le démon Gradle tourne sur un JDK 25 grâce à
  `gradle/gradle-daemon-jvm.properties` (`toolchainVersion=25`), même si `JAVA_HOME` pointe ailleurs.
- Fabric : Minecraft **26.3**, Loader **0.19.5** (choix de l'utilisateur), Fabric API **0.167.0+26.3**,
  plugin `net.fabricmc.fabric-loom` **1.19.4**. Versions dans `gradle.properties` ; minimums dans
  `src/main/resources/fabric.mod.json` (`fabricloader >=0.19.5`, `minecraft ~26.3`, `java >=25`).
  Minecraft 26.x n'est plus obfusqué : pas de mappings dans `build.gradle`, le code utilise les noms officiels Mojang.
- Mod **100 % client** (`"environment": "client"`), modid `chunklimitcounter`, package `com.eldrazy.chunklimitcounter`,
  version du mod 1.1.0, licence MIT. Aucune dépendance autre que Fabric API (pas de Mod Menu ni Cloth Config).
- Tests : JUnit 5 (`src/test`) et client gametests Fabric (`src/gametest`, source set séparé, jamais dans le jar).

### Architecture (`src/main/java/com/eldrazy/chunklimitcounter/`)

- `ChunkLimitCounterClient` : point d'entrée client (singleton `getInstance()`). Garde la config, le `TrackedRegistry`
  et le dernier `CountResult`. Recompte à la fin du tick client si : changement de monde ou de chunk, bloc cassé
  (`ClientPlayerBlockBreakEvents.AFTER`), clic sur un bloc (`UseBlockCallback`, marque « sale »), ou toutes les
  `safetyNetIntervalTicks` ticks (filet de sécurité pour pistons, entonnoirs, actions des autres joueurs…).
- `counting/` : `TrackedRegistry` (identifiants texte → `Block`/`EntityType`, résolus une fois), `ChunkCounter`
  (le comptage), `CountResult` (totaux par catégorie, compte par identifiant, autres blocs), `ChunkExporter` (export JSON).
- `config/` : `ModConfig`, `HudConfig`, `CategoryConfig`, `ConfigDefaults` (**la liste des limites**), `ConfigManager` (lecture/écriture Gson).
- `hud/` : `ChunkLimitHudElement` (enregistré par `HudElementRegistry.addLast`), `HudLayout` (couleurs, lignes,
  ancrage), `HudPanelRenderer` (dessin du panneau, partagé par le HUD et l'aperçu de déplacement).
- `gui/` : `ChunkDetailScreen` (détail par bloc/entité, boutons export JSON et capture d'écran),
  `HudPositionScreen` (glisser le HUD, régler l'opacité). Les deux écrans ne mettent pas le jeu en pause.
- `keybinds/ModKeyBindings` : touches K (afficher/masquer le HUD), J (écran de détail), L (déplacer le HUD), via `KeyMappingHelper`.
- Textes de l'interface : `assets/chunklimitcounter/lang/fr_fr.json` et `en_us.json`.

### Comptage (une seule colonne de chunk, toute la hauteur, jamais les chunks voisins)

- **Blocs simples** (fil de redstone, rails, répéteurs…) : lecture de la **palette** de chaque section 16×16×16
  (`PalettedContainer.count`), sections vides ignorées : pas de parcours bloc par bloc.
- **Block-entities** (coffres, fours, panneaux, bannières, têtes…) : lues directement dans `chunk.getBlockEntities()`,
  1 par entrée. Le scan de palette **ignore** tout bloc `EntityBlock` pour ne pas le compter deux fois.
- **Entités** (cadres et cadres lumineux) : `level.getEntities` sur la boîte de la colonne du chunk, filtrée sur les types suivis.
  Seules les entités connues du client sont vues.
- Un identifiant présent dans deux catégories compte dans les deux. Un identifiant inconnu du jeu est ignoré avec un
  avertissement dans le journal. Lignes vides, `//…` et `#…` sont ignorées.
- Option `showAllBlocksInDetailScreen` : les blocs et block-entities non suivis sont aussi comptés (`otherCounts`), pour l'écran de détail et l'export.

### Liste des limites (plugin Insights)

- Les catégories reprennent les limites par chunk appliquées côté serveur par le plugin
  [Insights](https://www.spigotmc.org/resources/insights.55561/). Le mod ne lit rien du serveur : la liste est
  **écrite en dur** dans `config/ConfigDefaults.java` : Conteneurs 128, Redstone 256, Blocs interactifs 512, Cadres 64
  (nom, limite, couleur, identifiants ; les variantes de bois et de couleur sont générées par boucles `WOOD_TYPES`/`DYE_COLORS`).
- **Volontairement non modifiable par le joueur** : `ModConfig.categories` est `transient`, jamais lu ni écrit dans
  `config.json`, et remplacé par `ConfigDefaults.create()` à chaque chargement (décision du commit « Lock category limits
  out of config.json »). Ne pas remettre les catégories dans le fichier de config.
- **Mettre à jour** : modifier `ConfigDefaults.java` (limite, ou identifiants `minecraft:…` tels qu'ils sont dans le
  registre du jeu), puis `gradlew build`. `ConfigDefaultsTest` échoue si un identifiant n'existe pas dans le jeu :
  à relancer après chaque changement de version de Minecraft (nouveaux blocs, blocs renommés). Garder les noms de
  catégories identiques entre `ConfigDefaults` et les tests.

### HUD (modèle du HUD d'Item_List)

- Un panneau unique : coordonnées du chunk en en-tête (gris, à droite), puis une ligne par catégorie : carré de
  couleur, texte `Nom : compte/limite`, barre de remplissage.
- Couleurs dans `HudLayout` : vert `0xFF6FCF97`, orange `0xFFF2C94C` à partir de 70 %, rouge `0xFFEB5757` et
  **gras** seulement quand la limite est **dépassée** (compte > limite). Fond noir semi-transparent, coins d'un pixel rognés, largeur min. 130 px.
- Position : coin d'ancrage (`TOP_LEFT`…`BOTTOM_RIGHT`) + décalage depuis ce coin, pour rester en place quelle que soit
  la taille de la fenêtre. Après un glisser, le HUD se raccroche au coin le plus proche (`applyDraggedPosition`).
  Opacité du fond 0–255, par pas de 17.
- Le HUD de `../item-list/` (Item_List) en est calqué : garder les deux cohérents si l'utilisateur le demande, sans
  modifier l'autre projet sans demande.

### Config et export

- Config : `config/chunklimitcounter/config.json` (dossier config du jeu), Gson. Champs : `safetyNetIntervalTicks`
  (20 par défaut), `showAllBlocksInDetailScreen` (false), `hud` (`visible`, `anchor`, `offsetX`, `offsetY`, `scale`,
  `backgroundAlpha`). Fichier illisible → config par défaut (erreur dans le journal). Le fichier est **réécrit à chaque
  chargement** (crée le fichier, efface d'anciennes `categories`). `scale` est enregistré mais pas utilisé par le rendu.
- Export (bouton de l'écran de détail) : `config/chunklimitcounter/exports/chunk_<x>_<z>_<aaaa-MM-jj_HH-mm-ss>.json`.
  Contenu : `chunkX`, `chunkZ`, `exportedAt`, `categories` (`name`, `limit`, `total`, `blocks` = identifiant → compte,
  seulement les comptes > 0), puis `others` si l'option « tout afficher » a rempli des comptes.

### Pièges

- API de rendu de la 26.x : `GuiGraphicsExtractor` et `extractRenderState(...)` (plus `GuiGraphics`/`render`),
  `graphics.text(...)`, `client.setScreenAndShow(...)`, `client.gui.screen()`. Vérifier les noms dans le jeu avant un portage.
- Le comptage est la **vue du client** : il peut différer de celui d'Insights (entités hors de portée, prédiction de
  pose avant confirmation du serveur). Le filet de sécurité corrige en au plus `safetyNetIntervalTicks` ticks.
- La largeur du panneau se mesure sur le texte **stylé** (gras plus large) : ne pas revenir à `font.width(String)`.
- Les noms des catégories sont écrits en français dans `ConfigDefaults` (pas traduits) et servent de clé dans
  `CountResult` et l'export.
- Les tests unitaires démarrent le registre de Minecraft (`MinecraftBootstrap`) et créent un dossier `logs/` (ignoré par Git).

## Lancer ou compiler

Compiler avec le wrapper Gradle du projet : `gradlew build` ; le .jar est produit dans `build/libs/`.

## Tests

- `gradlew build` lance aussi les tests unitaires (`src/test`) : couleurs et position du HUD, liste des limites
  (chaque identifiant doit exister dans le jeu : à relancer après un changement de version de Minecraft),
  lecture de la config, contenu de l'export JSON.
- `gradlew runClientGameTest` : test dans le vrai jeu (`src/gametest`), ouvre une fenêtre Minecraft (prévenir
  l'utilisateur). Il pose des blocs dans le chunk du joueur et vérifie les comptes. Regarder les captures dans
  `build/run/clientGameTest/screenshots/`.
