# Chunk Limit Counter

Mod client Fabric pour Minecraft **26.2** qui affiche en temps réel le nombre de blocs, block-entities et entités limités par chunk (par exemple par le plugin serveur [Insights](https://www.spigotmc.org/resources/insights.55561/)), sans avoir à poser/casser pour vérifier.

100% client-only : aucune dépendance serveur, fonctionne aussi en solo.

## Démonstration vidéo

[![Regarder la vidéo de démonstration](https://img.youtube.com/vi/apvWItJaixg/maxresdefault.jpg)](https://www.youtube.com/watch?v=apvWItJaixg)

## Captures d'écran

<!-- Ajoute ici tes captures d'écran, par exemple : -->
<!-- ![HUD en jeu](docs/screenshots/hud.png) -->
<!-- ![Écran de détail](docs/screenshots/detail.png) -->
<!-- ![Mode déplacement du HUD](docs/screenshots/move.png) -->

## Fonctionnalités

- **HUD en temps réel** : affiche chaque catégorie suivie (`Nom : compte/limite`), avec une couleur qui vire au rouge et le texte qui passe en gras uniquement en cas de dépassement réel de la limite.
- **Recalcul intelligent** : à chaque changement de chunk, à chaque pose/cassure de bloc, et en filet de sécurité toutes les X ticks (configurable) pour couvrir les mécanismes automatiques (pistons, entonnoirs, etc.).
- **Scan optimisé** : utilise la palette de chaque section de chunk pour ignorer rapidement les sections sans bloc suivi, et lit directement la liste des block-entities du chunk plutôt que de scanner bloc par bloc.
- **HUD déplaçable** : touche dédiée pour glisser le HUD à la souris n'importe où à l'écran, avec réglage de l'opacité du fond.
- **Écran de détail complet** : liste le compte exact par bloc/entité (pas juste le total par catégorie), avec les noms traduits automatiquement selon la langue du jeu.
- **Export JSON** : exporte le détail du chunk courant dans un fichier JSON depuis l'écran de détail.
- **Capture d'écran** : bouton dédié dans l'écran de détail.
- **Catégories et limites fixes** : conteneurs, redstone, blocs interactifs, cadres — codées en dur dans le mod pour éviter que les joueurs ne les modifient.

## Touches par défaut

| Touche | Action |
|---|---|
| `K` | Afficher/masquer le HUD |
| `J` | Ouvrir l'écran de détail du chunk |
| `L` | Activer le mode déplacement du HUD |

Les touches sont personnalisables dans les options du jeu (Commandes → Chunk Limit Counter).

## Configuration

Le fichier de config est généré au premier lancement dans :

```
config/chunklimitcounter/config.json
```

Il contient uniquement les réglages d'affichage, volontairement — les catégories et leurs limites ne sont **pas** éditables par ce fichier (elles sont fixées dans le code du mod) :
- `safetyNetIntervalTicks` : intervalle du recalcul de sécurité (défaut : 20 ticks = 1 seconde)
- `showAllBlocksInDetailScreen` : si `true`, l'écran de détail liste aussi les blocs/block-entities non suivis présents dans le chunk
- `hud` : position, ancrage, visibilité et opacité du HUD

## Prérequis

- Minecraft 26.2
- Fabric Loader ≥ 0.19.5
- Fabric API (build correspondant à 26.2)
- Java 25

## Compiler depuis les sources

```bash
./gradlew build
```

Le jar compilé se trouve dans `build/libs/`.

Pour lancer un client de test directement depuis le projet :

```bash
./gradlew runClient
```

## Licence

MIT — voir [LICENSE](LICENSE).
