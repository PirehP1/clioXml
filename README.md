# clioXml
Logiciel java d'interrogation de base de données XML à travers une interface web.

Alain Dallo, Stéphane Lamassé, Laurent Frobert

## Présentation

ClioXML est un logiciel développé avec le soutien du projet de recherche *Studium parisiense*. Son objectif est de permettre aux chercheurs comme aux étudiants d'explorer, d'interroger, d'exploiter les données structurées dans un fichier ou ensemble de fichiers xml. 
Il s'agit donc essentiellement de manipuler des données dans un objectif de recherche afin de produire graphiques, tableaux...
Celui-ci a donc quatre objectifs principaux :
* création, modification d'un schéma à la volée
* coder des éléments ou des attributs
* explorer les données par une interface permettant à l'utilisateur de se représenter et de comprendre les jeux de données en explorant leurs natures, leurs relations (tris croisés) et leurs distributions (tris à plat) ainsi qu'en mettant en lumière les densités d'informations
* interroger les données en effectuant des requêtes de manière intuitive via une interface graphique ou par la rédaction assistée d'une requête 
* les exploiter par des requêtes de requêtes, des requêtes sur tables et requêtes associées ou via une exportation de résultats ou de jeux de données pour une utilisation dans d'autres logiciels d'exploitation de données.

![Capture d'écran de l'interface de clioXml](https://github.com/PirehP1/clioXml/blob/master/capture_clioxml.png)


Nous espérons qu'il intéressera les étudiants et les chercheurs en sciences humaines n'ayant pas une grande habitude de l'utilisation de l'ordinateur et une très faible connaissance des principes des bases de données xml, des schémas et de leurs interrogations.

### Version serveur

La version serveur doit permettre à  une communauté constituée autour d'un projet d'interroger une structure commune pour construire son propre rapport aux données. Elle peut-être utilisée comme une interface pédagogique, ou comme un service.  Ainsi nous l'utilisons, à l'université Paris Panthéon-Sorbonne, pour permettre à nos étudiants d'interroger leurs données sans aucune installation de leur côté. 

### Version locale
Celle-ci est plutôt destinée aux chercheurs et aux utilisateurs particuliers.
* Le public ciblé n'aura pas forcément connaissance de la structure des données qu'il souhaite interroger au moment où il commence son exploration.
* Ce public ayant des modes de travail variés, le logiciel peut être utilisé en situation de mobilité avec ou sans accès à internet sur une base de données à distance sur serveur web ou sur une copie en local d'une base ou d'une partie de celle-ci.

## Installation

### À partir de la version compilée

Le plus simple consiste à télécharger la version compilée à partir de la [page Releases](https://github.com/PirehP1/clioXml/releases) du dépôt. Lien direct pour télécharger la dernière version en date : [https://github.com/PirehP1/clioXml/releases/download/v0.3/clioXml_build_windows.zip](https://github.com/PirehP1/clioXml/releases/download/v0.3/clioXml_build_windows.zip)

Une fois le fichier décompressé, clioXml peut être lancé via le fichier `launch.bat` (windows) ou `launch.sh` (linux et mac os) présents dans le répertoire `build/`. Il est ensuite possible d'accéder au logiciel via l'URL : [http://localhost:8090](http://localhost:8090).

### Compilation via ant
* Après avoir cloné le contenu du dépôt 
* Il faut compiler avec [Apache Ant](https://ant.apache.org/) (version jdk 1.8) qui utilise le fichier `Build.xml`. Pour cela, il faut se placer à la racine du répertoire `clioXml/` et :
```sh
$ ant
```
* un répertoire build est créé
* dans ce répertoire, exécuter le fichier `launch.sh`
```sh
$ ./launch.sh
```
* Accès à l'interface web via l'URL : [http://localhost:8090](http://localhost:8090).

### Installation d'Apache Ant sur Mac OS X
* Pour tester si ANT est installé, ouvrir l'application Terminal et entrer
```
ant
```
Si ant est installé, le Terminal affiche :
```
Buildfile: build.xml does not exist!
Build failed
```
L'installation n'est pas nécessaire.
Si ant n'est pas installé, le Terminal affiche :
```
-bash: ant: command not found
```

* [Télécharger](http://ant.apache.org/bindownload.cgi) la dernière version en date
* Décompresser le fichier téléchargé.
* Renommer le dossier obtenu en `ant` et déplacer-le dans votre dossier utilisateur, celui portant votre nom.
* Dans le Terminal, ouvrir le fichier `.bash_profile` avec la commande :
```
open -e ~/.bash_profile
```
* Si ce fichier n'existe pas, le Terminal affiche :
```
The file /Users/NomUtilisateur/.bash_profile does not exist.
```
* Dans ce cas, créer un nouveau document avec TextEdit.
* Choississer le format texte brut avec le menu "Format" > "Convertir au format Texte".
* Copier les lignes suivantes :
```
ANT_HOME='~/ant'
export PATH=${PATH}:$ANT_HOME/bin
```
* Enregistrer le fichier avec le nom et les réglages suivants :
	* Utiliser comme nom `.bash_profile` (avec le point)
	* Décocher "Utiliser «.txt» à défaut d'extension"
	* TextEdit vous prévient que les noms commençant par un point sont réservés au système. Confirmez en cliquant sur "Utiliser «.»".

* Si vous n'avez pas eu à créer le fichier `.bash_profile`, il s'est ouvert dans TextEdit. Rajouter à la fin du fichier les lignes suivantes :
```
ANT_HOME='~/ant'
export PATH=${PATH}:$ANT_HOME/bin
```
* Enregistrer et fermer.
* Pour vérifier que ant est bien installé, fermer le Terminal, relancer le, et taper la commande
```
ant
```
Si ant est installé, le Terminal affiche :
```
Buildfile: build.xml does not exist!
Build failed
```

## Utilisation de la version local 

### Linux et MacOs 
* Définir un nouveau projet, donnez lui le nom que vous souhaitez, puis entrer dans le projet 
* Il faut alors :
	* importer votre fichier xml, qui doit être compressé (.zip)
	* créer/importer un schéma pour ce projet 
