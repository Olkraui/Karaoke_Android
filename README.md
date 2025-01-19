# Karaoke_Android

Marre d'attendre le soir pour chanter avec vos amis ?  
Des difficultés à vous souvenir des paroles ?  

Grâce à notre application de karaoké, ces problèmes n'existeront plus !  

Faites profiter vos talents de chanteur à votre entourage, peu importe l'horaire ou le lieu !  

Alors n'attendez plus et téléchargez notre app pour devenir le nouveau Johnny ou la nouvelle Céline Dion !  

**Astuce** : utiliser notre app sous la douche décuplera votre talent et votre voix !! (L'équipe de dev déconseille fortement.)  
(**/!\ Attention** : cette astuce dépendra de l'étanchéité de votre smartphone. Nous ne nous tenons en aucun cas responsables dans les cas où votre smartphone se dégrade ou si vous commencez à entendre Claude François chanter avec vous !)
 
---

## Installation

1. **Téléchargez l'APK** :
   - Rendez-vous dans le dossier **Release** du projet et récupérez le fichier **`app-release.apk`**.
2. **Installez l'application sur votre téléphone** :
   - Copiez le fichier APK sur votre téléphone.
   - Recherchez-le dans votre gestionnaire de fichiers et ouvrez-le pour l'installer.
   - N'oubliez pas d'autoriser l'installation depuis des sources inconnues si nécessaire.

---

## Fonctionnalités principales

1. **Récupération des musiques** :
   - Les musiques disponibles sont récupérées via une URL prédéfinie.
   - Une interface appelée **`ApiService`** a été créée avec Retrofit pour interagir avec l'API.

2. **Affichage des musiques** :
   - Une fois les musiques récupérées, elles sont affichées dans la page principale (MainActivity).
   - L'utilisateur peut sélectionner une musique pour lancer la lecture.

3. **Lecture des musiques** :
   - Lorsque l'utilisateur sélectionne une musique, une nouvelle activité (LyricsActivity) s'ouvre.
   - En parallèle, l'application télécharge l'audio correspondant à la musique sélectionnée.
   - Les paroles brutes, contenant des timestamps, sont récupérées et affichées de manière synchronisée avec l'audio.

4. **Synchronisation des paroles avec l'audio** :
   - Un **timer** est lancé au début de la lecture de l'audio pour afficher les paroles en fonction des timestamps.
   - Une expression régulière (**Regex**) est utilisée pour analyser les timestamps dans les paroles et déterminer le début et la fin de chaque ligne.
   - Les paroles sont affichées progressivement, lettre par lettre, avec un effet d'éclaircissement.

---

## Limitations et améliorations futures

### **Ce qui manque :**
1. **Gestion précise des timestamps** :
   - L'évolution de la vitesse du curseur en fonction des timestamps présents au milieu des phrases n'est pas implémentée.
   - Actuellement, le curseur avance uniquement en fonction des timestamps de début et de fin des phrases.

2. **Amélioration visuelle** :
   - L'affichage avance actuellement par lettre entière.
   - L'objectif futur est de surligner chaque **pixel des lettres** pour une transition plus fluide.

### **Problème rencontré** :
- Lorsque nous avons essayé de gérer les timestamps au milieu des phrases, l'application a rencontré des crashs fréquents. 
- Notre approche initiale consistait à :
  1. Utiliser une Regex pour extraire les timestamps de début et de fin.
  2. Nettoyer le texte pour enlever ces timestamps.
  3. Rechercher et analyser les timestamps intermédiaires dans les phrases.
- Cependant, la gestion des morceaux de phrases avec le curseur a posé des problèmes complexes et instables.

---

## Décision finale
Pour garantir un minimum de fonctionnalité, nous avons décidé :
- De ne pas gérer les timestamps au milieu des phrases.
- De synchroniser le curseur uniquement en fonction des timestamps de début et de fin.

---

Auteurs : 
HEARD Baptiste
RENAUD François
