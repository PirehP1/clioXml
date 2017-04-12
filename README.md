# clioXml
Querying of XML Database 

Alain Dallo, Stéphane Lamassé, Laurent Frobert


(version [compilée](#compilation))

## Présentation

ClioXML est un logiciel développé avec le soutien du projet de recherche Studium parisiense. Son objectif est de permettre aux chercheurs comme aux étudiants d'explorer, d'interroger, d'exploiter les données structurées dans un fichier ou ensemble de fichiers xml. 
Il s'agit donc essentiellement de manipuler des données dans un objectif de recherche afin de produire graphiques, tableaux. 
	Celui-ci a donc quatre objectifs principaux :
* création, modification d'un schéma à la volée
* coder des éléments ou des attributs
* explorer les données par une interface permettant à l'utilisateur de se représenter et de comprendre les jeux de données en explorant leurs natures, leurs relations et leurs distributions ainsi qu'en mettant en lumière les densités d'informations
* interroger les données en effectuant des requêtes de manière intuitive via une interface graphique ou par la rédaction assistée d'une requête 
* les exploiter par des requêtes de requêtes, des requêtes sur tables et requêtes associées ou via une exportation de résultats ou de jeux de données pour une utilisation dans d'autres logiciels d'exploitation de données.
* 
Nous espérons qu'il intéressera les étudiants et les chercheurs en sciences-humaines n'ayant pas une grande habitude de l'utilisation de l'ordinateur et une très faible connaissance des principes des bases de données xml, des schémas et de leurs interrogations.

Le logiciel est développé en JAVA.

### Version serveur
Elle doit permettre à  une communauté constituée autour d'un projet d'interroger une structure commune pour construire son propre rapport aux données. Elle peut-être utilisée comme une interface pédagogique, ou comme un service.  Ainsi nous l'utilisons, à Paris 1, pour permettre à nos étudiants d'interroger leurs données sans aucune installation de leur côté. 
### Version locale
Celle-ci est plutôt destinée aux chercheurs
* Le public ciblé n'aura pas forcément connaissance de la structure des données qu'il souhaite interroger au moment où il commence son exploration.
* Ce public ayant des modes de travail variés, le logiciel peut être utilisé en situation de mobilité avec ou sans accès à internet sur une base de données à distance sur serveur web ou sur une copie en local d'une base ou d'une partie de celle-ci.

![Capture d'écran de l'interface de clioXml](https://github.com/PirehP1/clioXml/blob/master/capture_clioxml.png)


## Installation


### Linux et Mac OS
* Après avoir téléchargé le paquet 
* Il faut compiler avec ANT (version jdk 1.8)  qui utilise Build.xml 
* Sous Linux, ou MacOs dans une session 
```sh
$ ant
```
* un répertoire build est crée 
* dans ce répertoire vous pouvez lancer : **launch.sh**
```sh
$ ./launch.sh
```
* Vous pouvez accéder à ce logiciel en utilisant votre navigateur à l'adresse : [http://localhost:8090](http://localhost:8090). Evidemment le port peut-être changé 

#### Installation de ANT sur Mac OS X
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

* Sur http://ant.apache.org/bindownload.cgi, télécharger **apache-ant-1.9.7-bin.tar.gz** (le numéro de version peut changer)
* Si ce n'est pas fait automatiquement, décompresser le fichier téléchargé en double-cliquant dessus. Vous obtenez un dossier intitulé **apache-ant-1.9.7**.
* Renommer ce dossier en "ant", et déplacer le dans votre dossier utilisateur, celui portant votre nom.
* Dans le Terminal, ouvrir le fichier .bash_profile avec la commande :
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
	* Utiliser comme nom ".bash_profile" (avec le point)
	* Décocher "Utiliser «.txt» à défaut d'extension"
	* TextEdit vous prévient que les noms commençant par un point sont réservés au système. Confirmez en cliquant sur "Utiliser «.»".

* Si vous n'avez pas eu à créer le fichier .bash_profile, il s'est ouvert dans TextEdit. Rajouter à la fin du fichier les lignes suivantes :
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


### <a name="compilation"></a> Versions compilées
Vous pouvez accéder à une version compilée à partir de ce [lien](http://pireh-dev.univ-paris1.fr/ClioXml/clioxml-bin-0.29.8.zip).  
